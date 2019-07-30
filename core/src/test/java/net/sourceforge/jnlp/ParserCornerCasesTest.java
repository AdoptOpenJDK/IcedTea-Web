/* ParserCornerCases.java
   Copyright (C) 2011 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.testing.annotations.KnownToFail;
import net.adoptopenjdk.icedteaweb.xmlparser.Node;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLElement;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;
import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.NORMAL;

/**
 * Test various corner cases of the parser
 */
public class ParserCornerCasesTest {
    private static final ParserSettings defaultParser = new ParserSettings(false, true, true);

    @Test
    public void testCdata() throws ParseException, XMLParseException, IOException {
        String data = "<argument><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <!DOCT" +
                "YPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"> <properties> <entry key=\"key\">value</entry> </properties> ]]></argument>";
        XMLElement target = new XMLElement();
        target.parseFromReader(new StringReader(data));
        Assert.assertEquals("argument", target.getName());
        Assert.assertTrue("too small", target.getContent().length() > 20);
        Assert.assertTrue(target.getContent().contains("xml"));
        Assert.assertTrue(target.getContent().contains("DOCTYPE"));
        Assert.assertTrue(target.getContent().contains("<entry key=\"key\">value</entry>"));

        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node node = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        Assert.assertEquals("argument", node.getNodeName().getName());
        String contents = node.getNodeValue();
        Assert.assertTrue(contents.contains("xml"));
        Assert.assertTrue(contents.contains("DOCTYPE"));
        Assert.assertTrue(contents.contains("<entry key=\"key\">value</entry>"));
    }

    @Test
    public void testCdataNested() throws ParseException, XMLParseException, IOException {
        String data = "<jnlp>\n" +
                "<application-desc>\n" +
                "<argument>\n" +
                "<![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"> <properties> <entry key=\"key\">value</entry> </properties> ]]>" +
                "</argument>\n" +
                "<argument>1</argument>\n" +
                "</application-desc>\n" +
                "</jnlp>";
        XMLElement elem = new XMLElement();
        elem.parseFromReader(new StringReader(data));
        XMLElement target = (elem.enumerateChildren().nextElement()).enumerateChildren().nextElement();
        Assert.assertEquals("argument", target.getName());
        Assert.assertTrue("too small", target.getContent().length() > 20);
        Assert.assertTrue(target.getContent().contains("xml"));
        Assert.assertTrue(target.getContent().contains("DOCTYPE"));
        Assert.assertTrue(target.getContent().contains("<entry key=\"key\">value</entry>"));

        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node node = xmlParser.getRootNode(new ByteArrayInputStream(data.getBytes()));
        node = node.getFirstChild().getFirstChild();
        Assert.assertEquals("argument", node.getNodeName().getName());
        String contents = node.getNodeValue();
        Assert.assertTrue(contents.contains("xml"));
        Assert.assertTrue(contents.contains("DOCTYPE"));
        Assert.assertTrue(contents.contains("<entry key=\"key\">value</entry>"));
    }

    @Test
    @Ignore
    @KnownToFail
    public void testCDataFirstChild() throws XMLParseException, IOException {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\">\n" +
                "<![CDATA[Text you want to escape goes here...<test> random tag test </test>]]>\n" +
                "<information/>\n" +
                "</jnlp>";
        XMLElement elem = new XMLElement();
        elem.parseFromReader(new StringReader(xml));
    }

    @Test
    @Ignore
    @KnownToFail
    public void testCDataSecondChild() throws XMLParseException, IOException {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\">\n" +
                "<information/>\n" +
                "<![CDATA[Text you want to escape goes here...<test> random tag test </test>]]>\n" +
                "</jnlp>";
        XMLElement elem = new XMLElement();
        elem.parseFromReader(new StringReader(xml));
    }

    @Test
    public void testUnsupportedSpecNumber() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='11.11'></jnlp>";
        final XMLParser parser1 = XmlParserFactory.getParser(MALFORMED);
        Node root = parser1.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser parser = new Parser(null, null, root, defaultParser);
        Assert.assertEquals("11.11", parser.getSpecVersion().toString());
    }

    @Test
    public void testApplicationAndComponent() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp><application-desc/><component-desc/></jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser parser = new Parser(null, null, root, defaultParser);
        Assert.assertNotNull(parser.getEntryPointDesc(root));
    }

    @Test
    public void testCommentInElements() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='1.0' <!-- comment -->> </jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
        Assert.assertEquals("1.0", p.getSpecVersion().toString());
    }

    @Test
    public void testNestedComments() throws ParseException {
        String malformedJnlp = "<?xml?>" +
                "<jnlp><information><title>testNestedComments</title>" +
                "<vendor>IcedTea</vendor><description>" +
                "<!-- outer <!-- inner --> -->" +
                "</description></information></jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
        Assert.assertEquals(" -->", p.getInformationDescs(root).get(0).getDescription());
    }

    @Test
    public void testDoubleDashesInComments() throws ParseException {
        String malformedJnlp = "<?xml?>" +
                "<jnlp> <!-- \n" +
                " -- a very very long and \n" +
                " -- multiline comment \n" +
                " -- that contains double dashes \n" +
                " -->\n" +
                "  <information/>" +
                "</jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        new Parser(null, null, root, defaultParser);
    }

    @Test
    public void testCommentInElements2() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp <!-- comment --> spec='1.0'> </jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
        //default is used
        Assert.assertEquals("1.0+", p.getSpecVersion().toString());
    }

    @Test
    public void testCommentInElements2_malformedOff() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp <!-- comment --> spec='1.0'> </jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
        Assert.assertEquals("1.0", p.getSpecVersion().toString());
    }

    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testCommentInAttributes() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='<!-- something -->'></jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
    }

    @Test
    public void testCommentInAttributes_malformedOff() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='<!-- something -->'></jnlp>";
        final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
        Node root = xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, defaultParser);
        //default is used
        Assert.assertEquals("1.0+", p.getSpecVersion().toString());
    }

    @Test
    public void testCommentInElements3_malformedOff() throws IOException, ParseException {
        //having comment inside element declaration is invalid but internal parser can handle it
        try (InputStream fileStream = ClassLoader.getSystemClassLoader().getResourceAsStream("net/sourceforge/jnlp/templates/template5.jnlp")) {
            final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
            Node root = xmlParser.getRootNode(fileStream);
            String a = root.getChildNodes()[2].getAttribute("main-class");
            Assert.assertEquals("*", a);
        }
    }

    @Test
    @Ignore
    @KnownToFail
    public void testCommentInElements3_malformedOn() throws IOException, ParseException {
        //having comment inside element declaration is invalid anyway, so tagsoup can be excused for failing in this case
        try (InputStream fileStream = ClassLoader.getSystemClassLoader().getResourceAsStream("net/sourceforge/jnlp/templates/template5.jnlp")) {
            final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
            Node root = xmlParser.getRootNode(fileStream);
            String a = root.getChildNodes()[2].getAttribute("main-class");
            Assert.assertEquals("*", a);
        }
    }
}
