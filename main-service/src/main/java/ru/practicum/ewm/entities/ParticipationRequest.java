package ru.practicum.ewm.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "participation_requests")
public class ParticipationRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private RequestStatus status = RequestStatus.PENDING;

	private Timestamp created = Timestamp.from(Instant.now());

	@Transient
	public boolean isDataMatchRequest(long eventId, long initiatorId) {
		return Objects.equals(event.getInitiator().getId(), initiatorId)
				&& Objects.equals(event.getId(), eventId);

	}
}
