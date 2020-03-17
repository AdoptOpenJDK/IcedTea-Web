/* JNLPMatcherTest.java
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

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.testing.annotations.KnownToFail;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JNLPMatcherTest {

    private static final boolean IS_TEMPLATE = true;
    private static final boolean IS_NOT_TEMPLATE = false;
    private static final boolean MALFORMED_ALLOWED = false;
    private static final ParserSettings DEFAULT_SETTINGS = new ParserSettings(true, true, MALFORMED_ALLOWED);


    private final ClassLoader cl = ClassLoader.getSystemClassLoader();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @KnownToFail
    @Ignore
    public void testTemplateCDATA() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template0.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with CDATA", test.isMatch());
    }

    @Test
    public void testTemplateDuplicate() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template1.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with an exact duplicate of the launching JNLP file", test.isMatch());
    }

    @Test
    public void testTemplateWildCharsRandom() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template2.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with wildchars as attribute/element values", test.isMatch());
    }

    @Test
    public void testTemplateDifferentOrder() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template3.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with attributes/elements in different order", test.isMatch());
    }

    @Test
    public void testTemplateWildCharsAsAllValues() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template4.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with wildchars as ALL element/attribute values", test.isMatch());
    }

    @Test
    public void testTemplateComments() throws Exception {
        //having comment inside element declaration is invalid but internal parser can handle it
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template5.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing template with comments", test.isMatch());
    }

    @Test
    public void testTemplateDifferentValues() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template6.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing template with different attribute/element values", test.isMatch());
    }

    @Test
    public void testTemplateExtraChild() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template7.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing template by adding an additional children to element", test.isMatch());
    }

    @Test
    public void testTemplateFewerChild() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template8.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing template by removing children from element", test.isMatch());
    }

    @Test
    public void testTemplateDifferentFile() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template9.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing template with a complete different JNLP template file", test.isMatch());
    }

    @Test
    @KnownToFail
    @Ignore
    public void testApplicationCDATA() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application0.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing application with CDATA", test.isMatch());
    }

    @Test
    public void testApplicationDuplicate() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application1.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing application with an exact duplicate of the launching JNLP file", test.isMatch());
    }

    @Test
    public void testApplicationDifferentOrder() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application2.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing application with the same element/attribute name and value pair in different orders", test.isMatch());
    }

    @Test
    public void testApplicationComments() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application3.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertTrue("Testing application with comments", test.isMatch());
    }

    @Test
    public void testApplicationWildCharsRandom() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application4.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing application with wildchars as attribute/element values", test.isMatch());
    }

    @Test
    public void testApplicationDifferentCodebaseValue() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application5.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing application with a different codebase attribute value", test.isMatch());
    }

    @Test
    public void testApplicationExtraChild() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application6.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing application by adding additional children to element", test.isMatch());
    }

    @Test
    public void testApplicationFewerChild() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application7.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing application by removing children from element", test.isMatch());
    }

    @Test
    public void testApplicationDifferentFile() throws Exception {
        final JNLPMatcher test = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application8.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing application with a complete different JNLP application file", test.isMatch());
    }

    @Test
    public void testCallingMatchMultiple() throws Exception {
        final JNLPMatcher appMatcher = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/application/application8.jnlp", IS_NOT_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing by calling JNLPMatcher.match() multiple times. Checking to see if the returns value is consistent", appMatcher.isMatch());
        assertFalse("Testing by calling JNLPMatcher.match() multiple times. Checking to see if the returns value is consistent", appMatcher.isMatch());

        final JNLPMatcher tempMatcher = new JNLPMatcher(
                jarFile("net/sourceforge/jnlp/templates/template6.jnlp", IS_TEMPLATE),
                jnlpFile(),
                DEFAULT_SETTINGS
        );

        assertFalse("Testing by calling JNLPMatcher.match() multiple times. Checking to see if the returns value is consistent", tempMatcher.isMatch());
        assertFalse("Testing by calling JNLPMatcher.match() multiple times. Checking to see if the returns value is consistent", tempMatcher.isMatch());
    }

    @Test(timeout = 5000 /*ms*/)
    public void testIsMatchDoesNotHangOnLargeData() throws Exception {
        /* construct an alphabet containing characters 'a' to 'z' */
        final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        final int ALPHABET_SIZE = alphabet.length;

        /* generate a long but random string using the alphabet */
        final Random r = new Random();
        final int STRING_SIZE = 1024 * 1024; // 1 MB
        final StringBuilder descriptionBuilder = new StringBuilder(STRING_SIZE);
        for (int i = 0; i < STRING_SIZE; i++) {
            descriptionBuilder.append(alphabet[r.nextInt(ALPHABET_SIZE)]);
        }
        final String longDescription = descriptionBuilder.toString();

        final String file =
                "<jnlp>\n" +
                        "  <information>\n" +
                        "    <title>JNLPMatcher hangs on large file size</title>\n" +
                        "    <vendor>IcedTea</vendor>\n" +
                        "    <description>" + longDescription + "</description>\n" +
                        "  </information>\n" +
                        "</jnlp>\n";

        InputStream reader1 = new ByteArrayInputStream(file.getBytes(UTF_8));
        JNLPMatcher matcher = new JNLPMatcher(jarFile(reader1, IS_NOT_TEMPLATE), jnlpFile(file), DEFAULT_SETTINGS);
        assertTrue(matcher.isMatch());
    }

    private File jnlpFile() throws URISyntaxException {
        final URL url = cl.getResource("net/sourceforge/jnlp/launchApp.jnlp");
        assertNotNull(url);
        return Paths.get(url.toURI()).toFile();
    }

    private File jnlpFile(String content) throws IOException {
        final File jnlp = File.createTempFile("jnlpMatcherTest", ".jnlp", temporaryFolder.getRoot());
        FileUtils.saveFileUtf8(content, jnlp);
        return jnlp;
    }

    private File jarFile(String jnlpPath, boolean isTemplate) throws IOException {
        try (final InputStream inStream = cl.getResourceAsStream(jnlpPath)) {
            return jarFile(inStream, isTemplate);
        }
    }

    private File jarFile(InputStream in, boolean isTemplate) throws IOException {
        final File jar = File.createTempFile("jnlpMatcherTest", ".jar", temporaryFolder.getRoot());

        try (final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jar))) {
            final JarEntry jarEntry = new JarEntry(isTemplate ? JNLPMatcher.TEMPLATE : JNLPMatcher.APPLICATION);
            jarOutputStream.putNextEntry(jarEntry);
            IOUtils.copy(in, jarOutputStream);
        }

        return jar;
    }
}
