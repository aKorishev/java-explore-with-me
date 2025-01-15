package ru.practicum.statistic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import ru.practicum.statistic.dto.vlidators.TimeFormatConstraint;


@Builder(toBuilder = true)
public record StatisticRequest(
    long id,
    @NotBlank
    String app,
    @NotBlank
    String uri,
    @NotBlank
    String ip,
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TimeFormatConstraint
    String timestamp
) { }
