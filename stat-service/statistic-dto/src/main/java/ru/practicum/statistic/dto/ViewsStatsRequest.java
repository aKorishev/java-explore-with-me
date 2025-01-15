package ru.practicum.statistic.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@ToString
@Builder(toBuilder = true)
public class ViewsStatsRequest {
	@Singular("uri")
	private Set<String> uris;

	private LocalDateTime start;

	private LocalDateTime end;

	private boolean unique;

	private Integer limit;

	private String application;

	public boolean hasLimitCondition() {
		return limit != null && limit != 0;
	}
}
