package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

@Jacksonized
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompilationDto implements Serializable {

	private final long id;

	private final @NotBlank String title;

	private final boolean pinned;

	private final Set<EventShortDto> events;
}