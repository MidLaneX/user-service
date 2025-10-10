package com.midlane.project_management_tool_user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
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
 * 👥 TeamController Integration Tests
 *
 * Tests the complete team management workflow including:
 * - Creating teams within organizations
 * - Managing team members and team leads
 * - Team retrieval and filtering
 * - Team updates and deletion
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("👥 TeamController Integration Tests")
class TeamControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private Long testUserId;
    private Long organizationId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print())
                .build();

        // Create a test user first
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("teamtest@example.com")
                .password("TestPassword123!")
                .phone("+1234567890")
                .build();

        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User testUser = userRepository.findByEmail("teamtest@example.com").orElseThrow();
        testUserId = testUser.getId();

        // Create a test organization for teams
        CreateOrganizationRequest orgRequest = CreateOrganizationRequest.builder()
                .name("Test Organization for Teams")
                .description("Organization for team testing")
                .build();

        String orgResponse = mockMvc.perform(post("/api/users/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgRequest))
                .param("ownerId", testUserId.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract organization ID from response (assuming first organization gets ID 1 or similar)
        organizationId = 1L; // Adjust based on your actual implementation
    }

    @Test
    @DisplayName("✅ Should create team with valid data")
    void createTeam_WithValidData_ShouldReturnTeamResponse() throws Exception {
        // 🏗️ ARRANGE
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Development Team")
                .description("A team for software development")
                .teamType("DEVELOPMENT")
                .maxMembers(10)
                .organizationId(organizationId)
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .param("creatorId", testUserId.toString()))

                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Verify team details
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Development Team")))
                .andExpect(jsonPath("$.description", is("A team for software development")))
                .andExpect(jsonPath("$.teamType", is("DEVELOPMENT")))
                .andExpect(jsonPath("$.maxMembers", is(10)))
                .andExpect(jsonPath("$.organizationId", is(organizationId.intValue())))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("❌ Should return 400 when team name is missing")
    void createTeam_WithMissingName_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE - Invalid request with missing name
        CreateTeamRequest request = CreateTeamRequest.builder()
                .description("A team without name")
                .organizationId(organizationId)
                // name is missing
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .param("creatorId", testUserId.toString()))

                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Should retrieve all teams")
    void getAllTeams_ShouldReturnTeamList() throws Exception {
        // 🏗️ ARRANGE - Create a test team first
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Test Team for Retrieval")
                .description("Test team")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/teams"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].organizationId", notNullValue()));
    }

    @Test
    @DisplayName("✅ Should get team by ID")
    void getTeamById_WithValidId_ShouldReturnTeam() throws Exception {
        // 🏗️ ARRANGE - Create team first
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Team by ID Test")
                .description("Test team for ID retrieval")
                .organizationId(organizationId)
                .build();

        String createResponse = mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 🚀 ACT & ASSERT - Get by ID (using 1 as example, adjust as needed)
        mockMvc.perform(get("/api/users/teams/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Team by ID Test")))
                .andExpect(jsonPath("$.organizationId", is(organizationId.intValue())));
    }

    @Test
    @DisplayName("❌ Should return 404 when team not found")
    void getTeamById_WithInvalidId_ShouldReturn404() throws Exception {
        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/teams/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("✅ Should update team with valid data")
    void updateTeam_WithValidData_ShouldReturnUpdatedTeam() throws Exception {
        // 🏗️ ARRANGE - Create team first
        CreateTeamRequest createRequest = CreateTeamRequest.builder()
                .name("Original Team")
                .description("Original description")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // Prepare update request
        CreateTeamRequest updateRequest = CreateTeamRequest.builder()
                .name("Updated Team")
                .description("Updated description")
                .teamType("TESTING")
                .maxMembers(15)
                .organizationId(organizationId)
                .build();

        // 🚀 ACT & ASSERT
        mockMvc.perform(put("/api/users/teams/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .param("requesterId", testUserId.toString()))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Updated Team")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.teamType", is("TESTING")))
                .andExpect(jsonPath("$.maxMembers", is(15)));
    }

    @Test
    @DisplayName("✅ Should get team members")
    void getTeamMembers_ShouldReturnMemberList() throws Exception {
        // 🏗️ ARRANGE - Create team
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Members Test Team")
                .description("Test team for members")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/teams/{teamId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1)))); // At least the creator
    }

    @Test
    @DisplayName("✅ Should get teams by organization and user")
    void getTeamsByOrganizationAndUser_ShouldReturnFilteredTeams() throws Exception {
        // 🏗️ ARRANGE - Create team
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Organization User Team")
                .description("Test team for organization and user filtering")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(get("/api/users/teams/organization/{organizationId}/user/{userId}",
                organizationId, testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].name", is("Organization User Team")))
                .andExpect(jsonPath("$[0].organizationId", is(organizationId.intValue())));
    }

    @Test
    @DisplayName("✅ Should add member to team")
    void addMember_WithValidData_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Create team and another user
        CreateTeamRequest teamRequest = CreateTeamRequest.builder()
                .name("Add Member Test Team")
                .description("Test team for adding members")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teamRequest))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // Create another user to add as member
        RegisterRequest memberRequest = RegisterRequest.builder()
                .email("teammember@example.com")
                .password("MemberPassword123!")
                .phone("+9876543210")
                .build();

        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        User memberUser = userRepository.findByEmail("teammember@example.com").orElseThrow();
        Long memberId = memberUser.getId();

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/teams/{teamId}/members/{userId}", 1L, memberId)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isOk());

        // Verify member was added
        mockMvc.perform(get("/api/users/teams/{teamId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // Creator + new member
    }

    @Test
    @DisplayName("✅ Should set team lead")
    void setTeamLead_WithValidData_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Create team and add member
        CreateTeamRequest teamRequest = CreateTeamRequest.builder()
                .name("Team Lead Test Team")
                .description("Test team for team lead assignment")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teamRequest))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // Create and add member
        RegisterRequest memberRequest = RegisterRequest.builder()
                .email("newlead@example.com")
                .password("LeadPassword123!")
                .phone("+1122334455")
                .build();

        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        User memberUser = userRepository.findByEmail("newlead@example.com").orElseThrow();
        Long memberId = memberUser.getId();

        // Add member to team first
        mockMvc.perform(post("/api/users/teams/{teamId}/members/{userId}", 1L, memberId)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isOk());

        // 🚀 ACT & ASSERT - Set as team lead
        mockMvc.perform(put("/api/users/teams/{teamId}/lead/{userId}", 1L, memberId)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should remove member from team")
    void removeMember_WithValidData_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Create team and add member
        CreateTeamRequest teamRequest = CreateTeamRequest.builder()
                .name("Remove Member Test Team")
                .description("Test team for removing members")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teamRequest))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // Create and add member
        RegisterRequest memberRequest = RegisterRequest.builder()
                .email("toberemoved@example.com")
                .password("RemovePassword123!")
                .phone("+1111222333")
                .build();

        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        User memberUser = userRepository.findByEmail("toberemoved@example.com").orElseThrow();
        Long memberId = memberUser.getId();

        // Add member to team first
        mockMvc.perform(post("/api/users/teams/{teamId}/members/{userId}", 1L, memberId)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isOk());

        // 🚀 ACT & ASSERT - Remove member
        mockMvc.perform(delete("/api/users/teams/{teamId}/members/{userId}", 1L, memberId)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isNoContent());

        // Verify member was removed
        mockMvc.perform(get("/api/users/teams/{teamId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Only creator remains
    }

    @Test
    @DisplayName("✅ Should delete team when requested by team lead")
    void deleteTeam_ByTeamLead_ShouldSucceed() throws Exception {
        // 🏗️ ARRANGE - Create team
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("To Be Deleted Team")
                .description("This team will be deleted")
                .organizationId(organizationId)
                .build();

        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("creatorId", testUserId.toString()))
                .andExpect(status().isCreated());

        // 🚀 ACT & ASSERT
        mockMvc.perform(delete("/api/users/teams/{id}", 1L)
                .param("requesterId", testUserId.toString()))
                .andExpect(status().isNoContent());

        // Verify team is deleted
        mockMvc.perform(get("/api/users/teams/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("❌ Should return 400 when maxMembers exceeds limit")
    void createTeam_WithInvalidMaxMembers_ShouldReturn400() throws Exception {
        // 🏗️ ARRANGE - Invalid request with too many max members
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("Invalid Team")
                .description("Team with invalid max members")
                .maxMembers(150) // Exceeds limit of 100
                .organizationId(organizationId)
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // 🚀 ACT & ASSERT
        mockMvc.perform(post("/api/users/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .param("creatorId", testUserId.toString()))

                .andExpect(status().isBadRequest());
    }
}
