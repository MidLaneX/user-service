package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.*;
import com.midlane.project_management_tool_user_service.model.RefreshToken;
import com.midlane.project_management_tool_user_service.model.Role;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.RoleRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import com.midlane.project_management_tool_user_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SocialAuthService socialAuthService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TeamEventProducerService teamEventProducerService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role userRole;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Set up test data
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .phone("1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        userRole = Role.builder()
                .id(1L)
                .name(Role.USER)
                .permissions("USER_PERMISSIONS")
                .build();

        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .phone("1234567890")
                .role(userRole)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .passwordLastChanged(LocalDateTime.now())
                .emailLastChanged(LocalDateTime.now())
                .build();

        refreshToken = RefreshToken.builder()
                .token("refresh-token-123")
                .userEmail("test@example.com")
                .build();

        // Set access token expiration via reflection
        ReflectionTestUtils.setField(userService, "accessTokenExpiration", 900000L); // 15 minutes
    }

    @Test
    @DisplayName("Should register user successfully when email is not taken")
    void registerUser_Success() {
        // Given
        String deviceInfo = "test-device";
        UserDetails userDetails = mock(UserDetails.class);

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(Role.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token-123");
        when(refreshTokenService.createRefreshToken(userDetails, deviceInfo)).thenReturn(refreshToken);

        // When
        AuthResponse response = userService.registerUser(registerRequest, deviceInfo);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        // Verify interactions
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(teamEventProducerService).publishUserRegisteredEvent(anyLong(), anyString(), anyString(), anyString());
        verify(notificationService).sendWelcomeNotification(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists during registration")
    void registerUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registerRequest, "device"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already in use");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should create default role when USER role doesn't exist")
    void registerUser_CreatesDefaultRole() {
        // Given
        String deviceInfo = "test-device";
        UserDetails userDetails = mock(UserDetails.class);
        Role newRole = Role.builder().name(Role.USER).permissions("USER_PERMISSIONS").build();

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(Role.USER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token-123");
        when(refreshTokenService.createRefreshToken(userDetails, deviceInfo)).thenReturn(refreshToken);

        // When
        AuthResponse response = userService.registerUser(registerRequest, deviceInfo);

        // Then
        assertThat(response).isNotNull();
        verify(roleRepository).findByName(Role.USER);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void loginUser_Success() {
        // Given
        String deviceInfo = "test-device";
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token-123");
        when(refreshTokenService.createRefreshToken(userDetails, deviceInfo)).thenReturn(refreshToken);

        // When
        AuthResponse response = userService.loginUser(loginRequest, deviceInfo);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserEmail()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid login credentials")
    void loginUser_InvalidCredentials_ThrowsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        // When & Then
        assertThatThrownBy(() -> userService.loginUser(loginRequest, "device"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void refreshAccessToken_Success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123", "device-info");
        UserDetails userDetails = mock(UserDetails.class);

        when(refreshTokenService.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(userDetailsService.loadUserByUsername(refreshToken.getUserEmail())).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("new-access-token");

        // When
        RefreshTokenResponse response = userService.refreshAccessToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L); // 15 minutes in seconds

        verify(refreshTokenService).findByToken(request.getRefreshToken());
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtUtil).generateAccessToken(userDetails);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is not found")
    void refreshAccessToken_TokenNotFound_ThrowsException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token", "device-info");

        when(refreshTokenService.findByToken(request.getRefreshToken()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.refreshAccessToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token not found");

        verify(refreshTokenService).findByToken(request.getRefreshToken());
        verify(refreshTokenService, never()).verifyExpiration(any());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void getAllUsers_Success() {
        // Given
        User user1 = User.builder()
                .userId(1L)
                .email("user1@example.com")
                .role(userRole)
                .build();
        User user2 = User.builder()
                .userId(2L)
                .email("user2@example.com")
                .role(userRole)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<UserDTO> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUserId()).isEqualTo(1L);
        assertThat(users.get(0).getEmail()).isEqualTo("user1@example.com");
        assertThat(users.get(1).getUserId()).isEqualTo(2L);
        assertThat(users.get(1).getEmail()).isEqualTo("user2@example.com");

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should reset password successfully")
    void resetPassword_Success() {
        // Given
        Long userId = 1L;
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newHashedPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // When
        userService.resetPassword(userId, newPassword);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(savedUser.getPasswordLastChanged()).isNotNull();

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    @DisplayName("Should throw exception when user not found for password reset")
    void resetPassword_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.resetPassword(userId, newPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void findById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userService.findById(userId);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserId()).isEqualTo(userId);
        assertThat(foundUser.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void findById_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void findByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userService.findByEmail(email);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getUserId()).isEqualTo(testUser.getUserId());

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void findByEmail_UserNotFound_ThrowsException() {
        // Given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
    }
}
