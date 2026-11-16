package com.job.seeker.filter;

import com.job.seeker.common.Texts;
import java.util.Optional;

/** Rejects jobs whose title contains an excluded word or phrase ("intern", "principal", "sales"...). */
public final class TitleKeywordRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        for (String keyword : context.settings().excludedTitleKeywords()) {
            if (Texts.containsPhrase(context.paddedTitle(), keyword)) {
                return Optional.of(new Rejection(FilterReason.EXCLUDED_TITLE_KEYWORD, keyword));
            }
        }
        return Optional.empty();
    }
}
