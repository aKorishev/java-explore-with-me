package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventToUpdateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.EventService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {
	private final EventService eventService;

	@GetMapping
	public List<? extends EventShortDto> getEvents(@RequestParam(required = false) List<Long> users,
												   @RequestParam(required = false) List<String> states,
												   @RequestParam(required = false) List<Long> categories,
												   @RequestParam(required = false) String rangeStart,
												   @RequestParam(required = false) String rangeEnd,
												   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
												   @RequestParam(defaultValue = "10") @Positive int size) {
		return eventService.searchAdmin(
				users,
				states,
				categories,
				rangeStart,
				rangeEnd,
				from,
				size
		);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable long eventId, @RequestBody @Valid EventToUpdateDto updateInfo) {
		return eventService.updateByAdmin(eventId, updateInfo);
	}
}
