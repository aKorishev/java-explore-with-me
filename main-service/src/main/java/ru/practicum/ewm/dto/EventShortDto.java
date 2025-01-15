package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.time.LocalDateTime;

@Jacksonized
@Getter
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventShortDto implements Serializable {

	protected final Long id;

	protected final @NotBlank String title;

	protected final @NotBlank String annotation;

	protected final @NotNull CategoryDto category;

	protected final boolean paid;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	protected final @NotNull LocalDateTime eventDate;

	protected final @NotNull UserShortDto initiator;

	protected final @PositiveOrZero Long views;

	private final @PositiveOrZero Long confirmedRequests;
}