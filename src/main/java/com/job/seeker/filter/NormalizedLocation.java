package com.job.seeker.filter;

import java.util.Set;

/**
 * A job location after cleanup.
 *
 * @param raw              the original text, trimmed
 * @param canonical        a tidy label to store, such as "Pune", "Remote - India" or "India"
 * @param cities           canonical Indian cities found (sorted)
 * @param foreignCountries country codes other than India found (sorted)
 * @param indiaMentioned   India (or an Indian state) was named
 * @param remote           the text says remote / work from home / anywhere
 */
public record NormalizedLocation(
        String raw,
        String canonical,
        Set<String> cities,
        Set<String> foreignCountries,
        boolean indiaMentioned,
        boolean remote) {

    public boolean blank() {
        return raw == null || raw.isBlank();
    }

    /** Some evidence the job can be done from India. */
    public boolean indiaSignal() {
        return indiaMentioned || !cities.isEmpty();
    }

    /** We understood at least something in the text. */
    public boolean recognized() {
        return indiaSignal() || !foreignCountries.isEmpty() || remote;
    }
}
