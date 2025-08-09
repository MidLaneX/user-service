package com.midlane.project_management_tool_user_service.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String email,
    String firstName,
    String lastName,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
