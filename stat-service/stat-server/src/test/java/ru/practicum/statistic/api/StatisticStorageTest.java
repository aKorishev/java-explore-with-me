package ru.practicum.statistic.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.statistic.api.storage.StatisticEntity;
import ru.practicum.statistic.api.storage.StatisticStorage;
import ru.practicum.statistic.dto.StatisticInfo;
import ru.practicum.statistic.dto.vlidators.TimeFormatValidator;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
},
        showSql = false)
@ComponentScan(basePackages = "ru.practicum.statistic")
public class StatisticStorageTest {
    @Autowired
    private StatisticStorage statisticStorage;

    private Timestamp initTimestamp(String value) throws ParseException {
        var formater = new SimpleDateFormat(TimeFormatValidator.PATTERN);

        var date = formater.parse(value);

        return Timestamp.from(date.toInstant());
    }

    @Test
    public void postStatisticEntity() throws ParseException {
        var entity = initStatisticEntity("app", "uri", "2024-04-15 11:11:11");

        statisticStorage.postStaticEntity(entity);

        Assertions.assertEquals(entity.getId(), 1L);
    }

    private StatisticEntity initStatisticEntity(String app, String uri, String time) {
        var entity = new StatisticEntity();
        entity.setIp("ip");
        entity.setApp(app);
        entity.setUri(uri);
        entity.setTimestamp(Timestamp.valueOf(time));

        return entity;
    }

    @Test
    public void getStatisticInfoListTest() throws Exception {
        var time1 = "2024-04-10 10:00:00";
        var time2 = "2024-04-15 10:00:00";
        var time3 = "2024-04-20 10:00:00";
        var time4 = "2024-04-25 10:00:00";
        var time5 = "2024-04-30 10:00:00";
        var time6 = "2024-05-05 10:00:00";

        var app1 = "app1";
        var app2 = "app2";

        var uri1 = "uri1";
        var uri2 = "uri2";

        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri1, time1));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri1, time2));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri1, time3));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri2, time1));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri2, time2));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri2, time3));
        statisticStorage.postStaticEntity(initStatisticEntity(app1, uri2, time4));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri1, time1));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri1, time2));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri1, time3));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri1, time4));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri1, time5));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time1));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time2));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time3));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time4));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time5));
        statisticStorage.postStaticEntity(initStatisticEntity(app2, uri2, time6));

        var expectedAllItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri2).hits(6L).build(),
                StatisticInfo.builder().app(app2).uri(uri1).hits(5L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(4L).build(),
                StatisticInfo.builder().app(app1).uri(uri1).hits(3L).build());
        var allItems = statisticStorage.getStatistics("", "", new String[] {}, false);
        Assertions.assertEquals(expectedAllItems, allItems);

        var expectedUniqueAllItems = List.of(
                StatisticInfo.builder().app(app1).uri(uri1).hits(1L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(1L).build(),
                StatisticInfo.builder().app(app2).uri(uri1).hits(1L).build(),
                StatisticInfo.builder().app(app2).uri(uri2).hits(1L).build());
        var uniqueAllItems = statisticStorage.getStatistics("", "", new String[] {}, true);
        Assertions.assertEquals(expectedUniqueAllItems, uniqueAllItems);

        var expectedSelectAllUrisItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri2).hits(6L).build(),
                StatisticInfo.builder().app(app2).uri(uri1).hits(5L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(4L).build(),
                StatisticInfo.builder().app(app1).uri(uri1).hits(3L).build());
        var selectAllUrisItems = statisticStorage.getStatistics("", "", new String[] {uri1, uri2}, false);
        Assertions.assertEquals(expectedSelectAllUrisItems, selectAllUrisItems);

        var expectedSelectUrisItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri2).hits(6L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(4L).build());
        var selectUrisItems = statisticStorage.getStatistics("", "", new String[] {uri2}, false);
        Assertions.assertEquals(expectedSelectUrisItems, selectUrisItems);

        var expectedAfterStartItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri2).hits(5L).build(),
                StatisticInfo.builder().app(app2).uri(uri1).hits(4L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(3L).build(),
                StatisticInfo.builder().app(app1).uri(uri1).hits(2L).build());
        var selectAfterStartItems = statisticStorage.getStatistics("2024-04-15 10:00:00", "", new String[] {}, false);
        Assertions.assertEquals(expectedAfterStartItems, selectAfterStartItems);

        var expectedBeforeStartItems = List.of(
                StatisticInfo.builder().app(app1).uri(uri1).hits(2L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(2L).build(),
                StatisticInfo.builder().app(app2).uri(uri1).hits(2L).build(),
                StatisticInfo.builder().app(app2).uri(uri2).hits(2L).build());
        var selectBeforeStartItems = statisticStorage.getStatistics("", "2024-04-15 10:00:00", new String[] {}, false);
        Assertions.assertEquals(expectedBeforeStartItems, selectBeforeStartItems);

        var expectedBetweenStartItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri1).hits(3L).build(),
                StatisticInfo.builder().app(app2).uri(uri2).hits(3L).build(),
                StatisticInfo.builder().app(app1).uri(uri2).hits(2L).build(),
                StatisticInfo.builder().app(app1).uri(uri1).hits(1L).build());
        var selectBetweenStartItems = statisticStorage.getStatistics("2024-04-20 10:00:00", "2024-04-30 10:00:00", new String[] {}, false);
        Assertions.assertEquals(expectedBetweenStartItems, selectBetweenStartItems);

        var expectedBetweenStartAndUriItems = List.of(
                StatisticInfo.builder().app(app2).uri(uri1).hits(3L).build(),
                StatisticInfo.builder().app(app1).uri(uri1).hits(1L).build());
        var selectBetweenStartAndUriItems = statisticStorage.getStatistics("2024-04-20 10:00:00", "2024-04-30 10:00:00", new String[] {uri1}, false);
        Assertions.assertEquals(expectedBetweenStartAndUriItems, selectBetweenStartAndUriItems);
    }
}