package com.job.seeker.filter;

import java.util.Optional;

/**
 * Rejects jobs asking for far more or far less experience than the user's range.
 * Unknown requirements pass. A job asking "5+" has no upper bound, so it is only checked for being too senior.
 */
public final class ExperienceRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        ExperienceRange job = context.experience();
        if (job == null) {
            return Optional.empty();
        }
        Integer prefMin = context.settings().minExperienceYears();
        Integer prefMax = context.settings().maxExperienceYears();

        if (prefMax != null && job.min() != null && job.min() > prefMax) {
            return Optional.of(new Rejection(FilterReason.EXPERIENCE_TOO_SENIOR,
                    "asks " + job.min() + "+ years, limit " + prefMax));
        }
        if (prefMin != null && job.max() != null && job.max() < prefMin) {
            return Optional.of(new Rejection(FilterReason.EXPERIENCE_TOO_JUNIOR,
                    "asks up to " + job.max() + " years, minimum " + prefMin));
        }
        return Optional.empty();
    }
}
