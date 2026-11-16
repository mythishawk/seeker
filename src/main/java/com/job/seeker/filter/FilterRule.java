package com.job.seeker.filter;

import java.util.Optional;

/** One hard rule. Returns a Rejection to skip the job, or empty to let it through. */
public interface FilterRule {

    Optional<Rejection> check(FilterContext context);
}
