package com.sport_pro_be.modules.auth.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import com.sport_pro_be.modules.auth.interfaces.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IProfileService profileService;

    @GetMapping
    public ApiResponse<List<UserProfileResponse>> getAllUsers() {
        return ApiResponse.of("Users retrieved successfully", profileService.getAllProfiles());
    }
}
