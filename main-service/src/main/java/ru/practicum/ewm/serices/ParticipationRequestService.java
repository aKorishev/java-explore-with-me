package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.entities.EventEntity;
import ru.practicum.ewm.entities.ParticipationRequestEntity;
import ru.practicum.ewm.entities.RequestStatus;
import ru.practicum.ewm.entities.UserEntity;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {
	private final ParticipationRequestRepository requestRepository;
	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public List<ParticipationRequestDto> getUserRequests(long userId) {
		return requestRepository.findByRequesterId(userId)
				.stream()
				.map(Mapper::toParticipationRequestDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public ParticipationRequestDto addParticipationRequest(long participantId, long eventId) {
		// если запрос уже существует, то не создаем новый
		Optional<ParticipationRequestEntity> optRequest =
				requestRepository.findByRequesterIdAndId(participantId, eventId);

		if (optRequest.isPresent()) {
			//noinspection OptionalGetWithoutIsPresent
			return optRequest.map(Mapper::toParticipationRequestDto).get();
		}

		// находим указанное событие
		EventEntity eventEntity = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event", eventId));

		// если запрос на участие отправил инициатор события, то возвращаем ошибку
		if (eventEntity.getInitiator().getId().equals(participantId)) {
			throw new IllegalStateException("The initiator of the event is already a participant.");
		}

		// если событие не опубликовано, то в нем нельзя участвовать
		if (!eventEntity.isPublished()) {
			throw new IllegalStateException("Event not published");
		}

		// получаем количество подтвержденных заявок на участие и сравниваем его с лимитом
		// если лимит достигнут возвращаем ошибку
		int participantCount = requestRepository.countRequestsWithStatus(eventId, RequestStatus.CONFIRMED);

		if (eventEntity.getParticipantLimit() > 0 && eventEntity.getParticipantLimit() <= participantCount) {
			throw new IllegalStateException("The participant limit has been reached");
		}

		// находим пользователя отправившего заявку и создаём новый запрос на участие
		UserEntity participant = userRepository.findById(participantId)
				.orElseThrow(() -> new NotFoundException("User", participantId));

		ParticipationRequestEntity request = Mapper.toNewParticipationRequest(participant, eventEntity);
		if (eventEntity.getParticipantLimit() == 0) {
			request.setStatus(RequestStatus.CONFIRMED);
		}
		requestRepository.saveAndFlush(request);

		return Mapper.toParticipationRequestDto(request);
	}

	public ParticipationRequestDto cancelOwnRequest(long requesterId, long requestId) {
		ParticipationRequestEntity request = requestRepository.findByRequesterIdAndId(requesterId, requestId)
				.orElseThrow(() -> new IllegalArgumentException("No request for participation was " +
						"found according to the specified conditions"));
		request.setStatus(RequestStatus.CANCELED);

		return Mapper.toParticipationRequestDto(request);
	}
}
