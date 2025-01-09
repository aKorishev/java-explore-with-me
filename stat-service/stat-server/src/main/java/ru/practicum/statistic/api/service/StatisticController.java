package ru.practicum.statistic.api.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.StatisticInfo;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatisticController {
    public final StatisticService statisticService;

    @GetMapping("/stats")
    public List<StatisticInfo> getStatistics(
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end,
            @RequestParam(defaultValue = "") String[] uris,
            @RequestParam(defaultValue = "false") Boolean unique
            ) {
        return statisticService.getStatistics(start, end, uris, unique);
    }

    @PostMapping("/hit")
    public StatisticRequest postRequest(
            @Valid @RequestBody StatisticRequest statisticRequest) {
        return statisticService.postRequest(statisticRequest);
    }
}
