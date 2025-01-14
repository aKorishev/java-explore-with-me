package ru.practicum.ewm.serices;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entities.*;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.statistic.client.StatisticClient;
import ru.practicum.statistic.dto.ViewStats;
import ru.practicum.statistic.dto.ViewsStatsRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ComponentScan(basePackages = {"ru.practicum.statistic.client"})
@Slf4j
public class EventService {
	private final EventRepository eventRepo;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final ParticipationRequestRepository requestRepository;

	private final StatisticClient statisticClient;

	private final Map<String, Long> hits = new HashMap<>();


	public List<? extends EventShortDto> find(GetEventsRequest request) {

		// формируем условия выборки
		BooleanExpression conditions = makeEventsQueryConditions(request);

		// настраиваем страницу и сортировку
		GetEventsRequest.Page page = request.getPage();
		PageRequest pageRequest = PageRequest.of(page.getNumber(), page.getSize());

		// запрашиваем события из базы
		List<EventEntity> eventEntities = eventRepo.findAll(conditions, pageRequest).toList();

		// запрашиваем количество одобренных заявок на участие
		Map<Long, Long> eventToRequestsCount = getEventRequests(eventEntities);

		// запрашиваем количество просмотров каждого события у сервиса статистики
		Map<Long, Long> eventToViewsCount = getEventViews(eventEntities);

		// формируем функцию для формирования нужного ответа из собранных данных
		final Function<EventEntity, ? extends EventShortDto> mapper =
				makeSpecificMapper(eventToViewsCount, eventToRequestsCount, request.isShortFormat());

		// формируем окончательный результат
		return eventEntities.stream()
				.filter(e -> {
					// если необходимо убираем события на участие в которых уже нельзя подать заявку
					Long confirmedReqCount = eventToRequestsCount.getOrDefault(e.getId(), 0L);
					if (request.isOnlyAvailableForParticipation() && e.getParticipantLimit() > 0) {
						return e.getParticipantLimit() > confirmedReqCount;
					}
					return true;
				})
				.map(mapper)
				.sorted(EventDtoComparator.of(request.getSort()))
				.collect(Collectors.toList());
	}

	public EventFullDto findPublishedById(long id) {
		EventEntity eventEntity = eventRepo
				.findPublishedById(id)
				.orElseThrow(() -> new NotFoundException("Event", id));

		Long views = statisticClient.getStats(
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

		var req = QParticipationRequestEntity.participationRequestEntity;
		long reqCount = requestRepository.count(req.eventEntity.eq(eventEntity).and(req.status.eq(RequestStatus.CONFIRMED)));

		return Mapper.toEventFullDto(eventEntity, views, reqCount);
	}

	public void addHits(String uri) {
		if (!hits.containsKey(uri)) {
			hits.put(uri, 1L);
		}

		log.trace("Added hit for " + uri);
	}

	@Transactional
	public EventFullDto addEvent(EventToAddDto eventDto, long initiatorId) {
		if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
			throw new IllegalStateException("The date and time of the event must " +
					"be no earlier than two hours from the current time.");
		}

		UserEntity initiator = userRepository.findById(initiatorId)
				.orElseThrow(() -> new NotFoundException("User", initiatorId));

		CategoryEntity categoryEntity = categoryRepository.findById(eventDto.getCategory())
				.orElseThrow(() -> new NotFoundException("Category", eventDto.getCategory()));


		EventEntity eventEntity = eventRepo.save(Mapper.toEvent(eventDto, initiator, categoryEntity));
		return Mapper.toEventFullDto(eventEntity);
	}

