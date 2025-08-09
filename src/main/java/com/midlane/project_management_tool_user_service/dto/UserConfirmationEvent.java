package com.midlane.project_management_tool_user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConfirmationEvent {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_service_id")
    private Long userServiceId;

    private String email;

    @JsonProperty("event_type")
    private String eventType;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
