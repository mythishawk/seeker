package com.job.seeker.discovery;

import com.job.seeker.domain.SourceType;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Reads a company's open jobs from Lever's public postings API (no key needed).
 * Some companies are hosted in Lever's EU region, so a "not found" on the main host is retried on the EU host.
 * Lever occasionally answers with a web page instead of data; that surfaces as a failed fetch and is retried
 * on the next scheduled run.
 */
@Component
public class LeverClient implements JobBoardClient {

    private static final ParameterizedTypeReference<List<LeverMapper.Posting>> POSTINGS =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient globalClient;
    private final RestClient euClient;

    public LeverClient(DiscoveryProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.requestTimeout());
        requestFactory.setReadTimeout(properties.requestTimeout());
        this.globalClient = build(properties.leverBaseUrl(), properties.userAgent(), requestFactory);
        this.euClient = build(properties.leverEuBaseUrl(), properties.userAgent(), requestFactory);
    }

    private static RestClient build(String baseUrl, String userAgent, SimpleClientHttpRequestFactory factory) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .requestFactory(factory)
                .build();
    }

    @Override
    public SourceType type() {
        return SourceType.LEVER;
    }

    @Override
    public List<FetchedJob> fetch(String boardSlug) {
        try {
            List<LeverMapper.Posting> postings;
            try {
                postings = get(globalClient, boardSlug);
            } catch (HttpClientErrorException.NotFound e) {
                postings = get(euClient, boardSlug);
            }
            if (postings == null) {
                throw new BoardFetchException("Lever returned no posting list for '" + boardSlug + "'");
            }
            return LeverMapper.toFetchedJobs(postings);
        } catch (RestClientException e) {
            throw new BoardFetchException("Lever request failed for '" + boardSlug + "': " + e.getMessage(), e);
        }
    }

    private static List<LeverMapper.Posting> get(RestClient client, String slug) {
        return client.get()
                .uri("/v0/postings/{slug}?mode=json", slug)
                .retrieve()
                .body(POSTINGS);
    }
}
