package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.PutRequest;
import com.example.backend.RouteMate.payload.request.RouteRequest;
import com.example.backend.RouteMate.payload.response.RouteResponse;
import com.example.backend.RouteMate.service.RouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с маршрутами.
 * <p>
 * Обрабатывает запросы на создание, получение, обновление и поиск маршрутов.
 */
@RestController
@RequestMapping("/api")
public class RouteControllerApi {

    private static final Logger log = LogManager.getLogger(RouteControllerApi.class);
    private final RouteService routeService;

    /**
     * Конструктор контроллера для внедрения сервиса маршрутов.
     *
     * @param routeService сервис для работы с маршрутами
     */
    public RouteControllerApi(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Получает все маршруты пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return список маршрутов, принадлежащих пользователю
     */
    @GetMapping("/route/{userId}")
    public ResponseEntity<List<RouteResponse>> getAllRoutesByUserId(@PathVariable UUID userId) {
        try {
            List<RouteResponse> routeResponses = routeService.findRoutByUserId(userId);
            return new ResponseEntity<>(routeResponses, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            log.error("Error message: ", nullPointerException);
            return null;
        }
    }

    /**
     * Получает подробную информацию о маршруте по его идентификатору.
     *
     * @param routeId идентификатор маршрута
     * @return данные маршрута по указанному идентификатору
     */
    @GetMapping("/route/details/{routeId}")
    public ResponseEntity<RouteResponse> getAllRoutesByRouteId(@PathVariable UUID routeId) {
        try {
            RouteResponse routeResponses = routeService.findRoutByRouteId(routeId);
            return new ResponseEntity<>(routeResponses, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            log.error("Error message: ", nullPointerException);
            return null;
        }
    }

    /**
     * Получает список всех маршрутов.
     *
     * @return список всех маршрутов
     */
    @GetMapping("/all-route")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        try {
            List<RouteResponse> routeResponses = routeService.getAllRouts();
            return new ResponseEntity<>(routeResponses, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            log.error("Error message: ", nullPointerException);
            return null;
        }
    }

    /**
     * Выполняет поиск маршрутов по начальной и конечной локации.
     *
     * @param startLocation начальная локация маршрута
     * @param endLocation   конечная локация маршрута
     * @return список маршрутов, которые начинаются и заканчиваются в указанных локациях
     */
    @GetMapping("/route/search")
    public ResponseEntity<List<RouteResponse>> getRouteByStartAndEnd(@RequestParam String startLocation, @RequestParam String endLocation) {
        try {
            List<RouteResponse> response = routeService.findRouteByStartAndEndLocation(startLocation, endLocation);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception exception) {
            log.error("Error message: ", exception);
            return null;
        }
    }

    /**
     * Сохраняет новый маршрут.
     *
     * @param routeRequest данные о маршруте, которые нужно сохранить
     * @return сообщение о результате сохранения маршрута
     */
    @PostMapping("/save-route")
    public ResponseEntity<String> saveRoute(@ModelAttribute RouteRequest routeRequest) {
        try {
            routeService.saveRoute(routeRequest);
            return new ResponseEntity<>("Route was Saved! ", HttpStatus.CREATED);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-route/{routeId}")
    public ResponseEntity<String> deleteRouteMapping(@PathVariable UUID routeId) {
        try {
            routeService.deleteMapping(routeId);
            return new ResponseEntity<>("Route was Deleted! ", HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Получает изображение маршрута по его идентификатору.
     *
     * @param routeId идентификатор маршрута
     * @return изображение маршрута в виде байтов
     */
    @GetMapping("/route/{routeId}/image")
    public ResponseEntity<byte[]> getAllPosts(@PathVariable UUID routeId) {
        byte[] imageBytes = routeService.getImageByRouteId(routeId);
        if (imageBytes != null) {
            MediaType mediaType = routeService.getImageMediaType(imageBytes);

            return ResponseEntity.ok().contentType(mediaType).body(imageBytes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Обновляет маршрут по его идентификатору.
     *
     * @param idRoute      идентификатор маршрута
     * @param routeRequest данные маршрута для обновления
     * @return сообщение о результате обновления маршрута
     */
    @PutMapping("/put/{idRoute}")
    public ResponseEntity<String> updateUserById(
            @PathVariable UUID idRoute,
            @RequestBody PutRequest routeRequest
    ) {
        try {
            routeService.updateRouteById(idRoute, routeRequest);
            return ResponseEntity.ok("Route was successfully updated");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating route: " + e.getMessage());
        }
    }
}
