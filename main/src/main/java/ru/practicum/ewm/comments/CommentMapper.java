package ru.practicum.ewm.comments;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentNewDto;
import ru.practicum.ewm.events.Event;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment toComment(CommentNewDto commentNewDto, User author, Event event) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setText(commentNewDto.getText());
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public CommentDto toCommentDto(Comment comment, UserShortDto author, EventShortDto event) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                author,
                event,
                comment.getCreated(),
                comment.getEdited()
        );
    }
}
