package com.sport_pro_be.modules.auth.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.auth.dto.UpdateProfileRequest;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import com.sport_pro_be.modules.auth.interfaces.IProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;


    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.of(PROFILE_RETRIEVED, profileService.getProfile(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.of(PROFILE_UPDATED, profileService.updateProfile(userId, request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileResponse> updateAvatar(@RequestPart("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.of(AVATAR_UPDATED, profileService.updateAvatar(userId, file));
    }
}
