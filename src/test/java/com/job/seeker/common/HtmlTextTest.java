package com.job.seeker.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HtmlTextTest {

    @Test
    void listsAndParagraphsBecomeLines() {
        String html = "<p>Hello &amp; welcome</p><ul><li>Java</li><li>Spring</li></ul>";
        assertEquals("Hello & welcome\n- Java\n- Spring", HtmlText.toPlainText(html));
    }

    @Test
    void lineBreaksBecomeNewlines() {
        assertEquals("a\nb\nc", HtmlText.toPlainText("a<br>b<br/>c"));
    }

    @Test
    void entityEscapedMarkupIsRestoredThenStripped() {
        // Greenhouse style: the markup itself is escaped, and text entities are double escaped.
        assertEquals("Fish & Chips", HtmlText.toPlainText("&lt;p&gt;Fish &amp;amp; Chips&lt;/p&gt;"));
    }

    @Test
    void scriptsAndStylesAreDropped() {
        assertEquals("Hi", HtmlText.toPlainText("<style>p{color:red}</style><script>var a=1;</script>Hi"));
    }

    @Test
    void numericEntitiesAreDecoded() {
        assertEquals("a–b ’", HtmlText.toPlainText("a&#8211;b &#x2019;"));
    }

    @Test
    void blankAndNullGiveEmptyText() {
        assertEquals("", HtmlText.toPlainText(null));
        assertEquals("", HtmlText.toPlainText("   "));
    }

    @Test
    void extraSpacesAndBlankLinesAreCollapsed() {
        assertEquals("a b\n\nc", HtmlText.toPlainText("a    b\n\n\n\n\nc"));
    }
}
