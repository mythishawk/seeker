package com.job.seeker.discovery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.job.seeker.common.HtmlText;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** The shape of Greenhouse's public job-board answer, and its conversion to {@link FetchedJob}. */
public final class GreenhouseMapper {

    private GreenhouseMapper() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Posting> jobs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Posting(
            Long id,
            String title,
            Location location,
            @JsonProperty("absolute_url") String absoluteUrl,
            @JsonProperty("updated_at") String updatedAt,
            @JsonProperty("first_published") String firstPublished,
            String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name) {
    }

    public static List<FetchedJob> toFetchedJobs(Response response) {
        List<FetchedJob> out = new ArrayList<>();
        if (response == null || response.jobs() == null) {
            return out;
        }
        for (Posting p : response.jobs()) {
            if (p == null || p.id() == null || p.title() == null || p.title().isBlank()) {
                continue;
            }
            String location = p.location() == null ? null : p.location().name();
            Instant posted = parseInstant(p.firstPublished());
            if (posted == null) {
                posted = parseInstant(p.updatedAt());
            }
            out.add(new FetchedJob(
                    String.valueOf(p.id()),
                    p.title().trim(),
                    location == null || location.isBlank() ? null : location.trim(),
                    HtmlText.toPlainText(p.content()),
                    p.absoluteUrl(),
                    posted));
        }
        return out;
    }

    static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
