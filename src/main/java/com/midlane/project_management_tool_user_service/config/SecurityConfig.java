package com.midlane.project_management_tool_user_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF for testing/development
                .authorizeHttpRequests()
                .requestMatchers("/api/users/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow user & Swagger
                .anyRequest().permitAll() // Allow other requests too
                .and()
                .httpBasic().disable() // Disable basic auth
                .formLogin().disable(); // Disable form login

        return http.build();
    }
}
