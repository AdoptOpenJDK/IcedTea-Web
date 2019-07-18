// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFile;
import net.adoptopenjdk.icedteaweb.xmlparser.Node;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;

/**
 * Test various corner cases of the parser
 */
public class ParserVersionStringTest extends NoStdOutErrTest {
    private static Node root;
    private static Parser parser;

    @BeforeClass
    public static void setUp() throws ParseException {
        ClassLoader cl = ParserBasicTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        final ParserSettings defaultParserSettings = new ParserSettings();
        final InputStream jnlpStream = cl.getResourceAsStream("net/sourceforge/jnlp/ParserVersionStringTest.jnlp");
        final XMLParser xmlParser = XmlParserFactory.getParser(defaultParserSettings.getParserType());
        root = xmlParser.getRootNode(jnlpStream);
        parser = new Parser(new DummyJNLPFile(), null, root, defaultParserSettings);
    }

    @Test
    public void testSpecVersionsRequiredByJnlpFile() {
        Assert.assertEquals("1.0+", parser.getSpecVersion().toString());
    }

    @Test
    public void testJnlpFileVersion() {
        Assert.assertEquals("2.1.1-rc1", parser.getFileVersion().toString());
    }

   @Test
    public void testExactVersionOfMainJarResource() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);
        JARDesc mainJar = resources.getMainJAR();
        Assert.assertNotNull(mainJar);
        Assert.assertEquals("1.2", mainJar.getVersion().toString());
    }

    @Test
    public void testVersionStringsOfJarResources() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);
        final JARDesc[] jars = resources.getJARs();

        assertThat(jars, hasItemInArray(hasProperty("version", hasToString("2.4 2.5 2.6"))));
        assertThat(jars, hasItemInArray(hasProperty("version", hasToString("2.4* 2.5.0"))));
        assertThat(jars, hasItemInArray(hasProperty("version", hasToString("2.4+"))));
        assertThat(jars, hasItemInArray(hasProperty("version", hasToString("2.4.0_04 1.4*&2.4.1_02+"))));
    }

    @Test
    public void testVersionStringOfNativeLibs() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);
        final JARDesc[] jars = resources.getJARs();

        assertThat(jars, hasItemInArray(hasProperty("version", hasToString("1.0.0-rc1"))));
    }

    @Test
    public void testVersionStringOfExtensions() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);
        final ExtensionDesc[] extensions = resources.getExtensions();

        assertThat(extensions, hasItemInArray(hasProperty("version", hasToString("0.1.1"))));
    }
}
