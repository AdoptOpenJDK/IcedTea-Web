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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Random;
import net.sourceforge.jnlp.annotations.KnownToFail;

import org.junit.Assert;
import org.junit.Test;

public class JNLPMatcherTest {

    final String tests[] = {
            "Testing template with CDATA",
            "Testing template with an exact duplicate of the launching JNLP file",
            "Testing template with wildchars as attribute/element values",
            "Testing template with attributes/elements in different order",
            "Testing template with wildchars as ALL element/attribute values",
            "Testing template with comments",
            "Testing template with different attribute/element values",
            "Testing template by adding an additional children to element",
            "Testing template by removing children from element",
            "Testing template with a complete different JNLP template file ",
            "Testing application with CDATA",
            "Testing application with an exact duplicate of the launching JNLP file",
            "Testing application with the same element/attribute name and value pair in different orders",
            "Testing application with comments",
            "Testing application with wildchars as attribute/element values",
            "Testing application with a different codebase attribute value",
            "Testing application by adding additional children to element",
            "Testing application by removing children from element",
            "Testing application with a complete different JNLP application file",
            "Testing by calling JNLPMatcher.match() multiple times. Checking to see if the returns value is consistent" };

    final ClassLoader cl = ClassLoader.getSystemClassLoader();

    private InputStreamReader getLaunchReader() {
        InputStream launchStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/launchApp.jnlp");
        return new InputStreamReader(launchStream);
    }

    @Test
    @KnownToFail
    public void testTemplateCDATA() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template0.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[0], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateDuplicate() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template1.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[1], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateWildCharsRandom() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template2.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[2], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateDifferentOrder() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template3.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[3], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateWildCharsAsAllValues() throws JNLPMatcherException,
            IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template4.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[4], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateComments() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template5.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[5], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateDifferentValues() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template6.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[6], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateExtraChild() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template7.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[7], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateFewerChild() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template8.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[8], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testTemplateDifferentFile() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template9.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[9], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    @KnownToFail
    public void testApplicationCDATA() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application0.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[10], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationDuplicate() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application1.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[11], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationDifferentOrder() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application2.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[12], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationComments() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application3.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[13], true, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationWildCharsRandom() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application4.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[14], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationDifferentCodebaseValue() throws JNLPMatcherException,
            IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application5.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[15], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationExtraChild() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application6.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[16], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationFewerChild() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application7.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[17], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @Test
    public void testApplicationDifferentFile() throws JNLPMatcherException, IOException {

        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[18], false, test.isMatch());
        fileReader.close();
        launchReader.close();
    }

    @SuppressWarnings("unused")
    @Test
    public void testNullJNLPFiles() throws IOException {

        Exception expectedException = null;
        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        try {
            JNLPMatcher test = new JNLPMatcher(null, launchReader, false);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertEquals(
                "Checking exception after trying to create an instance with null signed application/template reader",
                expectedException.getClass().getName(),
                "net.sourceforge.jnlp.JNLPMatcherException");

        try {
            JNLPMatcher test = new JNLPMatcher(fileReader, null, false);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertEquals(
                "Checking exception after trying to create an instance with null launching JNLP file reader",
                expectedException.getClass().getName(),
                "net.sourceforge.jnlp.JNLPMatcherException");

        try {
            JNLPMatcher test = new JNLPMatcher(null, null, false);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertEquals(
                "Checking exception after trying to create an instance with both readers being null",
                expectedException.getClass().getName(),
                "net.sourceforge.jnlp.JNLPMatcherException");

        launchReader.close();
        fileReader.close();
    }

    @Test
    public void testCallingMatchMultiple() throws JNLPMatcherException, IOException {

        // Check with application
        InputStreamReader launchReader = this.getLaunchReader();

        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp");
        InputStreamReader fileReader = new InputStreamReader(fileStream);

        JNLPMatcher test = new JNLPMatcher(fileReader, launchReader, false);

        Assert.assertEquals(tests[19], false, test.isMatch());
        Assert.assertEquals(tests[19], false, test.isMatch());

        fileReader.close();
        launchReader.close();

        // Check with template
        launchReader = this.getLaunchReader();

        fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template6.jnlp");
        fileReader = new InputStreamReader(fileStream);

        test = new JNLPMatcher(fileReader, launchReader, true);

        Assert.assertEquals(tests[19], false, test.isMatch());
        Assert.assertEquals(tests[19], false, test.isMatch());

        fileReader.close();
        launchReader.close();
    }

    @Test (timeout=5000 /*ms*/)
    public void testIsMatchDoesNotHangOnLargeData() throws JNLPMatcherException {
        /* construct an alphabet containing characters 'a' to 'z' */
        final int ALPHABET_SIZE = 26;
        char[] alphabet = new char[ALPHABET_SIZE];
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            alphabet[i] = (char)('a' + i);
        }
        /* generate a long but random string using the alphabet */
        final Random r = new Random();
        final int STRING_SIZE = 1024 * 1024; // 1 MB
        StringBuilder descriptionBuilder = new StringBuilder(STRING_SIZE);
        for (int i = 0; i < STRING_SIZE; i++) {
            descriptionBuilder.append(alphabet[r.nextInt(ALPHABET_SIZE)]);
        }
        String longDescription = descriptionBuilder.toString();

        String file =
                "<jnlp>\n" +
                "  <information>\n" +
                "    <title>JNLPMatcher hanges on large file size</title>\n" +
                "    <vendor>IcedTea</vendor>\n" +
                "    <description>" + longDescription + "</description>\n" +
                "  </information>\n" +
                "</jnlp>\n";

        StringReader reader1 = new StringReader(file);
        StringReader reader2 = new StringReader(file);
        JNLPMatcher matcher = new JNLPMatcher(reader1, reader2, false);
        Assert.assertTrue(matcher.isMatch());
    }
}
