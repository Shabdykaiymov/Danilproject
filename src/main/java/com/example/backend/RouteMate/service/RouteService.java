package com.example.backend.RouteMate.service;

import com.example.backend.RouteMate.payload.request.PutRequest;
import com.example.backend.RouteMate.payload.request.RouteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RouteService {

    /**
     * Сохраняет новый маршрут.
     *
     * @param routeRequest Запрос с данными маршрута.
     */
    void saveRoute(RouteRequest routeRequest);

    /**
     * Находит маршруты по идентификатору пользователя.
     *
     * @param id Идентификатор пользователя.
     * @return Список маршрутов пользователя.
     */
    List<RouteResponse> findRoutByUserId(UUID id);

    /**
     * Находит маршруты по начальной и конечной локации.
     *
     * @param startLocation Начальная локация.
     * @param endLocation Конечная локация.
     * @return Список маршрутов с указанными локациями.
     */
    List<RouteResponse> findRouteByStartAndEndLocation(String startLocation, String endLocation);

    /**
     * Находит маршрут по идентификатору маршрута.
     *
     * @param routeId Идентификатор маршрута.
     * @return Маршрут с указанным идентификатором.
     */
    RouteResponse findRoutByRouteId(UUID routeId);

    /**
     * Получает все маршруты.
     *
     * @return Список всех маршрутов.
     */
    List<RouteResponse> getAllRouts();

    /**
     * Получает изображение маршрута по его идентификатору.
     *
     * @param routeId Идентификатор маршрута.
     * @return Массив байтов с изображением маршрута.
     */
    byte[] getImageByRouteId(UUID routeId);

    /**
     * Получает тип медиа для изображения маршрута.
     *
     * @param imageBytes Массив байтов изображения маршрута.
     * @return Тип медиа (например, image/jpeg).
     */
    MediaType getImageMediaType(byte[] imageBytes);

    /**
     * Обновляет маршрут по его идентификатору.
     *
     * @param idRoute Идентификатор маршрута.
     * @param routeRequest Запрос с обновленными данными маршрута.
     */
    void updateRouteById(UUID idRoute, PutRequest routeRequest);

    boolean deleteMapping(UUID routeId);
}
