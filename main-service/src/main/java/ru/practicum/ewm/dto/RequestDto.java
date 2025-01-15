package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;


@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestDto(
	Long id,
	long requester,
	long event,
	String status,
	LocalDateTime created) { }
