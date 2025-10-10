package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.OrganizationResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationMemberResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationTeamResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService Unit Tests")
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private CreateOrganizationRequest createRequest;
    private User testOwner;
    private User testMember;
    private Organization testOrganization;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        createRequest = CreateOrganizationRequest.builder()
                .name("Test Organization")
                .description("Test Description")
                .website("https://test.com")
                .industry("Technology")
                .size("10-50")
                .location("Test City")
                .build();

        testOwner = User.builder()
                .userId(1L)
                .email("owner@test.com")
                .firstName("Owner")
                .lastName("User")
                .build();

        testMember = User.builder()
                .userId(2L)
                .email("member@test.com")
                .firstName("Member")
                .lastName("User")
                .build();

        testOrganization = Organization.builder()
                .id(1L)
                .name("Test Organization")
                .description("Test Description")
                .website("https://test.com")
                .industry("Technology")
                .size("10-50")
                .location("Test City")
                .owner(testOwner)
                .status(Organization.OrganizationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .members(new HashSet<>())
                .teams(new HashSet<>())
                .build();

        testTeam = Team.builder()
                .id(1L)
                .name("Test Team")
                .description("Test Team Description")
                .teamType(Team.TeamType.DEVELOPMENT)
                .members(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("Should create organization successfully")
    void createOrganization_Success() {
        // Given
        Long ownerId = 1L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(testOwner));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);
        when(userRepository.save(any(User.class))).thenReturn(testOwner);

        // When
        OrganizationResponse result = organizationService.createOrganization(createRequest, ownerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Organization");
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getOwnerEmail()).isEqualTo("owner@test.com");

        verify(userRepository).findById(ownerId);
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(testOwner);
    }

    @Test
    @DisplayName("Should throw exception when owner not found during creation")
    void createOrganization_OwnerNotFound_ThrowsException() {
        // Given
        Long ownerId = 999L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> organizationService.createOrganization(createRequest, ownerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(ownerId);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update organization successfully")
    void updateOrganization_Success() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 1L;
        testOrganization.setOwner(testOwner);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        CreateOrganizationRequest updateRequest = CreateOrganizationRequest.builder()
                .name("Updated Organization")
                .description("Updated Description")
                .build();

        // When
        OrganizationResponse result = organizationService.updateOrganization(organizationId, updateRequest, requesterId);

        // Then
        assertThat(result).isNotNull();
        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to update organization")
    void updateOrganization_NonOwner_ThrowsException() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 2L; // Different from owner ID
        testOrganization.setOwner(testOwner);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When & Then
        assertThatThrownBy(() -> organizationService.updateOrganization(organizationId, createRequest, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only organization owner can update organization details");

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get organization by ID successfully")
    void getOrganizationById_Success() {
        // Given
        Long organizationId = 1L;
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When
        OrganizationResponse result = organizationService.getOrganizationById(organizationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(organizationId);
        assertThat(result.getName()).isEqualTo("Test Organization");

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    @DisplayName("Should throw exception when organization not found")
    void getOrganizationById_NotFound_ThrowsException() {
        // Given
        Long organizationId = 999L;
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> organizationService.getOrganizationById(organizationId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Organization not found with ID: 999");

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    @DisplayName("Should add member to organization successfully")
    void addMember_Success() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 1L;
        String userEmail = "member@test.com";

        testOrganization.setOwner(testOwner);
        testOrganization.setMembers(new HashSet<>());

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testMember));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        // When
        organizationService.addMember(organizationId, requesterId, userEmail);

        // Then
        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findByEmail(userEmail);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    @DisplayName("Should throw exception when adding member who is already a member")
    void addMember_AlreadyMember_ThrowsException() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 1L;
        String userEmail = "member@test.com";

        testOrganization.setOwner(testOwner);
        testOrganization.setMembers(new HashSet<>(List.of(testMember)));

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testMember));

        // When & Then
        assertThatThrownBy(() -> organizationService.addMember(organizationId, requesterId, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is already a member of this organization");

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove member from organization successfully")
    void removeMember_Success() {
        // Given
        Long organizationId = 1L;
        Long userId = 2L;
        Long requesterId = 1L;

        testOrganization.setOwner(testOwner);
        testOrganization.setMembers(new HashSet<>(List.of(testMember)));

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testMember));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        // When
        organizationService.removeMember(organizationId, userId, requesterId);

        // Then
        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userId);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    @DisplayName("Should throw exception when trying to remove organization owner")
    void removeMember_RemoveOwner_ThrowsException() {
        // Given
        Long organizationId = 1L;
        Long userId = 1L; // Same as owner ID
        Long requesterId = 1L;

        testOrganization.setOwner(testOwner);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));

        // When & Then
        assertThatThrownBy(() -> organizationService.removeMember(organizationId, userId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot remove organization owner");

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all organizations successfully")
    void getAllOrganizations_Success() {
        // Given
        List<Organization> organizations = List.of(testOrganization);
        when(organizationRepository.findAll()).thenReturn(organizations);

        // When
        List<OrganizationResponse> result = organizationService.getAllOrganizations();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Test Organization");

        verify(organizationRepository).findAll();
    }

    @Test
    @DisplayName("Should get organizations by owner successfully")
    void getOrganizationsByOwner_Success() {
        // Given
        Long ownerId = 1L;
        List<Organization> organizations = List.of(testOrganization);
        when(organizationRepository.findByOwnerId(ownerId)).thenReturn(organizations);

        // When
        List<Organization> result = organizationService.getOrganizationsByOwner(ownerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(testOrganization);

        verify(organizationRepository).findByOwnerId(ownerId);
    }

    @Test
    @DisplayName("Should get organizations by member successfully")
    void getOrganizationsByMember_Success() {
        // Given
        Long userId = 2L;
        List<Organization> organizations = List.of(testOrganization);
        when(organizationRepository.findByMemberId(userId)).thenReturn(organizations);

        // When
        List<Organization> result = organizationService.getOrganizationsByMember(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(testOrganization);

        verify(organizationRepository).findByMemberId(userId);
    }

    @Test
    @DisplayName("Should search organizations successfully")
    void searchOrganizations_Success() {
        // Given
        String searchTerm = "Test";
        List<Organization> organizations = List.of(testOrganization);
        when(organizationRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(organizations);

        // When
        List<Organization> result = organizationService.searchOrganizations(searchTerm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(testOrganization);

        verify(organizationRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    @DisplayName("Should check if user is member successfully")
    void isUserMember_Success() {
        // Given
        Long organizationId = 1L;
        Long userId = 2L;
        when(organizationRepository.isUserMemberOfOrganization(organizationId, userId)).thenReturn(true);

        // When
        boolean result = organizationService.isUserMember(organizationId, userId);

        // Then
        assertThat(result).isTrue();
        verify(organizationRepository).isUserMemberOfOrganization(organizationId, userId);
    }

    @Test
    @DisplayName("Should check if user is owner successfully")
    void isUserOwner_Success() {
        // Given
        Long organizationId = 1L;
        Long userId = 1L;
        when(organizationRepository.isUserOwnerOfOrganization(organizationId, userId)).thenReturn(true);

        // When
        boolean result = organizationService.isUserOwner(organizationId, userId);

        // Then
        assertThat(result).isTrue();
        verify(organizationRepository).isUserOwnerOfOrganization(organizationId, userId);
    }

    @Test
    @DisplayName("Should delete organization successfully")
    void deleteOrganization_Success() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 1L;

        testOrganization.setOwner(testOwner);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When
        organizationService.deleteOrganization(organizationId, requesterId);

        // Then
        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).delete(testOrganization);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete organization")
    void deleteOrganization_NonOwner_ThrowsException() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 2L; // Different from owner ID

        testOrganization.setOwner(testOwner);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When & Then
        assertThatThrownBy(() -> organizationService.deleteOrganization(organizationId, requesterId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only organization owner can delete organization");

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get organization members successfully")
    void getOrganizationMembers_Success() {
        // Given
        Long organizationId = 1L;
        testMember.setJobTitle("Developer");
        testMember.setDepartment("Engineering");
        testOrganization.setMembers(new HashSet<>(List.of(testMember)));

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When
        List<OrganizationMemberResponse> result = organizationService.getOrganizationMembers(organizationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId()).isEqualTo(2L);
        assertThat(result.getFirst().getEmail()).isEqualTo("member@test.com");
        assertThat(result.getFirst().getJobTitle()).isEqualTo("Developer");
        assertThat(result.getFirst().getDepartment()).isEqualTo("Engineering");

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    @DisplayName("Should get organization teams successfully")
    void getOrganizationTeams_Success() {
        // Given
        Long organizationId = 1L;
        testTeam.setMembers(new HashSet<>(List.of(testMember)));
        testOrganization.setTeams(new HashSet<>(List.of(testTeam)));

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));

        // When
        List<OrganizationTeamResponse> result = organizationService.getOrganizationTeams(organizationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTeamId()).isEqualTo(1L);
        assertThat(result.getFirst().getTeamName()).isEqualTo("Test Team");
        assertThat(result.getFirst().getTeamType()).isEqualTo("DEVELOPMENT");
        assertThat(result.getFirst().getMemberCount()).isEqualTo(1);

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    @DisplayName("Should get owned organizations successfully")
    void getOwnedOrganizations_Success() {
        // Given
        Long userId = 1L;
        testOwner.setOwnedOrganizations(new HashSet<>(List.of(testOrganization)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));

        // When
        List<OrganizationResponse> result = organizationService.getOwnedOrganizations(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Test Organization");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should add member by ID successfully")
    void addMemberById_Success() {
        // Given
        Long organizationId = 1L;
        Long requesterId = 1L;
        Long userIdToAdd = 2L;

        testOrganization.setOwner(testOwner);
        testOrganization.setMembers(new HashSet<>());

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrganization));
        when(userRepository.findById(userIdToAdd)).thenReturn(Optional.of(testMember));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(testOwner));
        when(userRepository.save(any(User.class))).thenReturn(testMember);

        // When
        organizationService.addMemberById(organizationId, requesterId, userIdToAdd);

        // Then
        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userIdToAdd);
        verify(userRepository).findById(requesterId);
        verify(userRepository).save(testMember);
    }
}
