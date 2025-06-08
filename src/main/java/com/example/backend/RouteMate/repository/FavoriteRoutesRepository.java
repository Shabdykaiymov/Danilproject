package com.example.backend.RouteMate.repository;

import com.example.backend.RouteMate.payload.request.FavoriteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import com.example.backend.RouteMate.service.FavoriteService;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с избранными маршрутами.
 * Содержит методы для добавления и удаления маршрутов из избранного и получения их списка.
 */
@Repository
public class FavoriteRoutesRepository implements FavoriteService {

    private final JdbcClient jdbcClient;

    /**
     * Конструктор для инициализации FavoriteRoutesRepository.
     *
     * @param jdbcClient JdbcClient для выполнения SQL-запросов.
     */
    public FavoriteRoutesRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Добавляет маршрут в список избранных.
     *
     * @param favoriteRequest Запрос с данными для добавления маршрута в избранное.
     */
    @Override
    public void saveFavoriteRoute(FavoriteRequest favoriteRequest) {
        String sql = """
                INSERT INTO favorite_routes(id, route_id, user_id, added_at) VALUES (?, ?, ?, ?);
                """;
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

        // Выполнение запроса на добавление маршрута в избранное
        jdbcClient.sql(sql).params(
                UUID.randomUUID(),
                favoriteRequest.routeId(),
                favoriteRequest.userId(),
                createdAt
        ).update();
    }

    /**
     * Удаляет маршрут из списка избранных.
     *
     * @param routeId Идентификатор маршрута, который нужно удалить.
     * @param userId Идентификатор пользователя, который удаляет маршрут.
     */
    @Override
    public void deleteFavoriteRoute(UUID routeId, UUID userId) {
        String sql = """
                    DELETE FROM favorite_routes WHERE route_id = ? AND user_id = ?;
                """;
        // Выполнение запроса на удаление маршрута из избранного
        jdbcClient.sql(sql).params(routeId, userId).update();
    }

    /**
     * Получает список маршрутов, добавленных в избранное пользователем.
     *
     * @param userId Идентификатор пользователя.
     * @return Список идентификаторов маршрутов.
     */
    @Override
    public List<UUID> getFavoriteRoutes(UUID userId) {
        String sql = """
                    SELECT route_id FROM favorite_routes WHERE user_id = ?
                """;

        // Выполнение запроса для получения списка избранных маршрутов
        return jdbcClient.sql(sql)
                .params(userId)
                .query(UUID.class)
                .list();
    }

    /**
     * Получает подробную информацию о маршрутах, добавленных в избранное.
     *
     * @param routeIds Список идентификаторов маршрутов.
     * @return Список объектов RouteResponse, содержащих подробную информацию о маршрутах.
     */
    @Override
    public List<RouteResponse> getFavoriteRouteDetails(List<UUID> routeIds) {
        String sql = """
            SELECT * FROM routes WHERE id IN (:ids)
        """;

        // Выполнение запроса для получения информации о маршрутах
        return jdbcClient.sql(sql)
                .param("ids", routeIds)
                .query(RouteResponse.class)
                .list();
    }
}
