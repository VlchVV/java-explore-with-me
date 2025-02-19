package ru.practicum.ewm.events;

import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventUpdateAdminDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventServiceAdmin {
    EventFullDto updateEventByAdmin(Long eventId, EventUpdateAdminDto updateEvent);

    List<EventFullDtoWithViews> getEventsByAdminParams(List<Long> users, List<String> states, List<Long> categories,
                                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                       Integer from, Integer size);
}
