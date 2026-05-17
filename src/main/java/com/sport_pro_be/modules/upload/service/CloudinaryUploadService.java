package com.sport_pro_be.modules.upload.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.modules.upload.constant.UploadMessageConstant;
import com.sport_pro_be.modules.upload.interfaces.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUploadService implements IUploadService {

    private final Cloudinary cloudinary;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload error: ", e);
            throw new AppException(UploadMessageConstant.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Cloudinary delete error: ", e);
            // We don't necessarily want to block the DB transaction if cloud deletion fails,
            // but we should log it.
        }
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File is empty", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(UploadMessageConstant.FILE_TOO_LARGE, HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new AppException(UploadMessageConstant.INVALID_FILE_TYPE, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Extracts the public ID from a Cloudinary URL.
     * Example: https://res.cloudinary.com/demo/image/upload/v12345/products/my_image.jpg -> products/my_image
     */
    private String extractPublicId(String imageUrl) {
        try {
            // Split by '/upload/'
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;

            // Remove versioning (e.g., 'v12345/') and extension (e.g., '.jpg')
            String pathWithId = parts[1].substring(parts[1].indexOf("/") + 1);
            int lastDotIndex = pathWithId.lastIndexOf(".");
            if (lastDotIndex != -1) {
                return pathWithId.substring(0, lastDotIndex);
            }
            return pathWithId;
        } catch (Exception e) {
            log.warn("Failed to extract publicId from URL: {}", imageUrl);
            return null;
        }
    }
}
