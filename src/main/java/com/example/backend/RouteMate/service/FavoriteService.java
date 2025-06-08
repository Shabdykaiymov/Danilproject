package com.example.backend.RouteMate.service;

import com.example.backend.RouteMate.payload.request.FavoriteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface FavoriteService {

    /**
     * Добавляет маршрут в избранное.
     *
     * @param favoriteRequest Запрос с данными маршрута.
     */
    void saveFavoriteRoute(FavoriteRequest favoriteRequest);

    /**
     * Удаляет маршрут из избранного.
     *
     * @param routeId Идентификатор маршрута.
     * @param userId Идентификатор пользователя.
     */
    void deleteFavoriteRoute(UUID routeId, UUID userId);

    /**
     * Получает список избранных маршрутов для пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список идентификаторов маршрутов.
     */
    List<UUID> getFavoriteRoutes(UUID userId);

    /**
     * Получает подробную информацию о избранных маршрутах.
     *
     * @param routeIds Список идентификаторов маршрутов.
     * @return Список маршрутов с подробной информацией.
     */
    List<RouteResponse> getFavoriteRouteDetails(List<UUID> routeIds);
}
