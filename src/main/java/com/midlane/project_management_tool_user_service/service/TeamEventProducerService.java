package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.TeamMemberAddedEvent;
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

    private static final String TEAM_MEMBER_ADDED_TOPIC = "team-member-added";
    private static final String DEFAULT_ROLE = "ADMIN";

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
