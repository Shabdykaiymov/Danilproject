package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.FavoriteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import com.example.backend.RouteMate.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с маршрутами, добавленными в избранное.
 * <p>
 * Обрабатывает запросы на добавление, удаление и получение избранных маршрутов.
 */
@RestController
@RequestMapping("/api")
public class FavoriteControllerApi {

    private final FavoriteService favoriteService;

    /**
     * Конструктор контроллера для внедрения сервиса избранных маршрутов.
     *
     * @param favoriteService сервис для работы с избранными маршрутами
     */
    public FavoriteControllerApi(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * Добавляет маршрут в избранное для пользователя.
     *
     * @param favoriteRequest данные о маршруте и пользователе
     * @return сообщение о результате добавления маршрута в избранное
     */
    @PostMapping("/add")
    public ResponseEntity<String> addFavorite(@RequestBody FavoriteRequest favoriteRequest) {
        favoriteService.saveFavoriteRoute(favoriteRequest);
        return new ResponseEntity<>("Favorite route was saved!", HttpStatus.CREATED);
    }

    /**
     * Удаляет маршрут из избранного для пользователя.
     *
     * @param routeId идентификатор маршрута
     * @param userId  идентификатор пользователя
     * @return сообщение о результате удаления маршрута из избранного
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFavorite(@RequestParam UUID routeId, @RequestParam UUID userId) {
        favoriteService.deleteFavoriteRoute(routeId, userId);
        return new ResponseEntity<>("Favorite route was deleted!", HttpStatus.OK);
    }

    /**
     * Получает список идентификаторов маршрутов, добавленных в избранное пользователем.
     *
     * @param userId идентификатор пользователя
     * @return список идентификаторов маршрутов, добавленных в избранное
     */
    @GetMapping("/list")
    public ResponseEntity<List<UUID>> getFavoriteRoutes(@RequestParam UUID userId) {
        List<UUID> favoriteRouteIds = favoriteService.getFavoriteRoutes(userId);
        return new ResponseEntity<>(favoriteRouteIds, HttpStatus.OK);
    }

    /**
     * Получает подробную информацию о маршрутах, добавленных в избранное, по их идентификаторам.
     *
     * @param routeIds список идентификаторов маршрутов
     * @return список маршрутов с их деталями
     */
    @GetMapping("/routes-by-ids")
    public ResponseEntity<List<RouteResponse>> getFavoriteRoutesByIds(@RequestParam List<UUID> routeIds) {
        List<RouteResponse> routes = favoriteService.getFavoriteRouteDetails(routeIds);
        return new ResponseEntity<>(routes, HttpStatus.OK);
    }
}
