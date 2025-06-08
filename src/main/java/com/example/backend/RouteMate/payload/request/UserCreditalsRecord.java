package com.example.backend.RouteMate.payload.request;

/**
 * Класс для представления данных пользователя при аутентификации.
 * Содержит информацию об имени пользователя и пароле.
 */
public record UserCreditalsRecord(
        /**
         * Имя пользователя для аутентификации.
         */
        String username,

        /**
         * Пароль пользователя для аутентификации.
         */
        String password
) {
}
