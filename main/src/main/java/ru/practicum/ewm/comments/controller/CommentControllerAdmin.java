package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.CommentService;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentControllerAdmin {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE /admin/comments/{commentId} / deleteComment");
        commentService.deleteComment(commentId);
    }
}
