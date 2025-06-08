package com.example.backend.RouteMate.repository;

import com.example.backend.RouteMate.payload.request.CommentRequest;
import com.example.backend.RouteMate.payload.response.CommentResponse;
import com.example.backend.RouteMate.service.CommentService;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с комментариями.
 * Содержит методы для сохранения комментариев и получения списка комментариев по маршруту.
 */
@Repository
public class CommentRepository implements CommentService {

    private final JdbcClient jdbcClient;

    /**
     * Конструктор для инициализации CommentRepository.
     *
     * @param jdbcClient JdbcClient для выполнения SQL-запросов.
     */
    public CommentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Сохраняет комментарий в базе данных.
     *
     * @param commentRequest Запрос с данными для создания нового комментария.
     */
    @Override
    public void saveComment(CommentRequest commentRequest) {
        String sql = """
                 INSERT INTO comments(id, route_id, user_id, comment, create_at) VALUES(?, ?, ?, ?, ?);
                """;
        Timestamp createdAt = commentRequest.createAt() != null ? commentRequest.createAt() : Timestamp.valueOf(LocalDateTime.now());

        // Выполнение запроса на вставку нового комментария
        jdbcClient.sql(sql).params(
                UUID.randomUUID(),
                commentRequest.routeId(),
                commentRequest.userId(),
                commentRequest.comment(),
                createdAt
        ).update();
    }

    /**
     * Возвращает список комментариев для указанного маршрута.
     *
     * @param routeId Идентификатор маршрута, для которого нужно получить комментарии.
     * @return Список комментариев.
     */
    @Override
    public List<CommentResponse> listOfComment(UUID routeId) {
        String sql = """
                SELECT * FROM comments WHERE route_id = :routeId;
                """;

        // Выполнение запроса для получения списка комментариев
        return jdbcClient.sql(sql).param("routeId", routeId).query(CommentResponse.class).list();
    }

    @Override
    public void delComment(UUID idComment) {
        String sqlQueryDropComment = """
                DELETE FROM comments WHERE id = :idComment;
                """;

        jdbcClient.sql(sqlQueryDropComment).param("idComment", idComment).update();
    }
}
