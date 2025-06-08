package com.example.backend.RouteMate.payload.response;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления ответа с информацией о маршруте.
 * Содержит данные маршрута, включая его название, описание, координаты, изображение и время создания/обновления.
 */
public record RouteResponse(
        /**
         * Уникальный идентификатор маршрута.
         */
        UUID id,

        /**
         * Название маршрута.
         */
        String name,

        /**
         * Описание маршрута.
         */
        String description,

        /**
         * Широта начальной точки маршрута.
         */
        double start_lat,

        /**
         * Долгота начальной точки маршрута.
         */
        double start_lng,

        /**
         * Широта конечной точки маршрута.
         */
        double end_lat,

        /**
         * Долгота конечной точки маршрута.
         */
        double end_lng,

        /**
         * Уникальный идентификатор пользователя, создавшего маршрут.
         */
        UUID userId,

        /**
         * Время создания маршрута.
         */
        Timestamp created_at,

        /**
         * Время последнего обновления маршрута.
         */
        Timestamp updated_at,

        /**
         * Местоположение начала маршрута.
         */
        String startLocation,

        /**
         * Местоположение конца маршрута.
         */
        String endLocation,

        /**
         * Изображение маршрута, представлено в виде массива байтов.
         */
        byte[] finishImage
) {
}
