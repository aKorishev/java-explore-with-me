package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

	private final HttpStatus status;

	private final String reason;

	private final String message;

	private final List<String> errors;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp = LocalDateTime.now();

	public ApiError(HttpStatus status, String reason, String message, String error) {
		this.status = status;
		this.reason = reason;
		this.message = message;
		if (error != null && !error.isBlank()) {
			this.errors = Collections.singletonList(error);
		} else {
			this.errors = null;
		}
	}
}