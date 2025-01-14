package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationToAddDto;
import ru.practicum.ewm.dto.CompilationToUpdateDto;
import ru.practicum.ewm.entities.Compilation;
import ru.practicum.ewm.entities.Event;
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
		List<Event> events;
		if (compilationDto.getEvents() == null || compilationDto.getEvents().isEmpty()) {
			events = new ArrayList<>();
		} else {
			events = eventRepository.findAllById(compilationDto.getEvents());
		}
		Compilation comp = repository.save(Mapper.toNewCompilation(compilationDto, events));
		return Mapper.toCompilationDto(comp);
	}

	@Transactional
	public void addEvents(long compId, Collection<Long> eventIds) {
		Compilation compilation = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));

		List<Event> events = eventRepository.findAllById(eventIds);

		compilation.getEvents().addAll(events);

		repository.save(compilation);
	}

	@Transactional
	public void removeEvents(long compId, Set<Long> eventIds) {
		Compilation compilation = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));

		compilation.getEvents().removeIf(event -> eventIds.contains(event.getId()));
		repository.save(compilation);
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

		Compilation compilation = repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка", compId));
		if (updateRequest.isTitleNeedUpdate()) {
			compilation.setTitle(updateRequest.getTitle());
		}
		if (updateRequest.isPinnedFlagNeedUpdate()) {
			compilation.setPinned(updateRequest.getPinned());
		}

		if (updateRequest.isEventListNeedUpdate()) {
			List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
			compilation.setEvents(new HashSet<>(events));
		}

		repository.save(compilation);

		return Mapper.toCompilationDto(compilation);
	}

	private static PageRequest page(int from, int size) {
		return PageRequest.of(from > 0 ? from / size : 0, size);
	}
}
