package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateProjectRequest;
import com.midlane.project_management_tool_user_service.dto.ProjectResponse;
import com.midlane.project_management_tool_user_service.model.Project;
import com.midlane.project_management_tool_user_service.repository.ProjectRepository;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;

    public ProjectResponse createProject(Long organizationId, CreateProjectRequest request) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new RuntimeException("Organization not found");
        }

        if (projectRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new RuntimeException("Project with this name already exists in this organization");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOrganizationId(organizationId);

        Project savedProject = projectRepository.save(project);
        return mapToProjectResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByOrganization(Long organizationId) {
        return projectRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToProjectResponse(project);
    }

    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getName().equals(request.getName()) &&
            projectRepository.existsByNameAndOrganizationId(request.getName(), project.getOrganizationId())) {
            throw new RuntimeException("Project with this name already exists in this organization");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);
        return mapToProjectResponse(savedProject);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found");
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setOrganizationId(project.getOrganizationId());
        response.setCreatedAt(project.getCreatedAt());
        return response;
    }
}
