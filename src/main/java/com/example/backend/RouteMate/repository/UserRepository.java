package com.example.backend.RouteMate.repository;

import com.example.backend.RouteMate.payload.request.UserRequest;
import com.example.backend.RouteMate.payload.response.UserResponse;
import com.example.backend.RouteMate.model.User;
import com.example.backend.RouteMate.service.UserService;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий для работы с пользователями.
 * Содержит методы для регистрации пользователей, получения информации и управления правами.
 */
@Repository
public class UserRepository implements UserService {

    private final JdbcClient jdbcClient;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Конструктор для инициализации UserRepository.
     *
     * @param jdbcClient JdbcClient для выполнения SQL-запросов.
     * @param bCryptPasswordEncoder BCryptPasswordEncoder для кодирования паролей.
     */
    public UserRepository(JdbcClient jdbcClient, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jdbcClient = jdbcClient;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param userRequest Запрос с данными для нового пользователя.
     */
    @Override
    public void saveUser(UserRequest userRequest) {
        String sqlQuery = """
                INSERT INTO users (id, username, password, email, firstname, lastname, enabled)
                       VALUES (?, ?, ?, ?, ?, ?, ?);
                """;
        UUID idUser = UUID.randomUUID();
        String encryptedPassword = bCryptPasswordEncoder.encode(userRequest.password());

        int result = jdbcClient.sql(sqlQuery).params(
                idUser,
                userRequest.username(),
                encryptedPassword,
                userRequest.email(),
                userRequest.firstname(),
                userRequest.lastname(),
                true
        ).update();

        if (result == 0) {
            throw new RuntimeException("Failed to insert user.");
        }

        addAuthorities(userRequest.username());
    }

    /**
     * Добавляет права пользователю.
     *
     * @param username Имя пользователя.
     */
    public void addAuthorities(String username) {
        String querySql = """
                INSERT INTO authorities (username, authority) VALUES (?, ?);
                """;

        try {
            int result = jdbcClient.sql(querySql).params(username, "ROLE_USER").update();
            if (result == 0) {
                throw new RuntimeException("Failed to insert authority.");
            }
        } catch (Exception e) {
            System.err.println("Error in adding authorities: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Ответ с данными пользователя.
     */
    @Override
    public UserResponse findUserById(UUID id) {
        String sqlQuery = """
                SELECT * FROM users WHERE id = :id;
                """;
        return jdbcClient.sql(sqlQuery).param("id", id).query(UserResponse.class).single();
    }

    /**
     * Проверяет наличие у пользователя роли администратора.
     *
     * @param username Имя пользователя.
     * @return true, если пользователь имеет роль администратора; иначе false.
     */
    @Override
    public boolean chetAuthorities(String username) {
        String sql = """
                SELECT COUNT(*) FROM authorities
                       WHERE username = :username AND authority = 'ROLE_ADMIN';
                """;
        Integer count = jdbcClient.sql(sql)
                .param("username", username)
                .query(Integer.class)
                .single();

        return count > 0;
    }

    /**
     * Проверяет, существует ли пользователь с данным именем пользователя.
     *
     * @param username Имя пользователя.
     * @return true, если пользователь существует; иначе false.
     */
    @Override
    public boolean checkUserByUserName(String username) {
        String querySql = """
                SELECT COUNT(*) > 0 FROM users WHERE username = :username;
                """;
        return jdbcClient.sql(querySql)
                .param("username", username)
                .query(Boolean.class)
                .single();
    }

    /**
     * Находит пользователя по его имени пользователя.
     *
     * @param username Имя пользователя.
     * @return Ответ с данными пользователя.
     */
    @Override
    public UserResponse getUserByUserName(String username) {
        String querySql = """
                SELECT * FROM users WHERE username = :username;
                """;
        return jdbcClient.sql(querySql)
                .param("username", username)
                .query(UserResponse.class)
                .single();
    }

    /**
     * Находит пользователя по имени пользователя (модель).
     *
     * @param username Имя пользователя.
     * @return Модель пользователя.
     */
    @Override
    public User getUserByUserNameModel(String username) {
        String querySql = """
                SELECT * FROM users WHERE username = :username;
                """;
        return jdbcClient.sql(querySql)
                .param("username", username)
                .query(User.class)
                .single();
    }
}
