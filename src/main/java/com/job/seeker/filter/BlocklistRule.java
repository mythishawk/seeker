package com.job.seeker.filter;

import com.job.seeker.common.CompanyNames;
import java.util.Optional;

/** Rejects jobs at blocklisted companies (current employer, companies the user rules out). */
public final class BlocklistRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        for (String blocked : context.settings().blockedCompanies()) {
            if (CompanyNames.matches(context.company(), blocked)) {
                return Optional.of(new Rejection(FilterReason.BLOCKLISTED_COMPANY, blocked));
            }
        }
        return Optional.empty();
    }
}
