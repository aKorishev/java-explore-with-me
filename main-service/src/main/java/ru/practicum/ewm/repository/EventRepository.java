package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entities.Event;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
	@Query(" select e " +
			"from Event e " +
			"where e.state like 'PUBLISHED' and id = ?1")
	Optional<Event> findPublishedById(long eventId);

	List<Event> findByInitiatorId(long userId, Pageable pageable);


	Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

	long countByCategoryId(long categoryId);

	@Query("select e.participantLimit from Event e where e.id = ?1")
	long findEventParticipantLimit(long eventId);
}