package ru.practicum.statistic.api.exceptions;

public class NotValidException extends RuntimeException {
    public NotValidException(String message) {
        super(message);
    }
}
