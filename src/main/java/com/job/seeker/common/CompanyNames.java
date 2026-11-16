package com.job.seeker.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Company-name cleanup so "Accenture India Pvt. Ltd." and "accenture" can be compared. */
public final class CompanyNames {

    private static final Set<String> LEGAL_SUFFIXES = Set.of(
            "pvt", "private", "ltd", "limited", "inc", "incorporated", "llc", "llp",
            "corp", "corporation", "co", "gmbh", "plc", "ag", "bv", "pte");

    private CompanyNames() {
    }

    /** Normalizes the name and drops trailing legal suffixes (Pvt, Ltd, Inc and so on). */
    public static String normalize(String name) {
        String normalized = Texts.normalize(name);
        if (normalized.isEmpty()) {
            return normalized;
        }
        List<String> words = new ArrayList<>(Arrays.asList(normalized.split(" ")));
        while (words.size() > 1 && LEGAL_SUFFIXES.contains(words.get(words.size() - 1))) {
            words.remove(words.size() - 1);
        }
        return String.join(" ", words);
    }

    /** True when the company equals the blocked name, or starts with it as whole words. */
    public static boolean matches(String normalizedCompany, String normalizedBlocked) {
        if (normalizedBlocked.isEmpty()) {
            return false;
        }
        return normalizedCompany.equals(normalizedBlocked)
                || normalizedCompany.startsWith(normalizedBlocked + " ");
    }
}
