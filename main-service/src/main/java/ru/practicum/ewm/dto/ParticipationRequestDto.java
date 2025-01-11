package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticipationRequestDto implements Serializable {

	private final Long id;

	private final long requester;

	private final long event;

	private final String status;

	private final LocalDateTime created;
}
