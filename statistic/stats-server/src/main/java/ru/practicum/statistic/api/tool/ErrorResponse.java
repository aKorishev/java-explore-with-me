package ru.practicum.statistic.api.tool;

import lombok.Value;

@Value
public class ErrorResponse {
    public String error;
    public String description;
}
