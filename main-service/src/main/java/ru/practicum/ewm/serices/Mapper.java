package ru.practicum.ewm.serices;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.entities.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mapper {

	public static EventEntity toEvent(EventToAddDto dto, UserEntity initiator, CategoryEntity categoryEntity) {
		return EventEntity.builder()
				.title(dto.getTitle())
				.annotation(dto.getAnnotation())
				.description(dto.getDescription())
				.categoryEntity(categoryEntity)
				.participantLimit(dto.getParticipantLimit())
				.paid(dto.isPaid())
				.eventDate(dto.getEventDate())
				.initiator(initiator)
				.latitude(dto.getLocation().getLat())
				.longitude(dto.getLocation().getLon())
				.requestModeration(dto.isRequestModeration())
				.build();
	}

	public static EventFullDto toEventFullDto(EventEntity eventEntity) {
		return toEventFullDto(eventEntity, null, null).toBuilder()
				.views(eventEntity.getViews())
				.confirmedRequests(eventEntity.getConfirmedRequests())
				.build();
	}

	public static EventFullDto toEventFullDto(EventEntity eventEntity, Long views, Long confirmedRequests) {
		return EventFullDto.builder()
				.id(eventEntity.getId())
				.title(eventEntity.getTitle())
				.annotation(eventEntity.getAnnotation())
				.description(eventEntity.getDescription())
				.category(toCategoryDto(eventEntity.getCategoryEntity()))
				.participantLimit(eventEntity.getParticipantLimit())
				.state(eventEntity.getState().name())
				.paid(eventEntity.getPaid())
				.eventDate(eventEntity.getEventDate())
				.createdOn(eventEntity.getCreatedOn())
				.publishedOn(eventEntity.getPublishedOn())
				.initiator(Mapper.toUserShortDto(eventEntity.getInitiator()))
				.location(new Location(eventEntity.getLatitude(), eventEntity.getLongitude()))
				.requestModeration(eventEntity.getRequestModeration())
				.views(views)
				.confirmedRequests(confirmedRequests)
				.build();
	}

	public static EventShortDto toEventShortDto(EventEntity eventEntity) {
		return toEventShortDto(eventEntity, null, null);
	}

	public static EventShortDto toEventShortDto(EventEntity eventEntity, Long viewsCount, Long confirmedReqsCount) {
		return EventShortDto.builder()
				.id(eventEntity.getId())
				.title(eventEntity.getTitle())
				.annotation(eventEntity.getAnnotation())
				.category(toCategoryDto(eventEntity.getCategoryEntity()))
				.paid(eventEntity.getPaid())
				.eventDate(eventEntity.getEventDate())
				.initiator(Mapper.toUserShortDto(eventEntity.getInitiator()))
				.views(viewsCount)
				.confirmedRequests(confirmedReqsCount)
				.build();
	}

	public static UserShortDto toUserShortDto(UserEntity userEntity) {
		return UserShortDto.of(userEntity.getId(), userEntity.getName());
	}

	public static RequestDto toRequestDto(RequestEntity request) {
		var localDateTime = request.getCreated().toLocalDateTime();
		localDateTime = LocalDateTime.of(
				localDateTime.getYear(),
				localDateTime.getMonth(),
				localDateTime.getDayOfMonth(),
				localDateTime.getHour(),
				localDateTime.getMinute(),
				localDateTime.getSecond());

		return new RequestDto(
				request.getId(),
				request.getRequester().getId(),
				request.getEventEntity().getId(),
				request.getStatus().name(),
				localDateTime
		);
	}

	public static RequestEntity toNewParticipationRequest(UserEntity participant, EventEntity eventEntity) {
		RequestEntity request = new RequestEntity();
		request.setRequester(participant);
		request.setEventEntity(eventEntity);
		// если для события отключена пре-модерация заявок на участие,
		// то запрос на участие считается подтвержденным автоматически
		if (!eventEntity.getRequestModeration()) {
			request.setStatus(RequestStatus.CONFIRMED);
		}
		return request;
	}

	public static CompilationDto toCompilationDto(CompilationEntity compilationEntity) {
		return new CompilationDto(compilationEntity.getId(),
				compilationEntity.getTitle(),
				compilationEntity.isPinned(),
				compilationEntity
						.getEventEntities()
						.stream()
						.map(Mapper::toEventShortDto)
						.collect(Collectors.toSet()));
	}

	public static UserDto toUserDto(UserEntity userEntity) {
		return UserDto.of(userEntity.getId(), userEntity.getName(), userEntity.getEmail());
	}

	public static UserEntity toNewUser(UserToAddDto dto) {
		var entity = new UserEntity();
		entity.setEmail(dto.getEmail());
		entity.setName(dto.getName());

		return entity;
	}

	public static CompilationEntity toNewCompilation(CompilationToAddDto dto, Collection<EventEntity> eventEntities) {
		CompilationEntity compilationEntity = new CompilationEntity();
		compilationEntity.setTitle(dto.getTitle());
		compilationEntity.setPinned(dto.isPinned());
		compilationEntity.setEventEntities(new HashSet<>(eventEntities));
		return compilationEntity;
	}

	public static CategoryDto toCategoryDto(CategoryEntity categoryEntity) {
		return new CategoryDto(categoryEntity.getId(), categoryEntity.getName());
	}

    public static CategoryEntity toNewCategory(CategoryToAddDto categoryDto) {
		CategoryEntity categoryEntity = new CategoryEntity();
		categoryEntity.setName(categoryDto.getName());
		return categoryEntity;
    }
}
