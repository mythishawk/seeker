package com.job.seeker.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommonTest {

    @Test
    void normalizeKeepsLettersAndDigitsOnly() {
        assertEquals("pune mh", Texts.normalize("Pune, MH"));
        assertEquals("remote india", Texts.normalize("  Remote - India  "));
        assertEquals("", Texts.normalize(null));
        assertEquals("", Texts.normalize("--"));
    }

    @Test
    void phraseMatchingIsWholeWord() {
        String padded = Texts.pad(Texts.normalize("Senior Staff Engineer"));
        assertTrue(Texts.containsPhrase(padded, "staff"));
        assertTrue(Texts.containsPhrase(padded, "staff engineer"));
        assertFalse(Texts.containsPhrase(padded, "sta"));
        assertFalse(Texts.containsPhrase(padded, ""));
    }

    @Test
    void companyNamesDropLegalSuffixes() {
        assertEquals("accenture india", CompanyNames.normalize("Accenture India Pvt. Ltd."));
        assertEquals("acme", CompanyNames.normalize("Acme Private Limited"));
        assertEquals("co", CompanyNames.normalize("Co"));
    }

    @Test
    void blocklistMatchesWholeWordsAtTheStart() {
        assertTrue(CompanyNames.matches("accenture india", "accenture"));
        assertTrue(CompanyNames.matches("accenture", "accenture"));
        assertFalse(CompanyNames.matches("accentureful", "accenture"));
        assertFalse(CompanyNames.matches("not accenture", "accenture"));
        assertFalse(CompanyNames.matches("anything", ""));
    }

    @Test
    void sha256MatchesKnownValue() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Hashing.sha256Hex("abc"));
    }

    @Test
    void dedupeHashIgnoresCaseAndPunctuationButNotLocation() {
        String a = JobHashing.dedupeHash("Acme Pvt Ltd", "Java Developer", "Pune, India");
        String b = JobHashing.dedupeHash("ACME", "java  developer!", "pune india");
        String c = JobHashing.dedupeHash("Acme", "Java Developer", "Mumbai");
        assertEquals(a, b);
        assertNotEquals(a, c);
    }
}
