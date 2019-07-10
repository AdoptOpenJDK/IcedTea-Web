/* ParserTest.java
   Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
 */

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationType;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.testing.mock.MockJNLPFile;
import net.adoptopenjdk.icedteaweb.xmlparser.Node;
import net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test various corner cases of the parser
 */
public class ParserTest extends NoStdOutErrTest {

    private static final String LANG = "en";
    private static final String COUNTRY = "CA";
    private static final String VARIANT = "utf8";
    private static final Locale LANG_LOCALE = new Locale(LANG);
    private static final Locale LANG_COUNTRY_LOCALE = new Locale(LANG, COUNTRY);
    private static final Locale ALL_LOCALE = new Locale(LANG, COUNTRY, VARIANT);

    ParserSettings defaultParser = new ParserSettings();
    ParserSettings strictParser = new ParserSettings(true, true, true);

    @Test(expected = MissingInformationException.class)
    public void testMissingInfoFullLocale() throws ParseException {
        String data = "<jnlp></jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInformationDescs(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testOneFullyLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);

        Assert.assertEquals("Title should be `English_CA_utf8_T' but wasn't",
                "English_CA_utf8_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_utf8_V' but wasn't",
                "English_CA_utf8_V", file.getVendor());
    }

    @Test
    public void testOneLangCountryLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_CA_T' but wasn't",
                "English_CA_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testOneLangLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testGeneralizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `Generalized_T' but wasn't",
                "Generalized_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());
    }

    @Test
    public void testTwoDifferentLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>French_T</title>\n"
                + "    <vendor>French_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testTwoLocalizedWithSameLangInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_CA_T' but wasn't",
                "English_CA_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testTwoSameLangOneMissingTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testTwoSameLangWithGeneralizedTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `Generalized_T' but wasn't",
                "Generalized_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingVendorFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingLocalizedTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingLocalizedVendorFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testEmptyLocalizedVendorFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testFallbackEmptyLocalizedTitleVendorFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly five info descs should be found", infoDescs.size() == 5);

        file.setInfo(infoDescs);

        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());

        parser.checkForInformation();
    }

    @Test(expected = MissingInformationException.class)
    public void testMissingInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp></jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInformationDescs(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testOneFullyLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testOneLangCountryLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_CA_T' but wasn't",
                "English_CA_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testOneLangLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testGeneralizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `Generalized_T' but wasn't",
                "Generalized_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());
    }

    @Test
    public void testTwoDifferentLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>French_T</title>\n"
                + "    <vendor>French_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testTwoLocalizedWithSameLangInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_CA_T' but wasn't",
                "English_CA_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testTwoSameLangOneMissingTitleLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test
    public void testTwoSameLangWithGeneralizedTitleLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `Generalized_T' but wasn't",
                "Generalized_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_CA_V' but wasn't",
                "English_CA_V", file.getVendor());
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingTitleLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingVendorLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingLocalizedTitleLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingLocalizedVendorLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedTitleLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title></title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testEmptyLocalizedVendorLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testFallbackEmptyLocalizedTitleVendorLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly five info descs should be found", infoDescs.size() == 5);

        file.setInfo(infoDescs);

        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());

        parser.checkForInformation();
    }

    @Test(expected = MissingInformationException.class)
    public void testMissingInfoLangLocale() throws ParseException {
        String data = "<jnlp></jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInformationDescs(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testOneFullyLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testOneLangCountryLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testOneLangLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testGeneralizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `Generalized_T' but wasn't",
                "Generalized_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());
    }

    @Test
    public void testTwoDifferentLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>French_T</title>\n"
                + "    <vendor>French_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testTwoLocalizedWithSameLangInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `English_V' but wasn't",
                "English_V", file.getVendor());
    }

    @Test
    public void testTwoSameLangOneMissingTitleLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingTitleLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingVendorLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly three info descs should be found", infoDescs.size() == 3);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testMissingLocalizedTitleLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <vendor>English_CA_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testMissingLocalizedVendorLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "  </information>\n"
                + "  <information locale='fr'>\n"
                + "    <title>English_CA_T</title>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedTitleLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title></title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testEmptyLocalizedVendorLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, strictParser);
        List<InformationDesc> infoDescs = new ArrayList<>();
        infoDescs.addAll(parser.getInformationDescs(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testFallbackEmptyLocalizedTitleVendorLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information>\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY + "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);

        Assert.assertTrue("Exactly five info descs should be found", infoDescs.size() == 5);

        file.setInfo(infoDescs);

        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());

        parser.checkForInformation();
    }

    @Test
    public void testTwoInformationDescWithDifferentOsAndArch() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information os=\"Win10\" arch=\"i386\">\n"
                + "    <title>Generalized_T</title>\n"
                + "    <vendor>Generalized_V</vendor>\n"
                + "  </information>\n"
                + "  <information os=\"MacOS\" arch=\"x86_64\">\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "  <information>\n"
                + "    <title>English_T</title>\n"
                + "    <vendor>English_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInformationDescs(root);
        Assert.assertTrue("Exactly two info descs should be found", infoDescs.size() == 3);

        Assert.assertThat(infoDescs.get(0).getOs(), is("Win10"));
        Assert.assertThat(infoDescs.get(0).getArch(), is("i386"));
        Assert.assertThat(infoDescs.get(1).getOs(), is("MacOS"));
        Assert.assertThat(infoDescs.get(1).getArch(), is("x86_64"));
        Assert.assertNull(infoDescs.get(2).getOs());
        Assert.assertNull(infoDescs.get(2).getArch());
    }

    @Test
    public void testOverwrittenCodebaseWithValidJnlpCodebase() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                "codebase=\"http://www.redhat.com/\"\n" +
                ">\n" +
                "</jnlp>";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser, overwrittenCodebase);

        Assert.assertEquals("http://www.redhat.com/", parser.getCodeBase().toExternalForm());
    }

    @Test
    public void testOverwrittenCodebaseWithInvalidJnlpCodebase() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                "codebase=\"this codebase is incorrect\"\n" +
                ">\n" +
                "</jnlp>";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser, overwrittenCodebase);

        Assert.assertEquals(overwrittenCodebase.toExternalForm(), parser.getCodeBase().toExternalForm());
    }

    @Test
    public void testOverwrittenCodebaseWithNoJnlpCodebase() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                ">\n" +
                "</jnlp>";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName().getName());
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser, overwrittenCodebase);

        Assert.assertEquals(overwrittenCodebase.toExternalForm(), parser.getCodeBase().toExternalForm());
    }


    @Test
    public void testEmptyCodebase() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp spec=\"1.5+\"\n"
                + "codebase=\"\"  aaa=\"\" "
                + ">\n"
                + "</jnlp>";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        final Node root = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", JNLPFile.JNLP_ROOT_ELEMENT, root.getNodeName().getName());
        final MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        ParseException eex = null;
        //non codebase element is unaffected
        final URL u = NodeUtils.getURL(root, "aaa", null, defaultParser.isStrict());
        Assert.assertEquals(null, u);
        try {
            NodeUtils.getURL(root, JNLPFile.CODEBASE_ATTRIBUTE, null, defaultParser.isStrict());
        }
        catch (ParseException ex) {
            eex = ex;
        }
        Assert.assertEquals(true, eex != null);
        Assert.assertEquals(true, eex instanceof ParseException);
    }


    @Test
    public void testNullMainClassApplication() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc>\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals(null, main1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, defaultParser, null);
        String main2 = parser2.getEntryPointDesc(root2).getMainClass();
        Assert.assertEquals(null, main2);

    }

    @Test
    public void testNullMainClassInstaller() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<installer-desc>\n"
                + "</installer-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals(null, main1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        String main2 = parser2.getEntryPointDesc(root2).getMainClass();
        Assert.assertEquals(null, main2);

    }

    @Test(expected = ParseException.class)
    public void testNullMainClassApplet() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<applet-desc>\n"
                + "</applet-desc>\n"
                + "</jnlp>";

        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        parser1.getEntryPointDesc(root1).getMainClass();
        //both throw
    }


    @Test
    public void testOkMainClassApplication() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"some.main.class\">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals("some.main.class", main1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        String main2 = parser2.getEntryPointDesc(root2).getMainClass();
        Assert.assertEquals("some.main.class", main2);

    }


    @Test(expected = ParseException.class)
    public void testNeedToBeTrimmed1MainClassApplication() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"  some.main.class  \">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals("some.main.class", main1);

        //strict throws
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        parser2.getEntryPointDesc(root2).getMainClass();

    }

    @Test(expected = ParseException.class)
    public void testNeedToBeTrimmed2MainClassApplication() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"\nsome.main.class\t\">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals("some.main.class", main1);

        //strict throws
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        parser2.getEntryPointDesc(root2).getMainClass();

    }

    @Test(expected = ParseException.class)
    public void testSpacesInsidePersistedMainClassApplication() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"\nsom e.main .class\t\">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals("som e.main .class", main1);

        //strict throws
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        parser2.getEntryPointDesc(root2).getMainClass();
    }

    @Test(expected = ParseException.class)
    public void testSpacesAroundDots() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"\nsome\t.\nanother . main\t.class. here\t\">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String main1 = parser1.getEntryPointDesc(root1).getMainClass();
        Assert.assertEquals("some . another . main .class. here", main1);

        //strict throws
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        parser2.getEntryPointDesc(root2).getMainClass();
    }

    @Test
    public void testProgressClassAttributeInApplicationDescriptor() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<application-desc main-class=\"some.MainClass\" progress-class=\"some.ProgressClass\">\n"
                + "</application-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String progressClass1 = parser1.getApplicationDesc(ApplicationType.JAVA, root1.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        String progressClass2 = parser2.getApplicationDesc(ApplicationType.JAVA, root2.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass2);
    }

    @Test
    public void testProgressClassAttributeInAppletDescriptor() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<applet-desc main-class=\"some.MainClass\" name=\"MyApplet\" width=\"100\" height=\"100\"\n"
                + "progress-class=\"some.ProgressClass\">\n"
                + "</applet-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String progressClass1 = parser1.getAppletDesc(root1.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        String progressClass2 = parser2.getAppletDesc(root2.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass2);
    }

    @Test
    public void testProgressClassAttributeInInstallerDescriptor() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n"
                + "<jnlp codebase=\"http://someNotExistingUrl.com\"  >\n"
                + "<installer-desc main-class=\"some.MainClass\" progress-class=\"some.ProgressClass\">\n"
                + "</installer-desc>\n"
                + "</jnlp>";

        final XMLParser defaultXmlParser = XmlParserFactory.getParser(defaultParser.getParserType());
        Node root1 = defaultXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root1.getNodeName().getName());
        MockJNLPFile file1 = new MockJNLPFile(LANG_LOCALE);
        Parser parser1 = new Parser(file1, null, root1, defaultParser, null);
        String progressClass1 = parser1.getInstallerDesc(root1.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass1);

        //strict also ok
        final XMLParser strictXmlParser = XmlParserFactory.getParser(strictParser.getParserType());
        Node root2 = strictXmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("Root name is not jnlp", "jnlp", root2.getNodeName().getName());
        MockJNLPFile file2 = new MockJNLPFile(LANG_LOCALE);
        Parser parser2 = new Parser(file2, null, root2, strictParser, null);
        String progressClass2 = parser2.getInstallerDesc(root2.getFirstChild()).getProgressClass();
        Assert.assertEquals("some.ProgressClass", progressClass2);
    }
}
