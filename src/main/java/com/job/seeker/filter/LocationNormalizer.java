package com.job.seeker.filter;

import com.job.seeker.common.Texts;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Maps messy location text ("Pune, MH", "Bangalore", "Remote - India") to one canonical form.
 * It scans the whole text for known place names (longest first), so multi-city strings work too.
 * Place names come from two editable files: locations/india-cities.csv and locations/countries.csv.
 */
public final class LocationNormalizer {

    private record Alias(String text, boolean city, String value) {
    }

    private static final List<String> REMOTE_PHRASES =
            List.of("remote", "remotely", "wfh", "work from home", "work from anywhere", "anywhere");

    private final List<Alias> aliases;

    public LocationNormalizer(List<String> cityLines, List<String> countryLines) {
        List<Alias> list = new ArrayList<>();
        parse(cityLines, true, list);
        parse(countryLines, false, list);
        list.sort(Comparator.<Alias>comparingInt(a -> a.text().length()).reversed());
        this.aliases = List.copyOf(list);
    }

    public static LocationNormalizer fromClasspath() {
        return new LocationNormalizer(
                readResource("/locations/india-cities.csv"),
                readResource("/locations/countries.csv"));
    }

    public NormalizedLocation normalize(String raw) {
        String cleaned = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        String normalized = Texts.normalize(cleaned);
        if (normalized.isEmpty()) {
            return new NormalizedLocation(cleaned, cleaned, Set.of(), Set.of(), false, false);
        }
        String padded = Texts.pad(normalized);
        String working = padded;
        Set<String> cities = new TreeSet<>();
        Set<String> countries = new TreeSet<>();
        for (Alias alias : aliases) {
            String needle = " " + alias.text() + " ";
            if (working.contains(needle)) {
                (alias.city() ? cities : countries).add(alias.value());
                // Remove what we matched so "navi mumbai" is not counted again as "mumbai".
                working = working.replace(needle, "  ");
            }
        }
        boolean remote = false;
        for (String phrase : REMOTE_PHRASES) {
            if (padded.contains(" " + phrase + " ")) {
                remote = true;
                break;
            }
        }
        boolean india = countries.remove("IN");
        String canonical = canonical(cleaned, cities, countries, india, remote);
        return new NormalizedLocation(cleaned, canonical,
                Collections.unmodifiableSet(cities), Collections.unmodifiableSet(countries), india, remote);
    }

    private static String canonical(String cleaned, Set<String> cities, Set<String> foreign, boolean india, boolean remote) {
        boolean indiaSignal = india || !cities.isEmpty();
        List<String> segments = new ArrayList<>();
        if (!cities.isEmpty()) {
            segments.add(String.join(", ", cities));
        } else if (indiaSignal && !remote) {
            segments.add("India");
        }
        if (remote) {
            if (indiaSignal) {
                segments.add("Remote - India");
            } else if (!foreign.isEmpty()) {
                segments.add("Remote - " + String.join(", ", foreign));
            } else {
                segments.add("Remote");
            }
        }
        if (segments.isEmpty() && !foreign.isEmpty()) {
            segments.add(String.join(", ", foreign));
        }
        if (segments.isEmpty()) {
            return cleaned;
        }
        return String.join(" | ", segments);
    }

    private static void parse(List<String> lines, boolean city, List<Alias> into) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int split = trimmed.indexOf(';');
            if (split < 0) {
                continue;
            }
            String value = trimmed.substring(0, split).trim();
            Set<String> texts = new LinkedHashSet<>();
            texts.add(Texts.normalize(value));
            for (String alias : trimmed.substring(split + 1).split("\\|")) {
                texts.add(Texts.normalize(alias));
            }
            for (String text : texts) {
                if (!text.isEmpty()) {
                    into.add(new Alias(text, city, value));
                }
            }
        }
    }

    private static List<String> readResource(String path) {
        try (InputStream in = LocationNormalizer.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
