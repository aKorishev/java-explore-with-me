package ru.practicum.statistic.api;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.statistic.api.service.StatisticController;
import ru.practicum.statistic.api.service.StatisticService;
import ru.practicum.statistic.dto.StatisticRequest;
import ru.practicum.statistic.dto.StatisticInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class StatisticControllerTest {
    @Mock
    StatisticService statisticService;

    @InjectMocks
    StatisticController statisticController;

    @Test
    public void getStatsTest() throws Exception {
        List<StatisticInfo> responses = List.of(
                StatisticInfo.builder().app("1").build(),
                StatisticInfo.builder().app("4").build(),
                StatisticInfo.builder().app("7").build());

        var mockMvc = MockMvcBuilders
                .standaloneSetup(statisticController)
                .build();

        Mockito.when(statisticService.getStatistics(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(String[].class),
                    Mockito.anyBoolean()))
                .thenReturn(responses);

        var empryUuris = new String[] {};

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app",is("1"), String.class))
                .andExpect(jsonPath("$.[1].app",is("4"), String.class))
                .andExpect(jsonPath("$.[2].app",is("7"), String.class));

        Mockito.verify(statisticService, Mockito.times(1))
                .getStatistics("","", empryUuris, false);

        mockMvc.perform(get("/stats?uris=app"))
                .andExpect(status().isOk());

        Mockito.verify(statisticService, Mockito.times(1))
                .getStatistics("","", new String[] {"app"}, false);

        mockMvc.perform(get("/stats?uris=app&uris=api"))
                .andExpect(status().isOk());

        Mockito.verify(statisticService, Mockito.times(1))
                .getStatistics("","", new String[] {"app", "api"}, false);

        mockMvc.perform(get("/stats?start=2024-05-25 11:55:23&end=2026-01-14 00:45:00"))
                .andExpect(status().isOk());

        Mockito.verify(statisticService, Mockito.times(1))
                .getStatistics("2024-05-25 11:55:23","2026-01-14 00:45:00", empryUuris, false);
    }

    @Test
    public void postValidatedDateRequestTest() throws Exception {
        var app = "ewm-main-service";
        var ip = "192.163.0.1";
        var uri = "/events";
        var timestamp = "2022-09-06 11:00:23";

        var requestStr = "{" +
                "\"app\": \"" + app + "\"," +
                "\"ip\": \"" + ip + "\"," +
                "\"uri\": \"" + uri + "\"," +
                "\"timestamp\": \"" + timestamp + "\"" +
                "}";
        var requestDto = StatisticRequest.builder()
                .app(app)
                .ip(ip)
                .timestamp(timestamp)
                .uri(uri)
                .build();

        var response = requestDto.toBuilder()
                .app("19")
                .build();

        var mockMvc = MockMvcBuilders
                .standaloneSetup(statisticController)
                .build();

        Mockito.when(statisticService.postRequest(Mockito.any(StatisticRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStr.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app", is("19"), String.class))
                .andExpect(jsonPath("$.uri", is(uri), String.class));

        Mockito.verify(statisticService, Mockito.times(1))
                .postRequest(requestDto);
    }



    @Test
    public void postNotValidatedDataRequestTest() throws Exception {
        var app = "ewm-main-service";
        var ip = "192.163.0.1";
        var uri = "/events";
        var timestamp = "2022-09-06 11:00:23";

        var requestStrBadTimestamp = "{" +
                "\"app\": \"" + app + "\"," +
                "\"ip\": \"" + ip + "\"," +
                "\"uri\": \"" + uri + "\"," +
                "\"timestamp\": \"2022-25-06 11:00:23\"" +
                "}";

        var requestStrBlankTimeStamp = "{" +
                "\"app\": \"" + app + "\"," +
                "\"ip\": \"" + ip + "\"," +
                "\"uri\": \"" + uri + "\"," +
                "\"timestamp\": \"\"" +
                "}";

        var requestStrBlankApp = "{" +
                "\"app\": \"\"," +
                "\"ip\": \"" + ip + "\"," +
                "\"uri\": \"" + uri + "\"," +
                "\"timestamp\": \"" + timestamp + "\"" +
                "}";

        var requestStrBlankIp = "{" +
                "\"app\": \"" + app + "\"," +
                "\"ip\": \"\"," +
                "\"uri\": \"" + uri + "\"," +
                "\"timestamp\": \"" + timestamp + "\"" +
                "}";

        var requestStrBlankUri = "{" +
                "\"app\": \"" + app + "\"," +
                "\"ip\": \"" + ip + "\"," +
                "\"uri\": \"\"," +
                "\"timestamp\": \"" + timestamp + "\"" +
                "}";

        var response = StatisticRequest.builder()
                .app(app)
                .ip(ip)
                .timestamp(timestamp)
                .uri(uri)
                .build();

        var mockMvc = MockMvcBuilders
                .standaloneSetup(statisticController)
                .build();

        Mockito.when(statisticService.postRequest(Mockito.any(StatisticRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStrBadTimestamp.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStrBlankTimeStamp.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStrBlankApp.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStrBlankUri.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(requestStrBlankIp.getBytes(StandardCharsets.UTF_8))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        Mockito.verify(statisticService, Mockito.never())
                .postRequest(Mockito.any(StatisticRequest.class));
    }
}
