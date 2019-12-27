/* InformationElementTest.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */
/**
http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
 */
package net.adoptopenjdk.icedteaweb.client.parts.splashscreen.parts;

import net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ParserSettings;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc.HOMEPAGE_ELEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class InformationElementTest {

    private static class TestDescriptionInfoItem extends DescriptionInfoItem {

        public TestDescriptionInfoItem(String value, DescriptionKind kind) {
            super(value, kind);
        }

        public String toXml() {
            if (kind == null) {
                return new TestInfoItem(type, value).toXml();
            }
            return "<" + type + " kind=\"" + kind.getValue() + "\">" + value + "</" + type + ">";
        }
    }

    private static class TestInfoItem extends InfoItem {

        public TestInfoItem(String type, String value) {
            super(type, value);
        }

        public String toXml() {
            if (type.equals(HOMEPAGE_ELEMENT)) {
                return "<" + type + " " + HomepageDesc.HREF_ATTRIBUTE + "=\"" + value + "\"/>";
            }
            return "<" + type + ">" + value + "</" + type + ">";
        }
    }
    private static final TestInfoItem title = new TestInfoItem(InformationDesc.TITLE_ELEMENT, "title exp");
    private static final TestInfoItem vendor = new TestInfoItem(InformationDesc.VENDOR_ELEMENT, "vendor exp");
    private static final TestInfoItem homepage = new TestInfoItem(HOMEPAGE_ELEMENT, "http://homepage.exp");
    private static final TestDescriptionInfoItem oneLineD = new TestDescriptionInfoItem("One Line", DescriptionKind.ONE_LINE);
    private static final TestDescriptionInfoItem toolTipD = new TestDescriptionInfoItem("Tooltip", DescriptionKind.TOOLTIP);
    private static final TestDescriptionInfoItem short1D = new TestDescriptionInfoItem("short1", DescriptionKind.SHORT);
    private static final TestDescriptionInfoItem short2D = new TestDescriptionInfoItem("short2", DescriptionKind.SHORT);
    private static final TestDescriptionInfoItem noKindD = new TestDescriptionInfoItem("noKind", null);
    private static final String testJnlpHeader =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<jnlp spec=\"1.0\" href=\"http://somehref.jnlp\" codebase=\"http://some.code.base\">\n"
            + "  <information>\n";
    private static final String testJnlpFooter = "    <offline/>\n"
            + "  </information>\n"
            + "  <resources>\n"
            + "    <j2se version=\"1.6+\"/>\n"
            + "    <jar href=\"somejar\"/>\n"
            + "  </resources>\n"
            + "  <application-desc main-class=\"someMainClass\">\n"
            + "  </application-desc>\n"
            + "</jnlp>";

    @Test
    public void testSetGetTitle() {
        final InformationElement ie = new InformationElement();
        assertNull("After creation value must be null", ie.getTitle());
        ie.setTitle(title.getValue());
        assertNotNull("After assignment value must NOT be null", ie.getTitle());
        assertTrue("After assignment value must be included in output", ie.getTitle().contains(title.getValue()));
    }

    @Test
    public void testSetGetVendor() {
        final InformationElement ie = new InformationElement();
        assertNull("After creation value must be null", ie.getVendor());
        ie.setvendor(vendor.getValue());
        assertNotNull("After assignment value must NOT be null", ie.getVendor());
        assertTrue("After assignment value must be included in output", ie.getVendor().contains(vendor.getValue()));
    }

    @Test
    public void testSetGetHomepage() {
        final InformationElement ie = new InformationElement();
        assertNull("After creation value must be null", ie.getHomepage());
        ie.setHomepage(homepage.getValue());
        assertNotNull("After assignment value must NOT be null", ie.getHomepage());
        assertTrue("After assignment value must be included in output", ie.getHomepage().contains(homepage.getValue()));
    }

    @Test
    public void addDescriptionTest() {
        final InformationElement ie = new InformationElement();
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should be empty", 0, ie.getDescriptions().size());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should be empty", 1, ie.getDescriptions().size());
        ie.addDescription(short1D.getValue(), short1D.getKind());
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should be empty", 2, ie.getDescriptions().size());
        ie.addDescription(short2D.getValue(), short2D.getKind());
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should remain same", 2, ie.getDescriptions().size());
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should be ", 3, ie.getDescriptions().size());
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        assertNotNull("Descriptions should never be null", ie.getDescriptions());
        assertEquals("Descriptions should be ", 4, ie.getDescriptions().size());


    }

    public void getBestMatchingDescriptionForSplashTest() {
        final InformationElement ie = new InformationElement();
        assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(short1D.getValue(), short1D.getKind());
        assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        assertNotNull(ie.getBestMatchingDescriptionForSplash());
        assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (noKindD.getValue()));
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        assertNotNull(ie.getBestMatchingDescriptionForSplash());
        assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (oneLineD.getValue()));
        ie.addDescription(short2D.getValue(), short2D.getKind());
        assertNotNull(ie.getBestMatchingDescriptionForSplash());
        assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (oneLineD.getValue()));


    }

    @Test
    public void getLongestDescriptionForSplashTest() {
        final InformationElement ie = new InformationElement();
        assertNull(ie.getLongestDescriptionForSplash());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        assertNotNull(ie.getLongestDescriptionForSplash());
        assertEquals(ie.getLongestDescriptionForSplash().getValue(), (toolTipD.getValue()));
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        assertNotNull(ie.getLongestDescriptionForSplash());
        assertEquals(ie.getLongestDescriptionForSplash().getValue(), (oneLineD.getValue()));
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());//disturb
        assertNotNull(ie.getLongestDescriptionForSplash());
        assertEquals(ie.getLongestDescriptionForSplash().getValue(), (noKindD.getValue()));
        ie.addDescription(short1D.getValue(), short1D.getKind());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());//disturb
        assertNotNull(ie.getLongestDescriptionForSplash());
        assertEquals(ie.getLongestDescriptionForSplash().getValue(), (short1D.getValue()));

    }

    @Test
    public void getDescriptionTest() {
        getBestMatchingDescriptionForSplashTest();

    }

    @Test
    public void getHeaderTest() {
        final InformationElement ie = new InformationElement();
        assertNotNull("Header should never be null", ie.getHeader());
        assertEquals(0, ie.getHeader().size());
        ie.setvendor(vendor.getValue());
        assertEquals(1, ie.getHeader().size());
        ie.setTitle(title.getValue());
        assertEquals(2, ie.getHeader().size());
        ie.setHomepage(homepage.getValue());
        assertEquals(3, ie.getHeader().size());
        ie.setTitle(homepage.getValue());
        assertEquals(3, ie.getHeader().size());
        ie.addDescription(toolTipD.getValue());
        assertEquals(3, ie.getHeader().size());
        ie.addDescription(oneLineD.getValue());
        assertEquals(3, ie.getHeader().size());
    }

    @Test
    public void createFromJNLP() throws ParseException, MalformedURLException {
        final ParserSettings parser = new ParserSettings();
        final InformationElement ie0 = InformationElement.createFromJNLP(null);
        assertNotNull(ie0);

        //title, vendor and homepage are obligatory.. not so much to test
        final String exJnlp2 = testJnlpHeader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + testJnlpFooter;
        final JNLPFile jnlpFile2 = new JNLPFile(new ByteArrayInputStream(exJnlp2.getBytes(UTF_8)), null,null, parser, null);
        final InformationElement ie2 = InformationElement.createFromJNLP(jnlpFile2);
        assertNotNull(ie2);
        assertEquals(3, ie2.getHeader().size());
        assertEquals(0, ie2.getDescriptions().size());

        final String exJnlp3 = testJnlpHeader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + toolTipD.toXml() + "\n" + testJnlpFooter;
        final JNLPFile jnlpFile3 = new JNLPFile(new ByteArrayInputStream(exJnlp3.getBytes(UTF_8)), null, null, parser, null);
        final InformationElement ie3 = InformationElement.createFromJNLP(jnlpFile3);
        assertNotNull(ie3);
        assertEquals(3, ie3.getHeader().size());
        assertEquals(1, ie3.getDescriptions().size());

        final String exJnlp4 = testJnlpHeader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + noKindD.toXml() + "\n" + testJnlpFooter;
        final JNLPFile jnlpFile4 = new JNLPFile(new ByteArrayInputStream(exJnlp4.getBytes(UTF_8)), null, null, parser, null);
        final InformationElement ie4 = InformationElement.createFromJNLP(jnlpFile4);
        assertNotNull(ie4);
        assertEquals(3, ie4.getHeader().size());
        assertEquals(1, ie4.getDescriptions().size());

        final String exJnlp5 = testJnlpHeader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + noKindD.toXml() + "\n" + toolTipD.toXml() + "\n" + testJnlpFooter;
        final JNLPFile jnlpFile5 = new JNLPFile(new ByteArrayInputStream(exJnlp5.getBytes(UTF_8)), null, null, parser, null);
        final InformationElement ie5 = InformationElement.createFromJNLP(jnlpFile5);
        assertNotNull(ie5);
        assertEquals(3, ie5.getHeader().size());
        assertEquals(2, ie5.getDescriptions().size());
    }

    @Test
    public void toXml() {
        final TestInfoItem i1 = new TestInfoItem("aa", "bb");
        assertTrue(i1.toXml().contains("aa"));
        assertTrue(i1.toXml().contains("bb"));
        assertTrue(i1.toXml().length() > 4);
        JEditorPaneBasedExceptionDialogTest.assertMarkup(i1.toXml());
    }
}
