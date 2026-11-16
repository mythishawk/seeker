package com.job.seeker.filter;

import java.util.Map;

/** Summary of one filtering run. */
public record FilterResult(
        boolean configured,
        boolean alreadyRunning,
        String message,
        int processed,
        int passed,
        int skipped,
        Map<String, Integer> skippedByReason) {

    public static FilterResult unconfigured() {
        return new FilterResult(false, false,
                "No preference row found. Add a profile and preference first (see docs/dev-seed.sql).",
                0, 0, 0, Map.of());
    }

    public static FilterResult busy() {
        return new FilterResult(true, true, "A filtering run is already in progress.", 0, 0, 0, Map.of());
    }
}
