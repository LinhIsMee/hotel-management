package com.spring3.hotel.management.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.allow-origin:http://localhost:5173}")
    private String allowOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = Arrays.stream(allowOrigin.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
        
        // Thêm protocol http:// cho domain nếu nó không chứa http:// hoặc https://
        allowedOrigins = allowedOrigins.stream()
            .map(origin -> {
                if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
                    return "http://" + origin;
                }
                return origin;
            })
            .collect(Collectors.toList());
        
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}