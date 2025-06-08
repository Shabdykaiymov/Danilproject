package com.example.backend.RouteMate.config.filter;

import com.example.backend.RouteMate.util.JwtUtil;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JwtAuthenticationFilter - фильтр, обрабатывающий JWT-аутентификацию.
 * <p>
 * Проверяет наличие и валидность JWT-токена в заголовке "Authorization".
 * В случае успеха устанавливает пользователя в SecurityContext.
 * При недействительном или отсутствующем токене — прерывает цепочку фильтров и возвращает 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    /**
     * Конструктор фильтра с внедрением утилиты JWT.
     *
     * @param jwtUtil утилита для работы с JWT (извлечение username, валидация и т.п.)
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Основная логика фильтра. Выполняется один раз на каждый запрос.
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException исключения сервлета
     * @throws IOException      ошибки ввода/вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                if (isValidJwt(token)) {
                    String username = jwtUtil.extractUsername(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        if (jwtUtil.validateToken(token, username)) {
                            SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>())
                            );
                        }
                    }
                } else {
                    logger.error("Invalid JWT format: {}", token);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT format.");
                    return;
                }
            } catch (MalformedJwtException e) {
                logger.error("Malformed JWT token: {}", token, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token.");
                return;
            }
        } else {
            logger.debug("No valid Authorization header found.");
        }

        // Продолжить выполнение фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Проверка валидности формата JWT.
     *
     * @param token JWT-токен
     * @return true, если формат валиден (3 части, разделённые точками)
     */
    private boolean isValidJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }
}
