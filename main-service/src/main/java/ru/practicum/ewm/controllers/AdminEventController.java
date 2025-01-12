package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventBase;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.GetEventsRequest;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.EventService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminEventController {
	private final EventService eventService;

	@GetMapping
	public List<? extends EventBase> getEvents(@RequestParam(required = false) List<Long> users,
											   @RequestParam(required = false) List<String> states,
											   @RequestParam(required = false) List<Long> categories,
											   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeStart,
											   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeEnd,
											   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
											   @RequestParam(defaultValue = "10") @Positive int size) {
		return eventService.find(
				GetEventsRequest.builder()
						.categories(categories)
						.initiators(users)
						.states(states)
						.dateRange(rangeStart, rangeEnd)
						.page(GetEventsRequest.Page.of(from, size))
						.shortFormat(false)
						.build()
		);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable long eventId, @RequestBody @Valid UpdateEventAdminRequest updateInfo) {
		return eventService.updateEventByAdmin(eventId, updateInfo);
	}
}
