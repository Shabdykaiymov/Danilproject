package com.example.backend.RouteMate.payload.response;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления ответа с информацией о точке маршрута.
 * Содержит уникальный идентификатор, название, описание, координаты и время создания точки.
 */
public record RoutePointResponse(
        /**
         * Уникальный идентификатор точки маршрута.
         */
        UUID id,

        /**
         * Уникальный идентификатор маршрута, к которому принадлежит точка.
         */
        UUID route_id,

        /**
         * Название точки маршрута.
         */
        String name,

        /**
         * Описание точки маршрута.
         */
        String description,

        /**
         * Широта точки маршрута.
         */
        double latitude,

        /**
         * Долгота точки маршрута.
         */
        double longitude,

        /**
         * Время создания точки маршрута.
         */
        Timestamp created_at
) {
}
