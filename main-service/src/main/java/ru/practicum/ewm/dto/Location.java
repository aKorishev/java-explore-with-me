package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Value
@Jacksonized
@Builder
@AllArgsConstructor(staticName = "of")
public class Location implements Serializable {

    float lat;

    float lon;
}
