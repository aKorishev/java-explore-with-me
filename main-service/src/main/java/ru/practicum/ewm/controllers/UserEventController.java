package ru.practicum.ewm.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping(path = "/users/{userId}/events")
public class UserEventController {
	private final EventService eventService;

	@GetMapping
	public List<EventFullDto> getEvents(@PathVariable long userId,
										 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
										 @RequestParam(defaultValue = "10") @Positive int size) {
		return eventService.findAllByInitiatorId(from, size, userId);
	}

	@GetMapping("/{eventId}")
	public EventFullDto getEvent(@PathVariable long userId, @PathVariable long eventId) {
		return eventService.findEventByInitiatorId(userId, eventId);
	}

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public EventFullDto addEvent(@PathVariable long userId, @Valid @RequestBody EventToAddDto eventDto) {
		return eventService.addEvent(eventDto, userId);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable long userId,
									@PathVariable long eventId,
									@Valid @RequestBody EventToUpdateDto request) {
		return eventService.updateByInitiator(userId, eventId, request);
	}

	@GetMapping("/{eventId}/requests")
	public List<RequestDto> getEventParticipants(@PathVariable long userId, @PathVariable long eventId) {
		return eventService.findRequestsByInitiatorId(userId, eventId);
	}

	@PatchMapping("/{eventId}/requests")
	public EventUpdateStatusResultDto changeRequestStatus(@PathVariable long userId,
														  @PathVariable long eventId,
														  @RequestBody EventUpdateStatusRequestDto updateRequest) {
		return eventService.updateRequestStatus(userId, eventId, updateRequest);
	}
}
