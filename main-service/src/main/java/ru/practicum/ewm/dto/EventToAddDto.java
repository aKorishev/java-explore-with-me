package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Jacksonized
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventToAddDto implements Serializable {

	@Size(min = 3, max = 120)
	private final @NotBlank String title;

	@Size(min = 20, max = 2000)
	private final @NotBlank String annotation;

	@Size(min = 20, max = 7000)
	private final @NotBlank String description;

	private final long category;

	private final @PositiveOrZero int participantLimit;

	private final boolean paid;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@Future
	@NotNull
	private final LocalDateTime eventDate;

	private @NonNull Location location;

	@Builder.Default
	private final boolean requestModeration = true;
}
