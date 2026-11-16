package com.job.seeker.filter;

import java.util.Optional;
import java.util.Set;

/**
 * Location and relocation rules.
 * <ul>
 *   <li>Blank location: let it through (nothing to judge).</li>
 *   <li>Mentions India or an Indian city: remote jobs need remote_ok; city jobs need willing_to_relocate, no city
 *       preference, or a preferred city; "India" alone passes because the city is unknown.</li>
 *   <li>Only other countries: rejected as not in India. Plain "Remote": allowed only when remote_ok.</li>
 *   <li>Text we do not recognize: rejected as LOCATION_UNRECOGNIZED so it can be reviewed and added to the
 *       location files.</li>
 * </ul>
 */
public final class LocationRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        NormalizedLocation loc = context.location();
        FilterSettings settings = context.settings();
        if (loc.blank()) {
            return Optional.empty();
        }

        if (loc.indiaSignal()) {
            boolean ok = false;
            if (loc.remote() && settings.remoteOk()) {
                ok = true;
            }
            if (!loc.cities().isEmpty()) {
                if (settings.willingToRelocate()
                        || settings.preferredCities().isEmpty()
                        || intersects(loc.cities(), settings.preferredCities())) {
                    ok = true;
                }
            } else if (!loc.remote()) {
                ok = true;
            }
            if (ok) {
                return Optional.empty();
            }
            if (loc.cities().isEmpty()) {
                return Optional.of(new Rejection(FilterReason.REMOTE_NOT_WANTED, loc.canonical()));
            }
            return Optional.of(new Rejection(FilterReason.LOCATION_NOT_PREFERRED, loc.canonical()));
        }

        if (loc.foreignCountries().isEmpty()) {
            if (loc.remote()) {
                return settings.remoteOk()
                        ? Optional.empty()
                        : Optional.of(new Rejection(FilterReason.REMOTE_NOT_WANTED, loc.canonical()));
            }
            return Optional.of(new Rejection(FilterReason.LOCATION_UNRECOGNIZED, loc.raw()));
        }
        return Optional.of(new Rejection(FilterReason.LOCATION_NOT_INDIA, loc.canonical()));
    }

    private static boolean intersects(Set<String> a, Set<String> b) {
        for (String value : a) {
            if (b.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
