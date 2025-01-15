package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.entities.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Value
@Builder
public class GetEventsRequest {
	static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	String text;
	List<Long> categories;
	List<Long> initiators;
	Boolean paid;
	Location location;
	Page page;
	LocalDateTime rangeStart;
	LocalDateTime rangeEnd;
	EnumSet<EventState> states;
	boolean onlyAvailableForParticipation;
	@Builder.Default
	Sort sort = Sort.EVENT_DATE;
	@Builder.Default
	boolean shortFormat = true;

	@Value
	@Builder
	@AllArgsConstructor(staticName = "of")
	public static class Location {
		float lat;
		float lon;
		short radius;

		public boolean isSpecified() {
			return lat != 0 && lon != 0;
		}
	}

	@Value
	@Builder
	@AllArgsConstructor(staticName = "of")
	public static class Page {
		int from;
		int size;

		public int getNumber() {
			return from > 0 ? from / size : 0;
		}
	}

	public boolean hasTextCondition() {
		return text != null && !text.isBlank();
	}

	public boolean hasCategoriesCondition() {
		return categories != null && !categories.isEmpty();
	}

	public boolean hasInitiatorsCondition() {
		return initiators != null && !initiators.isEmpty();
	}

	public boolean hasPaidCondition() {
		return paid != null;
	}

	public boolean hasLocationCondition() {
		return location != null && location.isSpecified();
	}

	public boolean hasStates() {
		return states != null && !states.isEmpty();
	}

	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	public static class GetEventsRequestBuilder {
		private EnumSet<EventState> states;
		private Sort sort = Sort.EVENT_DATE;

		public GetEventsRequestBuilder dateRange(String start, String end) {
			try {
				if (start != null && end != null && LocalDateTime.parse(end, DTF).isBefore(LocalDateTime.parse(start, DTF))) {
					throw new IllegalArgumentException("Дата начала диапазона не может быть раньше даты конца");
				}
				if (start != null && !start.isBlank()) {
					rangeStart = LocalDateTime.parse(start, DTF);
				}
				if (end != null && !end.isBlank()) {
					rangeEnd = LocalDateTime.parse(end, DTF);
				}
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("Cannot parse date range: " + start + " - " + end
						+ ". Format should be: " + DTF);
			}

			if (rangeStart != null && rangeEnd != null
					&& rangeStart.isAfter(rangeEnd)) {
				final LocalDateTime tmp = rangeStart;
				rangeStart = rangeEnd;
				rangeEnd = tmp;
			}
			return this;
		}

		public GetEventsRequestBuilder states(Collection<String> stateStrings) {
			if (this.states == null) {
				this.states = EnumSet.noneOf(EventState.class);
			}
			if (stateStrings != null) {
				stateStrings
						.stream()
						.map(EventState::from)
						.forEach(this.states::add);
			}
			return this;
		}

		public GetEventsRequestBuilder state(EventState state) {
			if (this.states == null) {
				this.states = EnumSet.noneOf(EventState.class);
			}
			this.states.add(state);
			return this;
		}

		public GetEventsRequestBuilder sort(String sort) {
			this.sort = Sort.from(sort);
			return this;
		}
	}

	public enum Sort {
		EVENT_DATE, VIEWS;

		public static Sort from(String sortString) {
			for (Sort sort : values()) {
				if (sort.name().equalsIgnoreCase(sortString)) {
					return sort;
				}
			}
			throw new IllegalArgumentException("Incorrect sort parameter: " + sortString);
		}
	}
}
