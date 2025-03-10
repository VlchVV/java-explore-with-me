package ru.practicum.ewm.events;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventNewDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;
import ru.practicum.ewm.events.dto.EventUpdateUserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, EventNewDto eventNewDto);

    EventFullDto updateEventByOwner(Long userId, Long eventId, EventUpdateUserDto updateEvent);

    List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size);

    EventFullDto getEventByOwner(Long userId, Long eventId);

    List<EventShortDtoWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                           Integer size, HttpServletRequest request);


    EventFullDtoWithViews getEventById(Long eventId, HttpServletRequest request);
}
