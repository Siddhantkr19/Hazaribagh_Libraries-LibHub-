package com.HazaribaghLibraries.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server; // ✅ Import the Server class
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class Swagger {

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. Define your Server Environments
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://libhub-backend.onrender.com") // ✅ Updated to your actual Render URL
                .description("Production Server (Live)");

        return new OpenAPI()
                // 2. Add the Servers to Swagger UI
                .servers(List.of(localServer, productionServer))

                // 3. Project Information
                .info(new Info()
                        .title("LibHub API Documentation")
                        .version("2.0")
                        .description("Backend API for the Hazaribagh Library Booking System"))

                // 4. Security Configuration for JWT
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            // Define your EXACT desired order here
            List<String> desiredOrder = List.of(
                    "Authentication",
                    "Libraries",
                    "Bookings",
                    "Reviews",
                    "Help Desk",
                    "Admin Control",
                    "Utility"
            );

            // Reorder the tags found by the scanner
            if (openApi.getTags() != null) {
                List<Tag> sortedTags = openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            int index = desiredOrder.indexOf(tag.getName());
                            // If a tag is not in the list, put it at the end (999)
                            return index == -1 ? 999 : index;
                        }))
                        .collect(Collectors.toList());
                openApi.setTags(sortedTags);
            }
        };
    }
}