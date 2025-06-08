package com.example.backend.RouteMate.controller.api;

import com.example.backend.RouteMate.payload.request.CommentRequest;
import com.example.backend.RouteMate.payload.response.CommentResponse;
import com.example.backend.RouteMate.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с комментариями пользователей.
 * <p>
 * Обрабатывает запросы на создание и получение комментариев для маршрутов.
 */
@RestController
@RequestMapping("/api")
public class CommentControllerApi {

    private final CommentService commentService;

    /**
     * Конструктор контроллера для внедрения сервиса комментариев.
     *
     * @param commentService сервис для работы с комментариями
     */
    public CommentControllerApi(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Получает список комментариев для указанного маршрута.
     *
     * @param routeId идентификатор маршрута
     * @return список комментариев для маршрута с кодом ответа 200 (OK)
     */
    @GetMapping("/get-comment/{routeId}")
    public ResponseEntity<List<CommentResponse>> getsComments(@PathVariable UUID routeId) {

        List<CommentResponse> listComment = commentService.listOfComment(routeId);
        return new ResponseEntity<>(listComment, HttpStatus.OK);
    }

    /**
     * Создаёт новый комментарий для маршрута.
     *
     * @param commentRequest данные нового комментария
     * @return сообщение о результате создания комментария
     */
    @PostMapping("/create-comment")
    public ResponseEntity<String> createComment(@RequestBody CommentRequest commentRequest) {
        try {
            commentService.saveComment(commentRequest);
            return new ResponseEntity<>("Comment was saved", HttpStatus.CREATED);
        } catch (Exception exception) {
            return new ResponseEntity<>("Comment was not saved", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/del/{idComment}")
    public ResponseEntity<String> deleteComment(@PathVariable UUID idComment) {
        try {
            commentService.delComment(idComment);
            return new ResponseEntity<>("Comment was deleted", HttpStatus.CREATED);
        } catch (Exception exception) {
            return new ResponseEntity<>("Comment was not deleted", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
