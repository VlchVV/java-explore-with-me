package ru.practicum.ewm.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.events.EventServiceAdmin;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventUpdateAdminDto;
import ru.practicum.ewm.stats.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventControllerAdmin {
    private final EventServiceAdmin eventServiceAdmin;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(
            @PathVariable Long eventId,
            @RequestBody @Valid EventUpdateAdminDto eventUpdateAdminDto) {
        log.info("PATCH / /admin/events/{eventId} / updateEventByAdmin");
        return eventServiceAdmin.updateEventByAdmin(eventId, eventUpdateAdminDto);
    }

    @GetMapping
    public List<EventFullDtoWithViews> getEventsByAdminParams(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
            @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET / /admin/events / getEventsByAdminParams");
        return eventServiceAdmin.getEventsByAdminParams(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}
