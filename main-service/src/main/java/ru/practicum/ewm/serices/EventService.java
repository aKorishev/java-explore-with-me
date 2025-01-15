package ru.practicum.ewm.serices;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entities.EventEntity;
import ru.practicum.ewm.entities.EventState;
import ru.practicum.ewm.entities.RequestEntity;
import ru.practicum.ewm.entities.RequestStatus;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.NotValidException;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.statistic.client.StatisticClient;
import ru.practicum.statistic.dto.ViewStats;
import ru.practicum.statistic.dto.ViewsStatsRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ComponentScan("ru.practicum.statistic.client")
public class EventService {

	private final EventRepository eventRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final RequestRepository requestRepository;
	private final StatisticClient statClient;

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Transactional
	public EventFullDto addEvent(EventToAddDto eventToAddDto, long userId) {
		if (eventToAddDto.getEventDate().isBefore(LocalDateTime.now()) ||
				eventToAddDto.getEventDate().equals(LocalDateTime.now())) {
			throw new IllegalStateException("Date of event cannot be in the past");
		}

		var userEntity = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User", userId));

		var categoryEntity = categoryRepository.findById(eventToAddDto.getCategory())
				.orElseThrow(() -> new NotFoundException("Category", eventToAddDto.getCategory()));

		var eventEntity = Mapper.toEvent(eventToAddDto, userEntity, categoryEntity);

		eventEntity.setCreatedOn(LocalDateTime.now());
		eventEntity.setPublishedOn(LocalDateTime.now());
		eventEntity.setState(EventState.PENDING);

		eventRepository.saveAndFlush(eventEntity);

		return Mapper.toEventFullDto(eventEntity);
	}

	@Transactional
	public List<EventFullDto> findAllByInitiatorId(int from, int size, long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User not found", userId);
		}

