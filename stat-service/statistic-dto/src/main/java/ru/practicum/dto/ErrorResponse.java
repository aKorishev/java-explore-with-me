package ru.practicum.dto;

public record ErrorResponse(
    String error,
    String description) { }
