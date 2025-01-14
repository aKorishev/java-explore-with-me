package ru.practicum.statistic.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.statistic.dto.EndpointHit;
import ru.practicum.statistic.dto.ViewStats;
import ru.practicum.statistic.dto.ViewsStatsRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatisticClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String application;
    private final String statsServiceUri;
    private final ObjectMapper json;
    private final HttpClient httpClient;

    public StatisticClient(@Value("${spring.application.name}") String application,
                           @Value("${services.stats-service.uri}") String statsServiceUri,
                           ObjectMapper json) {
        this.application = application;
        this.statsServiceUri = statsServiceUri;
        this.json = json;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void hit(HttpServletRequest userRequest) {
        EndpointHit hit = EndpointHit.builder()
                .app(application)
                .ip(userRequest.getRemoteAddr())
                .uri(userRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            HttpRequest.BodyPublisher bodyPublisher = HttpRequest
                    .BodyPublishers
                    .ofString(json.writeValueAsString(hit));

            HttpRequest hitRequest = HttpRequest.newBuilder()
                    .uri(URI.create(statsServiceUri + "/hit"))
                    .POST(bodyPublisher)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .build();

            httpClient.send(hitRequest, HttpResponse.BodyHandlers.discarding());

            log.trace("Record hit sended, " + hit);
        } catch (Exception e) {
            log.error("Record hit error", e);
        }
    }

    public List<ViewStats> getStats(ViewsStatsRequest request) {
        try {
            String start = URLEncoder.encode(DATE_TIME_FORMATTER.format(request.getStart()), StandardCharsets.UTF_8);
            String end = URLEncoder.encode(DATE_TIME_FORMATTER.format(request.getEnd()), StandardCharsets.UTF_8);

            String query = String.format("?start=%s&end=%s&unique=%b&application=%s", start, end, request.isUnique(), application);
            query += "&uris=" + String.join(",", request.getUris());
            if (request.hasLimitCondition()) {
                query += "&limit=" + request.getLimit();
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(statsServiceUri + "/stats" + query))
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                log.trace("Response from {}: {}", statsServiceUri + "/stats", response.body());

                return json.readValue(response.body(), new TypeReference<>() {
                });
            }
        } catch (Exception e) {
            log.error("Get view stats error", e);
        }
        return List.of();
    }
}