	public List<ParticipationRequestDto> findUserEventParticipationRequests(long initiatorId, long eventId) {
		return requestRepository.findUserEventParticipationRequests(initiatorId, eventId)
				.stream()
				.map(Mapper::toParticipationRequestDto)
				.collect(Collectors.toList());
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
					if (entity.isPublished())
						throw new IllegalStateException("Can't update an event in state " + entity.getState().name());

					entity.setState(EventState.CANCELED);
					wasUpdated = true;
					break;
				}
				case EventToUpdateDto.StateAction.PUBLISH_EVENT: {
					if (!entity.isPending())
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
	public EventFullDto updateEventByInitiator(long initiatorId,
											   long eventId,
											   EventToUpdateDto eventToUpdateDto) {
		EventEntity entity = eventRepo.findByIdAndInitiatorId(eventId, initiatorId)
						.orElseThrow(() -> new NotFoundException("Event", eventId));

		if (!(entity.isPending() || entity.isCanceled())) {
			throw new IllegalStateException("Only pending or canceled events can be changed");
		}

		var needUpdate = checkEventUpdateAndUpdateEntity(entity, eventToUpdateDto);

		if (!needUpdate) {
			throw new IllegalArgumentException("The event update request contains no updates.");
		}

		eventRepo.saveAndFlush(entity);

		return Mapper.toEventFullDto(entity);
	}

	public List<EventShortDto> findUserEvents(long userId, int from, int size) {
		PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(desc("createdOn")));

		return eventRepo.findByInitiatorId(userId, page)
				.stream()
				.map(Mapper::toEventShortDto)
				.collect(Collectors.toList());
	}

	public EventFullDto findUserEventById(long userId, long eventId) {
		return eventRepo
				.findByIdAndInitiatorId(eventId, userId)
				.map(Mapper::toEventFullDto)
				.orElseThrow(() -> new NotFoundException("Событие", eventId));
	}

	@Transactional
	public EventFullDto updateEvent(long eventId, EventToUpdateDto updateInfo) {
		EventEntity entity = eventRepo.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event", eventId));

		var isNeedToUpdate = checkEventUpdateAndUpdateEntity(entity, updateInfo);

		if (!isNeedToUpdate) {
			throw new IllegalArgumentException("The event update request contains no updates.");
		}

		eventRepo.saveAndFlush(entity);

		return Mapper.toEventFullDto(entity);
	}

	@Transactional
	public EventUpdateStatusResultDto changeParticipationReqStatus(long userId,
																   long eventId,
																   EventUpdateStatusRequestDto updateRequest) {
        return switch (updateRequest.getStatus()) {
            case CONFIRMED -> confirmParticipationRequests(userId, eventId, updateRequest.getRequestIds());
            case REJECTED -> rejectParticipationRequests(userId, eventId, updateRequest.getRequestIds());
            default -> throw new IllegalArgumentException("The specified status is not supported(" +
                    updateRequest.getStatus() + ")");
        };
	}


	private EventUpdateStatusResultDto confirmParticipationRequests(long eventInitiatorId,
																	long eventId,
																	Collection<Long> requestIds) {
		long limit = eventRepo.findEventParticipantLimit(eventId);
		// если ограничение не задано, то подтверждение заявок не требуется
		if (limit == 0) {
			throw new IllegalStateException("Confirmation of requests for participation is not required");
		}

		int confirmed = requestRepository.countRequestsWithStatus(eventId, RequestStatus.CONFIRMED);

		// если лимит уже достигнут, то все заявки вошедшие в доступный лимит - уже подтверждены,
		// а оставшиеся - отклонены. Нет смысла что-то делать.
		if (limit <= confirmed) {
			throw new IllegalStateException("The participant limit has been reached");
		}

		List<ParticipationRequestEntity> requestsForConfirmation;
		// Если количество заявок на подтверждение больше доступного количества
		if (requestIds.size() > limit - confirmed) {
			// выгружаем только те заявки, для которых хватает лимита
			requestsForConfirmation = requestRepository.findAllById(
					requestIds.stream()
							.limit(limit - confirmed)
							.collect(Collectors.toList())
			);
		} else {
			// выгружаем все указанные заявки
			requestsForConfirmation = requestRepository.findAllById(requestIds);
		}

		boolean allValid = checkRequestsStatusUpdatePossible(eventInitiatorId, eventId, requestsForConfirmation);
		if (!allValid) {
			throw new IllegalStateException("Unable to perform this operation. Some requests for participation do " +
					"not meet the necessary conditions.");
		}

		// обходим список указанных заявок и подтверждаем их
		for (ParticipationRequestEntity request : requestsForConfirmation) {
			//подтверждаем запрос
			request.setStatus(RequestStatus.CONFIRMED);
			confirmed++;
		}

		// если был достигнут лимит, то нужно отклонить все (не только указанные) заявки ожидающие подтверждения
		List<ParticipationRequestEntity> rejectedEvents;
		if (limit <= confirmed) {
			rejectedEvents = requestRepository
					.findEventRequestsWithExclusionList(eventId, RequestStatus.PENDING, requestsForConfirmation)
					.stream()
					.peek(req -> req.setStatus(RequestStatus.REJECTED))
					.toList();
		} else {
			rejectedEvents = new ArrayList<>();
		}

		// сохраняем все обработанные заявки
		List<ParticipationRequestEntity> processedReqs = new ArrayList<>(requestsForConfirmation);
		processedReqs.addAll(rejectedEvents);
		requestRepository.saveAll(processedReqs);

		return EventUpdateStatusResultDto.of(
				requestsForConfirmation.stream().map(Mapper::toParticipationRequestDto).collect(Collectors.toList()),
				rejectedEvents.stream().map(Mapper::toParticipationRequestDto).collect(Collectors.toList())
		);
	}

