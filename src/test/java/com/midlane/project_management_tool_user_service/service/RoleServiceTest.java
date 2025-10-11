package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateRoleRequest;
import com.midlane.project_management_tool_user_service.dto.RoleResponse;
import com.midlane.project_management_tool_user_service.model.Role;
import com.midlane.project_management_tool_user_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private CreateRoleRequest createRoleRequest;
    private Role testRole;

    @BeforeEach
    void setUp() {
        createRoleRequest = CreateRoleRequest.builder()
                .name("ADMIN")
                .permissions("ADMIN_PERMISSIONS")
                .build();

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setPermissions("ADMIN_PERMISSIONS");
    }

    @Test
    @DisplayName("Should create role successfully")
    void createRole_Success() {
        // Given
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        RoleResponse result = roleService.createRole(createRoleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ADMIN");
        assertThat(result.getPermissions()).isEqualTo("ADMIN_PERMISSIONS");

        verify(roleRepository).existsByName("ADMIN");
        
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        
        Role savedRole = roleCaptor.getValue();
        assertThat(savedRole.getName()).isEqualTo("ADMIN");
        assertThat(savedRole.getPermissions()).isEqualTo("ADMIN_PERMISSIONS");
    }

    @Test
    @DisplayName("Should throw exception when creating role with existing name")
    void createRole_ExistingName_ThrowsException() {
        // Given
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(createRoleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role with this name already exists");

        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void getAllRoles_Success() {
        // Given
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ADMIN");
        role1.setPermissions("ADMIN_PERMISSIONS");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("USER");
        role2.setPermissions("USER_PERMISSIONS");

        List<Role> roles = Arrays.asList(role1, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<RoleResponse> result = roleService.getAllRoles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ADMIN");
        assertThat(result.get(1).getName()).isEqualTo("USER");

        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Should get role by ID successfully")
    void getRoleById_Success() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

        // When
        RoleResponse result = roleService.getRoleById(roleId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ADMIN");
        assertThat(result.getPermissions()).isEqualTo("ADMIN_PERMISSIONS");

        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("Should throw exception when role not found by ID")
    void getRoleById_NotFound_ThrowsException() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.getRoleById(roleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("Should update role successfully")
    void updateRole_Success() {
        // Given
        Long roleId = 1L;
        CreateRoleRequest updateRequest = CreateRoleRequest.builder()
                .name("SUPER_ADMIN")
                .permissions("SUPER_ADMIN_PERMISSIONS")
                .build();

        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setName("SUPER_ADMIN");
        updatedRole.setPermissions("SUPER_ADMIN_PERMISSIONS");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("SUPER_ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // When
        RoleResponse result = roleService.updateRole(roleId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("SUPER_ADMIN");
        assertThat(result.getPermissions()).isEqualTo("SUPER_ADMIN_PERMISSIONS");

        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByName("SUPER_ADMIN");
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("Should update role with same name successfully")
    void updateRole_SameName_Success() {
        // Given
        Long roleId = 1L;
        CreateRoleRequest updateRequest = CreateRoleRequest.builder()
                .name("ADMIN") // Same name
                .permissions("UPDATED_ADMIN_PERMISSIONS")
                .build();

        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setName("ADMIN");
        updatedRole.setPermissions("UPDATED_ADMIN_PERMISSIONS");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // When
        RoleResponse result = roleService.updateRole(roleId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ADMIN");
        assertThat(result.getPermissions()).isEqualTo("UPDATED_ADMIN_PERMISSIONS");

        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).existsByName(anyString()); // Should not check existence for same name
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("Should throw exception when updating role with existing name")
    void updateRole_ExistingName_ThrowsException() {
        // Given
        Long roleId = 1L;
        CreateRoleRequest updateRequest = CreateRoleRequest.builder()
                .name("USER") // Different name that already exists
                .permissions("USER_PERMISSIONS")
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("USER")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(roleId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role with this name already exists");

        verify(roleRepository).findById(roleId);
        verify(roleRepository).existsByName("USER");
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent role")
    void updateRole_RoleNotFound_ThrowsException() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(roleId, createRoleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete role successfully")
    void deleteRole_Success() {
        // Given
        Long roleId = 1L;
        when(roleRepository.existsById(roleId)).thenReturn(true);

        // When
        roleService.deleteRole(roleId);

        // Then
        verify(roleRepository).existsById(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent role")
    void deleteRole_RoleNotFound_ThrowsException() {
        // Given
        Long roleId = 999L;
        when(roleRepository.existsById(roleId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");

        verify(roleRepository).existsById(roleId);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void getAllRoles_EmptyList_Success() {
        // Given
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<RoleResponse> result = roleService.getAllRoles();

        // Then
        assertThat(result).isEmpty();
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Should create role with null permissions successfully")
    void createRole_NullPermissions_Success() {
        // Given
        CreateRoleRequest requestWithNullPermissions = CreateRoleRequest.builder()
                .name("VIEWER")
                .permissions(null)
                .build();

        Role roleWithNullPermissions = new Role();
        roleWithNullPermissions.setId(2L);
        roleWithNullPermissions.setName("VIEWER");
        roleWithNullPermissions.setPermissions(null);

        when(roleRepository.existsByName("VIEWER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(roleWithNullPermissions);

        // When
        RoleResponse result = roleService.createRole(requestWithNullPermissions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("VIEWER");
        assertThat(result.getPermissions()).isNull();

        verify(roleRepository).existsByName("VIEWER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should create role with empty permissions successfully")
    void createRole_EmptyPermissions_Success() {
        // Given
        CreateRoleRequest requestWithEmptyPermissions = CreateRoleRequest.builder()
                .name("GUEST")
                .permissions("")
                .build();

        Role roleWithEmptyPermissions = new Role();
        roleWithEmptyPermissions.setId(3L);
        roleWithEmptyPermissions.setName("GUEST");
        roleWithEmptyPermissions.setPermissions("");

        when(roleRepository.existsByName("GUEST")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(roleWithEmptyPermissions);

        // When
        RoleResponse result = roleService.createRole(requestWithEmptyPermissions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("GUEST");
        assertThat(result.getPermissions()).isEqualTo("");

        verify(roleRepository).existsByName("GUEST");
        verify(roleRepository).save(any(Role.class));
    }
}
