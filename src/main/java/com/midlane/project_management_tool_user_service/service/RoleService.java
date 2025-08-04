package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateRoleRequest;
import com.midlane.project_management_tool_user_service.dto.RoleResponse;
import com.midlane.project_management_tool_user_service.model.Role;
import com.midlane.project_management_tool_user_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with this name already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setPermissions(request.getPermissions());

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return mapToRoleResponse(role);
    }

    public RoleResponse updateRole(Long id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!role.getName().equals(request.getName()) &&
            roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with this name already exists");
        }

        role.setName(request.getName());
        role.setPermissions(request.getPermissions());

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found");
        }
        roleRepository.deleteById(id);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setPermissions(role.getPermissions());
        return response;
    }
}
