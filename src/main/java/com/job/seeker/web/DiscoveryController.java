package com.job.seeker.web;

import com.job.seeker.discovery.DiscoveryRunResult;
import com.job.seeker.discovery.DiscoveryService;
import com.job.seeker.filter.FilterResult;
import com.job.seeker.filter.FilterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Manual triggers, handy for testing. The scheduler does the same on a timer. Both calls wait until done. */
@RestController
@RequestMapping("/api")
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final FilterService filterService;

    public DiscoveryController(DiscoveryService discoveryService, FilterService filterService) {
        this.discoveryService = discoveryService;
        this.filterService = filterService;
    }

    @PostMapping("/discovery/run")
    public DiscoveryRunResult runDiscovery() {
        return discoveryService.runAll();
    }

    @PostMapping("/filter/run")
    public FilterResult runFilter(@RequestParam(defaultValue = "false") boolean refilter) {
        return filterService.run(refilter);
    }
}
