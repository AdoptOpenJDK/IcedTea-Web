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
package net.sourceforge.jnlp.splashscreen.parts;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.ParserSettings;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class InformationElementTest {

    private static class TestDescriptionInfoItem extends DescriptionInfoItem {

        public TestDescriptionInfoItem(String value, String kind) {
            super(value, kind);
        }

        public String toXml() {
            if (kind == null) {
                return new TestInfoItem(type, value).toXml();
            }
            return "<" + type + " kind=\"" + kind + "\">" + value + "</" + type + ">";
        }
    }

    private static class TestInfoItem extends InfoItem {

        public TestInfoItem(String type, String value) {
            super(type, value);
        }

        public String toXml() {
            if (type.equals(homepage)) {
                return "<" + type + " " + homepageHref + "=\"" + value + "\"/>";
            }
            return "<" + type + ">" + value + "</" + type + ">";
        }
    }
    private final static TestInfoItem title = new TestInfoItem(InfoItem.title, "title exp");
    private final static TestInfoItem vendor = new TestInfoItem(InfoItem.vendor, "vendor exp");
    private final static TestInfoItem homepage = new TestInfoItem(InfoItem.homepage, "http://homepage.exp");
    private final static TestDescriptionInfoItem oneLineD = new TestDescriptionInfoItem("One Line", DescriptionInfoItem.descriptionKindOneLine);
    private final static TestDescriptionInfoItem toolTipD = new TestDescriptionInfoItem("Tooltip", DescriptionInfoItem.descriptionKindToolTip);
    private final static TestDescriptionInfoItem short1D = new TestDescriptionInfoItem("short1", DescriptionInfoItem.descriptionKindShort);
    private final static TestDescriptionInfoItem short2D = new TestDescriptionInfoItem("short2", DescriptionInfoItem.descriptionKindShort);
    private final static TestDescriptionInfoItem noKindD = new TestDescriptionInfoItem("noKind", null);
    private static final String testJnlpheader =
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

        InformationElement ie = new InformationElement();
        Assert.assertNull("After creation value must be null", ie.getTitle());
        ie.setTitle(title.getValue());
        Assert.assertNotNull("After assigmentvalue must NOT be null", ie.getTitle());
        Assert.assertTrue("After assigment value must be included in output", ie.getTitle().contains(title.getValue()));
    }

    @Test
    public void testSetGetvendor() {
        InformationElement ie = new InformationElement();
        Assert.assertNull("After creation value must be null", ie.getVendor());
        ie.setvendor(vendor.getValue());
        Assert.assertNotNull("After assigmentvalue must NOT be null", ie.getVendor());
        Assert.assertTrue("After assigment value must be included in output", ie.getVendor().contains(vendor.getValue()));
    }

    @Test
    public void testSetGetHomepage() {
        InformationElement ie = new InformationElement();
        Assert.assertNull("After creation value must be null", ie.getHomepage());
        ie.setHomepage(homepage.getValue());
        Assert.assertNotNull("After assigmentvalue must NOT be null", ie.getHomepage());
        Assert.assertTrue("After assigment value must be included in output", ie.getHomepage().contains(homepage.getValue()));
    }

    @Test
    public void addDescriptionTest() {
        InformationElement ie = new InformationElement();
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should be empty", 0, ie.getDescriptions().size());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should be empty", 1, ie.getDescriptions().size());
        ie.addDescription(short1D.getValue(), short1D.getKind());
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should be empty", 2, ie.getDescriptions().size());
        ie.addDescription(short2D.getValue(), short2D.getKind());
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should reamin same", 2, ie.getDescriptions().size());
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should be ", 3, ie.getDescriptions().size());
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        Assert.assertNotNull("Descriptions should never be null", ie.getDescriptions());
        Assert.assertEquals("Descriptions should be ", 4, ie.getDescriptions().size());


    }

    public void getBestMatchingDescriptionForSplashTest() {
        InformationElement ie = new InformationElement();
        Assert.assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        Assert.assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(short1D.getValue(), short1D.getKind());
        Assert.assertNull(ie.getBestMatchingDescriptionForSplash());
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        Assert.assertNotNull(ie.getBestMatchingDescriptionForSplash());
        Assert.assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (noKindD.getValue()));
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        Assert.assertNotNull(ie.getBestMatchingDescriptionForSplash());
        Assert.assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (oneLineD.getValue()));
        ie.addDescription(short2D.getValue(), short2D.getKind());
        Assert.assertNotNull(ie.getBestMatchingDescriptionForSplash());
        Assert.assertEquals(ie.getBestMatchingDescriptionForSplash().getValue(), (oneLineD.getValue()));


    }

    public void getLongestDescriptionForSplashTest() {
        InformationElement ie = new InformationElement();
        Assert.assertNull(ie.getLongestDescriptionForSplash());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());
        Assert.assertNotNull(ie.getLongestDescriptionForSplash());
        Assert.assertEquals(ie.getLongestDescriptionForSplash().getValue(), (toolTipD.getValue()));
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());
        Assert.assertNotNull(ie.getLongestDescriptionForSplash());
        Assert.assertEquals(ie.getLongestDescriptionForSplash().getValue(), (oneLineD.getValue()));
        ie.addDescription(noKindD.getValue(), noKindD.getKind());
        ie.addDescription(oneLineD.getValue(), oneLineD.getKind());//disturb
        Assert.assertNotNull(ie.getLongestDescriptionForSplash());
        Assert.assertEquals(ie.getLongestDescriptionForSplash().getValue(), (noKindD.getValue()));
        ie.addDescription(short1D.getValue(), short1D.getKind());
        ie.addDescription(toolTipD.getValue(), toolTipD.getKind());//disturb
        Assert.assertNotNull(ie.getLongestDescriptionForSplash());
        Assert.assertEquals(ie.getLongestDescriptionForSplash().getValue(), (short1D.getValue()));

    }

    @Test
    public void getDescriptionTest() {
        getBestMatchingDescriptionForSplashTest();

    }

    @Test
    public void getHeaderTest() {
        InformationElement ie = new InformationElement();
        Assert.assertNotNull("Header should never be null", ie.getHeader());
        Assert.assertEquals(0, ie.getHeader().size());
        ie.setvendor(vendor.getValue());
        Assert.assertEquals(1, ie.getHeader().size());
        ie.setTitle(title.getValue());
        Assert.assertEquals(2, ie.getHeader().size());
        ie.setHomepage(homepage.getValue());
        Assert.assertEquals(3, ie.getHeader().size());
        ie.setTitle(homepage.getValue());
        Assert.assertEquals(3, ie.getHeader().size());
        ie.addDescription(toolTipD.getValue());
        Assert.assertEquals(3, ie.getHeader().size());
        ie.addDescription(oneLineD.getValue());
        Assert.assertEquals(3, ie.getHeader().size());
    }

    @Test
    public void createFromJNLP() throws UnsupportedEncodingException, ParseException {
        ParserSettings parser = new ParserSettings();
        JNLPFile jnlpFile0 = null;
        InformationElement ie0 = InformationElement.createFromJNLP(jnlpFile0);
        Assert.assertNotNull(ie0);
        String exJnlp1 = "<jnlp>this is invalid jnlp<jnlp>";
        Exception ex = null;
        JNLPFile jnlpFile1 = null;
        try {
            jnlpFile1 = new JNLPFile(new ByteArrayInputStream(exJnlp1.getBytes("utf-8")), parser);
        } catch (Exception eex) {
            ex = eex;
        }
        Assert.assertNotNull(ex);
        InformationElement ie1 = InformationElement.createFromJNLP(jnlpFile1);
        Assert.assertNotNull(ie1);

        //title, vendor and homepage are obligatory.. not so much to test
        String exJnlp2 = testJnlpheader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + testJnlpFooter;
        JNLPFile jnlpFile2 = new JNLPFile(new ByteArrayInputStream(exJnlp2.getBytes("utf-8")), parser);
        InformationElement ie2 = InformationElement.createFromJNLP(jnlpFile2);
        Assert.assertNotNull(ie2);
        Assert.assertEquals(3, ie2.getHeader().size());
        Assert.assertEquals(0, ie2.getDescriptions().size());

        String exJnlp3 = testJnlpheader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + toolTipD.toXml() + "\n" + testJnlpFooter;
        JNLPFile jnlpFile3 = new JNLPFile(new ByteArrayInputStream(exJnlp3.getBytes("utf-8")), parser);
        InformationElement ie3 = InformationElement.createFromJNLP(jnlpFile3);
        Assert.assertNotNull(ie3);
        Assert.assertEquals(3, ie3.getHeader().size());
        Assert.assertEquals(1, ie3.getDescriptions().size());

        String exJnlp4 = testJnlpheader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + noKindD.toXml() + "\n" + testJnlpFooter;
        JNLPFile jnlpFile4 = new JNLPFile(new ByteArrayInputStream(exJnlp4.getBytes("utf-8")), parser);
        InformationElement ie4 = InformationElement.createFromJNLP(jnlpFile4);
        Assert.assertNotNull(ie4);
        Assert.assertEquals(3, ie4.getHeader().size());
        Assert.assertEquals(1, ie4.getDescriptions().size());

        String exJnlp5 = testJnlpheader + title.toXml() + "\n" + homepage.toXml() + "\n" + vendor.toXml() + "\n" + noKindD.toXml() + "\n" + toolTipD.toXml() + "\n" + testJnlpFooter;
        JNLPFile jnlpFile5 = new JNLPFile(new ByteArrayInputStream(exJnlp5.getBytes("utf-8")), parser);
        InformationElement ie5 = InformationElement.createFromJNLP(jnlpFile5);
        Assert.assertNotNull(ie5);
        Assert.assertEquals(3, ie5.getHeader().size());
        Assert.assertEquals(2, ie5.getDescriptions().size());


    }


    @Test
    public void toXml() {
        TestInfoItem i1 = new TestInfoItem("aa", "bb");
        Assert.assertTrue(i1.toXml().contains("aa"));
        Assert.assertTrue(i1.toXml().contains("bb"));
        Assert.assertTrue(i1.toXml().length() > 4);
        JEditorPaneBasedExceptionDialogTest.assertMarkup(i1.toXml());

    }
}
