package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Jacksonized
@Getter
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventFullDto extends EventShortDto {

	private final String description;

	private final int participantLimit;

	private final String state;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime publishedOn;

	private @NonNull Location location;

	private final boolean requestModeration;
}
