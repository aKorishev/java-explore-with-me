package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


@Builder
public record CommentDto(
    Long id,
    Long eventId,
    Long authorId,

    @NotBlank
    String text,
    LocalDateTime created,
    LocalDateTime lastUpdateTime) { }