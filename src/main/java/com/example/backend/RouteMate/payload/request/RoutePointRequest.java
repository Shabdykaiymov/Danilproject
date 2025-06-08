package com.example.backend.RouteMate.payload.request;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления данных запроса на создание точки маршрута.
 * Содержит информацию о маршруте, названии точки, ее описании, координатах и времени создания.
 */
public record RoutePointRequest(
        /**
         * Уникальный идентификатор маршрута, к которому добавляется точка.
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
