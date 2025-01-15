package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CategoryToAddDto;

import jakarta.validation.Valid;
import ru.practicum.ewm.serices.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {
	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public CategoryDto addCategory(@Valid @RequestBody CategoryToAddDto category) {
		return categoryService.save(category);
	}

	@PatchMapping("/{catId}")
	public CategoryDto updateCategory(@PathVariable long catId, @Valid @RequestBody CategoryDto category) {
		return categoryService.update(catId, category);
	}

	@DeleteMapping("/{catId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteCategory(@PathVariable long catId) {
		categoryService.delete(catId);
	}
}
