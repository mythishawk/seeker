package com.job.seeker.discovery;

/** What happened for one watchlist company in a discovery run. */
public record CompanyResult(
        Long sourceId,
        String company,
        String sourceType,
        boolean ok,
        int fetched,
        int created,
        int updated,
        int reopened,
        int closed,
        String error) {

    public static CompanyResult success(Long sourceId, String company, String sourceType,
                                        int fetched, int created, int updated, int reopened, int closed) {
        return new CompanyResult(sourceId, company, sourceType, true, fetched, created, updated, reopened, closed, null);
    }

    public static CompanyResult failure(Long sourceId, String company, String sourceType, String error) {
        return new CompanyResult(sourceId, company, sourceType, false, 0, 0, 0, 0, 0, error);
    }
}
