package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.RequestDto;
import ru.practicum.ewm.entities.*;
import ru.practicum.ewm.exceptions.IdIsAlreadyInUseException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
	private final RequestRepository requestRepository;
	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public List<RequestDto> getUserRequests(long userId) {
		return requestRepository.findAllByRequesterId(userId)
				.stream()
				.map(Mapper::toRequestDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public RequestDto addRequest(long userId, long eventId) {
		var requestOpt = requestRepository.findByRequesterIdAndEventEntityId(userId, eventId);

		if (requestOpt.isPresent())
			throw  new IdIsAlreadyInUseException("Pair userId and eventId have use yet");

		var eventEntity = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found", eventId));

		if (eventEntity.getInitiator().getId() == userId)
			throw new IllegalStateException("It is your event");

		if (eventEntity.getParticipantLimit() <= eventEntity.getConfirmedRequests() &&
				eventEntity.getParticipantLimit() != 0)
			throw new IllegalStateException("Participant limit exceeded");

		if (eventEntity.getState() != EventState.PUBLISHED)
			throw new IllegalStateException("Must be published");

		var userEntity = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found", userId));

		var requestEntity = new RequestEntity();
		requestEntity.setCreated(Timestamp.valueOf(LocalDateTime.now()));

		if (eventEntity.getRequestModeration() && eventEntity.getParticipantLimit() != 0) {
			requestEntity.setStatus(RequestStatus.PENDING);
		} else {
			eventEntity.setConfirmedRequests(eventEntity.getConfirmedRequests() + 1);
			eventRepository.saveAndFlush(eventEntity);
			requestEntity.setStatus(RequestStatus.CONFIRMED);
		}

		requestEntity.setRequester(userEntity);
		requestEntity.setEventEntity(eventEntity);

		requestRepository.saveAndFlush(requestEntity);
		return Mapper.toRequestDto(requestEntity);
	}

	@Transactional
	public RequestDto cancelOwnRequest(long userId, long requestId) {
		var requestEntity = requestRepository.findById(requestId)
				.orElseThrow(() -> new NotFoundException("Request not found", requestId));

		if (requestEntity.getRequester().getId() != userId)
			throw  new IllegalArgumentException("No request for participation was " +
					"found according to the specified conditions");

		requestEntity.setStatus(RequestStatus.CANCELED);

		requestRepository.saveAndFlush(requestEntity);

		return Mapper.toRequestDto(requestEntity);
	}
}
