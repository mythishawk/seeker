package com.job.seeker.filter;

import com.job.seeker.common.Texts;
import java.util.List;
import java.util.Optional;

/**
 * Keeps only the kind of role the user wants: the title must contain at least one of the required words or phrases
 * ("backend", "software engineer", "java"...). With no required words set, every title passes.
 */
public final class RoleKeywordRule implements FilterRule {

    @Override
    public Optional<Rejection> check(FilterContext context) {
        List<String> required = context.settings().requiredTitleKeywords();
        if (required == null || required.isEmpty()) {
            return Optional.empty();
        }
        for (String keyword : required) {
            if (Texts.containsPhrase(context.paddedTitle(), keyword)) {
                return Optional.empty();
            }
        }
        return Optional.of(new Rejection(FilterReason.TITLE_NOT_RELEVANT, Texts.truncate(context.paddedTitle().trim(), 120)));
    }
}
