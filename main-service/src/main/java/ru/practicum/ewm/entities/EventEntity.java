package ru.practicum.ewm.entities;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "events")
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class EventEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id", nullable = false)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String annotation;

	@Column(nullable = false)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cat_id", nullable = false)
	private CategoryEntity categoryEntity;

	private int participantLimit;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventState state = EventState.PENDING;

	private boolean paid;

	@Column(nullable = false)
	private LocalDateTime eventDate;

	private float latitude;

	private float longitude;

	@Builder.Default
	@Column(nullable = false)
	private LocalDateTime createdOn = LocalDateTime.now();

	private LocalDateTime publishedOn;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "initiator_id", nullable = false)
	private UserEntity initiator;

	@Builder.Default
	@Column(name = "req_moderation")
	private boolean requestModeration = true;

	@Transient
	public boolean isPublished() {
		return state.equals(EventState.PUBLISHED);
	}

	@Transient
	public boolean isCanceled() {
		return state.equals(EventState.CANCELED);
	}

	@Transient
	public boolean isPending() {
		return state.equals(EventState.PENDING);
	}
}
