package com.sport_pro_be.modules.auth.interfaces;

import com.sport_pro_be.modules.auth.dto.UpdateProfileRequest;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserProfileResponse updateAvatar(Long userId, MultipartFile file);
    java.util.List<UserProfileResponse> getAllProfiles();
}
