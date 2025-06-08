package com.example.backend.RouteMate.config;

import com.example.backend.RouteMate.service.UserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService — реализация интерфейса {@link UserDetailsService},
 * используемая для загрузки пользовательских данных (UserDetails) по имени пользователя.
 * <p>
 * Используется Spring Security для аутентификации пользователей.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости {@link UserService}.
     *
     * @param userService сервис для доступа к данным пользователя
     */
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Загружает пользователя по имени и преобразует его в объект {@link UserDetails},
     * который используется системой безопасности Spring для аутентификации.
     *
     * @param username имя пользователя
     * @return объект {@link UserDetails}, содержащий имя, пароль и роли
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.backend.RouteMate.model.User user = userService.getUserByUserNameModel(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь с таким именем не найден: " + username);
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER") // можно сделать динамически, если роли будут храниться в БД
                .build();
    }
}
