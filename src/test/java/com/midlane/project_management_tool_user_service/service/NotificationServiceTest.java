package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private NotificationService notificationService;

    private String testNotificationServiceUrl = "http://localhost:8084";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "notificationServiceUrl", testNotificationServiceUrl);
    }

    @Test
    @DisplayName("Should send welcome notification successfully with complete user info")
    void sendWelcomeNotification_WithCompleteUserInfo_Success() {
        // Given
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String role = "ADMIN";

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When
        notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);

        // Then
        verify(webClientBuilder).build();
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(testNotificationServiceUrl + "/api/v1/notifications/send");
        
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestBodySpec).bodyValue(notificationCaptor.capture());
        
        NotificationRequest capturedRequest = notificationCaptor.getValue();
        assertThat(capturedRequest.getRecipients()).containsExactly(userEmail);
        assertThat(capturedRequest.getSubject()).isEqualTo("Welcome to Project Management Tool");
        assertThat(capturedRequest.getTemplateName()).isEqualTo("welcome");
        assertThat(capturedRequest.getPriority()).isEqualTo("MEDIUM");
        
        Map<String, Object> templateData = capturedRequest.getTemplateData();
        assertThat(templateData.get("userName")).isEqualTo("John Doe");
        assertThat(templateData.get("userEmail")).isEqualTo(userEmail);
        assertThat(templateData.get("userRole")).isEqualTo(role);
        assertThat(templateData.get("dashboardUrl")).isEqualTo("https://app.example.com/dashboard");
        assertThat(templateData.get("createdDate")).isNotNull();
    }

    @Test
    @DisplayName("Should send welcome notification with only first name")
    void sendWelcomeNotification_WithOnlyFirstName_Success() {
        // Given
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = null;
        String role = "USER";

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When
        notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);

        // Then
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestBodySpec).bodyValue(notificationCaptor.capture());
        
        NotificationRequest capturedRequest = notificationCaptor.getValue();
        Map<String, Object> templateData = capturedRequest.getTemplateData();
        assertThat(templateData.get("userName")).isEqualTo("John");
    }

    @Test
    @DisplayName("Should send welcome notification with only last name")
    void sendWelcomeNotification_WithOnlyLastName_Success() {
        // Given
        String userEmail = "test@example.com";
        String firstName = null;
        String lastName = "Doe";
        String role = "USER";

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When
        notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);

        // Then
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestBodySpec).bodyValue(notificationCaptor.capture());
        
        NotificationRequest capturedRequest = notificationCaptor.getValue();
        Map<String, Object> templateData = capturedRequest.getTemplateData();
        assertThat(templateData.get("userName")).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should send welcome notification with default name when both names are null")
    void sendWelcomeNotification_WithNullNames_UsesDefaultName() {
        // Given
        String userEmail = "test@example.com";
        String firstName = null;
        String lastName = null;
        String role = "USER";

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When
        notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);

        // Then
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestBodySpec).bodyValue(notificationCaptor.capture());
        
        NotificationRequest capturedRequest = notificationCaptor.getValue();
        Map<String, Object> templateData = capturedRequest.getTemplateData();
        assertThat(templateData.get("userName")).isEqualTo("New User");
    }

    @Test
    @DisplayName("Should use default role when role is null")
    void sendWelcomeNotification_WithNullRole_UsesDefaultRole() {
        // Given
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String role = null;

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When
        notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);

        // Then
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestBodySpec).bodyValue(notificationCaptor.capture());
        
        NotificationRequest capturedRequest = notificationCaptor.getValue();
        Map<String, Object> templateData = capturedRequest.getTemplateData();
        assertThat(templateData.get("userRole")).isEqualTo("User");
    }

    @Test
    @DisplayName("Should handle WebClient error gracefully")
    void sendWelcomeNotification_WebClientError_HandledGracefully() {
        // Given
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String role = "USER";

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(NotificationRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection failed")));

        // When & Then - Should not throw exception
        assertThatCode(() -> {
            notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);
        }).doesNotThrowAnyException();

        verify(webClientBuilder).build();
        verify(webClient).post();
    }

    @Test
    @DisplayName("Should handle general exception gracefully")
    void sendWelcomeNotification_GeneralException_HandledGracefully() {
        // Given
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String role = "USER";

        when(webClientBuilder.build()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then - Should not throw exception
        assertThatCode(() -> {
            notificationService.sendWelcomeNotification(userEmail, firstName, lastName, role);
        }).doesNotThrowAnyException();

        verify(webClientBuilder).build();
    }
}
