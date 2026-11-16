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
                new RoleKeywordRule(),
                new LocationRule(),
                new ExperienceRule(),
                new DealBreakerRule()));
    }

    /**
     * The first hard rejection, if any. A review result (see Rejection.isReview) does not stop the remaining rules,
     * because a later rule may still reject the job outright; if none does, the first review result is returned.
     */
    public Optional<Rejection> firstRejection(FilterContext context) {
        Optional<Rejection> review = Optional.empty();
        for (FilterRule rule : rules) {
            Optional<Rejection> rejection = rule.check(context);
            if (rejection.isEmpty()) {
                continue;
            }
            if (!rejection.get().isReview()) {
                return rejection;
            }
            if (review.isEmpty()) {
                review = rejection;
            }
        }
        return review;
    }
}
