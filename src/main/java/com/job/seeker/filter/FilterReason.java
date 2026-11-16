package com.job.seeker.filter;

/** Why a job was skipped. The code is stored (with a short detail) in application.filter_reason. */
public enum FilterReason {
    BLOCKLISTED_COMPANY,
    EXCLUDED_TITLE_KEYWORD,
    LOCATION_NOT_INDIA,
    LOCATION_UNRECOGNIZED,
    LOCATION_NOT_PREFERRED,
    REMOTE_NOT_WANTED,
    EXPERIENCE_TOO_SENIOR,
    EXPERIENCE_TOO_JUNIOR,
    DEAL_BREAKER,
    DUPLICATE,
    JOB_CLOSED
}
