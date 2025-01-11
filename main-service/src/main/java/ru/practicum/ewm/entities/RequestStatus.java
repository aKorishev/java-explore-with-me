package ru.practicum.ewm.entities;

public enum RequestStatus {
	PENDING, CONFIRMED, CANCELED, REJECTED;

	public static RequestStatus from(String statusString) {
		for (RequestStatus status : values()) {
			if (status.name().equalsIgnoreCase(statusString)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Wrong request status [" + statusString + "]");
	}
}
