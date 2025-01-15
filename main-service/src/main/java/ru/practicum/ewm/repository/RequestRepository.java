package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.RequestEntity;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
	boolean existsByRequesterIdAndEventEntityId(int requesterId, int eventId);

	List<RequestEntity> findAllByRequesterId(long requesterId);

	List<RequestEntity> findAllByEventEntityId(long eventId);

	List<RequestEntity> findAllByIdIn(List<Long> ids);

	Optional<RequestEntity> findByRequesterIdAndEventEntityId(long requesterId, long eventId);
}