package com.job.seeker.filter;

/** Why a job was skipped. The code is stored (with a short detail) in application.filter_reason. */
public enum FilterReason {
    BLOCKLISTED_COMPANY,
    EXCLUDED_TITLE_KEYWORD,
    TITLE_NOT_RELEVANT,
    LOCATION_NOT_INDIA,
    LOCATION_UNRECOGNIZED,
    LOCATION_COUNTRY_UNKNOWN,
    LOCATION_NOT_PREFERRED,
    REMOTE_NOT_WANTED,
    EXPERIENCE_TOO_SENIOR,
    EXPERIENCE_TOO_JUNIOR,
    DEAL_BREAKER,
    DUPLICATE,
    JOB_CLOSED;

    /** True for outcomes that mean a person should look at the job instead of skipping it. */
    public boolean needsReview() {
        return this == LOCATION_COUNTRY_UNKNOWN;
    }
}
