package ru.practicum.ewm.comments;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentNewDto;
import ru.practicum.ewm.events.Event;
import ru.practicum.ewm.events.EventMapper;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.exceptions.BadRequestException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.requests.RequestRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.UserMapper;
import ru.practicum.ewm.users.UserRepository;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.enums.State.PUBLISHED;
import static ru.practicum.ewm.requests.enums.RequestStatus.CONFIRMED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public CommentDto addComment(Long userId, Long eventId, CommentNewDto commentNewDto) {
        User author = getUser(userId);
        Event event = getEvent(eventId);
        if (event.getState() != PUBLISHED) {
            throw new BadRequestException("Comments are available only for published events.");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentNewDto, author, event));
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        log.info("Add comment: {}", comment);
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentNewDto commentNewDto) {
        User author = getUser(userId);
        Comment comment = getComment(commentId);
        Event event = comment.getEvent();
        if (comment.getAuthor() != author) {
            throw new ValidationException("Only author can update comment.");
        }
        comment.setText(commentNewDto.getText());
        comment.setEdited(LocalDateTime.now());
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED));
        log.info("Update comment: {}", comment);
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size) {
        User author = getUser(userId);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, PageRequest.of(from / size, size));
        List<Long> eventIds = comments.stream().map(comment -> comment.getEvent().getId()).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(eventIds, CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        List<CommentDto> result = new ArrayList<>();
        for (Comment c : comments) {
            Long eventId = c.getEvent().getId();
            EventShortDto eventShort = EventMapper.toEventShortDto(c.getEvent(), confirmedRequests.get(eventId));
            result.add(CommentMapper.toCommentDto(c, userShort, eventShort));
        }
        log.info("Get comments by author: {}", result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long eventId, Integer from, Integer size) {
        Event event = getEvent(eventId);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        log.info("Get comments: {}", eventShort);
        return commentRepository.findAllByEventId(eventId, PageRequest.of(from / size, size))
                .stream()
                .map(c -> CommentMapper.toCommentDto(c, UserMapper.toUserShortDto(c.getAuthor()), eventShort))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long commentId) {
        Comment comment = getComment(commentId);
        UserShortDto userShort = UserMapper.toUserShortDto(comment.getAuthor());
        EventShortDto eventShort = EventMapper.toEventShortDto(comment.getEvent(),
                requestRepository.countByEventIdAndStatus(comment.getEvent().getId(), CONFIRMED));
        log.info("Get comment by id: {}", comment);
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User author = getUser(userId);
        Comment comment = getComment(commentId);
        if (comment.getAuthor() != author) {
            throw new ValidationException("Only author can delete the comment.");
        }
        log.info("Delete comment by id from user: {}", commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteComment(Long commentId) {
        getComment(commentId);
        log.info("Delete comment by id: {}", commentId);
        commentRepository.deleteById(commentId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " was not found"));
    }
}
