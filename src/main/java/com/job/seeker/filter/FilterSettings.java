package com.job.seeker.filter;

import java.util.List;
import java.util.Set;

/**
 * Everything the hard filters need from the user's profile and preferences.
 * Names and keywords are already normalized (see Texts.normalize / CompanyNames.normalize).
 *
 * @param blockedCompanies   normalized company names that must never be applied to
 * @param excludedTitleKeywords normalized words or phrases that reject a job when found in its title
 * @param dealBreakerKeywords   normalized phrases that reject a job when found in its title or description
 * @param preferredCities    canonical cities the user wants (empty means no preference)
 * @param remoteOk           remote jobs are welcome
 * @param willingToRelocate  any Indian city is fine
 * @param minExperienceYears lowest years-required the user will apply to (null = no limit)
 * @param maxExperienceYears highest years-required the user will apply to (null = no limit)
 */
public record FilterSettings(
        Set<String> blockedCompanies,
        List<String> excludedTitleKeywords,
        List<String> dealBreakerKeywords,
        Set<String> preferredCities,
        boolean remoteOk,
        boolean willingToRelocate,
        Integer minExperienceYears,
        Integer maxExperienceYears) {
}
