package com.example.backend.RouteMate.service;

import com.example.backend.RouteMate.payload.request.CommentRequest;
import com.example.backend.RouteMate.payload.response.CommentResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CommentService {

    /**
     * Сохраняет новый комментарий.
     *
     * @param commentRequest Запрос с данными комментария.
     */
    void saveComment(CommentRequest commentRequest);

    /**
     * Получает список комментариев для маршрута.
     *
     * @param routeId Идентификатор маршрута.
     * @return Список комментариев.
     */
    List<CommentResponse> listOfComment(UUID routeId);

    void delComment(UUID idComment);
}
