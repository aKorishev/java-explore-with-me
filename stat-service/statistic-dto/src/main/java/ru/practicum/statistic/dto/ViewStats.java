package ru.practicum.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private long hits;

    @Override
    public String toString() {
        return String.format("{app: %s; uri: %s; hits: %d}", app, uri, hits);
    }
}
