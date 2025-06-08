package com.example.backend.RouteMate.payload.response;

/**
 * Класс для представления ответа с JWT токеном.
 * Содержит сгенерированный токен для аутентифицированного пользователя.
 */
public record JwtResponse(
        /**
         * JWT токен для доступа.
         */
        String token
) {}
