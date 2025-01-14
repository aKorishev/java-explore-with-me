package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompilationDto(
		long id,
		@NotBlank String title,
		boolean pinned,
		Set<EventShortDto> events) { }