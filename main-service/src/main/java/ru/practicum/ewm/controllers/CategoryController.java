package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping(path = "/categories")
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
										   @RequestParam(defaultValue = "10") @Positive int size) {
		return categoryService.getAll(from, size);
	}

	@GetMapping("/{catId}")
	@ResponseStatus(HttpStatus.OK)
	public CategoryDto getCategory(@PathVariable long catId) {
		return categoryService.getCategory(catId);
	}
}
