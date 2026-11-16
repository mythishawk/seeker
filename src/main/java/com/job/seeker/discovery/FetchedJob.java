package com.job.seeker.discovery;

import java.time.Instant;

/** One job as read from a company job board, before it is saved. */
public record FetchedJob(
        String externalId,
        String title,
        String location,
        String description,
        String url,
        Instant postedAt) {
}
