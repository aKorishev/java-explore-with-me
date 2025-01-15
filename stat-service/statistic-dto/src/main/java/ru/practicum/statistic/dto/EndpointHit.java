package ru.practicum.statistic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import ru.practicum.statistic.dto.vlidators.TimeFormatValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointHit {
    private Long id;

    @NotNull
    private String app;

    @NotNull
    private String uri;

    @NotNull
    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return String.format("{id: %d; app: %s; uri; %s; ip: %s; timestamp: %s}", id, app, uri, ip, timestamp.format(DateTimeFormatter.ofPattern(TimeFormatValidator.PATTERN)));
    }
}
