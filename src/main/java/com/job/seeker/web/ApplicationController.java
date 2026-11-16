package com.job.seeker.web;

import com.job.seeker.domain.Application;
import com.job.seeker.domain.ApplicationStatus;
import com.job.seeker.domain.Job;
import com.job.seeker.repository.ApplicationRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only view of what the agent has found, newest first. Filter by status, for example SKIPPED. */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    public record ApplicationSummary(Long applicationId, Long jobId, String company, String title, String location,
                                     String locationNormalized, Integer minYears, Integer maxYears,
                                     ApplicationStatus status, String filterReason, String url) {
        static ApplicationSummary from(Application a) {
            Job j = a.getJob();
            return new ApplicationSummary(a.getId(), j.getId(), j.getCompany(), j.getTitle(), j.getLocation(),
                    j.getLocationNormalized(), j.getMinYears(), j.getMaxYears(), a.getStatus(),
                    a.getFilterReason(), j.getUrl());
        }
    }

    private final ApplicationRepository repository;

    public ApplicationController(ApplicationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ApplicationSummary> list(@RequestParam(required = false) ApplicationStatus status,
                                         @RequestParam(defaultValue = "100") int limit) {
        Pageable page = PageRequest.of(0, Math.min(Math.max(limit, 1), 500));
        List<Application> applications = status == null
                ? repository.findAllWithJob(page)
                : repository.findByStatusWithJob(status, page);
        return applications.stream().map(ApplicationSummary::from).toList();
    }
}
