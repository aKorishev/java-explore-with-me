package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventUpdateStatusRequestDto {
    private List<Long> requestIds;
    private Status status;

    public enum Status {
        CONFIRMED,
        REJECTED
    }
}