	public EventUpdateStatusResultDto rejectParticipationRequests(long eventInitiatorId,
																  long eventId,
																  Collection<Long> requestIds) {
		List<ParticipationRequestEntity> requestsForRejecting = requestRepository.findAllById(requestIds);

		// проверяем, что пользователь отклоняющий запрос - это инициатор мероприятия
		// и что идентификатор мероприятия соответствует идентификатору мероприятия в запросе
		boolean allValid = checkRequestsStatusUpdatePossible(eventInitiatorId, eventId, requestsForRejecting);
		if (!allValid) {
			throw new IllegalStateException("Unable to perform this operation. Some requests for participation do " +
					"not meet the necessary conditions.");
		}

		// отклоняем указанные заявки на участие
		requestsForRejecting.forEach(request -> request.setStatus(RequestStatus.REJECTED));
		requestRepository.saveAll(requestsForRejecting);
		List<ParticipationRequestDto> resultDtos =
				requestsForRejecting.stream()
						.map(Mapper::toParticipationRequestDto)
						.collect(Collectors.toList());
		return EventUpdateStatusResultDto.rejectedOnly(resultDtos);
	}

	private static BooleanExpression makeEventsQueryConditions(GetEventsRequest request) {
		var event = QEventEntity.eventEntity;

		List<BooleanExpression> conditions = new ArrayList<>();

		// если передан текст, ищем его в аннотации, описании и заголовке
		if (request.hasTextCondition()) {
			String textToSearch = request.getText();
			conditions.add(
					event.title.containsIgnoreCase(textToSearch)
							.or(event.annotation.containsIgnoreCase(textToSearch))
							.or(event.description.containsIgnoreCase(textToSearch))
			);
		}

		// если указан список категорий, фильтруем по нему
		if (request.hasCategoriesCondition()) {
			conditions.add(
					event.categoryEntity.id.in(request.getCategories())
			);
		}

		// если указан список инициаторов события, фильтруем по нему
		if (request.hasInitiatorsCondition()) {
			conditions.add(
					event.initiator.id.in(request.getInitiators())
			);
		}

		// если указан флаг - искать платные или бесплатные события, то фильтруем и по этому признаку
		if (request.hasPaidCondition()) {
			conditions.add(
					event.paid.eq(request.getPaid())
			);
		}

		// если указана локация в радиусе которой должно происходить событие, то учитываем этот параметр
		// если радиус не указан, но указаны широта и долгота, ищем ивенты с такими же данными
		if (request.hasLocationCondition()) {
			GetEventsRequest.Location location = request.getLocation();
			if (request.getLocation().getRadius() > 0) {
				conditions.add(
						Expressions.booleanTemplate("distance({0},{1},{2},{3}) <= {4}",
								event.latitude, event.longitude,
								location.getLat(), location.getLon(), location.getRadius())
				);
			} else {
				conditions.add(event.latitude.eq(location.getLat()).and(event.longitude.eq(location.getLon())));
			}
		}

		// если указан диапазон дат начала события, то учитываем этот диапазон
		// если не указан, то возвращаем только события, которые произойдут в будущем
		LocalDateTime rangeStart = request.getRangeStart() != null ? request.getRangeStart() : LocalDateTime.now();
		conditions.add(
				event.eventDate.goe(rangeStart)
		);

		if (request.getRangeEnd() != null) {
			conditions.add(
					event.eventDate.loe(request.getRangeEnd())
			);
		}

		// выгружаем события в указанном состоянии
		if (request.hasStates()) {
			conditions.add(QEventEntity.eventEntity.state.in(request.getStates()));
		}

		return conditions
				.stream()
				.reduce(BooleanExpression::and)
				.get();
	}

