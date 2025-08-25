package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import com.midlane.project_management_tool_user_service.model.User;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
