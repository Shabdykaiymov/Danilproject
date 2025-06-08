package com.example.backend.RouteMate.payload.response;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления ответа на запрос комментария.
 * Содержит информацию о пользователе, комментарии и времени его создания.
 */
public record CommentResponse(
        UUID id,
        /**
         * Уникальный идентификатор пользователя, оставившего комментарий.
         */
        UUID userId,

        /**
         * Текст комментария.
         */
        String comment,

        /**
         * Время создания комментария.
         */
        Timestamp createAt
) {
}
