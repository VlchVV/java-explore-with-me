package ru.practicum.ewm.stats.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.server.model.EndpointHitMapper;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;

    public EndpointHitDto saveHit(EndpointHitDto hit) {
        log.debug("Save hit: {}", hit);
        return EndpointHitMapper.toEndpointHitDto(statsRepository.save(EndpointHitMapper.toHit(hit)));
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            log.debug("Wrong timestamp: start must be before end");
            throw new DateTimeException("Wrong timestamp: start must be before end");
        }
        if (unique) {
            if (uris != null) {
                log.debug("Search unique URIs.");
                return statsRepository.findHitsWithUniqueIpWithUris(uris, start, end);
            }
            log.debug("Search unique, no URIs.");
            return statsRepository.findHitsWithUniqueIpWithoutUris(start, end);
        } else {
            if (uris != null) {
                log.debug("Search all URIs.");
                return statsRepository.findAllHitsWithUris(uris, start, end);
            }
            log.debug("Search all, no URIs.");
            return statsRepository.findAllHitsWithoutUris(start, end);
        }
    }
}
