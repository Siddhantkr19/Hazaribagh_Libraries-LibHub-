package com.HazaribaghLibraries.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Tells Spring: "If a URL starts with /uploads/, look in the uploads folder on disk"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}