		return eventRepository.findAllByInitiatorId(userId).stream()
				.map(Mapper::toEventFullDto)
				.skip(from)
				.limit(size)
				.collect(Collectors.toList());
	}

	@Transactional
	public EventFullDto findEventByInitiatorId(long userId, long eventId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User not found", userId);
		}
		var eventEntity = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found", eventId));

		if (eventEntity.getInitiator() == null || eventEntity.getInitiator().getId() != userId) {
			throw new NotValidException("Event's initiator is not equal to userId of request");
		}

		return Mapper.toEventFullDto(eventEntity);
	}

	public boolean checkEventUpdateAndUpdateEntity(EventEntity entity,
												   EventToUpdateDto eventToUpdateDto) {
		var wasUpdated = false;

		Predicate<String> predicateString = s -> s != null && !s.isBlank();

		if (eventToUpdateDto.eventDate() != null) {
			var eventDate = eventToUpdateDto.eventDate();

			if (eventDate.isBefore(LocalDateTime.now().plusHours(2)))
				throw new IllegalStateException("The date and time of the event must " +
						"be no earlier than two hours from the current time.");

			entity.setEventDate(eventDate);

			wasUpdated = true;
		}

		wasUpdated = updateEventValue(eventToUpdateDto.title(), entity::setTitle, predicateString) || wasUpdated;
		wasUpdated = updateEventValue(eventToUpdateDto.annotation(), entity::setAnnotation, predicateString) || wasUpdated;
		wasUpdated = updateEventValue(eventToUpdateDto.description(), entity::setDescription, predicateString) || wasUpdated;
		wasUpdated = updateEventValue(eventToUpdateDto.participantLimit(), entity::setParticipantLimit, Objects::nonNull) || wasUpdated;
		wasUpdated = updateEventValue(eventToUpdateDto.paid(), entity::setPaid, Objects::nonNull) || wasUpdated;
		wasUpdated = updateEventValue(eventToUpdateDto.requestModeration(), entity::setRequestModeration, Objects::nonNull) || wasUpdated;

		if (eventToUpdateDto.stateAction() != null) {
			switch (eventToUpdateDto.stateAction()) {
				case EventToUpdateDto.StateAction.CANCEL_REVIEW: {
					entity.setState(EventState.CANCELED);
					wasUpdated = true;
					break;
				}
				case EventToUpdateDto.StateAction.SEND_TO_REVIEW: {
					entity.setState(EventState.PENDING);
					wasUpdated = true;
					break;
				}
				case EventToUpdateDto.StateAction.REJECT_EVENT: {
					if (entity.getState() == EventState.PUBLISHED)
						throw new IllegalStateException("Can't update an event in state " + entity.getState().name());

					entity.setState(EventState.CANCELED);
					wasUpdated = true;
					break;
				}
				case EventToUpdateDto.StateAction.PUBLISH_EVENT: {
					if (entity.getState() != EventState.PENDING)
						throw new IllegalStateException("Can't publish the event because it's not in the right state: " +
								entity.getState());

					// нельзя публиковать событие, которое начнется раньше чем через час от текущего момента
					LocalDateTime oneHourLimit = LocalDateTime.now().plusHours(1);
					if (entity.getEventDate().isBefore(oneHourLimit)) {
						throw new IllegalStateException("The date and time of the event must be " +
								"no earlier than one hour from the current moment.");
					}
					entity.setState(EventState.PUBLISHED);
					entity.setPublishedOn(LocalDateTime.now());

					wasUpdated = true;
					break;
				}
			}
		}

		if (eventToUpdateDto.category() != null) {
			var catId = eventToUpdateDto.category();
			var category = categoryRepository.findById(catId)
					.orElseThrow(() -> new NotFoundException("Category", catId));
			entity.setCategoryEntity(category);

			wasUpdated = true;
		}

		if (eventToUpdateDto.location() != null) {
			var location = eventToUpdateDto.location();

			entity.setLatitude(location.getLat());
			entity.setLongitude(location.getLon());

			wasUpdated = true;
		}

		return wasUpdated;
	}

	public <T> boolean updateEventValue(T value, Consumer<T> consumer, Predicate<T> predicate) {
		if (!predicate.test(value))
			return false;

		consumer.accept(value);

		return true;
	}

	@Transactional
	public EventFullDto updateByInitiator(long userId, long eventId, EventToUpdateDto eventToUpdateDto) {
		var eventEntity = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event", eventId));

		if (!(eventEntity.getState() == EventState.PENDING || eventEntity.getState() == EventState.CANCELED)) {
			throw new IllegalStateException("Only pending or canceled events can be changed");
		}

		var isNeedToUpdate = checkEventUpdateAndUpdateEntity(eventEntity, eventToUpdateDto);

		if (!isNeedToUpdate) {
			throw new IllegalArgumentException("The event update request contains no updates.");
		}

		eventRepository.saveAndFlush(eventEntity);

		return Mapper.toEventFullDto(eventEntity);
	}

	@Transactional
	public EventFullDto updateByAdmin(long eventId, EventToUpdateDto eventToUpdateDto) {
		var eventEntity = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event", eventId));

		var isNeedToUpdate = checkEventUpdateAndUpdateEntity(eventEntity, eventToUpdateDto);

		if (!isNeedToUpdate) {
			throw new IllegalArgumentException("The event update request contains no updates.");
		}

		eventRepository.saveAndFlush(eventEntity);

		return Mapper.toEventFullDto(eventEntity);
	}

	@Transactional
	public List<EventFullDto> searchAdmin(List<Long> users,
										  List<String> states,
										  List<Long> categories,
										  String rangeStart,
										  String rangeEnd,
										  int from,
										  int size) {
		LocalDateTime startDateTime = rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null;
		LocalDateTime endDateTime = rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null;
		if (endDateTime != null && startDateTime != null) {
			if (startDateTime.isAfter(endDateTime)) {
				throw new NotValidException("Start time must be before end time");
			}
		}
		if (users != null && categories != null) {
			return eventRepository.findAllByInitiatorIdInAndCategoryEntityIdIn(users, categories)
					.stream()
					.filter(e -> ((states != null) ? states.contains(e.getState().toString()) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.map(Mapper::toEventFullDto)
					.toList();
		} else if (users == null && categories != null) {
			return eventRepository.findAllByCategoryEntityIdIn(categories)
					.stream()
					.filter(e -> ((states != null) ? states.contains(e.getState().toString()) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.map(Mapper::toEventFullDto)
					.toList();
		} else if (users != null && categories == null) {
			return eventRepository.findAllByInitiatorIdIn(users)
					.stream()
					.filter(e -> ((states != null) ? states.contains(e.getState().toString()) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.map(Mapper::toEventFullDto)
					.toList();
		} else {
			return eventRepository.findAll()
					.stream()
					.filter(e -> ((states != null) ? states.contains(e.getState().toString()) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.map(Mapper::toEventFullDto)
					.toList();
		}
	}

	@Transactional
	public List<EventFullDto> searchPublic(String text,
										   List<Long> categories,
										   Boolean paid,
										   String rangeStart,
										   String rangeEnd,
										   Boolean onlyAvailable,
										   String sort,
										   int from,
										   int size,
										   HttpServletRequest request) {
		LocalDateTime startDateTime = rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null;
		LocalDateTime endDateTime = rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null;
		if (endDateTime != null && startDateTime != null) {
			if (startDateTime.isAfter(endDateTime)) {
				throw new IllegalArgumentException("Start time must be before end time");
			}
		}
		if (categories != null) {
			List<EventEntity> eventFullDtoList = eventRepository.findAllByCategoryEntityIdIn(categories)
					.stream()
					.filter(e -> e.getState().equals(EventState.PUBLISHED)
							&&
							((text != null) ? (e.getAnnotation().toLowerCase().contains(text.toLowerCase()) ||
									e.getDescription().toLowerCase().contains(text.toLowerCase())) : true)
							&&
							((paid != null) ? e.getPaid().equals(paid) : true)
							&&
							((onlyAvailable != null) ? (onlyAvailable ? e.getParticipantLimit() >= e.getConfirmedRequests() : true) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.sorted((sort != null && sort.equals("EVENT_DATE")) ? Comparator.comparing(EventEntity::getEventDate).reversed() :
							(sort != null && sort.equals("VIEWS")) ? Comparator.comparing(EventEntity::getViews).reversed() :
									Comparator.comparing(EventEntity::getEventDate))
					.toList();

			for (var e : eventFullDtoList) {
				e.setViews(e.getViews() + 1);
				eventRepository.save(e);
			}

			return eventFullDtoList.stream()
					.map(Mapper::toEventFullDto)
					.toList();
		} else {
			List<EventEntity> eventFullDtoList = eventRepository.findAll()
					.stream()
					.filter(e -> e.getState().equals(EventState.PUBLISHED)
							&&
							((text != null) ? (e.getAnnotation().toLowerCase().contains(text.toLowerCase()) ||
									e.getDescription().toLowerCase().contains(text.toLowerCase())) : true)
							&&
							((paid != null) ? e.getPaid().equals(paid) : true)
							&&
							((onlyAvailable != null) ? (onlyAvailable ? e.getParticipantLimit() >= e.getConfirmedRequests() : true) : true)
							&&
							((startDateTime != null && endDateTime != null)
									? e.getEventDate().isAfter(startDateTime) && e.getEventDate().isBefore(endDateTime)
									: e.getEventDate().isAfter(LocalDateTime.now()))
					)
					.skip(from)
					.limit(size)
					.sorted((sort != null && sort.equals("EVENT_DATE")) ? Comparator.comparing(EventEntity::getEventDate).reversed() :
							(sort != null && sort.equals("VIEWS")) ? Comparator.comparing(EventEntity::getViews).reversed() :
									Comparator.comparing(EventEntity::getEventDate))
					.toList();

			for (var e : eventFullDtoList) {
				e.setViews(e.getViews() + 1);
				eventRepository.save(e);
			}

			return eventFullDtoList.stream()
					.map(Mapper::toEventFullDto)
					.toList();
		}
	}

	@Transactional
	public EventFullDto findByIdPublic(long id, HttpServletRequest request) {
		var eventEntity = eventRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Event", id));

		if (eventEntity.getState() != EventState.PUBLISHED)
			throw new NotFoundException("Published event", id);

		Long views = statClient.getStats(
						ViewsStatsRequest.builder()
								.uri("/events/" + id)
								.start(eventEntity.getPublishedOn())
								.end(LocalDateTime.now())
								.unique(true)
								.build()
				)
				.stream()
				.findAny()
				.map(ViewStats::getHits)
				.orElse(0L);

		log.trace("Getted vievs for " + "/events/" + id + " is " + views);

		eventEntity.setViews(views);

		return Mapper.toEventFullDto(eventEntity);
	}

	@Transactional
	public List<RequestDto> findRequestsByInitiatorId(long userId, long eventId) {
		return requestRepository.findAllByEventEntityId(eventId).stream()
				.map(Mapper::toRequestDto)
				.toList();
	}

	@Transactional
	public EventUpdateStatusResultDto updateRequestStatus(long userId, long eventId,
														  EventUpdateStatusRequestDto eventRequestStatusUpdateRequest) {

		if (eventRepository.existsById(eventId)) {
			var event = eventRepository.findById(eventId)
					.orElseThrow(() -> new NotFoundException("Event not found", eventId));

			if (event.getParticipantLimit() > event.getConfirmedRequests()) {
				List<RequestEntity> requests = requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());
				List<RequestEntity> rejectedRequests = new ArrayList<>();
				List<RequestEntity> acceptedRequests = new ArrayList<>();
				for (RequestEntity request : requests) {
					if (request.getStatus().equals(RequestStatus.PENDING.PENDING)) {
						if (eventRequestStatusUpdateRequest.getStatus().equals(EventUpdateStatusRequestDto.Status.CONFIRMED)) {
							if (event.getParticipantLimit() > event.getConfirmedRequests()) {
								request.setStatus(RequestStatus.CONFIRMED);
								acceptedRequests.add(request);
								requestRepository.save(request);
								event.setConfirmedRequests(event.getConfirmedRequests() + 1);
								eventRepository.save(event);
							} else {
								request.setStatus(RequestStatus.REJECTED);
								rejectedRequests.add(request);
								requestRepository.save(request);
							}

						} else {
							request.setStatus(RequestStatus.REJECTED);
							rejectedRequests.add(request);
							requestRepository.save(request);
						}

					} else {
						throw new IllegalStateException("Request status must be PENDING");
					}
				}

				var updateResult = new EventUpdateStatusResultDto();
				updateResult.setConfirmedRequests(acceptedRequests.stream()
						.map(Mapper::toRequestDto)
						.toList());
				updateResult.setRejectedRequests(rejectedRequests.stream()
						.map(Mapper::toRequestDto)
						.toList());
				return updateResult;

			} else {
				throw new IllegalStateException("Participant limit exceeded");
			}
		} else {
			throw new NotFoundException("Event not found", eventId);
		}
	}

	private EventFullDto getEventFullDto(EventEntity event,
										 int participantLimit,
										 String eventDate,
										 Location location,
										 String description,
										 String annotation,
										 String title,
										 long categoryId,
										 Boolean paid,
										 Boolean requestModeration) {
		if (categoryId != 0) {
			var catEntity = categoryRepository.findById(categoryId)
					.orElseThrow(() -> new NotFoundException("Category not found", categoryId));
			event.setCategoryEntity(catEntity);
		}

		if (participantLimit != 0) {
			event.setParticipantLimit(participantLimit);
		}
		if (eventDate != null) {
			event.setEventDate(LocalDateTime.parse(eventDate, formatter));
		}
		if (location != null) {
			event.setLatitude(location.getLat());
			event.setLongitude(location.getLon());
		}
		if (description != null) {
			event.setDescription(description);
		}
		if (annotation != null) {
			event.setAnnotation(annotation);
		}
		if (title != null) {
			event.setTitle(title);
		}
		if (paid != null) {
			event.setPaid(paid);
		}
		if (requestModeration != null) {
			event.setRequestModeration(requestModeration);
		}
		eventRepository.saveAndFlush(event);
		return Mapper.toEventFullDto(event);
	}
}
