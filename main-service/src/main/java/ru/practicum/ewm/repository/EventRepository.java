package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.entities.EventEntity;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long>, QuerydslPredicateExecutor<EventEntity> {
	@Query(" select e " +
			"from EventEntity e " +
			"where e.state like 'PUBLISHED' and id = ?1")
	Optional<EventEntity> findPublishedById(long eventId);

	List<EventEntity> findByInitiatorId(long userId, Pageable pageable);


	Optional<EventEntity> findByIdAndInitiatorId(long eventId, long userId);

	long countByCategoryEntityId(long categoryId);

	@Query("select e.participantLimit from EventEntity e where e.id = ?1")
	long findEventParticipantLimit(long eventId);
}