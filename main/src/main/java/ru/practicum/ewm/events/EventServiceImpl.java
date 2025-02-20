package ru.practicum.ewm.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.Category;
import ru.practicum.ewm.categories.CategoryMapper;
import ru.practicum.ewm.categories.CategoryRepository;
import ru.practicum.ewm.categories.CategoryServiceImpl;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.enums.State;
import ru.practicum.ewm.events.enums.StateActionPrivate;
import ru.practicum.ewm.exceptions.BadRequestException;
import ru.practicum.ewm.exceptions.ForbiddenException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.locations.LocationMapper;
import ru.practicum.ewm.locations.LocationService;
import ru.practicum.ewm.requests.RequestRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.UserRepository;
import ru.practicum.ewm.util.Util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.enums.State.PENDING;
import static ru.practicum.ewm.events.enums.State.PUBLISHED;
import static ru.practicum.ewm.events.enums.StateActionPrivate.CANCEL_REVIEW;
import static ru.practicum.ewm.events.enums.StateActionPrivate.SEND_TO_REVIEW;
import static ru.practicum.ewm.requests.enums.RequestStatus.CONFIRMED;


@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PROTECTED)
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final CategoryServiceImpl categoryService;
    final LocationService locationService;
    final RequestRepository requestRepository;
    final StatsClient statsClient;
    @Value("${app}")
    String app;

    @Override
    public EventFullDto addEvent(Long userId, EventNewDto eventNewDto) {
        Util.checkActualTime(eventNewDto.getEventDate());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found"));
        Long catId = eventNewDto.getCategory();
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Category with id=" + catId + " was not found"));
        Location location = locationService.upsertLocation(LocationMapper.toLocation(eventNewDto.getLocation()));
        Event event = EventMapper.toEvent(eventNewDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(PENDING);
        log.info("Event add" + event);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    public EventFullDto updateEventByOwner(Long userId, Long eventId, EventUpdateUserDto updateEvent) {
        Event event = getEvent(eventId, userId);
        if (event.getState() == PUBLISHED) {
            throw new ForbiddenException("Published events can't be updated");
        }
        updateFields(event, EventMapper.eventUpdateUserToUpdateEvent(updateEvent));
        if (updateEvent.getStateAction() != null) {
            StateActionPrivate stateActionPrivate = StateActionPrivate.valueOf(updateEvent.getStateAction());
            if (stateActionPrivate.equals(SEND_TO_REVIEW)) {
                event.setState(PENDING);
            } else if (stateActionPrivate.equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }
        log.info("Event update by Owner" + event);
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    protected void updateFields(Event event, EventUpdateDto updateEvent) {
        String annotation = updateEvent.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(updateEvent.getCategory())));
        }
        String description = updateEvent.getDescription();
        if (description != null && !description.isBlank()) {
            event.setDescription(description);
        }
        LocalDateTime eventDate = updateEvent.getEventDate();
        if (eventDate != null) {
            Util.checkActualTime(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(locationService.upsertLocation(LocationMapper.toLocation(updateEvent.getLocation())));
        }
        Boolean paid = updateEvent.getPaid();
        if (paid != null) {
            event.setPaid(paid);
        }
        Integer participantLimit = updateEvent.getParticipantLimit();
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        Boolean requestModeration = updateEvent.getRequestModeration();
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        String title = updateEvent.getTitle();
        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));
        log.info("getEventsByOwner");
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        log.info("getEventByOwner ");
        return EventMapper.toEventFullDto(getEvent(eventId, userId),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDtoWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("START can't be after END.");
        }

        Specification<Event> specification = EventSpecificationBuilder.build(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);

        PageRequest pageRequest;
        switch (sort) {
            case "EVENT_DATE":
                pageRequest = PageRequest.of(from / size, size, Sort.by("eventDate"));
                break;
            case "VIEWS":
                pageRequest = PageRequest.of(from / size, size, Sort.by("views").descending());
                break;
            default:
                throw new ValidationException("Unknown sort: " + sort);
        }
        List<Event> events = eventRepository.findAll(specification, pageRequest);
        List<EventShortDtoWithViews> result = new ArrayList<>();
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("Start was not found"));
        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));
        for (Event event : events) {
            ObjectMapper mapper = new ObjectMapper();
            List<ViewStats> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
            });
            if (!statsDto.isEmpty()) {
                result.add(EventMapper.toEventShortDtoWithViews(event, statsDto.get(0).getHits(),
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            } else {
                result.add(EventMapper.toEventShortDtoWithViews(event, 0L,
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            }
        }
        EndpointHitDto hit = new EndpointHitDto(app, request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now());
        statsClient.saveHit(hit);

        log.info("get Events");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithViews getEventById(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);
        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Event must be published.");
        }
        ResponseEntity<Object> response = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(),
                List.of(request.getRequestURI()), true);
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStats> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
        });
        EventFullDtoWithViews result;
        if (!statsDto.isEmpty()) {
            result = EventMapper.toEventFullDtoWithViews(event, statsDto.get(0).getHits(),
                    requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        } else {
            result = EventMapper.toEventFullDtoWithViews(event, 0L,
                    requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        }
        EndpointHitDto hit = new EndpointHitDto(app, request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now());
        statsClient.saveHit(hit);
        log.info("get Event by id {} ", eventId);
        return result;
    }

    protected Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    protected Event getEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
