package ru.practicum.ewm.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.EventService;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventNewDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.EventUpdateUserDto;
import ru.practicum.ewm.requests.RequestService;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.stats.util.Constants;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid EventNewDto eventNewDto) {
        log.info("POST / /users/{userId}/events / addEvent");
        return eventService.addEvent(userId, eventNewDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @RequestBody @Valid EventUpdateUserDto updateEvent) {
        log.info("PATCH / /users/{userId}/events/{eventId} / updateEventByOwner");
        return eventService.updateEventByOwner(userId, eventId, updateEvent);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH / /users/{userId}/events/{eventId}/requests / updateRequestsStatus");
        return requestService.updateRequestsStatus(userId, eventId, request);
    }

    @GetMapping
    List<EventShortDto> getEventsByOwner(@PathVariable Long userId,
                                         @RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
                                         @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET / /users/{userId}/events / getEventsByOwner");
        return eventService.getEventsByOwner(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET / /users/{userId}/events/{eventId} / getEventByOwner");
        return eventService.getEventByOwner(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequestsByEventOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET / /users/{userId}/events/{eventId}/requests / getRequestsByEventOwner");
        return requestService.getRequestsByEventOwner(userId, eventId);
    }
}
