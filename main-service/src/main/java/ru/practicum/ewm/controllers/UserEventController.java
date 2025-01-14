package ru.practicum.ewm.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.EventService;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users/{userId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserEventController {
	private final EventService eventService;

	@GetMapping
	public List<EventShortDto> getEvents(@PathVariable long userId,
										 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
										 @RequestParam(defaultValue = "10") @Positive int size) {
		return eventService.findUserEvents(userId, from, size);
	}

	@GetMapping("/{eventId}")
	public EventFullDto getEvent(@PathVariable long userId, @PathVariable long eventId) {
		return eventService.findUserEventById(userId, eventId);
	}

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public EventFullDto addEvent(@PathVariable long userId, @Valid @RequestBody NewEventDto eventDto) {
		return eventService.addEvent(eventDto, userId);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable long userId,
									@PathVariable long eventId,
									@Valid @RequestBody EventToUpdateDto request) {
		return eventService.updateEventByInitiator(userId, eventId, request);
	}

	@GetMapping("/{eventId}/requests")
	public List<ParticipationRequestDto> getEventParticipants(@PathVariable long userId, @PathVariable long eventId) {
		return eventService.findUserEventParticipationRequests(userId, eventId);
	}

	@PatchMapping("/{eventId}/requests")
	public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable long userId,
															  @PathVariable long eventId,
															  @RequestBody EventRequestStatusUpdateRequest updateRequest) {
		return eventService.changeParticipationReqStatus(userId, eventId, updateRequest);
	}
}
