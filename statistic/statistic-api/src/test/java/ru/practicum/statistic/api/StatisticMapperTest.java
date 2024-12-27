package ru.practicum.statistic.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.statistic.api.service.StatisticMapper;
import ru.practicum.statistic.api.storage.StatisticEntity;
import ru.practicum.statistic.dto.StatisticRequest;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootTest
public class StatisticMapperTest {
    @Test
    public void statisticEntityToDtoTest() throws Exception {
        var entity = new StatisticEntity();
        entity.setIp("ip99");
        entity.setUri("uri99");
        entity.setApp("app99");
        entity.setTimestamp(Timestamp.valueOf("2024-05-25 13:54:08"));

        var expectedDto = StatisticRequest.builder()
                .app("app99")
                .ip("ip99")
                .uri("uri99")
                .timestamp("2024-05-25 13:54:08")
                .build();

        var actualDto = StatisticMapper.toDto(entity);

        Assertions.assertEquals(expectedDto, actualDto);
    }

//    @Test
//    public void statisticInfoToDtoTest() throws Exception {
//        var info = new ru.practicum.statistic.api.storage.StatisticInfo() {
//            @Override
//            public String getApp() {
//                return "app";
//            }
//
//            @Override
//            public String getUri() {
//                return "uri";
//            }
//
//            @Override
//            public long getHits() {
//                return 966L;
//            }
//        };
//
//        var expectedDto = StatisticInfo.builder()
//                .app("app")
//                .uri("uri")
//                .hits(966L)
//                .build();
//
//        var actualDto = StatisticMapper.toDto(info);
//
//        Assertions.assertEquals(expectedDto, actualDto);
//    }

    @Test
    public void statisticRequestToNewEntityTest() throws Exception {
        var dto = StatisticRequest.builder()
                .app("app")
                .uri("uri")
                .ip("ip")
                .timestamp("2024-05-25 13:54:08")
                .build();

        var expectedEntity = new StatisticEntity();
        expectedEntity.setApp("app");
        expectedEntity.setIp("ip");
        expectedEntity.setUri("uri");
        expectedEntity.setTimestamp(Timestamp.valueOf(LocalDateTime.of(2024,5,25,13,54,8)));

        var actualEntity = StatisticMapper.toNewEntity(dto);

        Assertions.assertEquals(expectedEntity, actualEntity);
    }
}