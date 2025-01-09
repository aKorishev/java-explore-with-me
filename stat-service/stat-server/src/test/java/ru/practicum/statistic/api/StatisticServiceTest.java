package ru.practicum.statistic.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.api.service.StatisticMapper;
import ru.practicum.statistic.api.service.StatisticService;
import ru.practicum.statistic.api.storage.StatisticEntity;
import ru.practicum.statistic.api.storage.StatisticStorage;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.StatisticInfo;

import java.text.ParseException;
import java.util.List;

@SpringBootTest
public class StatisticServiceTest {
    @Mock
    StatisticStorage statisticStorage;

    @InjectMocks
    StatisticService statisticService;

    @Test
    public void postRequestTest() throws Exception {
        var requestDto = StatisticRequest.builder()
                .app("ewm-main-service")
                .ip("192.163.0.1")
                .timestamp("2024-05-25 13:54:08")
                .uri("/events")
                .build();

        var entityIn = StatisticMapper.toNewEntity(requestDto);

        var entityOut = StatisticMapper.toNewEntity(requestDto);
        entityOut.setApp("tested");
        entityOut.setId(10L);

        Mockito.when(statisticStorage.postStaticEntity(Mockito.any(StatisticEntity.class)))
                .thenReturn(entityOut);

        var expectedDto = StatisticMapper.toDto(entityOut);

        var actualDto = statisticService.postRequest(requestDto);

        Assertions.assertEquals(expectedDto, actualDto);

        Mockito.verify(statisticStorage, Mockito.times(1))
                .postStaticEntity(entityIn);
    }

    @Test
    public void postRequestParseExceptionTest() throws Exception {
        var requestDto = StatisticRequest.builder()
                .app("ewm-main-service")
                .ip("192.163.0.1")
                .timestamp("2024-05-25 13:54:08")
                .uri("/events")
                .build();

        Mockito.when(statisticStorage.postStaticEntity(Mockito.any(StatisticEntity.class)))
                .thenReturn(new StatisticEntity());

        try (MockedStatic<StatisticMapper> mapper = Mockito.mockStatic(StatisticMapper.class)) {
            mapper.when(() -> StatisticMapper.toNewEntity(Mockito.any(StatisticRequest.class)))
                    .thenThrow(new ParseException("Parse to fail", 0));
            statisticService.postRequest(requestDto);
        } catch (NotValidException ex) {
            Assertions.assertEquals("Parse to fail", ex.getMessage());

            return;
        } catch (Throwable e) {
            Assertions.fail(e.getMessage());
        }

        Assertions.fail();
    }

    @Test
    public void getStatisticsTest() {
        var expectedResponse = List.of(
                StatisticInfo.builder().app("app2").uri("uri2").hits(5L).build(),
                StatisticInfo.builder().app("app2").uri("uri1").hits(4L).build());

        var start = "start";
        var end = "end";
        var uris = new String[] {"uri1", "uri2"};

        Mockito.when(statisticStorage.getStatistics(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(String[].class),
                Mockito.anyBoolean()))
                .thenReturn(expectedResponse);

        var actualResponse = statisticService.getStatistics(start, end, uris, true);

        Assertions.assertEquals(expectedResponse, actualResponse);

        Mockito.verify(statisticStorage, Mockito.times(1))
                .getStatistics(start, end, uris, true);

        statisticService.getStatistics(start, end, uris, false);

        Mockito.verify(statisticStorage, Mockito.times(1))
                .getStatistics(start, end, uris, false);
    }
}