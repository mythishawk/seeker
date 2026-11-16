package com.job.seeker.filter;

import com.job.seeker.common.Texts;
import java.util.Optional;

/** Rejects jobs whose title or description contains one of the user's deal-breaker phrases. */
public final class DealBreakerRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        for (String phrase : context.settings().dealBreakerKeywords()) {
            if (Texts.containsPhrase(context.paddedTitle(), phrase)
                    || Texts.containsPhrase(context.paddedDescription(), phrase)) {
                return Optional.of(new Rejection(FilterReason.DEAL_BREAKER, phrase));
            }
        }
        return Optional.empty();
    }
}
