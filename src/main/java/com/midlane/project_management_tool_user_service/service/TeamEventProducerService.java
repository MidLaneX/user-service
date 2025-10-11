package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamEventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topic constants
    private static final String TEAM_MEMBER_ADDED_TOPIC = "team-member-added";
    private static final String USER_REGISTERED_TOPIC = "user-registered";
    private static final String TEAM_CREATED_TOPIC = "team-created";
    private static final String MEMBER_ADDED_TO_TEAM_TOPIC = "member-added-to-team";
    private static final String TEAM_MEMBER_REMOVED_TOPIC = "team-member-removed";
    private static final String DEFAULT_ROLE = "ADMIN";

    // 1. User Registration Event
    public void publishUserRegisteredEvent(Long userId, String email, String name, String profilePictureUrl) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(userId)
                    .email(email)
                    .name(name)
                    .profilePictureUrl(profilePictureUrl)
                    .timestamp(LocalDateTime.now())
                    .eventType("USER_REGISTERED")
                    .build();

            String key = "user-" + userId;

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(USER_REGISTERED_TOPIC, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish user registered event for userId: {}, error: {}",
                            userId, throwable.getMessage());
                } else {
                    log.info("Successfully published user registered event: userId={}, email={}, partition={}, offset={}",
                            userId, email,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error occurred while publishing user registered event for userId: {}", userId, e);
            throw new RuntimeException("Failed to publish user registered event", e);
        }
    }

    // 2. Team Creation Event
    public void publishTeamCreatedEvent(Long teamId, String teamName, String description, Long ownerId) {
        try {
            TeamCreatedEvent event = TeamCreatedEvent.builder()
                    .teamId(teamId)
                    .teamName(teamName)
                    .description(description)
                    .ownerId(ownerId)
                    .timestamp(LocalDateTime.now())
                    .eventType("TEAM_CREATED")
                    .build();

            String key = "team-" + teamId;

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TEAM_CREATED_TOPIC, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish team created event for teamId: {}, error: {}",
                            teamId, throwable.getMessage());
                } else {
                    log.info("Successfully published team created event: teamId={}, teamName={}, ownerId={}, partition={}, offset={}",
                            teamId, teamName, ownerId,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error occurred while publishing team created event for teamId: {}", teamId, e);
            throw new RuntimeException("Failed to publish team created event", e);
        }
    }

    // 3. Member Added to Team Event
    public void publishMemberAddedToTeamEvent(Long teamId, Long memberId, String role) {
        try {
            MemberAddedToTeamEvent event = MemberAddedToTeamEvent.builder()
                    .teamId(teamId)
                    .memberId(memberId)
                    .role(role)
                    .timestamp(LocalDateTime.now())
                    .eventType("MEMBER_ADDED_TO_TEAM")
                    .build();

            String key = "team-" + teamId;

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(MEMBER_ADDED_TO_TEAM_TOPIC, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish member added to team event for teamId: {}, memberId: {}, error: {}",
                            teamId, memberId, throwable.getMessage());
                } else {
                    log.info("Successfully published member added to team event: teamId={}, memberId={}, role={}, partition={}, offset={}",
                            teamId, memberId, role,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error occurred while publishing member added to team event for teamId: {}, memberId: {}",
                     teamId, memberId, e);
            throw new RuntimeException("Failed to publish member added to team event", e);
        }
    }

    // 4. Team Member Removed Event
    public void publishTeamMemberRemovedEvent(Long teamId, Long memberId) {
        try {
            TeamMemberRemovedEvent event = TeamMemberRemovedEvent.builder()
                    .teamId(teamId)
                    .memberId(memberId)
                    .timestamp(LocalDateTime.now())
                    .eventType("TEAM_MEMBER_REMOVED")
                    .build();

            String key = "team-" + teamId;

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TEAM_MEMBER_REMOVED_TOPIC, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish team member removed event for teamId: {}, memberId: {}, error: {}",
                            teamId, memberId, throwable.getMessage());
                } else {
                    log.info("Successfully published team member removed event: teamId={}, memberId={}, partition={}, offset={}",
                            teamId, memberId,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error occurred while publishing team member removed event for teamId: {}, memberId: {}",
                     teamId, memberId, e);
            throw new RuntimeException("Failed to publish team member removed event", e);
        }
    }

    // 5. Team Member Added Event
    public void publishTeamMemberAddedEvent(Long userId, Long organizationId, Long teamId,
                                          String teamName, String organizationName,
                                          String userEmail, String userName) {
        try {
            TeamMemberAddedEvent event = TeamMemberAddedEvent.builder()
                    .userId(userId)
                    .organizationId(organizationId)
                    .teamId(teamId)
                    .role(DEFAULT_ROLE)
                    .timestamp(LocalDateTime.now())
                    .eventType("TEAM_MEMBER_ADDED")
                    .teamName(teamName)
                    .organizationName(organizationName)
                    .userEmail(userEmail)
                    .userName(userName)
                    .build();

            // Create a key for partitioning - using teamId for better distribution
            String key = "team-" + teamId;

            CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TEAM_MEMBER_ADDED_TOPIC, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish team member added event for userId: {}, teamId: {}, error: {}",
                             userId, teamId, throwable.getMessage());
                } else {
                    log.info("Successfully published team member added event: userId={}, teamId={}, organizationId={}, partition={}, offset={}",
                            userId, teamId, organizationId,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error occurred while publishing team member added event for userId: {}, teamId: {}",
                     userId, teamId, e);
            throw new RuntimeException("Failed to publish team member added event", e);
        }
    }
}
