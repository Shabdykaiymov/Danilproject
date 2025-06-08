package com.example.backend.RouteMate.repository;

import com.example.backend.RouteMate.payload.request.PutRequest;
import com.example.backend.RouteMate.payload.request.RoutePointRequest;
import com.example.backend.RouteMate.payload.request.RouteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import com.example.backend.RouteMate.service.RoutePointService;
import com.example.backend.RouteMate.service.RouteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с маршрутами.
 * Содержит методы для создания, обновления, получения маршрутов и работы с изображениями.
 */
@Repository
public class RouteRepository implements RouteService {

    private final JdbcClient jdbcClient;
    private final RoutePointService routePointService;

    /**
     * Конструктор для инициализации RouteRepository.
     *
     * @param jdbcClient        JdbcClient для выполнения SQL-запросов.
     * @param routePointService Сервис для работы с точками маршрута.
     */
    public RouteRepository(JdbcClient jdbcClient, RoutePointService routePointService) {
        this.jdbcClient = jdbcClient;
        this.routePointService = routePointService;
    }

    /**
     * Сохраняет новый маршрут в базе данных.
     *
     * @param routeRequest Запрос с данными для создания нового маршрута.
     */
    @Override
    public void saveRoute(RouteRequest routeRequest) {
        String sqlQuery = """
                INSERT INTO routes (id, name, description, start_lat, start_lng, end_lat, end_lng, user_id, created_at, updated_at, start_location, end_location, finish_image)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;
        UUID uuidRoute = java.util.UUID.randomUUID();
        Timestamp createdAt = routeRequest.createdAt() != null ? routeRequest.createdAt() : Timestamp.valueOf(LocalDateTime.now());

        try {
            // Сохраняем маршрут
            jdbcClient.sql(sqlQuery).params(
                    uuidRoute,
                    routeRequest.name(),
                    routeRequest.description(),
                    routeRequest.startLat(),
                    routeRequest.startLng(),
                    routeRequest.endLat(),
                    routeRequest.endLng(),
                    routeRequest.userId(),
                    createdAt,
                    routeRequest.updateAt(),
                    routeRequest.startLocation(),
                    routeRequest.endLocation(),
                    routeRequest.finishImage().getBytes()
            ).update();

            // Разбираем и сохраняем точки маршрута
            ObjectMapper objectMapper = new ObjectMapper();
            List<RoutePointRequest> parsedPoints = objectMapper.readValue(
                    routeRequest.points(),
                    new TypeReference<List<RoutePointRequest>>() {
                    }
            );
            for (RoutePointRequest point : parsedPoints) {
                point = new RoutePointRequest(
                        uuidRoute,
                        point.name(),
                        point.description(),
                        point.longitude(),
                        point.latitude(),
                        point.created_at()
                );
                routePointService.addPointInRoutes(point);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Находит все маршруты для пользователя по его идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Список маршрутов.
     */
    @Override
    public List<RouteResponse> findRoutByUserId(UUID id) {
        String sqlQuery = """
                SELECT * FROM routes WHERE user_id = :userId;
                """;
        return jdbcClient.sql(sqlQuery).param("userId", id).query(RouteResponse.class).list();
    }

    /**
     * Получает все маршруты.
     *
     * @return Список всех маршрутов.
     */
    @Override
    public List<RouteResponse> getAllRouts() {
        String sqlQuery = """
                SELECT * FROM routes;
                """;
        return jdbcClient.sql(sqlQuery).query(RouteResponse.class).list();
    }

    /**
     * Получает изображение маршрута по его идентификатору.
     *
     * @param routeId Идентификатор маршрута.
     * @return Изображение маршрута в виде массива байтов.
     */
    @Override
    public byte[] getImageByRouteId(UUID routeId) {
        String querySql = """
                SELECT finish_image FROM routes WHERE id = :routeId;
                """;
        return jdbcClient.sql(querySql)
                .param("routeId", routeId)
                .query(byte[].class)
                .optional().orElse(null);
    }

    /**
     * Определяет тип изображения по первым байтам.
     *
     * @param imageBytes Массив байтов изображения.
     * @return Тип изображения.
     */
    @Override
    public MediaType getImageMediaType(byte[] imageBytes) {
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
            return MediaType.IMAGE_JPEG;
        } else if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50) {
            return MediaType.IMAGE_PNG;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Обновляет маршрут по его идентификатору.
     *
     * @param idRoute      Идентификатор маршрута.
     * @param routeRequest Запрос с новыми данными для маршрута.
     */
    @Override
    public void updateRouteById(UUID idRoute, PutRequest routeRequest) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE routes SET ");
        List<Object> params = new ArrayList<>();

        if (routeRequest.description() != null) {
            queryBuilder.append("description = ?, ");
            params.add(routeRequest.description());
        }
        if (routeRequest.startLocation() != null) {
            queryBuilder.append("start_location = ?, ");
            params.add(routeRequest.startLocation());
        }
        if (routeRequest.endLocation() != null) {
            queryBuilder.append("end_location = ?, ");
            params.add(routeRequest.endLocation());
        }

        if (!params.isEmpty()) {
            queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());
        }

        queryBuilder.append(" WHERE id = ?");
        params.add(idRoute);

        int result = jdbcClient.sql(queryBuilder.toString())
                .params(params)
                .update();

        if (result == 0) {
            throw new RuntimeException("Route not found with id: " + idRoute);
        }
    }


    @Override
    public boolean deleteMapping(UUID routeId) {
        String sqlQueryDropComment = """
                DELETE FROM comments WHERE route_id = :routeId;
                """;

        jdbcClient.sql(sqlQueryDropComment).param("routeId", routeId).update();

        String sqlQueryDropRoutePoints = """
                DELETE FROM route_points WHERE route_id = :routeId;
                """;

        jdbcClient.sql(sqlQueryDropRoutePoints).param("routeId", routeId).update();

        String sqlQueryDropFavoriteRoutes = """
                DELETE FROM favorite_routes WHERE route_id = :routeId;
                """;

        jdbcClient.sql(sqlQueryDropFavoriteRoutes).param("routeId", routeId).update();

        String sqlQueryDropRoute = """
                DELETE FROM routes WHERE id = :routeId;
                """;

        int affectedRows = jdbcClient.sql(sqlQueryDropRoute)
                .param("routeId", routeId)
                .update();

        return affectedRows > 0;
    }

    /**
     * Находит маршрут по его идентификатору.
     *
     * @param routeId Идентификатор маршрута.
     * @return Объект маршрута.
     */
    @Override
    public RouteResponse findRoutByRouteId(UUID routeId) {
        String sqlQuery = """
                SELECT * FROM routes WHERE id = :routeId;
                """;
        return jdbcClient.sql(sqlQuery).param("routeId", routeId).query(RouteResponse.class).single();
    }

    /**
     * Находит маршруты по стартовому и конечному местоположению.
     *
     * @param startLocation Стартовое местоположение.
     * @param endLocation   Конечное местоположение.
     * @return Список маршрутов.
     */
    @Override
    public List<RouteResponse> findRouteByStartAndEndLocation(String startLocation, String endLocation) {
        String sqlQuery = """
                SELECT * FROM routes WHERE start_location = ? AND end_location = ?;
                """;

        return jdbcClient.sql(sqlQuery)
                .param(startLocation)
                .param(endLocation)
                .query(RouteResponse.class)
                .list();
    }
}
