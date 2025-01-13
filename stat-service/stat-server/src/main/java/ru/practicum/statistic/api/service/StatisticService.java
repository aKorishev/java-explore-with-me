package ru.practicum.statistic.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.api.storage.StatisticStorage;
import ru.practicum.statistic.dto.EndpointHit;
import ru.practicum.statistic.dto.StatisticInfo;
import ru.practicum.statistic.dto.ViewStats;
import ru.practicum.statistic.dto.vlidators.TimeFormatValidator;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {
    private final StatisticStorage storage;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormatValidator.PATTERN);

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

        if (start ==  null || start.isBlank())
            throw new NotValidException("Start is empty");

        if (end ==  null || end.isBlank())
            throw new NotValidException("End is empty");

        try {
            var startTimeStamp = Timestamp.from(simpleDateFormat.parse(start).toInstant());
            var endTimeStamp = Timestamp.from(simpleDateFormat.parse(end).toInstant());

            if (startTimeStamp.after(endTimeStamp))
                throw new NotValidException("Start need be before end");

            return storage.getCalculatedStatistics(startTimeStamp, endTimeStamp, uris, limit, unique);
        } catch (ParseException e) {
            throw new NotValidException(e.getMessage());
        }
    }
}
