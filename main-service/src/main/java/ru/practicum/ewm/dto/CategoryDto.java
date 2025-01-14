package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryDto(
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	Long id,

	@NotBlank
	@Size(min = 1, max = 50)
	String name) { }
