package ru.practicum.ewm.events.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.EventService;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;
import ru.practicum.ewm.stats.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDtoWithViews> getEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                  LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                  LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                  @RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero
                                                  Integer from,
                                                  @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive
                                                  Integer size,
                                                  HttpServletRequest request) {
        log.info("GET / /events / getEvents");
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDtoWithViews getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("GET / /events/{eventId} / getEventById");
        return eventService.getEventById(eventId, request);
    }
}