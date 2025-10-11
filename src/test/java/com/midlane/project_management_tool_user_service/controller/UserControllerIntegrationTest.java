package com.midlane.project_management_tool_user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midlane.project_management_tool_user_service.dto.RegisterRequest;
import com.midlane.project_management_tool_user_service.dto.UpdateUserProfileRequest;
import com.midlane.project_management_tool_user_service.dto.PasswordResetRequest;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 👤 UserController Integration Tests
 *
 * Tests the complete user profile management workflow including:
 * - Getting user profiles with proper authentication
 * - Updating user profiles and validation
 * - Password reset functionality
 * - Security and authorization
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("👤 UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private Long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print())
                .build();

        // Create a test user first
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("usertest@example.com")
                .password("TestPassword123!")
                .phone("+1234567890")
                .build();

        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User testUser = userRepository.findByEmail("usertest@example.com").orElseThrow();
        testUserId = testUser.getId();
    }

    @Test
    @DisplayName("✅ Should get user profile with valid user ID and proper authentication")
    @WithMockUser(roles = "USER")
    void getUserProfile_WithValidUserId_ShouldReturnUserDTO() throws Exception {
        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/auth/user/profile/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Verify user profile details
                .andExpect(jsonPath("$.id", is(testUserId.intValue())))
                .andExpect(jsonPath("$.email", is("usertest@example.com")))
                .andExpect(jsonPath("$.phone", is("+1234567890")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("❌ Should return 404 when user profile not found")
    @WithMockUser(roles = "USER")
    void getUserProfile_WithInvalidUserId_ShouldReturn404() throws Exception {
        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/auth/user/profile/{userId}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("GET_PROFILE_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("❌ Should return 403 when accessing without proper authentication")
    void getUserProfile_WithoutAuthentication_ShouldReturn403() throws Exception {
        // 🚀 ACT & ASSERT - No @WithMockUser annotation, so no authentication
        mockMvc.perform(get("/api/auth/user/profile/{userId}", testUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("✅ Should update user profile with valid data")
    @WithMockUser(roles = "USER")
    void updateUserProfile_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // 🏗️ ARRANGE
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setJobTitle("Software Engineer");
        request.setDepartment("Engineering");

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/profile/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Verify updated profile details
                .andExpect(jsonPath("$.id", is(testUserId.intValue())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.jobTitle", is("Software Engineer")))
                .andExpect(jsonPath("$.department", is("Engineering")));
    }

    @Test
    @DisplayName("❌ Should return 400 when profile update data is invalid")
    @WithMockUser(roles = "USER")
    void updateUserProfile_WithInvalidData_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE - Invalid data (name too short)
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("J"); // Too short, minimum is 2 characters
        request.setLastName("D");  // Too short, minimum is 2 characters

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/profile/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Should return 400 when updating non-existent user")
    @WithMockUser(roles = "USER")
    void updateUserProfile_WithInvalidUserId_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/profile/{userId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("UPDATE_PROFILE_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("✅ Should reset password with valid data")
    @WithMockUser(roles = "USER")
    void resetPassword_WithValidData_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("NewSecurePassword123!");

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/reset-password/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));
    }

    @Test
    @DisplayName("❌ Should return 400 when resetting password for non-existent user")
    @WithMockUser(roles = "USER")
    void resetPassword_WithInvalidUserId_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("ValidNewPassword123!");

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/reset-password/{userId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("RESET_PASSWORD_ERROR")))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("✅ Should handle partial profile updates")
    @WithMockUser(roles = "USER")
    void updateUserProfile_WithPartialData_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Only update job title
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setJobTitle("Senior Software Engineer");
        // Other fields are null/not set

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/auth/user/profile/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserId.intValue())))
                .andExpect(jsonPath("$.jobTitle", is("Senior Software Engineer")));
    }

    @Test
    @DisplayName("✅ Should maintain user data consistency after updates")
    @WithMockUser(roles = "USER")
    void updateUserProfile_ShouldMaintainDataConsistency() throws Exception {
        // 🏗️ ARRANGE - Update profile
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Updated");
        request.setLastName("User");

        mockMvc.perform(put("/api/auth/user/profile/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 🚀 ACT & ASSERT - Verify email and other core data unchanged
        mockMvc.perform(get("/api/auth/user/profile/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserId.intValue())))
                .andExpect(jsonPath("$.email", is("usertest@example.com"))) // Should remain unchanged
                .andExpect(jsonPath("$.phone", is("+1234567890"))) // Should remain unchanged
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("User")));
    }

    @Test
    @DisplayName("❌ Should deny access without USER role")
    @WithMockUser(roles = "ADMIN") // Wrong role
    void getUserProfile_WithWrongRole_ShouldBeDenied() throws Exception {
        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/auth/user/profile/{userId}", testUserId))
                .andExpect(status().isForbidden());
    }
}
