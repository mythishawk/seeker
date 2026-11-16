package com.job.seeker.common;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns job-description HTML into readable plain text. Plain Java, no framework. */
public final class HtmlText {

    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile("(?is)<(script|style)[^>]*>.*?</\\1>");
    private static final Pattern LIST_ITEM = Pattern.compile("(?i)<li[^>]*>");
    private static final Pattern LINE_BREAK = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern BLOCK_END =
            Pattern.compile("(?i)</(?:p|div|li|ul|ol|h[1-6]|tr|table|section|article|blockquote)>");
    private static final Pattern TAG = Pattern.compile("<[^>]+>");
    private static final Pattern ENTITY = Pattern.compile("&(#[xX][0-9a-fA-F]+|#[0-9]+|[A-Za-z][A-Za-z0-9]*);");
    private static final Pattern SPACES = Pattern.compile("[ \\t\\x0B\\f\\r]+");
    private static final Pattern BLANK_LINES = Pattern.compile("\\n{3,}");

    private static final Map<String, String> NAMED = Map.ofEntries(
            Map.entry("amp", "&"), Map.entry("lt", "<"), Map.entry("gt", ">"),
            Map.entry("quot", "\""), Map.entry("apos", "'"), Map.entry("nbsp", " "),
            Map.entry("ndash", "–"), Map.entry("mdash", "—"), Map.entry("bull", "•"),
            Map.entry("middot", "·"), Map.entry("rsquo", "’"), Map.entry("lsquo", "‘"),
            Map.entry("ldquo", "“"), Map.entry("rdquo", "”"), Map.entry("hellip", "…"),
            Map.entry("copy", "©"), Map.entry("reg", "®"), Map.entry("trade", "™"));

    private HtmlText() {
    }

    public static String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String s = html;
        // Greenhouse sends its markup entity-escaped ("&lt;p&gt;"): turn it back into real tags first.
        if (s.indexOf('<') < 0 && s.contains("&lt;")) {
            s = unescape(s);
        }
        s = SCRIPT_OR_STYLE.matcher(s).replaceAll(" ");
        s = LIST_ITEM.matcher(s).replaceAll("- ");
        s = LINE_BREAK.matcher(s).replaceAll("\n");
        s = BLOCK_END.matcher(s).replaceAll("\n");
        s = TAG.matcher(s).replaceAll("");
        s = unescape(s);
        s = s.replace(' ', ' ');
        s = SPACES.matcher(s).replaceAll(" ");

        StringBuilder joined = new StringBuilder(s.length());
        String[] lines = s.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                joined.append('\n');
            }
            joined.append(lines[i].strip());
        }
        return BLANK_LINES.matcher(joined).replaceAll("\n\n").strip();
    }

    static String unescape(String s) {
        Matcher m = ENTITY.matcher(s);
        StringBuilder out = new StringBuilder(s.length());
        while (m.find()) {
            String replacement = decode(m.group(1));
            m.appendReplacement(out, Matcher.quoteReplacement(replacement != null ? replacement : m.group()));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String decode(String name) {
        if (name.charAt(0) == '#') {
            try {
                boolean hex = name.length() > 1 && (name.charAt(1) == 'x' || name.charAt(1) == 'X');
                int codePoint = hex ? Integer.parseInt(name.substring(2), 16) : Integer.parseInt(name.substring(1));
                if (codePoint > 0 && Character.isValidCodePoint(codePoint)) {
                    return new String(Character.toChars(codePoint));
                }
            } catch (NumberFormatException e) {
                return null;
            }
            return null;
        }
        return NAMED.get(name);
    }
}
