package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationDto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.CompilationService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationController {
	private final CompilationService compService;

	@GetMapping
	public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
												@RequestParam(defaultValue = "0") @PositiveOrZero int from,
												@RequestParam(defaultValue = "10") @Positive int size) {
		if (pinned == null) {
			return compService.getAll(from, size);
		}
		return compService.getByPinFlag(pinned, from, size);
	}

	@GetMapping("/{compId}")
	@ResponseStatus(HttpStatus.OK)
	public CompilationDto getCompilation(@PathVariable long compId) {
		return compService.getById(compId);
	}
}
