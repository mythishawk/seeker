package com.job.seeker.filter;

import java.util.List;
import java.util.Optional;

/** Runs the hard rules in order and returns the first rejection. All rules are plain Java, never AI. */
public final class FilterEngine {

    private final List<FilterRule> rules;

    public FilterEngine(List<FilterRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /** Cheapest rules first; the description-scanning rule goes last. */
    public static FilterEngine standard() {
        return new FilterEngine(List.of(
                new BlocklistRule(),
                new TitleKeywordRule(),
                new LocationRule(),
                new ExperienceRule(),
                new DealBreakerRule()));
    }

    public Optional<Rejection> firstRejection(FilterContext context) {
        for (FilterRule rule : rules) {
            Optional<Rejection> rejection = rule.check(context);
            if (rejection.isPresent()) {
                return rejection;
            }
        }
        return Optional.empty();
    }
}
