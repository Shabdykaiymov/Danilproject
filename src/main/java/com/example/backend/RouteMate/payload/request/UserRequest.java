package com.example.backend.RouteMate.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Класс для представления данных запроса на регистрацию пользователя.
 * Содержит информацию о имени пользователя, пароле, email и других данных.
 */
public record UserRequest(

        /**
         * Логин пользователя для регистрации.
         */
        @JsonProperty("login")
        @NotBlank(message = "Username is required")
        String username,

        /**
         * Пароль пользователя для регистрации.
         * Пароль должен быть не менее 8 символов.
         */
        @JsonProperty("code")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        /**
         * Электронная почта пользователя.
         * Должна быть в правильном формате.
         */
        @JsonProperty("mail")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        /**
         * Имя пользователя.
         */
        @JsonProperty("name")
        @NotBlank(message = "FirstName is required")
        String firstname,

        /**
         * Фамилия пользователя.
         */
        @JsonProperty("surname")
        @NotBlank(message = "LastName is required")
        String lastname
) {
}
