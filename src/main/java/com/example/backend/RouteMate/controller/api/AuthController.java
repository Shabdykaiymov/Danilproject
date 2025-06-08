package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.UserCreditalsRecord;
import com.example.backend.RouteMate.payload.response.JwtResponse;
import com.example.backend.RouteMate.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для обработки авторизации пользователей.
 * <p>
 * Принимает логин и пароль, проверяет их, и если всё ок — возвращает JWT токен.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Конструктор с внедрением менеджера аутентификации и JWT утилиты.
     */
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Обрабатывает POST-запрос на вход в систему.
     *
     * @param userCreditalsRecord логин и пароль в виде DTO (record)
     * @return JWT токен, если вход успешен, иначе ошибка
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authMapping(@RequestBody UserCreditalsRecord userCreditalsRecord) {
        try {
            // Создаём токен из введённых логина и пароля
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userCreditalsRecord.username(), userCreditalsRecord.password());

            // Проверяем пользователя через Spring Security
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            logger.info("Получены данные для аутентификации: username = {}", userCreditalsRecord.username());

            // Генерируем JWT токен
            String token = jwtUtil.getSecretToken(userCreditalsRecord.username());

            logger.info("Токен успешно сгенерирован для пользователя {}", userCreditalsRecord.username());

            return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Ошибка при аутентификации пользователя: {}", userCreditalsRecord.username(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
