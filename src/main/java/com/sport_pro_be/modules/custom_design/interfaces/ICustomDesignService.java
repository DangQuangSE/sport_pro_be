package com.sport_pro_be.modules.custom_design.interfaces;

import com.sport_pro_be.modules.custom_design.domain.CustomDesign;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignRequest;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ICustomDesignService {

    /**
     * Saves a new custom design (uploads image to Cloudinary, calculates price, persists to DB).
     */
    CustomDesignResponse saveDesign(Long userId, MultipartFile file, CustomDesignRequest request);

    /**
     * Returns a paginated list of designs belonging to the authenticated user.
     */
    Page<CustomDesignResponse> getMyDesigns(Long userId, Pageable pageable);

    /**
     * Returns the details of a specific design. Throws 403 if the design does not belong to the user.
     */
    CustomDesignResponse getDesignDetail(Long userId, Long designId);

    /**
     * Retrieves the CustomDesign entity after verifying ownership. Used internally by CartService / OrderService.
     */
    CustomDesign findAndVerifyOwnership(Long userId, Long designId);
}
