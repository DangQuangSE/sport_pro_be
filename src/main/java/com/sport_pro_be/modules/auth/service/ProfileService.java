package com.sport_pro_be.modules.auth.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.dto.UpdateProfileRequest;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import com.sport_pro_be.modules.auth.enums.Role;
import com.sport_pro_be.modules.auth.interfaces.IProfileService;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.upload.service.CloudinaryUploadService;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_PROFILE", module = "AUTH")
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_AVATAR", module = "AUTH")
    public UserProfileResponse updateAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        String avatarUrl = uploadService.uploadFile(file, "avatars/" + userId);
        user.setAvatar(avatarUrl);

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserProfileResponse> getAllProfiles() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_USER_ROLE", module = "AUTH")
    public UserProfileResponse updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.setRole(Role.valueOf(role.toUpperCase()));
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @Loggable(action = "SET_USER_ACTIVE", module = "AUTH")
    public UserProfileResponse setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.setActive(active);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @Loggable(action = "DELETE_USER", module = "AUTH")
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .tier(user.getTier().name())
                .totalSpending(user.getTotalSpending())
                .isActive(user.isActive())
                .build();
    }
}
