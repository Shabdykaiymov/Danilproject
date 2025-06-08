package com.example.backend.RouteMate.service;

import com.example.backend.RouteMate.payload.request.RoutePointRequest;
import com.example.backend.RouteMate.payload.response.RoutePointResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RoutePointService {

    /**
     * Добавляет точку в маршрут.
     *
     * @param pointRequest Запрос с данными точки.
     */
    void addPointInRoutes(RoutePointRequest pointRequest);

    /**
     * Получает список точек для заданного маршрута.
     *
     * @param routeId Идентификатор маршрута.
     * @return Список точек маршрута.
     */
    List<RoutePointResponse> getListPoints(UUID routeId);
}
