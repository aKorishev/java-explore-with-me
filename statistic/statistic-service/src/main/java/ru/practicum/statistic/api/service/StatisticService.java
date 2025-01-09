package ru.practicum.statistic.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.api.storage.StatisticStorage;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.StatisticInfo;

import java.text.ParseException;
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

    public StatisticRequest postRequest(StatisticRequest statisticRequest) {
        try {
            var entity = StatisticMapper.toNewEntity(statisticRequest);

            entity = storage.postStaticEntity(entity);

            return StatisticMapper.toDto(entity);
        } catch (ParseException e) {
            throw new NotValidException(e.getMessage());
        }
    }
}
