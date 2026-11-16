package com.job.seeker.common;

/** Small text helpers shared by discovery and filtering. Plain Java, no framework. */
public final class Texts {

    private Texts() {
    }

    /**
     * Lowercases, keeps only letters and digits, and joins the words with single spaces.
     * "Pune, MH" becomes "pune mh". Null becomes an empty string.
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(text.length());
        boolean gap = false;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isLetterOrDigit(cp)) {
                if (gap && out.length() > 0) {
                    out.append(' ');
                }
                gap = false;
                out.appendCodePoint(Character.toLowerCase(cp));
            } else {
                gap = true;
            }
        }
        return out.toString();
    }

    /** Wraps normalized text in spaces so a whole-word phrase check is a plain contains. */
    public static String pad(String normalized) {
        return " " + normalized + " ";
    }

    /** True when the (already normalized) phrase appears in the padded text as whole words. */
    public static boolean containsPhrase(String paddedText, String normalizedPhrase) {
        return !normalizedPhrase.isEmpty() && paddedText.contains(" " + normalizedPhrase + " ");
    }

    public static String truncate(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }
}
