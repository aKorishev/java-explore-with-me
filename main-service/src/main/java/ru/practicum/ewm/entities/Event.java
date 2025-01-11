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
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String annotation;

	@Column(nullable = false)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cat_id", nullable = false)
	private Category category;

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
	private User initiator;

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
