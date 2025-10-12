package com.midlane.project_management_tool_user_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import com.midlane.project_management_tool_user_service.service.TeamEventProducerService;
import com.midlane.project_management_tool_user_service.service.KafkaHealthService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public TeamEventProducerService mockTeamEventProducerService() {
        return mock(TeamEventProducerService.class);
    }

    @Bean
    @Primary 
    public KafkaHealthService mockKafkaHealthService() {
        return mock(KafkaHealthService.class);
    }
}