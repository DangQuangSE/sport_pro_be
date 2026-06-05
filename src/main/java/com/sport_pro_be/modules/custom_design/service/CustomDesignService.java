package com.sport_pro_be.modules.custom_design.service;

import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.custom_design.constant.CustomDesignMessageConstant;
import com.sport_pro_be.modules.custom_design.domain.CustomDesign;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignRequest;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignResponse;
import com.sport_pro_be.modules.custom_design.interfaces.ICustomDesignService;
import com.sport_pro_be.modules.custom_design.repository.CustomDesignRepository;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.enums.PrintingElementType;
import com.sport_pro_be.modules.printing.repository.PrintingMaterialRepository;
import com.sport_pro_be.modules.printing.repository.PrintingPriceConfigRepository;
import com.sport_pro_be.modules.upload.interfaces.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomDesignService implements ICustomDesignService {

    private final CustomDesignRepository customDesignRepository;
    private final PrintingMaterialRepository materialRepository;
    private final PrintingPriceConfigRepository priceConfigRepository;
    private final UserRepository userRepository;
    private final IUploadService uploadService;

    @Override
    public CustomDesignResponse saveDesign(Long userId, MultipartFile file, MultipartFile backFile, CustomDesignRequest request) {
        // Step 1: Validate file (outside transaction)
        uploadService.validateFile(file);
        if (backFile != null) {
            uploadService.validateFile(backFile);
        }

        // Step 2: Upload to Cloudinary (outside @Transactional — rule_be #5: never hold DB connection during external API calls)
        log.info("Uploading custom design images for user id: {}", userId);
        String imageUrl = uploadService.uploadFile(file, CustomDesignMessageConstant.UPLOAD_FOLDER);
        String backImageUrl = null;
        if (backFile != null) {
            try {
                backImageUrl = uploadService.uploadFile(backFile, CustomDesignMessageConstant.UPLOAD_FOLDER);
            } catch (Exception e) {
                log.warn("Back image upload failed. Deleting uploaded front image: {}", imageUrl);
                uploadService.deleteFile(imageUrl);
                throw e;
            }
        } else {
            backImageUrl = imageUrl;
        }

        // Step 3: Persist to DB (short, focused @Transactional)
        try {
            return persistDesign(userId, imageUrl, backImageUrl, request);
        } catch (Exception e) {
            // Compensating action: delete orphaned images from Cloudinary
            log.warn("DB save failed after Cloudinary upload. Deleting orphaned images: {}, {}", imageUrl, backImageUrl);
            uploadService.deleteFile(imageUrl);
            if (backFile != null && backImageUrl != null && !backImageUrl.equals(imageUrl)) {
                uploadService.deleteFile(backImageUrl);
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomDesignResponse> getMyDesigns(Long userId, Pageable pageable) {
        return customDesignRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomDesignResponse getDesignDetail(Long userId, Long designId) {
        CustomDesign design = findAndVerifyOwnership(userId, designId);
        return mapToResponse(design);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomDesignResponse getDesignDetailAdmin(Long designId) {
        CustomDesign design = customDesignRepository.findById(designId)
                .orElseThrow(() -> new AppException(CustomDesignMessageConstant.DESIGN_NOT_FOUND, HttpStatus.NOT_FOUND));
        return mapToResponse(design);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomDesign findAndVerifyOwnership(Long userId, Long designId) {
        return customDesignRepository.findByIdAndUserId(designId, userId)
                .orElseThrow(() -> new AppException(CustomDesignMessageConstant.DESIGN_NOT_FOUND, HttpStatus.NOT_FOUND));
    }


    // ──────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────

    @Transactional
    protected CustomDesignResponse persistDesign(Long userId, String imageUrl, String backImageUrl, CustomDesignRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CustomDesignMessageConstant.DESIGN_NOT_FOUND));

        PrintingMaterial material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new AppException(CustomDesignMessageConstant.MATERIAL_NOT_FOUND, HttpStatus.NOT_FOUND));

        BigDecimal totalPrintingPrice = calculatePrintingPrice(material, request.getNumTextLines(), request.getNumImages());

        CustomDesign design = CustomDesign.builder()
                .user(user)
                .printingMaterial(material)
                .designImageUrl(imageUrl)
                .backDesignImageUrl(backImageUrl)
                .designMetadata(request.getMetadata())
                .numTextLines(request.getNumTextLines())
                .numImages(request.getNumImages())
                .totalPrintingPrice(totalPrintingPrice)
                .build();

        CustomDesign saved = customDesignRepository.save(design);
        log.info("Custom design saved with id: {} for user id: {}", saved.getId(), userId);
        return mapToResponse(saved);
    }

    /**
     * Formula:
     * totalPrintingPrice = material.basePrice
     *                    + (numTextLines × PriceConfig[TEXT].unitPrice)
     *                    + (numImages    × PriceConfig[IMAGE].unitPrice)
     */
    private BigDecimal calculatePrintingPrice(PrintingMaterial material, int numTextLines, int numImages) {
        BigDecimal textUnitPrice = getUnitPrice(PrintingElementType.TEXT);
        BigDecimal imageUnitPrice = getUnitPrice(PrintingElementType.IMAGE);

        return material.getBasePrice()
                .add(textUnitPrice.multiply(BigDecimal.valueOf(numTextLines)))
                .add(imageUnitPrice.multiply(BigDecimal.valueOf(numImages)));
    }

    private BigDecimal getUnitPrice(PrintingElementType type) {
        PrintingPriceConfig config = priceConfigRepository.findByType(type)
                .orElseThrow(() -> new AppException(
                        String.format(CustomDesignMessageConstant.PRICE_CONFIG_NOT_FOUND, type.name()),
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
        return config.getUnitPrice();
    }

    private CustomDesignResponse mapToResponse(CustomDesign design) {
        return CustomDesignResponse.builder()
                .id(design.getId())
                .designImageUrl(design.getDesignImageUrl())
                .backDesignImageUrl(design.getBackDesignImageUrl())
                .designMetadata(design.getDesignMetadata())
                .printingMaterialId(design.getPrintingMaterial().getId())
                .printingMaterialName(design.getPrintingMaterial().getName())
                .numTextLines(design.getNumTextLines())
                .numImages(design.getNumImages())
                .totalPrintingPrice(design.getTotalPrintingPrice())
                .createdAt(design.getCreatedAt())
                .build();
    }
}
