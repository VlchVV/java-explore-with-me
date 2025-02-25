package ru.practicum.ewm.categories.controller;

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
import ru.practicum.ewm.categories.CategoryService;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.stats.util.Constants;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryControllerPublic {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(value = Constants.REQ_PARAM_FROM, defaultValue = Constants.DEFAULT_PAGE_FROM) @PositiveOrZero Integer from,
                                           @RequestParam(value = Constants.REQ_PARAM_SIZE, defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("GET /categories / getCategories");
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable Long categoryId) {
        log.info("GET /categories / getCategoryById");
        return categoryService.getCategoryById(categoryId);
    }

}
