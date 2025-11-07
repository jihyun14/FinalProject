package com.prinCipal.chatbot.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 👈 /api/ 로 시작하는 모든 요청
            .allowedOrigins("http://localhost:3000") // 👈 프론트엔드 주소 (VITE)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 👈 PUT, PATCH 허용
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}