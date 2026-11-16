package com.job.seeker.common;

/** The cross-source "same job" fingerprint: company + title + location. */
public final class JobHashing {

    private JobHashing() {
    }

    public static String dedupeHash(String company, String title, String location) {
        return Hashing.sha256Hex(CompanyNames.normalize(company)
                + "|" + Texts.normalize(title)
                + "|" + Texts.normalize(location));
    }
}
