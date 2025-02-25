package ru.practicum.ewm.requests;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.Event;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.enums.State;
import ru.practicum.ewm.exceptions.ForbiddenException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.requests.enums.RequestStatus;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.requests.enums.RequestStatus.CONFIRMED;
import static ru.practicum.ewm.requests.enums.RequestStatus.PENDING;
import static ru.practicum.ewm.requests.enums.RequestStatus.REJECTED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public RequestDto addRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        User user = getUser(userId);
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ForbiddenException("Request is already exist.");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new ForbiddenException("Initiator can't send request to his own event.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ForbiddenException("Participation is possible only in published event.");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <=
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED)) {
            throw new ForbiddenException("Participant limit has been reached.");
        }
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        if (event.getRequestModeration() && event.getParticipantLimit() != 0) {
            request.setStatus(PENDING);
        } else {
            request.setStatus(CONFIRMED);
        }
        log.info("Request was created");
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest statusUpdateRequest) {
        User initiator = getUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found."));
        if (!event.getInitiator().equals(initiator)) {
            throw new ValidationException("User isn't initiator.");
        }
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ForbiddenException("The participant limit has been reached.");
        }
        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected = new ArrayList<>();
        List<Request> requests = requestRepository.findAllByEventIdAndIdInAndStatus(eventId,
                statusUpdateRequest.getRequestIds(), PENDING);
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if (statusUpdateRequest.getStatus() == REJECTED) {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toRequestDto(request));
            }
            if (statusUpdateRequest.getStatus() == CONFIRMED && event.getParticipantLimit() > 0 &&
                    (confirmedRequests + i) < event.getParticipantLimit()) {
                request.setStatus(CONFIRMED);
                confirmed.add(RequestMapper.toRequestDto(request));
            } else {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toRequestDto(request));
            }
        }
        log.info("Request was updated");
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        log.info("Request was canceled");
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        checkUser(userId);
        eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
        log.info("Request by Event owner");
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByUser(Long userId) {
        checkUser(userId);
        log.info("Request by User");
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found"));
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}
