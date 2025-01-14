package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationToAddDto;
import ru.practicum.ewm.dto.CompilationToUpdateDto;
import ru.practicum.ewm.entities.CompilationEntity;
import ru.practicum.ewm.entities.EventEntity;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
	private final CompilationRepository repository;
	private final EventRepository eventRepository;

	public List<CompilationDto> getAll(int from, int size) {
		return repository.findAll(page(from, size))
				.stream()
				.map(Mapper::toCompilationDto)
				.collect(Collectors.toList());
	}

	public List<CompilationDto> getByPinFlag(boolean pinned, int from, int size) {
		return repository.findByPinned(pinned, page(from, size))
				.stream()
				.map(Mapper::toCompilationDto)
				.collect(Collectors.toList());
	}

	public CompilationDto getById(long compId) {
		return repository
				.findById(compId)
				.map(Mapper::toCompilationDto)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));
	}

	@Transactional
	public CompilationDto save(CompilationToAddDto compilationDto) {
		List<EventEntity> eventEntities;
		if (compilationDto.getEvents() == null || compilationDto.getEvents().isEmpty()) {
			eventEntities = new ArrayList<>();
		} else {
			eventEntities = eventRepository.findAllById(compilationDto.getEvents());
		}
		CompilationEntity comp = repository.save(Mapper.toNewCompilation(compilationDto, eventEntities));
		return Mapper.toCompilationDto(comp);
	}

	@Transactional
	public void addEvents(long compId, Collection<Long> eventIds) {
		CompilationEntity compilationEntity = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));

		List<EventEntity> eventEntities = eventRepository.findAllById(eventIds);

		compilationEntity.getEventEntities().addAll(eventEntities);

		repository.save(compilationEntity);
	}

	@Transactional
	public void removeEvents(long compId, Set<Long> eventIds) {
		CompilationEntity compilationEntity = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));

		compilationEntity.getEventEntities().removeIf(event -> eventIds.contains(event.getId()));
		repository.save(compilationEntity);
	}

	@Transactional
	public void delete(long compId) {
		if (repository.existsById(compId)) {
			repository.deleteById(compId);
		} else {
			throw new NotFoundException("Подборка", compId);
		}
	}

	@Transactional
	public CompilationDto update(long compId, CompilationToUpdateDto updateRequest) {

		if (!updateRequest.isNeedAnyUpdates()) {
			throw new IllegalArgumentException("The compilation update request contains no updates.");
		}

		CompilationEntity compilationEntity = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));
		if (updateRequest.isTitleNeedUpdate()) {
			compilationEntity.setTitle(updateRequest.getTitle());
		}
		if (updateRequest.isPinnedFlagNeedUpdate()) {
			compilationEntity.setPinned(updateRequest.getPinned());
		}

		if (updateRequest.isEventListNeedUpdate()) {
			List<EventEntity> eventEntities = eventRepository.findAllById(updateRequest.getEvents());
			compilationEntity.setEventEntities(new HashSet<>(eventEntities));
		}

		repository.save(compilationEntity);

		return Mapper.toCompilationDto(compilationEntity);
	}

	private static PageRequest page(int from, int size) {
		return PageRequest.of(from > 0 ? from / size : 0, size);
	}
}
