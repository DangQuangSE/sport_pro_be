package com.sport_pro_be.modules.auth.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import com.sport_pro_be.modules.auth.interfaces.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IProfileService profileService;

    @GetMapping
    public ApiResponse<List<UserProfileResponse>> getAllUsers() {
        return ApiResponse.of(USERS_RETRIEVED, profileService.getAllProfiles());
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserProfileResponse> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        return ApiResponse.of(USER_ROLE_UPDATED, profileService.updateUserRole(id, role));
    }

    @PutMapping("/{id}/active")
    public ApiResponse<UserProfileResponse> setActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ApiResponse.of(USER_STATUS_UPDATED, profileService.setUserActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        profileService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.of(USER_DELETED, null));
    }
}
