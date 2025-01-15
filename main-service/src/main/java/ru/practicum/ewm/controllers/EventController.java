package ru.practicum.ewm.controllers;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.EventService;
import ru.practicum.statistic.client.StatisticClient;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/events")
public class EventController {
	private final EventService eventService;
	private final StatisticClient statisticClient;

	@GetMapping
	public List<? extends EventShortDto> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
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

		List<? extends EventShortDto> result = eventService.searchPublic(
				text,
				categories,
				paid,
				rangeStart,
				rangeEnd,
				onlyAvailable,
				sort,
				from,
				size,
				httpRequest
		);

		statisticClient.hit(httpRequest);

		return result;
	}



	@GetMapping("/{id}")
	public EventFullDto getEvent(@PathVariable long id, HttpServletRequest request) {
		EventFullDto result = eventService.findByIdPublic(id, request);
		statisticClient.hit(request);
		return result;
	}
}