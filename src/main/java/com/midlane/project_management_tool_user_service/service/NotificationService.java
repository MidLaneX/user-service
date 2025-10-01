package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${notification.service.url:http://localhost:8084}")
    private String notificationServiceUrl;

    public void sendWelcomeNotification(String userEmail, String firstName, String lastName, String role) {
        try {
            String fullName = buildFullName(firstName, lastName);
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .recipients(List.of(userEmail))
                    .subject("Welcome to Project Management Tool")
                    .templateName("welcome")
                    .templateData(Map.of(
                            "userName", fullName,
                            "userEmail", userEmail,
                            "userRole", role != null ? role : "User",
                            "createdDate", formattedDate,
                            "dashboardUrl", "https://app.example.com/dashboard"
                    ))
                    .priority("MEDIUM")
                    .build();

            WebClient webClient = webClientBuilder.build();

            webClient.post()
                    .uri(notificationServiceUrl + "/api/v1/notifications/send")
                    .bodyValue(notificationRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .subscribe(
                            response -> log.info("Welcome notification sent successfully for user: {}", userEmail),
                            error -> log.error("Failed to send welcome notification for user: {}", userEmail, error)
                    );

        } catch (Exception e) {
            log.error("Error sending welcome notification for user: {}", userEmail, e);
        }
    }

    private String buildFullName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return "New User";
        }
    }
}
