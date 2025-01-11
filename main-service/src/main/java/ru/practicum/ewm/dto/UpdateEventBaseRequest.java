package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateEventBaseRequest implements Serializable, PartialUpdateRequest {
	@Size(min = 3, max = 120)
	private @Nullable String title;

	@Size(min = 20, max = 2000)
	private @Nullable String annotation;

	@Size(min = 20, max = 7000)
	private @Nullable String description;

	private @Nullable Long category;

	private @PositiveOrZero Integer participantLimit;

	private @Nullable Boolean paid;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@Nullable @Future
	private LocalDateTime eventDate;

	private @Nullable Location location;

	private @Nullable Boolean requestModeration;

	@JsonIgnore
	public boolean isNeedAnyUpdates() {
		return isTitleNeedUpdate() ||
				isAnnotationNeedUpdate() ||
				isDescriptionNeedUpdate() ||
				isCategoryNeedUpdate() ||
				isParticipantLimitNeedUpdate() ||
				isPaidFlagNeedUpdate() ||
				isEventDateNeedUpdate() ||
				isLocationNeedUpdate() ||
				isRequestModerationNeedUpdate();
	}

	@JsonIgnore
	public boolean isTitleNeedUpdate() {
		return title != null && !title.isBlank();
	}

	@JsonIgnore
	public boolean isAnnotationNeedUpdate() {
		return annotation != null && !annotation.isBlank();
	}

	@JsonIgnore
	public boolean isDescriptionNeedUpdate() {
		return description != null && !description.isBlank();
	}

	@JsonIgnore
	public boolean isCategoryNeedUpdate() {
		return category != null;
	}

	@JsonIgnore
	public boolean isParticipantLimitNeedUpdate() {
		return participantLimit != null;
	}

	@JsonIgnore
	public boolean isPaidFlagNeedUpdate() {
		return paid != null;
	}

	@JsonIgnore
	public boolean isEventDateNeedUpdate() {
		return eventDate != null;
	}

	@JsonIgnore
	public boolean isLocationNeedUpdate() {
		return location != null;
	}

	@JsonIgnore
	public boolean isRequestModerationNeedUpdate() {
		return requestModeration != null;
	}
}