	private Map<Long, Long> getEventRequests(Collection<EventEntity> eventEntities) {
		var req = QParticipationRequestEntity.participationRequestEntity;

		BooleanExpression condition = req.status.eq(RequestStatus.CONFIRMED)
				.and(req.eventEntity.in(eventEntities));

		Iterable<ParticipationRequestEntity> reqs = requestRepository.findAll(condition);
		return StreamSupport
				.stream(reqs.spliterator(), false)
				.collect(Collectors.groupingBy(r -> r.getEventEntity().getId(), Collectors.counting()));
	}

	private Map<Long, Long> getEventViews(Collection<EventEntity> eventEntities) {
		Map<String, Long> eventUriAndIdMap = eventEntities.stream()
				.map(EventEntity::getId)
				.collect(Collectors.toMap(id -> "/events/" + id, Function.identity()));

		List<ViewStats> stats = statisticClient.getStats(
				ViewsStatsRequest.builder()
						.uris(eventUriAndIdMap.keySet())
						.unique(true)
						.build()
		);

		return stats.stream()
				.collect(Collectors.toMap(
						stat -> eventUriAndIdMap.get(stat.getUri()),
						ViewStats::getHits
				));
	}

	private Function<EventEntity, ? extends EventShortDto> makeSpecificMapper(Map<Long, Long> eventToViewsCount,
																			  Map<Long, Long> eventToRequestsCount,
																			  boolean isShortFormat) {
		if (isShortFormat) {
			// определяем функцию мэппинга
			return event -> Mapper.toEventShortDto(
					event,
					eventToViewsCount.getOrDefault(event.getId(), 0L),
					eventToRequestsCount.getOrDefault(event.getId(), 0L)
			);
		} else {
			// определяем функцию мэппинга
			return event -> Mapper.toEventFullDto(
					event,
					eventToViewsCount.getOrDefault(event.getId(), 0L),
					eventToRequestsCount.getOrDefault(event.getId(), 0L)
			);
		}
	}

	private static boolean checkRequestsStatusUpdatePossible(long initiatorId,
															 long eventId,
															 List<ParticipationRequestEntity> requests) {
		// проверяем, что пользователь подтверждающий запрос - это инициатор мероприятия
		// и что идентификатор мероприятия соответствует идентификатору мероприятия в заявке,
		// а также что запрос находится в состоянии PENDING
		Predicate<ParticipationRequestEntity> validationPredicate = request ->
				request.isDataMatchRequest(eventId, initiatorId)
						&& request.getStatus().equals(RequestStatus.PENDING);

		return requests.stream().allMatch(validationPredicate);
	}

	@RequiredArgsConstructor(staticName = "of")
	private static class EventDtoComparator<T extends EventShortDto> implements Comparator<T> {
		private final GetEventsRequest.Sort sort;

		@Override
		public int compare(T event1, T event2) {
            return switch (sort) {
                case VIEWS -> Long.compare(event1.getViews(), event2.getViews());
                default -> event1.getEventDate().compareTo(event2.getEventDate());
            };
		}
	}
}
