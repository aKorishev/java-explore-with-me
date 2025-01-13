package ru.practicum.ewm.controllers;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.entities.EventState;
import ru.practicum.ewm.serices.EventService;
import ru.practicum.statistic.client.StatisticClient;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventController {
	private final EventService eventService;
	private final StatisticClient statisticClient;

	@GetMapping
	public List<? extends EventBase> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
											   @RequestParam(required = false) List<Long> categories,
											   @RequestParam(required = false) Boolean paid,
											   @RequestParam(defaultValue = "0") long lat,
											   @RequestParam(defaultValue = "0") long lon,
											   @RequestParam(defaultValue = "0") @PositiveOrZero short radius,
											   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeStart,
											   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeEnd,
											   @RequestParam(defaultValue = "false") boolean onlyAvailable,
											   @RequestParam(defaultValue = "EVENT_DATE") String sort,
											   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
											   @RequestParam(defaultValue = "10") @Positive int size,
											   HttpServletRequest httpRequest) {

		List<? extends EventBase> result = eventService.find(
				GetEventsRequest.builder()
						.state(EventState.PUBLISHED)
						.text(text)
						.categories(categories)
						.paid(paid)
						.location(GetEventsRequest.Location.of(lat, lon, radius))
						.dateRange(rangeStart, rangeEnd)
						.onlyAvailableForParticipation(onlyAvailable)
						.page(GetEventsRequest.Page.of(from, size))
						.shortFormat(true)
						.sort(sort)
						.build()
		);

		statisticClient.hit(httpRequest);

		return result;
	}



	@GetMapping("/{id}")
	public EventFullDto getEvent(@PathVariable long id, HttpServletRequest request) {
		EventFullDto result = eventService.findPublishedById(id);
		statisticClient.hit(request);
		eventService.addHits("/events/" + id);
		return result;
	}
}