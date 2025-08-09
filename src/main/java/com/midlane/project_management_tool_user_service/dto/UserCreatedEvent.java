package com.midlane.project_management_tool_user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    @JsonProperty("user_id")
    private Long userId; // Changed field name to camelCase and added JsonProperty annotation

    private String email;

    @JsonProperty("event_type")
    private String eventType; // Changed field name to camelCase and added JsonProperty annotation
}
