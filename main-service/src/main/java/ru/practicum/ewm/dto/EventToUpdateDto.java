package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventToUpdateDto(
        @Size(min = 3, max = 120)
        @Nullable String title,

        @Size(min = 20, max = 2000)
        @Nullable String annotation,

        @Size(min = 20, max = 7000)
        @Nullable String description,

        @Nullable Long category,

        @PositiveOrZero Integer participantLimit,

        @Nullable Boolean paid,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Nullable @Future
        LocalDateTime eventDate,

        @Nullable Location location,

        @Nullable Boolean requestModeration,

        @Nullable StateAction stateAction
) {
    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT,
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}
