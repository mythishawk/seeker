package com.job.seeker.filter;

import com.job.seeker.common.CompanyNames;
import com.job.seeker.common.Texts;

/** One job, prepared for the rules. Text is normalized and padded once, not once per rule. */
public record FilterContext(
        String company,
        String paddedTitle,
        String paddedDescription,
        NormalizedLocation location,
        ExperienceRange experience,
        FilterSettings settings) {

    public static FilterContext of(String company, String title, String description,
                                   NormalizedLocation location, ExperienceRange experience,
                                   FilterSettings settings) {
        return new FilterContext(
                CompanyNames.normalize(company),
                Texts.pad(Texts.normalize(title)),
                Texts.pad(Texts.normalize(description)),
                location,
                experience,
                settings);
    }
}
