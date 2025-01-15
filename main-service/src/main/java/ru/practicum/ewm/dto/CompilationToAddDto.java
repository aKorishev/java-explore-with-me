package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Set;

@Jacksonized
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompilationToAddDto implements Serializable {

	@NotBlank
	@Size(min = 1, max = 50)
	private final String title;

	private final boolean pinned;

	private final @Nullable Set<Long> events;
}