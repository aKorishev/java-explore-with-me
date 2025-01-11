package ru.practicum.statistic.api.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.ViewStats;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatisticController {
    public final StatisticService statisticService;

    @GetMapping("/stats")
    public List<ViewStats> getStatistics(
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "") List<String> uris,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "false") Boolean unique
            ) {
        return statisticService.getCalculatedStatistics(start, end, uris, limit, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatisticRequest postRequest(
            @Valid @RequestBody StatisticRequest statisticRequest) {
        return statisticService.postRequest(statisticRequest);
    }
}
