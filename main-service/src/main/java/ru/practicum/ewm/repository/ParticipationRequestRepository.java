package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entities.ParticipationRequest;
import ru.practicum.ewm.entities.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository
		extends JpaRepository<ParticipationRequest, Long>, QuerydslPredicateExecutor<ParticipationRequest> {

	@Query(" select p " +
			"from ParticipationRequest p " +
			"where p.event.initiator.id = ?1 and p.event.id = ?2")
	List<ParticipationRequest> findUserEventParticipationRequests(Long initiatorId, Long eventId);

	Optional<ParticipationRequest> findByRequesterIdAndId(Long id, Long id1);

	List<ParticipationRequest> findByRequesterId(Long id);

	@Query(" select count(p) " +
			"from ParticipationRequest p " +
			"where p.event.id = ?1 and p.status = ?2 ")
	int countRequestsWithStatus(long eventId, RequestStatus status);

	@Query(" select p " +
			"from ParticipationRequest p " +
			"where p.event.id = ?1 and p.status = ?2" +
			"  and p not in ?3")
	List<ParticipationRequest> findEventRequestsWithExclusionList(Long eventId,
																  RequestStatus status,
																  Collection<ParticipationRequest> excludeList);
}