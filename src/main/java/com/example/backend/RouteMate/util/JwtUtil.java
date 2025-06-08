package com.example.backend.RouteMate.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final String secretKey = "mySecretKey";

    /**
     * Создает JWT токен для пользователя.
     *
     * @param username Имя пользователя.
     * @return Строка с JWT токеном.
     */
    public String getSecretToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // Токен истечет через 1 час.
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Извлекает имя пользователя из токена.
     *
     * @param token JWT токен.
     * @return Имя пользователя, заключенное в токене.
     * @throws JwtException Исключение при ошибке парсинга токена.
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            logger.error("JWT parsing error: " + e.getMessage());
            throw new SignatureException("Invalid JWT signature or expired token", e);
        }
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token JWT токен.
     * @return true, если токен истек, иначе false.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату истечения токена.
     *
     * @param token JWT токен.
     * @return Дата истечения токена.
     */
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    /**
     * Проверяет, является ли токен действительным для данного пользователя.
     *
     * @param token    JWT токен.
     * @param username Имя пользователя.
     * @return true, если токен действителен, иначе false.
     */
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }
}
