package com.job.seeker.discovery;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Settings for job discovery (prefix seeker.discovery in application.properties). */
@ConfigurationProperties(prefix = "seeker.discovery")
public record DiscoveryProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("PT6H") Duration interval,
        @DefaultValue("PT1M") Duration initialDelay,
        @DefaultValue("PT1S") Duration delayBetweenCompanies,
        @DefaultValue("PT30S") Duration requestTimeout,
        @DefaultValue("seeker-job-agent/0.1 (personal job search)") String userAgent,
        @DefaultValue("https://boards-api.greenhouse.io") String greenhouseBaseUrl,
        @DefaultValue("https://api.lever.co") String leverBaseUrl,
        @DefaultValue("https://api.eu.lever.co") String leverEuBaseUrl) {
}
