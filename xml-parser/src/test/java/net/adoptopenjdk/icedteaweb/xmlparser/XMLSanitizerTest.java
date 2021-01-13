package net.adoptopenjdk.icedteaweb.xmlparser;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class XMLSanitizerTest {

    @Test
    public void shouldNotChangeAnyNonXmlComment() {
        // given
        final String text = "Some very funny String with <xml> in it and </some> <!- almost--> comment";
        final Reader input = new StringReader(text);

        // when
        final String result = XMLSanitizer.sanitizeXml(input);

        // then
        assertEquals(text, result);
    }

    @Test
    public void shouldRemoveXmlComment() {
        // given
        final String text = "The following comment <!-- this one --> should be removed";
        final Reader input = new StringReader(text);

        // when
        final String result = XMLSanitizer.sanitizeXml(input);

        // then
        assertEquals(text.replaceAll("<!-- this one -->", ""), result);
    }

    @Test
    public void shouldRemoveWeiredQuestionMarkXmlComment() {
        // given
        final String text = "The following comment <?-- this one --> should be removed";
        final Reader input = new StringReader(text);

        // when
        final String result = XMLSanitizer.sanitizeXml(input);

        // then
        assertEquals(text.replaceAll("<\\?-- this one -->", ""), result);
    }
}
