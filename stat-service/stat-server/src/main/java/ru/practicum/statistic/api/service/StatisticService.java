package ru.practicum.statistic.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.api.storage.StatisticStorage;
import ru.practicum.statistic.dto.EndpointHit;
import ru.practicum.statistic.dto.StatisticInfo;
import ru.practicum.statistic.dto.ViewStats;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private final StatisticStorage storage;

    public List<StatisticInfo> getStatistics(
            String start,
            String end,
            String[] uris,
            Boolean unique) {

        return storage.getStatistics(start, end, uris, unique);
    }

    public EndpointHit postRequest(EndpointHit statisticRequest) {
        try {
            //var entity = StatisticMapper.toEndPointHitEntity(statisticRequest);

            var entity = storage.postStaticEntity(statisticRequest);

            //return StatisticMapper.toDto(entity);

            return entity;
        } catch (Exception e) {
            throw new NotValidException(e.getMessage());
        }
    }

    public List<ViewStats> getCalculatedStatistics(
            String start,
            String end,
            List<String> uris,
            Integer limit,
            Boolean unique) {
        return storage.getCalculatedStatistics(start, end, uris, limit, unique);
    }
}
