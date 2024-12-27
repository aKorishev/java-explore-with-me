package ru.practicum.statistic.api.exceptions;

public class IdIsAlreadyInUseException extends RuntimeException {
    public IdIsAlreadyInUseException(String message) {
        super(message);
    }
}
