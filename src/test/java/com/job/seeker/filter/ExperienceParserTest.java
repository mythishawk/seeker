package com.job.seeker.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExperienceParserTest {

    private final ExperienceParser parser = new ExperienceParser();

    private ExperienceRange parse(String title, String description) {
        Optional<ExperienceRange> r = parser.parse(title, description);
        return r.orElse(null);
    }

    @Test
    void rangeWithDash() {
        ExperienceRange r = parse("Java Developer", "We need 3-5 years of experience in Java.");
        assertEquals(3, r.min());
        assertEquals(5, r.max());
    }

    @Test
    void rangeWithToAndShortWords() {
        ExperienceRange r = parse("Dev", "Exp: 2 to 4 yrs in backend");
        assertEquals(2, r.min());
        assertEquals(4, r.max());
    }

    @Test
    void openEndedAsk() {
        ExperienceRange r = parse("Dev", "Requires 5+ years of experience with Spring.");
        assertEquals(5, r.min());
        assertNull(r.max());
    }

    @Test
    void minimumWording() {
        ExperienceRange r = parse("Dev", "Minimum 2 years experience required");
        assertEquals(2, r.min());
        assertNull(r.max());
    }

    @Test
    void titleIsTrustedWithoutTheWordExperience() {
        ExperienceRange r = parse("Software Engineer (2-4 years)", "Join our team.");
        assertEquals(2, r.min());
        assertEquals(4, r.max());
    }

    @Test
    void zeroToTwo() {
        ExperienceRange r = parse("Dev", "0-2 years of experience");
        assertEquals(0, r.min());
        assertEquals(2, r.max());
    }

    @Test
    void rangeBeatsSingleNumbers() {
        ExperienceRange r = parse("Dev", "3-5 years of experience overall. 7 years of experience in cloud is a plus.");
        assertEquals(3, r.min());
        assertEquals(5, r.max());
    }

    @Test
    void largestSingleNumberWins() {
        ExperienceRange r = parse("Dev", "1+ years of experience with Docker. 3+ years of experience with Java.");
        assertEquals(3, r.min());
        assertNull(r.max());
    }

    @Test
    void numbersWithoutTheWordExperienceAreIgnoredInDescriptions() {
        assertTrue(parser.parse("Dev", "Our company was founded 25 years ago.").isEmpty());
    }

    @Test
    void decimalYearsAreRoundedDown() {
        ExperienceRange r = parse("Dev", "2.5 years of experience");
        assertEquals(2, r.min());
    }

    @Test
    void fresherJobs() {
        ExperienceRange r = parse("Trainee Developer", "Freshers are welcome to apply.");
        assertEquals(0, r.min());
        assertEquals(1, r.max());
    }

    @Test
    void nothingFound() {
        assertTrue(parser.parse("Dev", "A great job at a great company.").isEmpty());
        assertTrue(parser.parse(null, null).isEmpty());
    }
}
