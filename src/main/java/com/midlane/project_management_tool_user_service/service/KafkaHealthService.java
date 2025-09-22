package com.midlane.project_management_tool_user_service.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaHealthService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @EventListener(ApplicationReadyEvent.class)
    public void checkKafkaConnection() {
        log.info("🔄 Checking Kafka connection to: {}", bootstrapServers);

        CompletableFuture.runAsync(() -> {
            try {
                Properties props = new Properties();
                props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
                props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);

                try (AdminClient adminClient = AdminClient.create(props)) {
                    // Try to list topics to verify connection
                    adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
                    log.info("✅ Successfully connected to Kafka at: {}", bootstrapServers);
                    log.info("📡 Kafka producer is ready to send events");
                } catch (Exception e) {
                    log.error("❌ Failed to connect to Kafka at: {}. Error: {}", bootstrapServers, e.getMessage());
                    log.warn("⚠️  Team member events will not be published until Kafka is available");
                }
            } catch (Exception e) {
                log.error("❌ Error during Kafka health check: {}", e.getMessage());
            }
        });
    }
}
