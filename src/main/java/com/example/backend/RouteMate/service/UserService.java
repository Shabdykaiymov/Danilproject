package com.example.backend.RouteMate.service;

import com.example.backend.RouteMate.model.User;
import com.example.backend.RouteMate.payload.request.UserRequest;
import com.example.backend.RouteMate.payload.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserService {

    /**
     * Сохраняет нового пользователя.
     *
     * @param userRequest Запрос с данными пользователя.
     */
    void saveUser(UserRequest userRequest);

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Ответ с данными пользователя.
     */
    UserResponse findUserById(UUID id);

    /**
     * Находит пользователя по имени.
     *
     * @param username Имя пользователя.
     * @return Ответ с данными пользователя.
     */
    UserResponse getUserByUserName(String username);

    /**
     * Получает объект пользователя по имени.
     *
     * @param username Имя пользователя.
     * @return Объект пользователя.
     */
    User getUserByUserNameModel(String username);

    /**
     * Проверяет, есть ли у пользователя необходимые полномочия (например, администратор).
     *
     * @param username Имя пользователя.
     * @return true, если у пользователя есть необходимые полномочия, иначе false.
     */
    boolean chetAuthorities(String username);

    /**
     * Проверяет, существует ли пользователь с таким именем.
     *
     * @param username Имя пользователя.
     * @return true, если пользователь существует, иначе false.
     */
    boolean checkUserByUserName(String username);
}
