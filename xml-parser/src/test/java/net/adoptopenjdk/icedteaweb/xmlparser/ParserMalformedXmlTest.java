/* ParserMalformedXml.java
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

package net.adoptopenjdk.icedteaweb.xmlparser;

import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.adoptopenjdk.icedteaweb.testing.annotations.KnownToFail;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;
import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.NORMAL;

/**
 * Test how well the parser deals with malformed xml
 */
public class ParserMalformedXmlTest {

    private static String originalJnlp = null;

    @BeforeClass
    public static void setUp() throws IOException {
        ClassLoader cl = ParserMalformedXmlTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        InputStream is = cl.getResourceAsStream("jnlps/basic.jnlp");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder jnlpBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jnlpBuilder.append(line).append("\n");
        }
        originalJnlp = jnlpBuilder.toString();
    }

    @Test
    public void testMissingXmlDeclaration() throws ParseException {
        String malformedJnlp = originalJnlp.replaceFirst("<\\?xml.*\\?>", "");
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Test
    @Ignore
    @KnownToFail
    public void testMalformedArguments() throws ParseException {
        String malformedJnlp = originalJnlp.replace("arg2</argument", "arg2<argument");
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Test
    public void testTagNotClosed() throws ParseException {
        String malformedJnlp = originalJnlp.replace("</jnlp>", "<jnlp>");
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Test
    public void testUnquotedAttributes() throws ParseException {
        String malformedJnlp = originalJnlp.replace("'jnlp.jnlp'", "jnlp.jnlp");
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Test(expected = ParseException.class)
    public void testTagNotClosedNoTagSoup() throws ParseException {
        String malformedJnlp = originalJnlp.replace("</jnlp>", "<jnlp>");
        final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Test(expected = ParseException.class)
    public void testUnquotedAttributesNoTagSoup() throws ParseException {
        String malformedJnlp = originalJnlp.replace("'jnlp.jnlp'", "jnlp.jnlp");
        final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
        xmlParser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
    }

    @Bug(id = "PR2690")
    @Test
    public void testXmlBomTagSoupOff() throws ParseException {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("jnlps/EFBBBF.jnlp");
        Assert.assertNotNull(is);
        final XMLParser xmlParser = XmlParserFactory.getParser(NORMAL);
        xmlParser.getRootNode(is);
    }

    @Test
    public void testXmlBomTagSoupOn() throws ParseException {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("jnlps/EFBBBF.jnlp");
        Assert.assertNotNull(is);
        final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
        xmlParser.getRootNode(is);
    }

}
