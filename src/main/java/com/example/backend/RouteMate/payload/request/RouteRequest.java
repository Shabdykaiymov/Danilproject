package com.example.backend.RouteMate.payload.request;

import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Класс для представления данных запроса на создание или обновление маршрута.
 * Содержит информацию о названии, описании, координатах, изображении и других параметрах маршрута.
 */
public record RouteRequest(
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
        double startLat,

        /**
         * Долгота начальной точки маршрута.
         */
        double startLng,

        /**
         * Широта конечной точки маршрута.
         */
        double endLat,

        /**
         * Долгота конечной точки маршрута.
         */
        double endLng,

        /**
         * Уникальный идентификатор пользователя, который создает маршрут.
         */
        UUID userId,

        /**
         * Время создания маршрута.
         */
        Timestamp createdAt,

        /**
         * Время последнего обновления маршрута.
         */
        Timestamp updateAt,

        /**
         * Местоположение начала маршрута.
         */
        String startLocation,

        /**
         * Местоположение конца маршрута.
         */
        String endLocation,

        /**
         * Изображение маршрута, загружаемое пользователем.
         */
        MultipartFile finishImage,

        /**
         * Список точек маршрута в виде строки.
         */
        String points
) {
}
