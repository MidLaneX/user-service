package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserCreatedEvent;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = "user.added", containerFactory = "userEventListenerContainerFactory")
    @Transactional
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        try {
            // Validate event data
            if (event.getEmail() == null || event.getEmail().trim().isEmpty() || event.getUserId() == null) {
                log.warn("Invalid event received: missing email or userId");
                return;
            }

            // Validate event type
            if (!"USER_CREATED".equals(event.getEventType())) {
                return;
            }

            // Check if user already exists to prevent duplicates
            if (userRepository.existsByEmail(event.getEmail()) ||
                userRepository.existsByAuthServiceUserId(event.getUserId())) {
                log.info("User already exists, skipping creation for email: {}", event.getEmail());
                return;
            }

            // Create user with minimal information from auth service
            User user = new User();
            user.setAuthServiceUserId(event.getUserId());
            user.setEmail(event.getEmail());
            user.setStatus(User.UserStatus.PENDING);

            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());

            // Publish confirmation event
            kafkaProducerService.publishUserConfirmation(savedUser);

        } catch (Exception e) {
            log.error("Error processing user creation event: {}", e.getMessage());
        }
    }
}
