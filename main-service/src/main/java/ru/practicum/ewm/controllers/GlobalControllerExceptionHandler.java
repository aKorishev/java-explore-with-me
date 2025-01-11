package ru.practicum.ewm.controllers;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.ewm.dto.ApiError;
import ru.practicum.ewm.exceptions.NotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {
	private final boolean isDetailsNeeded;

	public GlobalControllerExceptionHandler(@Value("${api-errors.add-stack-traces: false}") boolean isDetailsNeeded) {
		this.isDetailsNeeded = isDetailsNeeded;
	}


	@ExceptionHandler({NotFoundException.class})
	public ResponseEntity<ApiError> handleNotFound(Exception ex) {
		ApiError apiError = new ApiError(
				HttpStatus.NOT_FOUND,
				"The required object was not found.",
				ex.getLocalizedMessage(),
				error(ex)
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	@ExceptionHandler({IllegalArgumentException.class})
	public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
		ApiError apiError = new ApiError(
				HttpStatus.BAD_REQUEST,
				"The passed data causes an error.",
				ex.getLocalizedMessage(),
				error(ex)
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	@ExceptionHandler({IllegalStateException.class})
	public ResponseEntity<ApiError> handleIllegalState(Exception ex) {
		ApiError apiError = new ApiError(
				HttpStatus.CONFLICT,
				"For the requested operation the conditions are not met.",
				ex.getLocalizedMessage(),
				error(ex)
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	public @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																		@NonNull HttpHeaders headers,
																		@NonNull HttpStatus status,
																		@NonNull WebRequest request) {

		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> String.format("Field: %s. Error: %s. Value: %s",
						error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
				.collect(Collectors.toList());

		ApiError apiError = new ApiError(
				HttpStatus.BAD_REQUEST,
				"Incorrectly made request.",
				String.format("During [%s] validation %d errors were found", ex.getObjectName(), ex.getErrorCount()),
				errors
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		List<String> errors = new ArrayList<>();
		for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			errors.add(violation.getRootBeanClass().getName() + " " +
					violation.getPropertyPath() + ": " + violation.getMessage());
		}

		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "One of the restrictions has been violated",
				ex.getLocalizedMessage(), errors);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Object> handleDbConstraintViolation(DataIntegrityViolationException ex, WebRequest request) {
		ApiError apiError = new ApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated",
				ex.getLocalizedMessage(), error(ex));

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}


	@ExceptionHandler({Exception.class})
	public ResponseEntity<ApiError> handleAll(Exception ex) {
		ApiError apiError = new ApiError(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error occurred",
				ex.getLocalizedMessage(),
				error(ex)
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	protected @NonNull ResponseEntity<Object> handleExceptionInternal(Exception ex,
																	  Object body,
																	  @NonNull HttpHeaders headers,
																	  @NonNull HttpStatus status,
																	  @NonNull WebRequest request) {
		ApiError apiError = new ApiError(
				status,
				"Error occurred",
				ex.getLocalizedMessage(),
				body == null ? error(ex) : body.toString()
		);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	private String error(Exception e) {
		if (isDetailsNeeded) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} else {
			return null;
		}
	}
}
