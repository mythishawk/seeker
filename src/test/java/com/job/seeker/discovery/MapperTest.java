package com.job.seeker.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapperTest {

    @Test
    void greenhousePostingBecomesFetchedJob() {
        GreenhouseMapper.Posting posting = new GreenhouseMapper.Posting(
                123L, "  Java Developer ", new GreenhouseMapper.Location("Pune, India"),
                "https://boards.greenhouse.io/acme/jobs/123", "2024-01-14T10:55:28-05:00", null,
                "&lt;p&gt;Need 3-5 years&lt;/p&gt;");
        List<FetchedJob> jobs = GreenhouseMapper.toFetchedJobs(new GreenhouseMapper.Response(List.of(posting)));

        assertEquals(1, jobs.size());
        FetchedJob job = jobs.get(0);
        assertEquals("123", job.externalId());
        assertEquals("Java Developer", job.title());
        assertEquals("Pune, India", job.location());
        assertEquals("Need 3-5 years", job.description());
        assertEquals("https://boards.greenhouse.io/acme/jobs/123", job.url());
        assertEquals(Instant.parse("2024-01-14T15:55:28Z"), job.postedAt());
    }

    @Test
    void greenhousePrefersFirstPublishedAndSkipsBrokenPostings() {
        List<GreenhouseMapper.Posting> postings = new ArrayList<>();
        postings.add(new GreenhouseMapper.Posting(1L, "A", null, null, "2024-02-01T00:00:00Z", "2024-01-01T00:00:00Z", null));
        postings.add(new GreenhouseMapper.Posting(null, "No id", null, null, null, null, null));
        postings.add(new GreenhouseMapper.Posting(2L, "  ", null, null, null, null, null));
        postings.add(null);
        List<FetchedJob> jobs = GreenhouseMapper.toFetchedJobs(new GreenhouseMapper.Response(postings));

        assertEquals(1, jobs.size());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), jobs.get(0).postedAt());
        assertNull(jobs.get(0).location());
        assertEquals("", jobs.get(0).description());
    }

    @Test
    void emptyGreenhouseResponseGivesNoJobs() {
        assertTrue(GreenhouseMapper.toFetchedJobs(null).isEmpty());
        assertTrue(GreenhouseMapper.toFetchedJobs(new GreenhouseMapper.Response(null)).isEmpty());
    }

    @Test
    void leverPostingBecomesFetchedJob() {
        LeverMapper.Posting posting = new LeverMapper.Posting(
                "abc-1", "Backend Engineer", "https://jobs.lever.co/acme/abc-1", 1700000000000L,
                new LeverMapper.Categories("Pune", List.of("Pune", "Remote - India"), "Eng", "Full-time"),
                "Intro text", null, "Extra text", null,
                List.of(new LeverMapper.Section("Requirements", "<li>3+ years Java</li>")),
                "remote");
        List<FetchedJob> jobs = LeverMapper.toFetchedJobs(List.of(posting));

        assertEquals(1, jobs.size());
        FetchedJob job = jobs.get(0);
        assertEquals("abc-1", job.externalId());
        assertEquals("Backend Engineer", job.title());
        assertEquals("Pune; Remote - India", job.location());
        assertEquals(Instant.ofEpochMilli(1700000000000L), job.postedAt());
        assertTrue(job.description().startsWith("Intro text"));
        assertTrue(job.description().contains("Requirements\n- 3+ years Java"));
        assertTrue(job.description().endsWith("Extra text"));
    }

    @Test
    void leverRemoteWorkplaceIsAddedToTheLocation() {
        LeverMapper.Posting posting = new LeverMapper.Posting(
                "x", "Dev", null, "2024-03-01T00:00:00Z",
                new LeverMapper.Categories("Bengaluru", null, null, null),
                "d", null, null, null, null, "remote");
        FetchedJob job = LeverMapper.toFetchedJobs(List.of(posting)).get(0);
        assertEquals("Bengaluru; Remote", job.location());
        assertEquals(Instant.parse("2024-03-01T00:00:00Z"), job.postedAt());
    }

    @Test
    void leverSkipsPostingsWithoutIdOrTitle() {
        List<LeverMapper.Posting> postings = new ArrayList<>();
        postings.add(new LeverMapper.Posting(null, "T", null, null, null, null, null, null, null, null, null));
        postings.add(new LeverMapper.Posting("1", "", null, null, null, null, null, null, null, null, null));
        assertTrue(LeverMapper.toFetchedJobs(postings).isEmpty());
        assertTrue(LeverMapper.toFetchedJobs(null).isEmpty());
    }
}
