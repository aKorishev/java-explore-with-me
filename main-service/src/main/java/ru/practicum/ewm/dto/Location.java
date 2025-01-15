package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Location implements Serializable {
    float lat;
    float lon;
}
