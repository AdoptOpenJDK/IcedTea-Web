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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.jnlp.mock.MockJNLPFile;

import org.junit.Assert;
import org.junit.Test;

/** Test various corner cases of the parser */
public class ParserTest {

    private static final String LANG = "en";
    private static final String COUNTRY = "CA";
    private static final String VARIANT = "utf8";
    private static final Locale LANG_LOCALE = new Locale(LANG);
    private static final Locale LANG_COUNTRY_LOCALE = new Locale(LANG, COUNTRY);
    private static final Locale ALL_LOCALE = new Locale(LANG, COUNTRY, VARIANT);

    ParserSettings defaultParser=new ParserSettings();
    @Test(expected = MissingInformationException.class)
    public void testMissingInfoFullLocale() throws ParseException {
        String data = "<jnlp></jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInfo(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test
    public void testOneFullyLocalizedInfoFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly two info descs should be found",infoDescs.size() == 2);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedTitleFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingVendorException.class)
    public void testEmptyLocalizedVendorFullLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(ALL_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInfo(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testOneFullyLocalizedInfoLangCountryLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly two info descs should be found",infoDescs.size() == 2);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_COUNTRY_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        parser.getInfo(root);
    }

    @Test(expected = MissingTitleException.class)
    public void testEmptyLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "'>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly one info desc should be found", infoDescs.size() == 1);

        file.setInfo(infoDescs);
        parser.checkForInformation();
    }

    @Test(expected = MissingTitleException.class)
    public void testOneFullyLocalizedInfoLangLocale() throws ParseException {
        String data = "<jnlp>\n"
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>English_CA_utf8_T</title>\n"
                + "    <vendor>English_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

        Assert.assertTrue("Exactly two info descs should be found",infoDescs.size() == 2);

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = new ArrayList<InformationDesc>();
        infoDescs.addAll(parser.getInfo(root));

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
                + "  <information locale='" + LANG + "_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title></title>\n"
                + "    <vendor></vendor>\n"
                + "  </information>\n"
                + "  <information locale='fr_" + COUNTRY +  "." + VARIANT + "'>\n"
                + "    <title>French_CA_utf8_T</title>\n"
                + "    <vendor>French_CA_utf8_V</vendor>\n"
                + "  </information>\n"
                + "</jnlp>\n";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser);
        List<InformationDesc> infoDescs = parser.getInfo(root);

        Assert.assertTrue("Exactly five info descs should be found", infoDescs.size() == 5);

        file.setInfo(infoDescs);

        Assert.assertEquals("Title should be `English_T' but wasn't",
                "English_T", file.getTitle());
        Assert.assertEquals("Vendor should be `Generalized_V' but wasn't",
                "Generalized_V", file.getVendor());

        parser.checkForInformation();
    }

    @Test
    public void testOverwrittenCodebaseWithValidJnlpCodebase() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                "codebase=\"http://www.redhat.com/\"\n" +
                ">\n" +
                "</jnlp>";

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
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

        Node root = Parser.getRootNode(new ByteArrayInputStream(data.getBytes()), defaultParser);
        Assert.assertEquals("Root name is not jnlp", "jnlp", root.getNodeName());
        URL overwrittenCodebase = new URL("http://icedtea.classpath.org");

        MockJNLPFile file = new MockJNLPFile(LANG_LOCALE);
        Parser parser = new Parser(file, null, root, defaultParser, overwrittenCodebase);

        Assert.assertEquals(overwrittenCodebase.toExternalForm(), parser.getCodeBase().toExternalForm());
    }
}
