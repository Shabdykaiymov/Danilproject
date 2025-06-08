package com.example.backend.RouteMate.config;

import com.example.backend.RouteMate.config.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * WebSecurityConfig — основная конфигурация безопасности Spring Security.
 * <p>
 * Настраивает доступ к маршрутам, добавляет JWT-фильтр, шифрование паролей и CORS.
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Lazy
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Конструктор с внедрением сервисов безопасности.
     */
    public WebSecurityConfig(@Lazy CustomUserDetailsService customUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Конфигурация CORS — разрешает запросы с указанных доменов (например, с фронта на localhost:3000).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8082"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Менеджер аутентификации, использующий {@link CustomUserDetailsService} и BCrypt.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }

    /**
     * Основная конфигурация безопасности:
     * - Отключает CSRF
     * - Включает CORS
     * - Настраивает публичные маршруты
     * - Все остальные требуют аутентификацию
     * - Добавляет фильтр JWT до стандартного логина
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http.csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests((requests) -> requests
                            .requestMatchers("/api/**").permitAll()
                            .requestMatchers("/api/route-search").permitAll()
                            .requestMatchers("/api/route/{routeId}/image").permitAll()
                            .requestMatchers("/api/{username}").permitAll()
                            .requestMatchers("/api/create-comment").permitAll()
                            .requestMatchers("/api/get-comment/{routeId}").permitAll()
                            .requestMatchers("/api/routes-by-ids").permitAll()
                            .requestMatchers("/api/list").permitAll()
                            .requestMatchers("/api/delete-route/**").permitAll()
                            .requestMatchers("/api/put/**").permitAll()
                            .requestMatchers("/api/del/**").permitAll()
                            .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Кодировщик паролей BCrypt — безопасное хеширование паролей.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
