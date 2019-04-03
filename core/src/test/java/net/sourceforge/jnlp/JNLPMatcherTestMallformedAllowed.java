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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import net.sourceforge.jnlp.annotations.KnownToFail;
import org.junit.Assert;
import org.junit.Test;

public class JNLPMatcherTestMallformedAllowed {

    final String tests[] = JNLPMatcherTest.tests;

    private final ClassLoader cl = ClassLoader.getSystemClassLoader();
    private final boolean MALLFORMED_ALLOWED = true;

    private InputStream getLaunchReader() {
        return cl.getResourceAsStream("net/sourceforge/jnlp/launchApp.jnlp");
    }

    @Test
    @KnownToFail
    public void testTemplateCDATA() throws JNLPMatcherException, IOException {

        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template0.jnlp")) {

            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
             Assert.assertEquals(tests[0], true, test.isMatch());
        }
    }

    @Test
    public void testTemplateDuplicate() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template1.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[1], true, test.isMatch());
        }
    }

    @Test
    public void testTemplateWildCharsRandom() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template2.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[2], true, test.isMatch());
        }
    }

    @Test
    public void testTemplateDifferentOrder() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template3.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[3], true, test.isMatch());
        }
    }

    @Test
    public void testTemplateWildCharsAsAllValues() throws JNLPMatcherException,
            IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template4.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[4], true, test.isMatch());
        }
    }

    @Test
    @KnownToFail
    public void testTemplateComments() throws JNLPMatcherException, IOException {
    //heving comment inside element declaration is invalid anyway, so tagsoup can be excused for failing in this case
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template5.jnlp")) {
                       JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[5], true, test.isMatch());
        }
    }

    @Test
    public void testTemplateDifferentValues() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template6.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[6], false, test.isMatch());
        }
    }

    @Test
    public void testTemplateExtraChild() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template7.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[7], false, test.isMatch());
        }
    }

    @Test
    public void testTemplateFewerChild() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template8.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[8], false, test.isMatch());
        }
    }

    @Test
    public void testTemplateDifferentFile() throws JNLPMatcherException, IOException {

        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template9.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[9], false, test.isMatch());
        }
    }

    @Test
    @KnownToFail
    public void testApplicationCDATA() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application0.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[10], true, test.isMatch());
        }
    }

    @Test
    public void testApplicationDuplicate() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application1.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[11], true, test.isMatch());
        }
    }

    @Test
    public void testApplicationDifferentOrder() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application2.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[12], true, test.isMatch());
        }
    }

    @Test
    public void testApplicationComments() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application3.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[13], true, test.isMatch());
        }
    }

    @Test
    public void testApplicationWildCharsRandom() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application4.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[14], false, test.isMatch());
        }
    }

    @Test
    public void testApplicationDifferentCodebaseValue() throws JNLPMatcherException,
            IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application5.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[15], false, test.isMatch());
        }
    }

    @Test
    public void testApplicationExtraChild() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application6.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[16], false, test.isMatch());
        }
    }

    @Test
    public void testApplicationFewerChild() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application7.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[17], false, test.isMatch());
        }
    }

    @Test
    public void testApplicationDifferentFile() throws JNLPMatcherException, IOException {
        try (InputStream launchReader = this.getLaunchReader(); InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp")) {
            JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            Assert.assertEquals(tests[18], false, test.isMatch());
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testNullJNLPFiles() throws IOException {

        Exception expectedException = null;
        InputStream fileStream;
        try (InputStream launchReader = this.getLaunchReader()) {
            fileStream = cl
                    .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp");
            try {
                JNLPMatcher test = new JNLPMatcher(null, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            } catch (Exception e) {
                expectedException = e;
            }
            Assert.assertEquals(
                    "Checking exception after trying to create an instance with null signed application/template reader",
                    expectedException.getClass().getName(),
                    "net.sourceforge.jnlp.JNLPMatcherException");
            try {
                JNLPMatcher test = new JNLPMatcher(fileStream, null, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            } catch (Exception e) {
                expectedException = e;
            }
            Assert.assertEquals(
                    "Checking exception after trying to create an instance with null launching JNLP file reader",
                    expectedException.getClass().getName(),
                    "net.sourceforge.jnlp.JNLPMatcherException");
            try {
                JNLPMatcher test = new JNLPMatcher(null, null, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
            } catch (Exception e) {
                expectedException = e;
            }
            Assert.assertEquals(
                    "Checking exception after trying to create an instance with both readers being null",
                    expectedException.getClass().getName(),
                    "net.sourceforge.jnlp.JNLPMatcherException");
        }        fileStream.close();
    }

    @Test
    public void testCallingMatchMultiple() throws JNLPMatcherException, IOException {
        // Check with application
        InputStream launchReader = this.getLaunchReader();
        InputStream fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/application/application8.jnlp");
        
        JNLPMatcher test = new JNLPMatcher(fileStream, launchReader, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));

        Assert.assertEquals(tests[19], false, test.isMatch());
        Assert.assertEquals(tests[19], false, test.isMatch());

        fileStream.close();
        launchReader.close();

        // Check with template
        launchReader = this.getLaunchReader();

        fileStream = cl
                .getResourceAsStream("net/sourceforge/jnlp/templates/template6.jnlp");

        test = new JNLPMatcher(fileStream, launchReader, true, new ParserSettings(true, true, MALLFORMED_ALLOWED));

        Assert.assertEquals(tests[19], false, test.isMatch());
        Assert.assertEquals(tests[19], false, test.isMatch());

        fileStream.close();
        launchReader.close();
    }

    @Test (timeout=5000 /*ms*/)
    public void testIsMatchDoesNotHangOnLargeData() throws JNLPMatcherException, UnsupportedEncodingException {
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

        InputStream reader1 = new ByteArrayInputStream(file.getBytes("utf-8"));
        InputStream reader2 = new ByteArrayInputStream(file.getBytes("utf-8"));
        JNLPMatcher matcher = new JNLPMatcher(reader1, reader2, false, new ParserSettings(true, true, MALLFORMED_ALLOWED));
        Assert.assertTrue(matcher.isMatch());
    }
}
