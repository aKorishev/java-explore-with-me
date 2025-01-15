package ru.practicum.ewm.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ErrorResponse;
import ru.practicum.ewm.exceptions.IdIsAlreadyInUseException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.NotValidException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerNotFound(final NotFoundException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "NotFoundException",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerNotValid(final NotValidException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "NotValidException",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerNotValid(final MethodArgumentNotValidException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "NotValidException",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerNotValid(final MissingServletRequestParameterException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "NotValidException",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerNotValid(final IllegalArgumentException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "NotValidException",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlerNotValid(final DataIntegrityViolationException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "CONFLICT",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlerNotValid(final IllegalStateException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "CONFLICT",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlerNotValid(final IdIsAlreadyInUseException e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                "CONFLICT",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerOther(final Exception e) {
        log.debug(e.getMessage(), e);

        return new ErrorResponse(
                e.getClass().getName(),
                e.getMessage());
    }
}
