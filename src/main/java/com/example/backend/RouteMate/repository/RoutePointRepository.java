package com.example.backend.RouteMate.repository;

import com.example.backend.RouteMate.payload.request.RoutePointRequest;
import com.example.backend.RouteMate.payload.response.RoutePointResponse;
import com.example.backend.RouteMate.service.RoutePointService;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с точками маршрута.
 * Содержит методы для добавления точек в маршруты и получения списка точек для маршрута.
 */
@Repository
public class RoutePointRepository implements RoutePointService {

    private final JdbcClient jdbcClient;

    /**
     * Конструктор для инициализации RoutePointRepository.
     *
     * @param jdbcClient JdbcClient для выполнения SQL-запросов.
     */
    public RoutePointRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Добавляет точку в маршрут.
     *
     * @param pointRequest Запрос с данными для добавления новой точки.
     */
    @Override
    public void addPointInRoutes(RoutePointRequest pointRequest) {
        String sqlQuery = """
                INSERT INTO route_points (id, route_id, name, description, latitude, longitude, created_at) VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        // Выполнение запроса на добавление точки в маршрут
        jdbcClient.sql(sqlQuery).params(
                UUID.randomUUID(),
                pointRequest.route_id(),
                pointRequest.name(),
                pointRequest.description(),
                pointRequest.latitude(),
                pointRequest.longitude(),
                pointRequest.created_at()
        ).update();
    }

    /**
     * Получает список точек маршрута.
     *
     * @param routeId Идентификатор маршрута.
     * @return Список точек маршрута.
     */
    @Override
    public List<RoutePointResponse> getListPoints(UUID routeId) {
        String sqlQuery = """
                SELECT * FROM route_points WHERE route_id = :routeId;
                """;

        // Выполнение запроса для получения списка точек маршрута
        return jdbcClient.sql(sqlQuery).param("routeId", routeId).query(RoutePointResponse.class).list();
    }
}
