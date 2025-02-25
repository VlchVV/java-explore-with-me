package ru.practicum.ewm.compilations.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilations.CompilationService;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.stats.util.Constants;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationControllerPublic {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET /compilations");
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compilationId}")
    public CompilationDto getCompilationById(@PathVariable Long compilationId) {
        log.info("GET /compilations/{compilationId} / getCompilationById");
        return compilationService.getCompilationById(compilationId);
    }
}
