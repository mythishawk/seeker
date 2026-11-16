package com.job.seeker.discovery;

import com.job.seeker.domain.SourceType;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Reads a company's open jobs from Greenhouse's public job-board API (no key needed). */
@Component
public class GreenhouseClient implements JobBoardClient {

    private final RestClient restClient;

    public GreenhouseClient(DiscoveryProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.requestTimeout());
        requestFactory.setReadTimeout(properties.requestTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.greenhouseBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public SourceType type() {
        return SourceType.GREENHOUSE;
    }

    @Override
    public List<FetchedJob> fetch(String boardSlug) {
        try {
            GreenhouseMapper.Response body = restClient.get()
                    .uri("/v1/boards/{slug}/jobs?content=true", boardSlug)
                    .retrieve()
                    .body(GreenhouseMapper.Response.class);
            if (body == null || body.jobs() == null) {
                throw new BoardFetchException("Greenhouse returned no job list for '" + boardSlug + "'");
            }
            return GreenhouseMapper.toFetchedJobs(body);
        } catch (RestClientException e) {
            throw new BoardFetchException("Greenhouse request failed for '" + boardSlug + "': " + e.getMessage(), e);
        }
    }
}
