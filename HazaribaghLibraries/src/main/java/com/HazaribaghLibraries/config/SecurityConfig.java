package com.HazaribaghLibraries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery)
                // We disable this because we are using Postman/React, not standard HTML forms
                .csrf(csrf -> csrf.disable())

                // 2. Define URL Access Rules
                .authorizeHttpRequests(auth -> auth
                        // Allow everyone to access ANY URL starting with /api/
                        .requestMatchers("/api/**").permitAll()
                        // Lock everything else (optional)
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}