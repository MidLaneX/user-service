package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.*;
import com.midlane.project_management_tool_user_service.model.AuthProvider;
import com.midlane.project_management_tool_user_service.model.RefreshToken;
import com.midlane.project_management_tool_user_service.model.Role;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import com.midlane.project_management_tool_user_service.repository.RoleRepository;
import com.midlane.project_management_tool_user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthService socialAuthService;
    private final NotificationService notificationService;
    private final TeamEventProducerService teamEventProducerService; // Add this injection

    @Value("${jwt.access-token.expiration}") // 15 minutes
    private long accessTokenExpiration;

    public AuthResponse registerUser(RegisterRequest request, String deviceInfo) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Get or create default USER role
        Role userRole = roleRepository.findByName(Role.USER)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(Role.USER)
                            .permissions("USER_PERMISSIONS")
                            .build();
                    return roleRepository.save(newRole);
                });

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(userRole); // Set Role entity
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setEmailLastChanged(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Log user registration
        log.info("User registered successfully: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        log.info("Trying to publish user registration event to Kafka for userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        // Publish user registration event for collaboration service
        try {
            log.info("Publishing user registered event for userId={}, email={}", savedUser.getId(), savedUser.getEmail());
            teamEventProducerService.publishUserRegisteredEvent(
                    savedUser.getUserId(),
                    savedUser.getEmail(),
                    savedUser.getFirstName() + " " + (savedUser.getLastName() != null ? savedUser.getLastName() : ""),
                    savedUser.getProfilePictureUrl()
            );
        } catch (Exception e) {
            log.error("Failed to publish user registered event for user: {}", savedUser.getEmail(), e);
            // Don't fail the registration if event publishing fails
        }

        // Send welcome notification
        try {
            notificationService.sendWelcomeNotification(
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getRole().getName()
            );
        } catch (Exception e) {
            log.error("Failed to send welcome notification for user: {}", savedUser.getEmail(), e);
            // Don't fail the registration if notification fails
        }

        // Generate tokens using RSA
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails, deviceInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .userId(savedUser.getUserId()) // Include userId for frontend
                .userEmail(savedUser.getEmail())
                .role(savedUser.getRole().getName())
                .build();
    }

    public AuthResponse loginUser(LoginRequest request, String deviceInfo) {
        try {
            // Use the properly configured AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Find User by email to get userId and role
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate tokens using RSA
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String accessToken = jwtUtil.generateAccessToken(userDetails);

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails, deviceInfo);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                    .userId(user.getUserId()) // Include userId for frontend
                    .userEmail(user.getEmail())
                    .role(user.getRole().getName())
                    .build();

        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUserEmail());
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .build();
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .jobTitle(user.getJobTitle())
                .department(user.getDepartment())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Encode the new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());

        // Save the updated user
        userRepository.save(user);

        // Log user update instead of publishing to Kafka
        log.info("User password reset: userId={}, email={}", user.getId(), user.getEmail());

        // Revoke all refresh tokens for security after password change
        refreshTokenService.revokeAllUserTokens(user.getEmail());
    }

    @Transactional
    public void changePassword(String userEmail, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());

        // Save the updated user
        userRepository.save(user);

        // Log user update instead of publishing to Kafka
        log.info("User password changed: userId={}, email={}", user.getId(), user.getEmail());

        // Revoke all refresh tokens for security after password change
        refreshTokenService.revokeAllUserTokens(user.getEmail());
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setRole(newRole);

        // Save the updated user
        userRepository.save(user);

        // Log user update instead of publishing to Kafka
        log.info("User role updated: userId={}, email={}, newRole={}", user.getId(), user.getEmail(), newRole.getName());

        // Revoke all refresh tokens when role changes for security
        // This forces the user to log in again to get tokens with updated role claims
        refreshTokenService.revokeAllUserTokens(user.getEmail());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Log user deletion instead of publishing to Kafka
        log.info("User deleted: userId={}, email={}", user.getId(), user.getEmail());

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user.getEmail());

        // Delete user
        userRepository.delete(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public AuthResponse authenticateWithSocial(SocialLoginRequest request) {
        // Get user info from social provider
        SocialUserInfo socialUserInfo = socialAuthService.getUserInfo(request.getProvider(), request.getAccessToken());

        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isEmpty()) {
            throw new RuntimeException("Email not provided by " + request.getProvider() + " provider");
        }

        // Check if user exists by email
        Optional<User> existingUser = userRepository.findByEmail(socialUserInfo.getEmail());

        User user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update social provider info if it's a local account being linked
            if (user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(AuthProvider.valueOf(socialUserInfo.getProvider().toUpperCase()));
                user.setProviderId(socialUserInfo.getId());
                user.setFirstName(socialUserInfo.getFirstName());
                user.setLastName(socialUserInfo.getLastName());
                user.setProfilePictureUrl(socialUserInfo.getProfilePictureUrl());
                userRepository.save(user);
                
                // Log user update instead of publishing to Kafka
                log.info("User social info updated: userId={}, email={}", user.getId(), user.getEmail());
            }
        } else {
            // Create new user from social login
            user = createUserFromSocialInfo(socialUserInfo);
            isNewUser = true;
            
            // Log user creation
            log.info("New user created from social login: userId={}, email={}", user.getId(), user.getEmail());

            // Publish user registered event to Kafka for new social users
            try {
                String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                                 (user.getLastName() != null ? " " + user.getLastName() : "");
                fullName = fullName.trim();

                teamEventProducerService.publishUserRegisteredEvent(
                    user.getUserId(),
                    user.getEmail(),
                    fullName.isEmpty() ? user.getEmail() : fullName,
                    user.getProfilePictureUrl()
                );

                log.info("Published user registered event for social login user: userId={}, email={}",
                        user.getUserId(), user.getEmail());
            } catch (Exception e) {
                log.error("Failed to publish user registered event for social login user: userId={}, email={}",
                         user.getUserId(), user.getEmail(), e);
                // Don't fail the registration if event publishing fails
            }

            // Send welcome notification for new social login users
            try {
                notificationService.sendWelcomeNotification(
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().getName()
                );
            } catch (Exception e) {
                log.error("Failed to send welcome notification for social login user: {}", user.getEmail(), e);
                // Don't fail the registration if notification fails
            }
        }

        // Generate RSA-based tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        // Create refresh token with default device info for social login
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails, "Social Login - " + request.getProvider());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .userId(user.getUserId()) // Include userId for frontend
                .userEmail(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    private User createUserFromSocialInfo(SocialUserInfo socialUserInfo) {
        // Get or create default USER role
        Role userRole = roleRepository.findByName(Role.USER)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(Role.USER)
                            .permissions("USER_PERMISSIONS")
                            .build();
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setEmail(socialUserInfo.getEmail());
        user.setFirstName(socialUserInfo.getFirstName());
        user.setLastName(socialUserInfo.getLastName());
        user.setProfilePictureUrl(socialUserInfo.getProfilePictureUrl());
        user.setProvider(AuthProvider.valueOf(socialUserInfo.getProvider().toUpperCase()));
        user.setProviderId(socialUserInfo.getId());
        user.setPasswordHash(null); // No password for social login
        user.setRole(userRole); // Use Role entity instead of Role.USER
        user.setEmailLastChanged(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public UserDTO updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update only the fields that are provided (not null)
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

        // Save the updated user
        User savedUser = userRepository.save(user);

        // Log user profile update
        log.info("User profile updated: userId={}, email={}", user.getId(), user.getEmail());

        return mapToUserDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return mapToUserDTO(user);
    }

    @Transactional(readOnly = true)
    public MeResponse getCurrentUserInfo(Long useId) {
        User user = userRepository.findById(useId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + useId));

        MeResponse response = new MeResponse();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setProfilePictureUrl(user.getProfilePictureUrl());

        return response;
    }
}
