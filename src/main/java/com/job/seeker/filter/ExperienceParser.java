package com.job.seeker.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds the years of experience a job asks for ("2-5 years", "3+ years of experience").
 * This is a heuristic. A title like "Developer (2-4 years)" is trusted on its own; in the description a
 * number only counts when the word "experience" is close by. Ranges beat single numbers, and when there are only
 * single numbers the largest wins (small ones are usually about one tool, not the whole role).
 */
public final class ExperienceParser {

    private static final String NUM = "(?<![\\d.])(\\d{1,2}(?:\\.\\d)?)";
    private static final Pattern RANGE = Pattern.compile(
            NUM + "\\s*(?:-|\u2013|\u2014|to|and)\\s*(\\d{1,2}(?:\\.\\d)?)\\s*\\+?\\s*(?:years?|yrs?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SINGLE = Pattern.compile(
            NUM + "\\s*(\\+)?\\s*(?:years?|yrs?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPERIENCE_WORD = Pattern.compile(
            "\\b(?:experience|experienced|exp)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FRESHER = Pattern.compile(
            "\\b(?:freshers?|entry[- ]level)\\b", Pattern.CASE_INSENSITIVE);

    private static final int WINDOW_BEFORE = 30;
    private static final int WINDOW_AFTER = 35;
    private static final int MAX_YEARS = 40;

    public Optional<ExperienceRange> parse(String title, String description) {
        Optional<ExperienceRange> fromTitle = scan(title, false);
        if (fromTitle.isPresent()) {
            return fromTitle;
        }
        Optional<ExperienceRange> fromBody = scan(description, true);
        if (fromBody.isPresent()) {
            return fromBody;
        }
        String all = (title == null ? "" : title) + "\n" + (description == null ? "" : description);
        if (FRESHER.matcher(all).find()) {
            return Optional.of(new ExperienceRange(0, 1));
        }
        return Optional.empty();
    }

    private Optional<ExperienceRange> scan(String text, boolean requireExperienceWord) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        List<int[]> rangeSpans = new ArrayList<>();
        ExperienceRange firstRange = null;
        Matcher range = RANGE.matcher(text);
        while (range.find()) {
            if (requireExperienceWord && !experienceNearby(text, range.start(), range.end())) {
                continue;
            }
            double a = Double.parseDouble(range.group(1));
            double b = Double.parseDouble(range.group(2));
            double lo = Math.min(a, b);
            double hi = Math.max(a, b);
            if (hi > MAX_YEARS) {
                continue;
            }
            rangeSpans.add(new int[] {range.start(), range.end()});
            if (firstRange == null) {
                firstRange = new ExperienceRange((int) Math.floor(lo), (int) Math.ceil(hi));
            }
        }
        if (firstRange != null) {
            return Optional.of(firstRange);
        }

        Integer bestMin = null;
        Matcher single = SINGLE.matcher(text);
        while (single.find()) {
            if (insideAny(rangeSpans, single.start())) {
                continue;
            }
            if (requireExperienceWord && !experienceNearby(text, single.start(), single.end())) {
                continue;
            }
            double years = Double.parseDouble(single.group(1));
            if (years > MAX_YEARS) {
                continue;
            }
            int min = (int) Math.floor(years);
            if (bestMin == null || min > bestMin) {
                bestMin = min;
            }
        }
        return bestMin == null ? Optional.empty() : Optional.of(new ExperienceRange(bestMin, null));
    }

    private static boolean experienceNearby(String text, int start, int end) {
        String before = text.substring(Math.max(0, start - WINDOW_BEFORE), start);
        String after = text.substring(end, Math.min(text.length(), end + WINDOW_AFTER));
        return EXPERIENCE_WORD.matcher(before).find() || EXPERIENCE_WORD.matcher(after).find();
    }

    private static boolean insideAny(List<int[]> spans, int position) {
        for (int[] span : spans) {
            if (position >= span[0] && position < span[1]) {
                return true;
            }
        }
        return false;
    }
}
