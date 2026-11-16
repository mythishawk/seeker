package com.job.seeker.discovery;

import com.job.seeker.common.Hashing;
import com.job.seeker.common.JobHashing;
import com.job.seeker.common.Texts;
import com.job.seeker.domain.Application;
import com.job.seeker.domain.ApplicationStatus;
import com.job.seeker.domain.CompanySource;
import com.job.seeker.domain.Job;
import com.job.seeker.domain.SourceType;
import com.job.seeker.filter.FilterReason;
import com.job.seeker.repository.ApplicationRepository;
import com.job.seeker.repository.CompanySourceRepository;
import com.job.seeker.repository.JobRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Walks the watchlist, reads each company's job board, and keeps the job and application tables in step.
 *
 * <p>Per company: fetch (no database transaction held), then save everything in one transaction. A company that
 * fails is recorded and skipped; it never stops the others. Running twice in a row changes nothing.
 */
@Service
public class DiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryService.class);

    /** A board that suddenly returns nothing while we know of this many open jobs is treated as a bad fetch. */
    private static final int SUSPICIOUS_EMPTY_OPEN_JOBS = 5;

    private final CompanySourceRepository companySourceRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final Map<SourceType, JobBoardClient> clients = new EnumMap<>(SourceType.class);
    private final DiscoveryProperties properties;
    private final Clock clock;
    private final TransactionTemplate tx;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public DiscoveryService(CompanySourceRepository companySourceRepository,
                            JobRepository jobRepository,
                            ApplicationRepository applicationRepository,
                            List<JobBoardClient> boardClients,
                            DiscoveryProperties properties,
                            Clock clock,
                            PlatformTransactionManager transactionManager) {
        this.companySourceRepository = companySourceRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.properties = properties;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
        for (JobBoardClient client : boardClients) {
            clients.put(client.type(), client);
        }
    }

    /** Checks every active company on the watchlist. Only one run at a time. */
    public DiscoveryRunResult runAll() {
        if (!running.compareAndSet(false, true)) {
            return DiscoveryRunResult.busy(clock.instant());
        }
        Instant started = clock.instant();
        try {
            List<CompanySource> sources = companySourceRepository.findByActiveTrue();
            List<CompanyResult> results = new ArrayList<>();
            for (int i = 0; i < sources.size(); i++) {
                results.add(discoverOne(sources.get(i)));
                if (i < sources.size() - 1) {
                    pause();
                }
            }
            DiscoveryRunResult result = DiscoveryRunResult.of(started, clock.instant(), results);
            log.info("Discovery finished: {} companies ok, {} failed, {} new jobs, {} closed",
                    result.companiesOk(), result.companiesFailed(), result.newJobs(), result.closedJobs());
            return result;
        } finally {
            running.set(false);
        }
    }

    private CompanyResult discoverOne(CompanySource source) {
        Long id = source.getId();
        String type = source.getSourceType().name();
        JobBoardClient client = clients.get(source.getSourceType());
        if (client == null) {
            String error = "No reader for source type " + type;
            recordFailure(id, error);
            return CompanyResult.failure(id, source.getCompany(), type, error);
        }

        List<FetchedJob> fetched;
        try {
            fetched = client.fetch(source.getBoardSlug());
        } catch (RuntimeException e) {
            String error = describe(e);
            log.warn("Discovery failed for {} ({} {}): {}", source.getCompany(), type, source.getBoardSlug(), error);
            recordFailure(id, error);
            return CompanyResult.failure(id, source.getCompany(), type, error);
        }

        try {
            return tx.execute(status -> saveFetched(id, fetched));
        } catch (RuntimeException e) {
            String error = describe(e);
            log.error("Saving jobs failed for {} ({}): {}", source.getCompany(), type, error, e);
            recordFailure(id, error);
            return CompanyResult.failure(id, source.getCompany(), type, error);
        }
    }

    /** Runs inside one transaction: either everything for this company is saved or nothing is. */
    private CompanyResult saveFetched(Long sourceId, List<FetchedJob> fetched) {
        CompanySource source = companySourceRepository.findById(sourceId).orElseThrow();
        Instant now = clock.instant();
        String sourceKey = source.getSourceType().name().toLowerCase(Locale.ROOT);

        Map<String, Job> existing = new HashMap<>();
        for (Job job : jobRepository.findByCompanySourceId(sourceId)) {
            existing.put(job.getExternalId(), job);
        }
        Map<Long, Application> applicationsByJob = new HashMap<>();
        for (Application application : applicationRepository.findByCompanySourceIdFetchJob(sourceId)) {
            applicationsByJob.put(application.getJob().getId(), application);
        }

        Set<String> seen = new HashSet<>();
        int created = 0;
        int updated = 0;
        int reopened = 0;
        for (FetchedJob f : fetched) {
            if (isBlank(f.externalId()) || isBlank(f.title()) || !seen.add(f.externalId())) {
                continue;
            }
            Job job = existing.get(f.externalId());
            if (job == null) {
                Job elsewhere = jobRepository.findBySourceAndExternalId(sourceKey, f.externalId()).orElse(null);
                if (elsewhere != null) {
                    // Already tracked under another watchlist row: just note that it is still open.
                    elsewhere.setLastSeenAt(now);
                    continue;
                }
                createJob(source, sourceKey, f, now);
                created++;
                continue;
            }

            job.setLastSeenAt(now);
            Application application = applicationsByJob.get(job.getId());
            if (job.getClosedAt() != null) {
                job.setClosedAt(null);
                reopen(job, application);
                reopened++;
            }
            if (refreshContent(job, f, application)) {
                updated++;
            }
        }

        int closed = closeMissing(existing, seen, fetched.isEmpty(), applicationsByJob, now);

        source.setLastCheckedAt(now);
        source.setLastError(null);
        source.setConsecutiveFailures(0);
        return CompanyResult.success(sourceId, source.getCompany(), source.getSourceType().name(),
                fetched.size(), created, updated, reopened, closed);
    }

    private void createJob(CompanySource source, String sourceKey, FetchedJob f, Instant now) {
        Job job = new Job();
        job.setCompanySource(source);
        job.setSource(sourceKey);
        job.setExternalId(f.externalId());
        job.setCompany(source.getCompany());
        applyContent(job, f);
        job.setLastSeenAt(now);
        jobRepository.save(job);

        Application application = new Application();
        application.setJob(job);
        application.setStatus(ApplicationStatus.DISCOVERED);
        applicationRepository.save(application);
    }

    private void applyContent(Job job, FetchedJob f) {
        job.setTitle(Texts.truncate(f.title().trim(), 300));
        job.setLocation(Texts.truncate(f.location(), 300));
        job.setDescription(f.description());
        job.setDescriptionHash(isBlank(f.description()) ? null : Hashing.sha256Hex(f.description()));
        job.setUrl(Texts.truncate(f.url(), 1000));
        job.setPostedAt(f.postedAt());
        job.setDedupeHash(JobHashing.dedupeHash(job.getCompany(), job.getTitle(), job.getLocation()));
    }

    /** If the posting's title, location or text changed, store the new version and ask for a fresh filter pass. */
    private boolean refreshContent(Job job, FetchedJob f, Application application) {
        String newTitle = Texts.truncate(f.title().trim(), 300);
        String newLocation = Texts.truncate(f.location(), 300);
        String newHash = isBlank(f.description()) ? null : Hashing.sha256Hex(f.description());
        boolean changed = !Objects.equals(job.getTitle(), newTitle)
                || !Objects.equals(job.getLocation(), newLocation)
                || !Objects.equals(job.getDescriptionHash(), newHash);
        if (!changed) {
            return false;
        }
        applyContent(job, f);
        job.setFilteredAt(null);
        if (application != null && (application.getStatus() == ApplicationStatus.DISCOVERED || skippedByFilter(application))) {
            application.setStatus(ApplicationStatus.DISCOVERED);
            application.setFilterReason(null);
        }
        return true;
    }

    private int closeMissing(Map<String, Job> existing, Set<String> seen, boolean fetchedNothing,
                             Map<Long, Application> applicationsByJob, Instant now) {
        List<Job> openMissing = new ArrayList<>();
        int openBefore = 0;
        for (Job job : existing.values()) {
            if (job.getClosedAt() == null) {
                openBefore++;
                if (!seen.contains(job.getExternalId())) {
                    openMissing.add(job);
                }
            }
        }
        if (fetchedNothing && openBefore >= SUSPICIOUS_EMPTY_OPEN_JOBS) {
            log.warn("Board returned no jobs although {} are open here; not closing anything this run", openBefore);
            return 0;
        }
        for (Job job : openMissing) {
            close(job, applicationsByJob.get(job.getId()), now);
        }
        return openMissing.size();
    }

    private void close(Job job, Application application, Instant now) {
        job.setClosedAt(now);
        if (application == null) {
            return;
        }
        switch (application.getStatus()) {
            case DISCOVERED, MATCHED -> {
                application.setStatus(ApplicationStatus.SKIPPED);
                application.setFilterReason(FilterReason.JOB_CLOSED.name());
            }
            case DRAFTED, AWAITING_APPROVAL -> application.setStatus(ApplicationStatus.EXPIRED);
            default -> {
                // Already applied, in an interview, declined and so on: leave it alone.
            }
        }
    }

    private void reopen(Job job, Application application) {
        if (application != null
                && application.getStatus() == ApplicationStatus.SKIPPED
                && FilterReason.JOB_CLOSED.name().equals(application.getFilterReason())) {
            application.setStatus(ApplicationStatus.DISCOVERED);
            application.setFilterReason(null);
            job.setFilteredAt(null);
        }
    }

    private static boolean skippedByFilter(Application application) {
        return application.getStatus() == ApplicationStatus.SKIPPED
                && application.getFilterReason() != null
                && !application.getFilterReason().startsWith(FilterReason.JOB_CLOSED.name());
    }

    private void recordFailure(Long sourceId, String error) {
        try {
            tx.executeWithoutResult(status -> companySourceRepository.findById(sourceId).ifPresent(source -> {
                source.setLastCheckedAt(clock.instant());
                source.setLastError(Texts.truncate(error, 500));
                source.setConsecutiveFailures(source.getConsecutiveFailures() + 1);
            }));
        } catch (RuntimeException e) {
            log.error("Could not record the failure for source {}", sourceId, e);
        }
    }

    private void pause() {
        long millis = properties.delayBetweenCompanies().toMillis();
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String describe(Throwable e) {
        return Texts.truncate(e.getClass().getSimpleName() + ": " + e.getMessage(), 500);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
