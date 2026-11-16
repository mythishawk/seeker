package com.job.seeker.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.job.seeker.common.CompanyNames;
import com.job.seeker.common.Texts;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FilterRulesTest {

    private final LocationNormalizer locations = LocationNormalizer.fromClasspath();

    private FilterSettings settings(Set<String> preferredCities, boolean remoteOk, boolean relocate,
                                    Integer minYears, Integer maxYears) {
        return new FilterSettings(
                Set.of(CompanyNames.normalize("Accenture")),
                List.of(Texts.normalize("intern"), Texts.normalize("tech lead")),
                List.of(Texts.normalize("security clearance")),
                preferredCities, remoteOk, relocate, minYears, maxYears,
                List.of(Texts.normalize("backend"), Texts.normalize("back end"), Texts.normalize("software engineer"), Texts.normalize("java")));
    }

    private FilterContext context(String company, String title, String description, String location,
                                  ExperienceRange experience, FilterSettings settings) {
        return FilterContext.of(company, title, description, locations.normalize(location), experience, settings);
    }

    private FilterContext simple(String location, FilterSettings settings) {
        return context("Acme", "Java Developer", "Build services.", location, null, settings);
    }

    private FilterReason reasonOf(Optional<Rejection> rejection) {
        return rejection.map(Rejection::reason).orElse(null);
    }

    private final FilterSettings puneOnly = settings(Set.of("Pune"), false, false, 1, 5);
    private final FilterSettings relocating = settings(Set.of("Pune"), true, true, 1, 5);

    // ---- blocklist ----

    @Test
    void blocklistedCompanyIsRejected() {
        FilterContext ctx = context("Accenture India Pvt Ltd", "Java Developer", "", "Pune", null, relocating);
        assertEquals(FilterReason.BLOCKLISTED_COMPANY, reasonOf(new BlocklistRule().check(ctx)));
    }

    @Test
    void similarCompanyNameIsNotBlocked() {
        FilterContext ctx = context("Accentureful Labs", "Java Developer", "", "Pune", null, relocating);
        assertTrue(new BlocklistRule().check(ctx).isEmpty());
    }

    // ---- title keywords ----

    @Test
    void excludedTitleWordsAreRejectedAsWholeWords() {
        assertEquals(FilterReason.EXCLUDED_TITLE_KEYWORD, reasonOf(new TitleKeywordRule().check(
                context("Acme", "Java Intern", "", "Pune", null, relocating))));
        assertEquals(FilterReason.EXCLUDED_TITLE_KEYWORD, reasonOf(new TitleKeywordRule().check(
                context("Acme", "Backend Tech Lead", "", "Pune", null, relocating))));
        assertTrue(new TitleKeywordRule().check(
                context("Acme", "Internal Tools Developer", "", "Pune", null, relocating)).isEmpty());
    }

    // ---- location ----

    @Test
    void preferredCityPasses() {
        assertTrue(new LocationRule().check(simple("Pune, MH", puneOnly)).isEmpty());
    }

    @Test
    void otherIndianCityIsRejectedWhenNotRelocating() {
        assertEquals(FilterReason.LOCATION_NOT_PREFERRED,
                reasonOf(new LocationRule().check(simple("Bangalore", puneOnly))));
    }

    @Test
    void otherIndianCityPassesWhenRelocating() {
        assertTrue(new LocationRule().check(simple("Bangalore", relocating)).isEmpty());
    }

    @Test
    void anyCityPassesWhenNoCityPreferenceIsSet() {
        FilterSettings noPreference = settings(Set.of(), false, false, null, null);
        assertTrue(new LocationRule().check(simple("Chennai", noPreference)).isEmpty());
    }

    @Test
    void multiCityJobPassesIfOneCityIsPreferred() {
        assertTrue(new LocationRule().check(simple("Mumbai; Pune", puneOnly)).isEmpty());
    }

    @Test
    void remoteIndiaNeedsRemoteOk() {
        assertTrue(new LocationRule().check(simple("Remote - India", relocating)).isEmpty());
        assertEquals(FilterReason.REMOTE_NOT_WANTED, reasonOf(new LocationRule().check(simple("Remote - India", puneOnly))));
    }

    @Test
    void plainRemoteNeedsRemoteOk() {
        assertEquals(FilterReason.LOCATION_COUNTRY_UNKNOWN, reasonOf(new LocationRule().check(simple("Remote", relocating))));
        assertEquals(FilterReason.REMOTE_NOT_WANTED, reasonOf(new LocationRule().check(simple("Remote", puneOnly))));
    }

    @Test
    void foreignJobsAreRejected() {
        assertEquals(FilterReason.LOCATION_NOT_INDIA, reasonOf(new LocationRule().check(simple("London, UK", relocating))));
        assertEquals(FilterReason.LOCATION_NOT_INDIA, reasonOf(new LocationRule().check(simple("Remote - US", relocating))));
    }

    @Test
    void indiaAloneAndBlankPass() {
        assertTrue(new LocationRule().check(simple("India", puneOnly)).isEmpty());
        assertTrue(new LocationRule().check(simple("", puneOnly)).isEmpty());
        assertTrue(new LocationRule().check(simple(null, puneOnly)).isEmpty());
    }

    @Test
    void unknownPlacesAreFlaggedForReview() {
        assertEquals(FilterReason.LOCATION_UNRECOGNIZED,
                reasonOf(new LocationRule().check(simple("Reykjavik", relocating))));
    }

    // ---- experience ----

    @Test
    void tooSeniorIsRejected() {
        FilterContext ctx = context("Acme", "Dev", "", "Pune", new ExperienceRange(7, null), puneOnly);
        assertEquals(FilterReason.EXPERIENCE_TOO_SENIOR, reasonOf(new ExperienceRule().check(ctx)));
    }

    @Test
    void tooJuniorIsRejected() {
        FilterContext ctx = context("Acme", "Dev", "", "Pune", new ExperienceRange(0, 0), puneOnly);
        assertEquals(FilterReason.EXPERIENCE_TOO_JUNIOR, reasonOf(new ExperienceRule().check(ctx)));
    }

    @Test
    void experienceInsideTheRangeOrUnknownPasses() {
        assertTrue(new ExperienceRule().check(
                context("Acme", "Dev", "", "Pune", new ExperienceRange(3, 5), puneOnly)).isEmpty());
        assertTrue(new ExperienceRule().check(
                context("Acme", "Dev", "", "Pune", new ExperienceRange(5, null), puneOnly)).isEmpty());
        assertTrue(new ExperienceRule().check(
                context("Acme", "Dev", "", "Pune", new ExperienceRange(0, 2), puneOnly)).isEmpty());
        assertTrue(new ExperienceRule().check(context("Acme", "Dev", "", "Pune", null, puneOnly)).isEmpty());
    }

    @Test
    void noExperienceLimitsMeansNothingIsRejected() {
        FilterSettings open = settings(Set.of(), true, true, null, null);
        assertTrue(new ExperienceRule().check(
                context("Acme", "Dev", "", "Pune", new ExperienceRange(15, null), open)).isEmpty());
    }

    // ---- deal-breakers ----

    @Test
    void dealBreakerPhraseInDescriptionIsRejected() {
        FilterContext ctx = context("Acme", "Dev", "Candidates must hold an active Security Clearance.", "Pune", null, relocating);
        assertEquals(FilterReason.DEAL_BREAKER, reasonOf(new DealBreakerRule().check(ctx)));
    }

    @Test
    void dealBreakerIsNotTriggeredByPartialWords() {
        FilterContext ctx = context("Acme", "Dev", "No clearance sale here, only security.", "Pune", null, relocating);
        assertTrue(new DealBreakerRule().check(ctx).isEmpty());
    }

    // ---- engine ----

    @Test
    void engineReturnsTheFirstRejectionInOrder() {
        FilterEngine engine = FilterEngine.standard();
        // Blocklisted AND in London: the blocklist rule runs first.
        FilterContext ctx = context("Accenture", "Java Developer", "", "London", null, relocating);
        Optional<Rejection> rejection = engine.firstRejection(ctx);
        assertEquals(FilterReason.BLOCKLISTED_COMPANY, rejection.get().reason());
        assertEquals("BLOCKLISTED_COMPANY: accenture", rejection.get().format());
    }

    @Test
    void engineLetsAGoodJobThrough() {
        FilterContext ctx = context("Acme", "Java Backend Developer", "3-5 years of experience.", "Pune",
                new ExperienceRange(3, 5), puneOnly);
        assertTrue(FilterEngine.standard().firstRejection(ctx).isEmpty());
    }

    // ---- role keywords ----

    @Test
    void titleMustContainARequiredWord() {
        assertTrue(new RoleKeywordRule().check(context("Acme", "Java Backend Developer", "", "Pune", null, relocating)).isEmpty());
        assertTrue(new RoleKeywordRule().check(context("Acme", "Senior Back-End Engineer", "", "Pune", null, relocating)).isEmpty());
        assertTrue(new RoleKeywordRule().check(context("Acme", "Software Engineer II", "", "Pune", null, relocating)).isEmpty());
        assertEquals(FilterReason.TITLE_NOT_RELEVANT,
                reasonOf(new RoleKeywordRule().check(context("Acme", "Risk Operations Analyst", "", "Pune", null, relocating))));
    }

    @Test
    void javaDoesNotMatchJavaScript() {
        assertEquals(FilterReason.TITLE_NOT_RELEVANT,
                reasonOf(new RoleKeywordRule().check(context("Acme", "JavaScript Designer", "", "Pune", null, relocating))));
    }

    @Test
    void noRequiredWordsMeansEveryTitlePasses() {
        FilterSettings open = new FilterSettings(Set.of(), List.of(), List.of(), Set.of(), true, true, null, null, List.of());
        assertTrue(new RoleKeywordRule().check(context("Acme", "Risk Operations Analyst", "", "Pune", null, open)).isEmpty());
    }

    // ---- review outcome and the engine ----

    @Test
    void plainRemoteIsAReviewResultNotARejection() {
        FilterContext ctx = context("Acme", "Java Developer", "", "Remote", null, relocating);
        Optional<Rejection> result = FilterEngine.standard().firstRejection(ctx);
        assertEquals(FilterReason.LOCATION_COUNTRY_UNKNOWN, reasonOf(result));
        assertTrue(result.get().isReview());
    }

    @Test
    void aHardRejectionBeatsAnEarlierReview() {
        // Plain Remote would be a review, but the title is not a wanted role, so it is skipped.
        FilterContext ctx = context("Acme", "Risk Operations Analyst", "", "Remote", null, relocating);
        Optional<Rejection> result = FilterEngine.standard().firstRejection(ctx);
        assertEquals(FilterReason.TITLE_NOT_RELEVANT, reasonOf(result));
        assertTrue(!result.get().isReview());

        // Plain Remote, wanted role, but too senior: still skipped, not reviewed.
        FilterContext senior = context("Acme", "Java Developer", "", "Remote", new ExperienceRange(8, null), relocating);
        assertEquals(FilterReason.EXPERIENCE_TOO_SENIOR, reasonOf(FilterEngine.standard().firstRejection(senior)));
    }

    @Test
    void remoteIndiaForAWantedRolePassesCleanly() {
        FilterContext ctx = context("Acme", "Backend Engineer", "", "Remote - India", null, relocating);
        assertTrue(FilterEngine.standard().firstRejection(ctx).isEmpty());
    }
}
