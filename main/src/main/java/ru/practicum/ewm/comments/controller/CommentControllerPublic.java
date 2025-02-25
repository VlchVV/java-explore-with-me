package ru.practicum.ewm.comments.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comments.CommentService;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.stats.util.Constants;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentControllerPublic {
    private final CommentService commentService;

    @GetMapping("/event/{eventId}")
    List<CommentDto> getComments(@PathVariable Long eventId,
                                 @RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
                                 @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET /comments/event/{eventId} / getComments");
        return commentService.getComments(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("GET /comments/{commentId} / getCommentById");
        return commentService.getCommentById(commentId);
    }
}
