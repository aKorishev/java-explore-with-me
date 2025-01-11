package ru.practicum.ewm.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Set;


@Entity
@Getter
@Setter
@Table(name = "compilations")
public class Compilation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	private String title;

	private boolean pinned;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "compilation_of_events",
			joinColumns = @JoinColumn(name = "comp_id"),
			inverseJoinColumns = @JoinColumn(name = "event_id")
	)
	private Set<Event> events;
}