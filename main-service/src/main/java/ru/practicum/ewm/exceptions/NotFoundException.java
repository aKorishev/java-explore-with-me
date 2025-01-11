package ru.practicum.ewm.exceptions;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String objectName, long objectId) {
		super(objectName + " with id=" + objectId + " was not found.");
	}
}
