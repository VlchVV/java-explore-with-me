package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.CommentService;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentNewDto;
import ru.practicum.ewm.stats.util.Constants;

import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @RequestBody @Valid CommentNewDto commentNewDto) {
        log.info("POST /users/{userId}/comments/{eventId} / addComment");
        return commentService.addComment(userId, eventId, commentNewDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid CommentNewDto commentNewDto) {
        log.info("PATCH /users/{userId}/comments/{eventId}/{commentId} / updateComment");
        return commentService.updateComment(userId, commentId, commentNewDto);
    }

    @GetMapping
    public List<CommentDto> getCommentsByAuthor(@PathVariable Long userId,
                                                @RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
                                                @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET /users/{userId}/comments / getCommentsByAuthor");
        return commentService.getCommentsByAuthor(userId, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("DELETE /users/{userId}/comments/{commentId} / deleteComment");
        commentService.deleteComment(userId, commentId);
    }
}
