package com.job.seeker.discovery;

import java.time.Instant;
import java.util.List;

/** Summary of one discovery run over the whole watchlist. */
public record DiscoveryRunResult(
        Instant startedAt,
        Instant finishedAt,
        boolean skippedBecauseAlreadyRunning,
        int companiesOk,
        int companiesFailed,
        int newJobs,
        int closedJobs,
        List<CompanyResult> companies) {

    public static DiscoveryRunResult of(Instant startedAt, Instant finishedAt, List<CompanyResult> companies) {
        int ok = 0;
        int failed = 0;
        int created = 0;
        int closed = 0;
        for (CompanyResult c : companies) {
            if (c.ok()) {
                ok++;
                created += c.created();
                closed += c.closed();
            } else {
                failed++;
            }
        }
        return new DiscoveryRunResult(startedAt, finishedAt, false, ok, failed, created, closed, List.copyOf(companies));
    }

    public static DiscoveryRunResult busy(Instant now) {
        return new DiscoveryRunResult(now, now, true, 0, 0, 0, 0, List.of());
    }
}
