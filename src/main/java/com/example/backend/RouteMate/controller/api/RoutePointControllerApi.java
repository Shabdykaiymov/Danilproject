package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.RoutePointRequest;
import com.example.backend.RouteMate.payload.response.RoutePointResponse;
import com.example.backend.RouteMate.service.RoutePointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с точками маршрута.
 * <p>
 * Обрабатывает запросы на добавление точек в маршрут и получение списка точек маршрута.
 */
@RestController
@RequestMapping("/api")
public class RoutePointControllerApi {

    private final RoutePointService routePointService;

    /**
     * Конструктор контроллера для внедрения сервиса точек маршрута.
     *
     * @param routePointService сервис для работы с точками маршрута
     */
    public RoutePointControllerApi(RoutePointService routePointService) {
        this.routePointService = routePointService;
    }

    /**
     * Получает список точек маршрута по идентификатору маршрута.
     *
     * @param routeId идентификатор маршрута
     * @return список точек маршрута
     */
    @GetMapping("/point/{routeId}")
    public ResponseEntity<List<RoutePointResponse>> getListPointFromRoutePoint(@PathVariable UUID routeId) {
        List<RoutePointResponse> listPoint = routePointService.getListPoints(routeId);

        return new ResponseEntity<>(listPoint, HttpStatus.OK);
    }

    /**
     * Добавляет точку в маршрут.
     *
     * @param routePointRequest данные точки маршрута для добавления
     * @return сообщение о результате добавления точки в маршрут
     */
    @PostMapping("/set-point")
    public ResponseEntity<String> setPointInRoute(@RequestBody RoutePointRequest routePointRequest) {
        try {
            routePointService.addPointInRoutes(routePointRequest);
            return new ResponseEntity<>("Point was set!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Point was not set!", HttpStatus.BAD_REQUEST);
        }
    }
}
