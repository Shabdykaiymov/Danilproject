package com.example.backend.RouteMate.payload.request;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления данных запроса на создание комментария.
 * Содержит информацию о маршруте, пользователе, тексте комментария и времени создания.
 */
public record CommentRequest(
        /**
         * Уникальный идентификатор маршрута, к которому добавляется комментарий.
         */
        UUID routeId,

        /**
         * Уникальный идентификатор пользователя, который оставляет комментарий.
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
