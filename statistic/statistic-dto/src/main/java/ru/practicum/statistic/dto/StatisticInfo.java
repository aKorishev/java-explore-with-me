package ru.practicum.statistic.dto;

import lombok.Builder;

@Builder
public record StatisticInfo(
        String app,
        String uri,
        long hits) { }
