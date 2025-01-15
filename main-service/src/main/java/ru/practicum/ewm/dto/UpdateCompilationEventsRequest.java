package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCompilationEventsRequest implements Serializable {
	private final @NotEmpty Set<Long> events;
}