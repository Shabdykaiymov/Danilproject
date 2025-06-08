package com.example.backend.RouteMate.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Класс для представления ответа с информацией о пользователе.
 * Содержит данные пользователя, включая его имя, фамилию, email и идентификатор.
 */
public record UserResponse(

        /**
         * Уникальный идентификатор пользователя.
         */
        UUID id,

        /**
         * Логин пользователя.
         */
        String username,

        /**
         * Электронная почта пользователя.
         */
        String email,

        /**
         * Имя пользователя.
         */
        String firstname,

        /**
         * Фамилия пользователя.
         */
        String lastname
) {
}
