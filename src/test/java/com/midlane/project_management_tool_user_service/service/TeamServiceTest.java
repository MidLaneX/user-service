package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
import com.midlane.project_management_tool_user_service.dto.MemberDetailsResponse;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.Role;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Unit Tests")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamEventProducerService teamEventProducerService;

    @InjectMocks
    private TeamService teamService;

    private CreateTeamRequest createTeamRequest;
    private Organization testOrganization;
    private User testUser;
    private User testOwner;
    private Team testTeam;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name(Role.USER)
                .permissions("USER_PERMISSIONS")
                .build();

        testOwner = User.builder()
                .userId(1L)
                .email("owner@example.com")
                .firstName("Owner")
                .lastName("User")
                .role(userRole)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .passwordLastChanged(LocalDateTime.now())
                .emailLastChanged(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .userId(2L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role(userRole)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .passwordLastChanged(LocalDateTime.now())
                .emailLastChanged(LocalDateTime.now())
                .build();

        testOrganization = Organization.builder()
                .id(1L)
                .name("Test Organization")
                .description("Test Description")
                .owner(testOwner)
                .members(new HashSet<>(Arrays.asList(testOwner, testUser)))
                .status(Organization.OrganizationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        createTeamRequest = CreateTeamRequest.builder()
                .name("Test Team")
                .description("Test Team Description")
                .teamType("DEVELOPMENT")
                .maxMembers(10)
                .organizationId(1L)
                .build();

        testTeam = Team.builder()
                .id(1L)
                .name("Test Team")
                .description("Test Team Description")
                .teamType(Team.TeamType.DEVELOPMENT)
                .maxMembers(10)
                .organization(testOrganization)
                .status(Team.TeamStatus.ACTIVE)
                .members(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create team successfully when valid request is provided")
    void createTeam_Success() {
        // Given
        Long creatorId = testOwner.getUserId();
        
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(creatorId))
                .thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class)))
                .thenReturn(testTeam);

        // When
        TeamResponse response = teamService.createTeam(createTeamRequest, creatorId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Team");
        assertThat(response.getDescription()).isEqualTo("Test Team Description");
        assertThat(response.getTeamType()).isEqualTo("DEVELOPMENT");
        assertThat(response.getMaxMembers()).isEqualTo(10);
        assertThat(response.getOrganizationId()).isEqualTo(1L);
        assertThat(response.getOrganizationName()).isEqualTo("Test Organization");

        // Verify interactions
        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository).findById(creatorId);
        verify(teamRepository, times(2)).save(any(Team.class)); // Once for team creation, once for adding creator as member
        verify(teamEventProducerService).publishTeamCreatedEvent(anyLong(), anyString(), anyString(), anyLong());
        verify(teamEventProducerService).publishTeamMemberAddedEvent(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
        verify(teamEventProducerService).publishMemberAddedToTeamEvent(anyLong(), anyLong(), eq("OWNER"));
    }

    @Test
    @DisplayName("Should throw exception when organization not found during team creation")
    void createTeam_OrganizationNotFound_ThrowsException() {
        // Given
        Long creatorId = testOwner.getUserId();
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(createTeamRequest, creatorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Organization not found with ID: " + createTeamRequest.getOrganizationId());

        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository, never()).findById(anyLong());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw exception when creator not found during team creation")
    void createTeam_CreatorNotFound_ThrowsException() {
        // Given
        Long creatorId = 999L;
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(creatorId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(createTeamRequest, creatorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + creatorId);

        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository).findById(creatorId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw exception when creator is not member of organization")
    void createTeam_CreatorNotOrganizationMember_ThrowsException() {
        // Given
        User nonMemberUser = User.builder()
                .userId(3L)
                .email("nonmember@example.com")
                .firstName("Non")
                .lastName("Member")
                .role(userRole)
                .build();
        
        Long creatorId = nonMemberUser.getUserId();
        
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(creatorId))
                .thenReturn(Optional.of(nonMemberUser));

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(createTeamRequest, creatorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User must be a member of the organization to create teams");

        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository).findById(creatorId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should use default team type when invalid team type is provided")
    void createTeam_InvalidTeamType_ThrowsException() {
        // Given
        createTeamRequest.setTeamType("INVALID_TYPE");
        Long creatorId = testOwner.getUserId();
        
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(creatorId))
                .thenReturn(Optional.of(testOwner));

        // When & Then
        assertThatThrownBy(() -> teamService.createTeam(createTeamRequest, creatorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid team type: INVALID_TYPE");

        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository).findById(creatorId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should get team by ID successfully when team exists")
    void getTeamById_Success() {
        // Given
        Long teamId = 1L;
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));

        // When
        TeamResponse response = teamService.getTeamById(teamId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(teamId);
        assertThat(response.getName()).isEqualTo("Test Team");
        assertThat(response.getOrganizationId()).isEqualTo(1L);

        verify(teamRepository).findById(teamId);
    }

    @Test
    @DisplayName("Should throw exception when team not found by ID")
    void getTeamById_TeamNotFound_ThrowsException() {
        // Given
        Long teamId = 999L;
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.getTeamById(teamId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Team not found with ID: " + teamId);

        verify(teamRepository).findById(teamId);
    }

    @Test
    @DisplayName("Should add member to team successfully")
    void addMember_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId();

        testTeam.setTeamLead(testOwner); // Set team lead for permission check
        
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // When
        teamService.addMember(teamId, userId, requesterId);

        // Then
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        verify(teamEventProducerService).publishTeamMemberAddedEvent(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
        verify(teamEventProducerService).publishMemberAddedToTeamEvent(anyLong(), anyLong(), eq("MEMBER"));
    }

    @Test
    @DisplayName("Should throw exception when adding member to non-existent team")
    void addMember_TeamNotFound_ThrowsException() {
        // Given
        Long teamId = 999L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId();

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.addMember(teamId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Team not found with ID: " + teamId);

        verify(teamRepository).findById(teamId);
        verify(userRepository, never()).findById(anyLong());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw exception when adding non-existent user to team")
    void addMember_UserNotFound_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long userId = 999L;
        Long requesterId = testOwner.getUserId();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamService.addMember(teamId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: " + userId);

        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw exception when requester has insufficient permissions")
    void addMember_InsufficientPermissions_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testUser.getUserId(); // User trying to add themselves without permissions

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> teamService.addMember(teamId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient permissions to add team members");

        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw exception when adding user who is not organization member")
    void addMember_UserNotOrganizationMember_ThrowsException() {
        // Given
        User nonOrgMember = User.builder()
                .userId(3L)
                .email("external@example.com")
                .firstName("External")
                .lastName("User")
                .role(userRole)
                .build();

        Long teamId = 1L;
        Long userId = nonOrgMember.getUserId();
        Long requesterId = testOwner.getUserId();

        testTeam.setTeamLead(testOwner);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(nonOrgMember));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));

        // When & Then
        assertThatThrownBy(() -> teamService.addMember(teamId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User must be a member of the organization to join teams");

        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should remove member from team successfully")
    void removeMember_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId();

        testTeam.setTeamLead(testOwner);
        testTeam.addMember(testUser); // Add user to team first

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // When
        teamService.removeMember(teamId, userId, requesterId);

        // Then
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        verify(teamEventProducerService).publishTeamMemberRemovedEvent(teamId, userId);
    }

    @Test
    @DisplayName("Should allow user to remove themselves from team")
    void removeMember_SelfRemoval_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testUser.getUserId(); // User removing themselves

        testTeam.addMember(testUser);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testUser));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // When
        teamService.removeMember(teamId, userId, requesterId);

        // Then
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        verify(teamEventProducerService).publishTeamMemberRemovedEvent(teamId, userId);
    }

    @Test
    @DisplayName("Should set team lead successfully")
    void setTeamLead_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId(); // Organization owner

        testTeam.addMember(testUser); // User must be team member first

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // When
        teamService.setTeamLead(teamId, userId, requesterId);

        // Then
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        assertThat(testTeam.getTeamLead()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should throw exception when setting team lead for non-team member")
    void setTeamLead_UserNotTeamMember_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId();

        // Don't add user to team

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));

        // When & Then
        assertThatThrownBy(() -> teamService.setTeamLead(teamId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User must be a team member to become team lead");

        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @DisplayName("Should update team successfully")
    void updateTeam_Success() {
        // Given
        Long teamId = 1L;
        Long requesterId = testOwner.getUserId();
        
        CreateTeamRequest updateRequest = CreateTeamRequest.builder()
                .name("Updated Team Name")
                .description("Updated Description")
                .teamType("QA")
                .maxMembers(15)
                .build();

        testTeam.setTeamLead(testOwner);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // When
        TeamResponse response = teamService.updateTeam(teamId, updateRequest, requesterId);

        // Then
        assertThat(response).isNotNull();
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        
        // Verify team was updated
        assertThat(testTeam.getName()).isEqualTo("Updated Team Name");
        assertThat(testTeam.getDescription()).isEqualTo("Updated Description");
        assertThat(testTeam.getTeamType()).isEqualTo(Team.TeamType.QA);
        assertThat(testTeam.getMaxMembers()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should delete team successfully when requester is organization owner")
    void deleteTeam_Success() {
        // Given
        Long teamId = 1L;
        Long requesterId = testOwner.getUserId();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));

        // When
        teamService.deleteTeam(teamId, requesterId);

        // Then
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).delete(testTeam);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete team")
    void deleteTeam_InsufficientPermissions_ThrowsException() {
        // Given
        Long teamId = 1L;
        Long requesterId = testUser.getUserId(); // Not the organization owner

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> teamService.deleteTeam(teamId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only organization owner can delete teams");

        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    @DisplayName("Should get all teams successfully")
    void getAllTeams_Success() {
        // Given
        List<Team> teams = Arrays.asList(testTeam);
        when(teamRepository.findAll()).thenReturn(teams);

        // When
        List<TeamResponse> responses = teamService.getAllTeams();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Test Team");
        verify(teamRepository).findAll();
    }

    @Test
    @DisplayName("Should get teams by organization successfully")
    void getTeamsByOrganization_Success() {
        // Given
        Long organizationId = 1L;
        List<Team> teams = Arrays.asList(testTeam);
        when(teamRepository.findByOrganizationId(organizationId)).thenReturn(teams);

        // When
        List<Team> result = teamService.getTeamsByOrganization(organizationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Team");
        verify(teamRepository).findByOrganizationId(organizationId);
    }

    @Test
    @DisplayName("Should get teams by member successfully")
    void getTeamsByMember_Success() {
        // Given
        Long userId = testUser.getUserId();
        List<Team> teams = Arrays.asList(testTeam);
        when(teamRepository.findByMemberId(userId)).thenReturn(teams);

        // When
        List<Team> result = teamService.getTeamsByMember(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Team");
        verify(teamRepository).findByMemberId(userId);
    }

    @Test
    @DisplayName("Should get teams by organization and member successfully")
    void getTeamsByOrganizationAndMember_Success() {
        // Given
        Long organizationId = 1L;
        Long userId = testUser.getUserId();
        
        testTeam.addMember(testUser); // Add user to team
        List<Team> teams = Arrays.asList(testTeam);
        
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(teamRepository.findByMemberId(userId)).thenReturn(teams);

        // When
        List<TeamResponse> result = teamService.getTeamsByOrganizationAndMember(organizationId, userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Team");
        assertThat(result.get(0).getOrganizationId()).isEqualTo(organizationId);
        
        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userId);
        verify(teamRepository).findByMemberId(userId);
    }

    @Test
    @DisplayName("Should get team members successfully")
    void getTeamMembers_Success() {
        // Given
        Long teamId = 1L;
        testTeam.addMember(testOwner);
        testTeam.addMember(testUser);
        testTeam.setTeamLead(testOwner);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));

        // When
        List<MemberDetailsResponse> members = teamService.getTeamMembers(teamId);

        // Then
        assertThat(members).hasSize(2);
        verify(teamRepository).findById(teamId);
        
        // Verify at least one member has team lead flag set
        boolean hasTeamLead = members.stream().anyMatch(MemberDetailsResponse::isTeamLead);
        assertThat(hasTeamLead).isTrue();
    }

    @Test
    @DisplayName("Should check if user is team member successfully")
    void isUserMember_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        when(teamRepository.isUserMemberOfTeam(teamId, userId)).thenReturn(true);

        // When
        boolean result = teamService.isUserMember(teamId, userId);

        // Then
        assertThat(result).isTrue();
        verify(teamRepository).isUserMemberOfTeam(teamId, userId);
    }

    @Test
    @DisplayName("Should check if user is team lead successfully")
    void isUserTeamLead_Success() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        when(teamRepository.isUserTeamLead(teamId, userId)).thenReturn(true);

        // When
        boolean result = teamService.isUserTeamLead(teamId, userId);

        // Then
        assertThat(result).isTrue();
        verify(teamRepository).isUserTeamLead(teamId, userId);
    }

    @Test
    @DisplayName("Should get teams with available slots successfully")
    void getTeamsWithAvailableSlots_Success() {
        // Given
        List<Team> teams = Arrays.asList(testTeam);
        when(teamRepository.findTeamsWithAvailableSlots()).thenReturn(teams);

        // When
        List<Team> result = teamService.getTeamsWithAvailableSlots();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Team");
        verify(teamRepository).findTeamsWithAvailableSlots();
    }

    @Test
    @DisplayName("Should handle Kafka event publishing failures gracefully in createTeam")
    void createTeam_KafkaFailure_ContinuesExecution() {
        // Given
        Long creatorId = testOwner.getUserId();
        
        when(organizationRepository.findById(createTeamRequest.getOrganizationId()))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(creatorId))
                .thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class)))
                .thenReturn(testTeam);
        
        // Mock Kafka failure
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(teamEventProducerService).publishTeamCreatedEvent(anyLong(), anyString(), anyString(), anyLong());

        // When
        TeamResponse response = teamService.createTeam(createTeamRequest, creatorId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Team");
        
        // Verify team creation still succeeded despite Kafka failure
        verify(organizationRepository).findById(createTeamRequest.getOrganizationId());
        verify(userRepository).findById(creatorId);
        verify(teamRepository, times(2)).save(any(Team.class));
        verify(teamEventProducerService).publishTeamCreatedEvent(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Should handle Kafka event publishing failures gracefully in addMember")
    void addMember_KafkaFailure_ContinuesExecution() {
        // Given
        Long teamId = 1L;
        Long userId = testUser.getUserId();
        Long requesterId = testOwner.getUserId();

        testTeam.setTeamLead(testOwner);
        
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);
        
        // Mock Kafka failure
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(teamEventProducerService).publishTeamMemberAddedEvent(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

        // When & Then - Should not throw exception
        assertThatCode(() -> teamService.addMember(teamId, userId, requesterId))
                .doesNotThrowAnyException();

        // Verify member addition still succeeded despite Kafka failure
        verify(teamRepository).findById(teamId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(requesterId);
        verify(teamRepository).save(testTeam);
        verify(teamEventProducerService).publishTeamMemberAddedEvent(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
    }
}
