package com.job.seeker.filter;

import com.job.seeker.common.CompanyNames;
import com.job.seeker.common.JobHashing;
import com.job.seeker.common.Texts;
import com.job.seeker.domain.Application;
import com.job.seeker.domain.ApplicationStatus;
import com.job.seeker.domain.Job;
import com.job.seeker.domain.Preference;
import com.job.seeker.repository.ApplicationRepository;
import com.job.seeker.repository.CompanyBlocklistRepository;
import com.job.seeker.repository.PreferenceRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Cleans each new job (location, years of experience, dedupe fingerprint) and applies the hard rules.
 * Jobs that fail a rule become SKIPPED with a reason; jobs that pass stay DISCOVERED and wait for scoring.
 * No AI is involved. Work is done in batches of 200, each in its own transaction.
 */
@Service
public class FilterService {

    private static final Logger log = LoggerFactory.getLogger(FilterService.class);
    private static final int BATCH_SIZE = 200;
    private static final int MAX_LOCATION_LENGTH = 200;

    private final PreferenceRepository preferenceRepository;
    private final CompanyBlocklistRepository blocklistRepository;
    private final ApplicationRepository applicationRepository;
    private final LocationNormalizer locationNormalizer;
    private final ExperienceParser experienceParser;
    private final FilterEngine filterEngine;
    private final Clock clock;
    private final TransactionTemplate tx;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public FilterService(PreferenceRepository preferenceRepository,
                         CompanyBlocklistRepository blocklistRepository,
                         ApplicationRepository applicationRepository,
                         LocationNormalizer locationNormalizer,
                         ExperienceParser experienceParser,
                         FilterEngine filterEngine,
                         Clock clock,
                         PlatformTransactionManager transactionManager) {
        this.preferenceRepository = preferenceRepository;
        this.blocklistRepository = blocklistRepository;
        this.applicationRepository = applicationRepository;
        this.locationNormalizer = locationNormalizer;
        this.experienceParser = experienceParser;
        this.filterEngine = filterEngine;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /**
     * @param refilter false: only jobs not looked at yet. true: look at every DISCOVERED job again and give
     *                 jobs skipped by a filter a second chance (use after changing preferences or location files).
     */
    public FilterResult run(boolean refilter) {
        if (!running.compareAndSet(false, true)) {
            return FilterResult.busy();
        }
        try {
            FilterSettings settings = tx.execute(status -> loadSettings());
            if (settings == null) {
                log.warn("Filtering skipped: no preference row found");
                return FilterResult.unconfigured();
            }
            Tally tally = new Tally();
            long afterId = 0;
            while (true) {
                final long cursor = afterId;
                Long lastId = tx.execute(status -> processBatch(cursor, refilter, settings, tally));
                if (lastId == null) {
                    break;
                }
                afterId = lastId;
            }
            log.info("Filtering finished: {} looked at, {} passed, {} skipped", tally.processed, tally.passed, tally.skipped);
            return tally.toResult();
        } finally {
            running.set(false);
        }
    }

    private FilterSettings loadSettings() {
        Optional<Preference> first = preferenceRepository.findAll().stream().findFirst();
        if (first.isEmpty()) {
            return null;
        }
        Preference preference = first.get();

        Set<String> blocked = new TreeSet<>();
        blocklistRepository.findAll().forEach(entry -> {
            String name = CompanyNames.normalize(entry.getCompanyName());
            if (!name.isEmpty()) {
                blocked.add(name);
            }
        });

        Set<String> preferredCities = new TreeSet<>();
        for (String location : preference.getLocations()) {
            if (location != null) {
                preferredCities.addAll(locationNormalizer.normalize(location).cities());
            }
        }

        return new FilterSettings(
                blocked,
                normalizeAll(preference.getExcludedTitleKeywords()),
                normalizeAll(preference.getDealBreakerKeywords()),
                preferredCities,
                preference.isRemoteOk(),
                preference.getProfile().isWillingToRelocate(),
                preference.getMinExperienceYears(),
                preference.getMaxExperienceYears());
    }

    private static List<String> normalizeAll(List<String> values) {
        List<String> out = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                String normalized = Texts.normalize(value);
                if (!normalized.isEmpty()) {
                    out.add(normalized);
                }
            }
        }
        return out;
    }

    /** Returns the id of the last application handled, or null when there was nothing left. */
    private Long processBatch(long afterId, boolean refilter, FilterSettings settings, Tally tally) {
        Pageable page = PageRequest.of(0, BATCH_SIZE);
        List<Application> batch = refilter
                ? applicationRepository.findRefilterCandidates(afterId, ApplicationStatus.DISCOVERED, ApplicationStatus.SKIPPED, page)
                : applicationRepository.findPendingFilter(afterId, ApplicationStatus.DISCOVERED, page);
        if (batch.isEmpty()) {
            return null;
        }
        Instant now = clock.instant();
        for (Application application : batch) {
            evaluate(application, settings, now, tally);
        }
        return batch.get(batch.size() - 1).getId();
    }

    private void evaluate(Application application, FilterSettings settings, Instant now, Tally tally) {
        Job job = application.getJob();

        NormalizedLocation location = locationNormalizer.normalize(job.getLocation());
        ExperienceRange experience = experienceParser.parse(job.getTitle(), job.getDescription()).orElse(null);

        job.setLocationNormalized(Texts.truncate(location.canonical(), MAX_LOCATION_LENGTH));
        job.setMinYears(experience == null ? null : experience.min());
        job.setMaxYears(experience == null ? null : experience.max());
        job.setDedupeHash(JobHashing.dedupeHash(job.getCompany(), job.getTitle(), location.canonical()));
        job.setFilteredAt(now);

        FilterContext context = FilterContext.of(job.getCompany(), job.getTitle(), job.getDescription(),
                location, experience, settings);
        Optional<Rejection> rejection = filterEngine.firstRejection(context);
        if (rejection.isEmpty()
                && applicationRepository.existsLiveTwin(job.getDedupeHash(), job.getId(), ApplicationStatus.SKIPPED)) {
            rejection = Optional.of(new Rejection(FilterReason.DUPLICATE, "same company, title and location as an earlier posting"));
        }

        tally.processed++;
        if (rejection.isPresent()) {
            application.setStatus(ApplicationStatus.SKIPPED);
            application.setFilterReason(rejection.get().format());
            tally.skipped++;
            tally.reasons.merge(rejection.get().reason().name(), 1, Integer::sum);
        } else {
            application.setStatus(ApplicationStatus.DISCOVERED);
            application.setFilterReason(null);
            tally.passed++;
        }
    }

    private static final class Tally {
        int processed;
        int passed;
        int skipped;
        final Map<String, Integer> reasons = new TreeMap<>();

        FilterResult toResult() {
            return new FilterResult(true, false, "Done.", processed, passed, skipped, Map.copyOf(reasons));
        }
    }
}
