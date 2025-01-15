package ru.practicum.statistic.api.service;

import ru.practicum.statistic.api.storage.StatisticEntity;
import ru.practicum.statistic.dto.EndpointHit;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.vlidators.TimeFormatValidator;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class StatisticMapper {
    private static DateFormat format = new SimpleDateFormat(TimeFormatValidator.PATTERN);

    public static StatisticRequest toDto(StatisticEntity entity) {
        return StatisticRequest.builder()
                .uri(entity.getUri())
                .ip(entity.getIp())
                .app(entity.getApp())
                .timestamp(format.format(entity.getTimestamp()))
                .build();
    }

    public static StatisticRequest toDto(EndpointHit entity) {
        /*var d = entity.getTimestamp();
        var da = d.format(DateTimeFormatter.ofPattern(TimeFormatValidator.PATTERN));*/
        var timestamp = format.format(entity.getTimestamp());

        return StatisticRequest.builder()
                .uri(entity.getUri())
                .ip(entity.getIp())
                .app(entity.getApp())
                .timestamp(timestamp)
                .build();
    }

    public static StatisticEntity toNewEntity(StatisticRequest statisticRequest) throws ParseException {
        var entity = new StatisticEntity();

        var timeStamp = format.parse(statisticRequest.timestamp());

        entity.setUri(statisticRequest.uri());
        entity.setIp(statisticRequest.ip());
        entity.setApp(statisticRequest.app());
        entity.setTimestamp(Timestamp.from(timeStamp.toInstant()));

        return entity;
    }

    public static EndpointHit toEndPointHitEntity(StatisticRequest statisticRequest) throws ParseException {
        var instant = format.parse(statisticRequest.timestamp()).toInstant();

        var entity = EndpointHit.builder()
                .uri(statisticRequest.uri())
                .ip(statisticRequest.ip())
                .app(statisticRequest.app())
                .timestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .build();

        return entity;
    }
}
