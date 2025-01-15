package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.EventEntity;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
	List<EventEntity> findAllByInitiatorId(long id);

	boolean existsByIdAndInitiatorId(long eventId, long initiatorId);

	boolean existsByCategoryEntityId(long categoryId);

	List<EventEntity> findAllByInitiatorIdInAndCategoryEntityIdIn(List<Long> initiatorIds, List<Long> categoryIds);

	List<EventEntity> findAllByInitiatorIdIn(List<Long> initiatorIds);

	List<EventEntity> findAllByCategoryEntityIdIn(List<Long> categoryIds);

	List<EventEntity> findAllByIdIn(List<Long> ids);
}