package ru.practicum.ewm.exceptions;

public class IdIsAlreadyInUseException extends RuntimeException {
    public IdIsAlreadyInUseException(String message) {
        super(message);
    }
}
