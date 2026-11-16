package com.job.seeker.filter;

import com.job.seeker.common.Texts;

/** A rule said no. */
public record Rejection(FilterReason reason, String detail) {

    /** A soft result: the job goes to the review list instead of being skipped. */
    public boolean isReview() {
        return reason.needsReview();
    }

    /** The text stored in application.filter_reason, for example "BLOCKLISTED_COMPANY: accenture". */
    public String format() {
        String text = detail == null || detail.isBlank() ? reason.name() : reason.name() + ": " + detail;
        return Texts.truncate(text, 300);
    }
}
