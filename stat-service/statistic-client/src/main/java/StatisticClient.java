import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statistic.dto.StatisticRequest;

import java.util.List;

@Slf4j
public class StatisticClient {
    private final RestTemplate restTemplate;
    private final String statsPath;
    private final String postStats;

    @Autowired
    public StatisticClient(
            @Value("${statistic.api.url}") String statisticApiUrl,
            @Value("${statistic.post.path}") String statsPath,
            @Value("${statistic.stats.path}") String postStats,
            RestTemplateBuilder builder) {

        restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statisticApiUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();

        this.postStats = postStats;
        this.statsPath = statsPath;
    }

    public StatisticClient(
            String statsPath,
            String postStats,
            RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        this.postStats = postStats;
        this.statsPath = statsPath;
    }

    public ResponseEntity<Object> postRequest(StatisticRequest statisticRequest) {
        log.trace("try post " + statisticRequest);

        var headers = getHeaders();

        HttpEntity<Object> restQueryEntity = new HttpEntity<>(statisticRequest, headers);

        try {
            var response = restTemplate.exchange(
                    postStats,
                    HttpMethod.POST,
                    restQueryEntity,
                    Object.class);

            return prepareGatewayResponse(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<Object> getStats() {
        log.trace("try get stats");

        var headers = getHeaders();

        HttpEntity<Object> restQueryEntity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(
                    statsPath,
                    HttpMethod.GET,
                    restQueryEntity,
                    Object.class);

            return prepareGatewayResponse(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    private ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
