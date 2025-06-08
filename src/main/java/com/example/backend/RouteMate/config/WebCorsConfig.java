package com.example.backend.RouteMate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebCorsConfig — конфигурация CORS (Cross-Origin Resource Sharing).
 * <p>
 * Позволяет фронтенду (например, на Vue/React) делать запросы к backend,
 * даже если они находятся на разных доменах (localhost:3000 ↔ localhost:8080).
 */
@Configuration
public class WebCorsConfig {

    /**
     * Разрешённые источники (origin), с которых можно делать запросы к серверу.
     * Значения берутся из application.properties:
     * app.cors.allowed-origins=http://localhost:3000, ...
     */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Создаёт и настраивает политику CORS для всех HTTP-запросов.
     *
     * @return WebMvcConfigurer с заданной CORS-конфигурацией
     */
    @Bean
    public WebMvcConfigurer corsConfigure() {
        return new WebMvcConfigurer() {
            /**
             * Настройка правил CORS:
             * - Разрешённые источники (allowedOrigins)
             * - Разрешённые методы: GET, POST, PUT, DELETE, OPTIONS
             * - Разрешены любые заголовки
             * - Разрешены кук/заголовки авторизации (credentials)
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
