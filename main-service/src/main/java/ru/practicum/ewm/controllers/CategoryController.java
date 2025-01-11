package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.CategoryService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping
	public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
										   @RequestParam(defaultValue = "10") @Positive int size) {
		return categoryService.getAll(from, size);
	}

	@GetMapping("/{catId}")
	public CategoryDto getCategory(@PathVariable long catId) {
		return categoryService.getCategory(catId);
	}
}
