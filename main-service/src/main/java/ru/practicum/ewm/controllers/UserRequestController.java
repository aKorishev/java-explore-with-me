package ru.practicum.ewm.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.RequestDto;
import ru.practicum.ewm.serices.RequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class UserRequestController {
	private final RequestService requestService;

	@GetMapping
	public List<RequestDto> getUserRequests(@PathVariable long userId) {
		return requestService.getUserRequests(userId);
	}

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public RequestDto addParticipationRequest(@PathVariable long userId, @RequestParam long eventId) {
		return requestService.addRequest(userId, eventId);
	}

	@PatchMapping("/{requestId}/cancel")
	public RequestDto cancelRequest(@PathVariable long userId, @PathVariable long requestId) {
		return requestService.cancelOwnRequest(userId, requestId);
	}
}
