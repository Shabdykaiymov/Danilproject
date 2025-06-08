package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.UserRequest;
import com.example.backend.RouteMate.payload.response.UserResponse;
import com.example.backend.RouteMate.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для работы с пользователями.
 * <p>
 * Обрабатывает запросы на создание пользователей, проверку их существования,
 * получение информации о пользователях и проверку их ролей.
 */
@RestController
@RequestMapping("/api")
public class UserControllerApi {

    private static final Logger log = LogManager.getLogger(UserControllerApi.class);
    private final UserService userService;

    /**
     * Конструктор контроллера для внедрения сервиса пользователей.
     *
     * @param userService сервис для работы с пользователями
     */
    public UserControllerApi(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param userRequest данные пользователя для сохранения
     * @return сообщение о результате создания пользователя
     */
    @PostMapping("/save-user")
    public ResponseEntity<String> saveUserMapping(@Valid @RequestBody UserRequest userRequest) {
        try {
            userService.saveUser(userRequest);
            return new ResponseEntity<>("User was Saved!", HttpStatus.CREATED);
        } catch (Exception exception) {
            return new ResponseEntity<>("User was not save!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Проверяет существование пользователя по имени.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует, иначе false
     */
    @GetMapping("/check/{username}")
    public ResponseEntity<Boolean> checkUserByUserName(@PathVariable String username) {
        boolean userResponse = userService.checkUserByUserName(username);
        return userResponse
                ? new ResponseEntity<>(true, HttpStatus.OK)
                : new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    /**
     * Получает информацию о пользователе по ID.
     *
     * @param idUser уникальный идентификатор пользователя
     * @return информация о пользователе
     */
    @GetMapping("/user/{idUser}")
    public ResponseEntity<UserResponse> findUserByIdMapping(@PathVariable UUID idUser) {
        try {
            UserResponse userResponse = userService.findUserById(idUser);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        } catch (NullPointerException nullPointerException) {
            log.error("Error message: ", nullPointerException);
            return null;
        }
    }

    /**
     * Получает информацию о пользователе по имени.
     *
     * @param username имя пользователя
     * @return информация о пользователе
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUserName(@PathVariable String username) {
        UserResponse userResponse = userService.getUserByUserName(username);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    /**
     * Проверяет роль пользователя.
     *
     * @param username имя пользователя
     * @return true, если у пользователя есть роль, иначе false
     */
    @GetMapping("/role/{username}")
    public ResponseEntity<Boolean> secureRoleUser(@PathVariable String username) {
        boolean responseSecure = userService.chetAuthorities(username);
        return new ResponseEntity<>(responseSecure, HttpStatus.OK);
    }
}
