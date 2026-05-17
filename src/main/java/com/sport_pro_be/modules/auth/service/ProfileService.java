package com.sport_pro_be.modules.auth.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.dto.UpdateProfileRequest;
import com.sport_pro_be.modules.auth.dto.UserProfileResponse;
import com.sport_pro_be.modules.auth.interfaces.IProfileService;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.upload.service.CloudinaryUploadService;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_PROFILE", module = "AUTH")
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String avatarUrl = uploadService.uploadFile(file, "avatars/" + userId);
        user.setAvatar(avatarUrl);
        
        user = userRepository.save(user);
        return mapToResponse(user);
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
                .build();
    }
}
