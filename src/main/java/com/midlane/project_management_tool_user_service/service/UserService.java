package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateUserRequest;
import com.midlane.project_management_tool_user_service.dto.UpdateUserProfileRequest;
import com.midlane.project_management_tool_user_service.dto.UserResponse;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName()); // In real app, hash this password
        user.setStatus(User.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email/username already exists for other users
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName()); // Hash in real app
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Update user profile - typically called after user registration through auth service
     * to complete the profile with first name and last name
     */
    public UserResponse updateUserProfile(String email, UpdateUserProfileRequest request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(User.UserStatus.ACTIVE); // Activate user after profile completion
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Get user by email - useful for profile completion flow
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return mapToUserResponse(user);
    }

    /**
     * Get user by auth service user ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByAuthServiceUserId(Long authServiceUserId) {
        User user = userRepository.findByAuthServiceUserId(authServiceUserId);
        if (user == null) {
            throw new RuntimeException("User not found with auth service user ID: " + authServiceUserId);
        }
        return mapToUserResponse(user);
    }

    /**
     * Check if user profile is complete (has first name and last name)
     */
    @Transactional(readOnly = true)
    public boolean isProfileComplete(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        return user.getFirstName() != null && user.getLastName() != null;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setAuthServiceUserId(user.getAuthServiceUserId()); // Added auth service user ID
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
