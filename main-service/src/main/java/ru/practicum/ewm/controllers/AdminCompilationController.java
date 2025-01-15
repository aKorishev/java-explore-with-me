package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationToAddDto;
import ru.practicum.ewm.dto.CompilationToUpdateDto;

import jakarta.validation.Valid;
import ru.practicum.ewm.serices.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class AdminCompilationController {
	private final CompilationService compService;

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public CompilationDto saveCompilation(@Valid @RequestBody CompilationToAddDto compilationDto) {
		return compService.save(compilationDto);
	}

	@PatchMapping("/{compId}")
	public CompilationDto updateCompilation(@PathVariable long compId,
											@Valid @RequestBody CompilationToUpdateDto updateRequest) {
		return compService.update(compId, updateRequest);
	}

	@DeleteMapping("/{compId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteCompilation(@PathVariable long compId) {
		compService.delete(compId);
	}
}
