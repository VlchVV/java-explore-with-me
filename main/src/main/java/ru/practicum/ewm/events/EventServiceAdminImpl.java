package ru.practicum.ewm.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.CategoryRepository;
import ru.practicum.ewm.categories.CategoryServiceImpl;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventUpdateAdminDto;
import ru.practicum.ewm.events.enums.State;
import ru.practicum.ewm.events.enums.StateActionAdmin;
import ru.practicum.ewm.exceptions.ForbiddenException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.locations.LocationService;
import ru.practicum.ewm.requests.RequestRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.users.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.enums.State.PENDING;
import static ru.practicum.ewm.events.enums.State.PUBLISHED;
import static ru.practicum.ewm.events.enums.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.ewm.events.enums.StateActionAdmin.REJECT_EVENT;
import static ru.practicum.ewm.requests.enums.RequestStatus.CONFIRMED;

@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceAdminImpl extends EventServiceImpl implements EventServiceAdmin {
    @Value("${app}")
    String app;

    public EventServiceAdminImpl(EventRepository eventRepository, UserRepository userRepository, CategoryRepository categoryRepository, CategoryServiceImpl categoryService, LocationService locationService, RequestRepository requestRepository, StatsClient statsClient) {
        super(eventRepository, userRepository, categoryRepository, categoryService, locationService, requestRepository, statsClient);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, EventUpdateAdminDto updateEvent) {
        Event event = getEvent(eventId);
        if (updateEvent.getStateAction() != null) {
            StateActionAdmin stateAction = StateActionAdmin.valueOf(updateEvent.getStateAction());
            if (!event.getState().equals(PENDING) && stateAction.equals(PUBLISH_EVENT)) {
                throw new ForbiddenException("Not pending");
            }
            if (event.getState().equals(PUBLISHED) && stateAction.equals(REJECT_EVENT)) {
                throw new ForbiddenException("Already published.");
            }
            if (stateAction.equals(PUBLISH_EVENT)) {
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (stateAction.equals(REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        updateFields(event, EventMapper.eventUpdateAdminToUpdateEvent(updateEvent));
        log.info("Event update by Admin" + event);
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDtoWithViews> getEventsByAdminParams(List<Long> users, List<String> states, List<Long> categories,
                                                              LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                              Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Incorrectly made request.");
        }
        Specification<Event> specification = Specification.where(null);
        if (users != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(states));
        }
        if (categories != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from / size, size));
        List<EventFullDtoWithViews> result = new ArrayList<>();
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("Start was not found"));
        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED).stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));
        for (Event event : events) {
            ObjectMapper mapper = new ObjectMapper();
            List<ViewStats> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
            });
            if (!statsDto.isEmpty()) {
                result.add(EventMapper.toEventFullDtoWithViews(event, statsDto.get(0).getHits(),
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            } else {
                result.add(EventMapper.toEventFullDtoWithViews(event, 0L,
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            }
        }
        log.info("get Events by Admin");
        return result;
    }
}
