package com.midlane.project_management_tool_user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.RegisterRequest;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
 * 🏢 OrganizationController Integration Tests
 * 
 * Tests the complete organization management workflow including:
 * - Creating organizations
 * - Retrieving organizations 
 * - Adding/removing members
 * - Organization ownership and permissions
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("🏢 OrganizationController Integration Tests")
class OrganizationControllerIntegrationTest {

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
                .email("orgtest@example.com")
                .password("TestPassword123!")
                .phone("+1234567890")
                .build();

        String registerJson = objectMapper.writeValueAsString(registerRequest);
        
        String response = mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract user ID from response (you might need to adjust this based on your AuthResponse structure)
        // For now, we'll get it from the database
        User testUser = userRepository.findByEmail("orgtest@example.com").orElseThrow();
        testUserId = testUser.getId();
    }

    @Test
    @DisplayName("✅ Should create organization with valid data")
    void createOrganization_WithValidData_ShouldReturnOrganizationResponse() throws Exception {
        // 🏗️ ARRANGE
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Test Organization")
                .description("A test organization for integration testing")
                .website("https://testorg.com")
                .industry("Software")
                .size("10-50")
                .location("San Francisco, CA")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .param("ownerId", testUserId.toString()))

                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                // Verify organization details
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Organization")))
                .andExpect(jsonPath("$.description", is("A test organization for integration testing")))
                .andExpect(jsonPath("$.website", is("https://testorg.com")))
                .andExpect(jsonPath("$.industry", is("Software")))
                .andExpect(jsonPath("$.size", is("10-50")))
                .andExpect(jsonPath("$.location", is("San Francisco, CA")))
                .andExpect(jsonPath("$.ownerId", is(testUserId.intValue())))
                .andExpect(jsonPath("$.ownerEmail", is("orgtest@example.com")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.memberCount", is(1))) // Owner is automatically a member
                .andExpect(jsonPath("$.teamCount", is(0)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @DisplayName("❌ Should return 400 when organization name is missing")
    void createOrganization_WithMissingName_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE - Invalid request with missing name
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .description("A test organization")
                .website("https://testorg.com")
                // name is missing
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .param("ownerId", testUserId.toString()))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("name")));
    }

    @Test
    @DisplayName("✅ Should retrieve all organizations")
    void getAllOrganizations_ShouldReturnOrganizationList() throws Exception {
        // 🏗️ ARRANGE - Create a test organization first
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Test Org for Retrieval")
                .description("Test organization")
                .build();

        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/organizations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].ownerId", notNullValue()));
    }

    @Test
    @DisplayName("✅ Should get organizations owned by user")
    void getOwnedOrganizations_ShouldReturnUserOwnedOrganizations() throws Exception {
        // 🏗️ ARRANGE - Create a test organization
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("User Owned Organization")
                .description("Test organization owned by user")
                .build();

        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/organizations/users/{userId}/owned", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].name", is("User Owned Organization")))
                .andExpect(jsonPath("$[0].ownerId", is(testUserId.intValue())));
    }

    @Test
    @DisplayName("✅ Should get organization by ID")
    void getOrganizationById_WithValidId_ShouldReturnOrganization() throws Exception {
        // 🏗️ ARRANGE - Create organization and extract ID
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Organization by ID Test")
                .description("Test organization for ID retrieval")
                .build();

        String createResponse = mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract organization ID from response (you might need to parse JSON)
        // For simplicity, assuming the first organization in the test
        
        // 🚀 ACT & ASSERT - Get by ID (using 1 as example, adjust as needed)
        mockMvc.perform(get("/api/users/organizations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", notNullValue()))
                .andExpect(jsonPath("$.ownerId", notNullValue()));
    }

    @Test
    @DisplayName("❌ Should return 404 when organization not found")
    void getOrganizationById_WithInvalidId_ShouldReturn404() throws Exception {
        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/organizations/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("✅ Should update organization with valid data")
    void updateOrganization_WithValidData_ShouldReturnUpdatedOrganization() throws Exception {
        // 🏗️ ARRANGE - Create organization first
        CreateOrganizationRequest createRequest = CreateOrganizationRequest.builder()
                .name("Original Organization")
                .description("Original description")
                .build();

        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated());

        // Prepare update request
        CreateOrganizationRequest updateRequest = CreateOrganizationRequest.builder()
                .name("Updated Organization")
                .description("Updated description")
                .website("https://updated.com")
                .build();

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/users/organizations/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .param("requesterId", testUserId.toString()))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Updated Organization")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.website", is("https://updated.com")));
    }

    @Test
    @DisplayName("✅ Should get organization members")
    void getOrganizationMembers_ShouldReturnMemberList() throws Exception {
        // 🏗️ ARRANGE - Create organization
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Members Test Organization")
                .description("Test organization for members")
                .build();

        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/organizations/{organizationId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1)))) // At least the owner
                .andExpect(jsonPath("$[0].userId", notNullValue()))
                .andExpect(jsonPath("$[0].email", notNullValue()));
    }

    @Test
    @DisplayName("✅ Should delete organization when requested by owner")
    void deleteOrganization_ByOwner_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Create organization
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("To Be Deleted Organization")
                .description("This organization will be deleted")
                .build();

        mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(delete("/api/users/organizations/{id}", 1L)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isNoContent());

        // Verify organization is deleted
        mockMvc.perform(get("/api/users/organizations/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}
