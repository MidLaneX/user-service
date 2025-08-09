package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserConfirmationEvent;
import com.midlane.project_management_tool_user_service.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, UserConfirmationEvent> kafkaTemplate;

    public void publishUserConfirmation(User user) {
        try {
            UserConfirmationEvent event = new UserConfirmationEvent(
                user.getAuthServiceUserId(),
                user.getId(),
                user.getEmail(),
                "USER_CONFIRMED",
                user.getStatus().name(),
                LocalDateTime.now()
            );

            kafkaTemplate.send("user.confirmed", user.getEmail(), event);
            log.info("Published user confirmation event for email: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to publish user confirmation event for email: {}", user.getEmail(), e);
        }
    }
}
