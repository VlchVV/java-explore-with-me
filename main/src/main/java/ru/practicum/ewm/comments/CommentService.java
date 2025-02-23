package ru.practicum.ewm.comments;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentNewDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, CommentNewDto commentNewDto);

    CommentDto updateComment(Long userId, Long commentId, CommentNewDto commentNewDto);

    List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size);

    List<CommentDto> getComments(Long eventId, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);

    void deleteComment(Long userId, Long commentId);

    void deleteComment(Long commentId);
}
