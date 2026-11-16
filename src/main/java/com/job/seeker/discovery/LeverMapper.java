package com.job.seeker.discovery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.job.seeker.common.HtmlText;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** The shape of Lever's public postings answer, and its conversion to {@link FetchedJob}. */
public final class LeverMapper {

    private LeverMapper() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Posting(
            String id,
            String text,
            String hostedUrl,
            Object createdAt,
            Categories categories,
            String descriptionPlain,
            String description,
            String additionalPlain,
            String additional,
            List<Section> lists,
            String workplaceType) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Categories(String location, List<String> allLocations, String team, String commitment) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Section(String text, String content) {
    }

    public static List<FetchedJob> toFetchedJobs(List<Posting> postings) {
        List<FetchedJob> out = new ArrayList<>();
        if (postings == null) {
            return out;
        }
        for (Posting p : postings) {
            if (p == null || p.id() == null || p.id().isBlank() || p.text() == null || p.text().isBlank()) {
                continue;
            }
            out.add(new FetchedJob(p.id(), p.text().trim(), location(p), description(p), p.hostedUrl(), postedAt(p.createdAt())));
        }
        return out;
    }

    static String location(Posting p) {
        String base = null;
        if (p.categories() != null) {
            List<String> all = p.categories().allLocations();
            if (all != null && !all.isEmpty()) {
                List<String> parts = new ArrayList<>();
                for (String loc : all) {
                    if (loc != null && !loc.isBlank()) {
                        parts.add(loc.trim());
                    }
                }
                if (!parts.isEmpty()) {
                    base = String.join("; ", parts);
                }
            }
            if (base == null && p.categories().location() != null && !p.categories().location().isBlank()) {
                base = p.categories().location().trim();
            }
        }
        if ("remote".equalsIgnoreCase(p.workplaceType())
                && (base == null || !base.toLowerCase().contains("remote"))) {
            base = base == null ? "Remote" : base + "; Remote";
        }
        return base;
    }

    static String description(Posting p) {
        StringBuilder sb = new StringBuilder();
        String intro = isBlank(p.descriptionPlain()) ? HtmlText.toPlainText(p.description()) : p.descriptionPlain().strip();
        sb.append(intro);
        if (p.lists() != null) {
            for (Section section : p.lists()) {
                if (section == null) {
                    continue;
                }
                if (!isBlank(section.text())) {
                    sb.append("\n\n").append(section.text().strip());
                }
                String body = HtmlText.toPlainText(section.content());
                if (!body.isEmpty()) {
                    sb.append('\n').append(body);
                }
            }
        }
        String extra = isBlank(p.additionalPlain()) ? HtmlText.toPlainText(p.additional()) : p.additionalPlain().strip();
        if (!extra.isEmpty()) {
            sb.append("\n\n").append(extra);
        }
        return sb.toString().strip();
    }

    static Instant postedAt(Object createdAt) {
        if (createdAt instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue());
        }
        if (createdAt instanceof CharSequence s) {
            try {
                return Instant.parse(s);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
