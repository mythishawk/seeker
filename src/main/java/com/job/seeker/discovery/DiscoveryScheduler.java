package com.job.seeker.discovery;

import com.job.seeker.filter.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** The timer: every few hours, find new jobs and then run the hard filters on them. */
@Component
@ConditionalOnProperty(prefix = "seeker.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DiscoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryScheduler.class);

    private final DiscoveryService discoveryService;
    private final FilterService filterService;

    public DiscoveryScheduler(DiscoveryService discoveryService, FilterService filterService) {
        this.discoveryService = discoveryService;
        this.filterService = filterService;
    }

    @Scheduled(
            fixedDelayString = "${seeker.discovery.interval:PT6H}",
            initialDelayString = "${seeker.discovery.initial-delay:PT1M}")
    public void run() {
        try {
            discoveryService.runAll();
            filterService.run(false);
        } catch (RuntimeException e) {
            log.error("Scheduled discovery run failed", e);
        }
    }
}
