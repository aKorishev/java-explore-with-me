package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entities.ParticipationRequestEntity;
import ru.practicum.ewm.entities.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository
		extends JpaRepository<ParticipationRequestEntity, Long>, QuerydslPredicateExecutor<ParticipationRequestEntity> {

	@Query(" select p " +
			"from ParticipationRequestEntity p " +
			"where p.eventEntity.initiator.id = ?1 and p.eventEntity.id = ?2")
	List<ParticipationRequestEntity> findUserEventParticipationRequests(Long initiatorId, Long eventId);

	Optional<ParticipationRequestEntity> findByRequesterIdAndId(Long id, Long id1);

	List<ParticipationRequestEntity> findByRequesterId(Long id);

	@Query(" select count(p) " +
			"from ParticipationRequestEntity p " +
			"where p.eventEntity.id = ?1 and p.status = ?2 ")
	int countRequestsWithStatus(long eventId, RequestStatus status);

	@Query(" select p " +
			"from ParticipationRequestEntity p " +
			"where p.eventEntity.id = ?1 and p.status = ?2" +
			"  and p not in ?3")
	List<ParticipationRequestEntity> findEventRequestsWithExclusionList(Long eventId,
                                                                        RequestStatus status,
                                                                        Collection<ParticipationRequestEntity> excludeList);
}