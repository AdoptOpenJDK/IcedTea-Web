/* JNLPFileTest.java
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

import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JnlpInformationElementTest extends NoStdOutErrTest{

    private JNLPFile setUp(final String jnlpContent) throws ParseException, MalformedURLException {
        final URL codeBase = new URL("http://icedtea.classpath.org");
        final InputStream is = new ByteArrayInputStream(jnlpContent.getBytes());
        return new JNLPFile(is, codeBase, new ParserSettings(false,false,false));
    }

    @Test
    public void testGetInformationByLocale() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\">\n"
                + "    <title>Titel</title>\n"
                + "    <vendor>Hersteller</vendor>\n"
                + "    <description>Beschreibung</description>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.GERMAN);

        // then
        assertThat(information.getTitle(), is("Titel"));
        assertThat(information.getVendor(), is("Hersteller"));
        assertThat(information.getDescription(), is("Beschreibung"));
    }

    @Test
    public void testGetInformationByLocaleAndOsWithFallbacksForMissing() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\">\n"
                + "    <title>Titel</title>\n"
                + "    <vendor>Hersteller</vendor>\n"
                + "  </information>\n"
                + "  <information locale=\"de\" os=\"Mac OS X\">\n"
                + "    <vendor>Apple</vendor>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.GERMAN, "Mac OS X", null);

        // then
        assertThat(information.getTitle(), is("Titel"));
        assertThat(information.getVendor(), is("Apple"));
        assertThat(information.getDescription(), is("Description"));
    }

    @Test
    public void testGetInformationByOsWithFallbacksForMissing() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\">\n"
                + "    <title>Titel</title>\n"
                + "    <vendor>Hersteller</vendor>\n"
                + "  </information>\n"
                + "  <information os=\"Mac OS X\">\n"
                + "    <vendor>Apple</vendor>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.GERMAN, "Mac OS X", null);

        // then
        assertThat(information.getTitle(), is("Titel"));
        assertThat(information.getVendor(), is("Apple"));
        assertThat(information.getDescription(), is("Description"));
    }

    @Test
    public void testGetInformationByLocaleAndOsAndArchWithFallbacksForMissing() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\">\n"
                + "    <title>Titel</title>\n"
                + "    <vendor>Hersteller</vendor>\n"
                + "    <description>Beschreibung</description>\n"
                + "  </information>\n"
                + "  <information locale=\"en\" os=\"Mac OS X\" arch=\"x86_64\">\n"
                + "    <title>MacOS Title</title>\n"
                + "    <vendor>Apple</vendor>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.ENGLISH, "Mac OS X", "x86_64");

        // then
        assertThat(information.getTitle(), is("MacOS Title"));
        assertThat(information.getVendor(), is("Apple"));
        assertThat(information.getDescription(), is("Description"));
    }

    @Test
    public void testGetInformationFromFallbackAsNoSuitableLocaleAndOsAndArchMatch() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\" os=\"Win10\" arch=\"i386\">\n"
                + "    <title>Titel</title>\n"
                + "    <vendor>Microsoft</vendor>\n"
                + "    <description>Beschreibung</description>\n"
               + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.ENGLISH, "Mac OS X", "x86_64");

        // then
        assertThat(information.getTitle(), is("Title"));
        assertThat(information.getVendor(), is("Vendor"));
        assertThat(information.getDescription(), is("Description"));
    }

    @Test
    public void testGetSpecificInformationWithOsPrefixMatchWithFallbacksForMissing() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"de\" os=\"Win10\">\n"
                + "    <vendor>Windows</vendor>\n"
                + "    <description>Beschreibung</description>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.GERMAN, "Win", null);

        // then
        assertThat(information.getTitle(), is("Title"));
        assertThat(information.getVendor(), is("Windows"));
        assertThat(information.getDescription(), is("Beschreibung"));
    }

    @Test
    public void testGetSpecificInformationWithArchPrefixMatchWithFallbacksForMissing() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <vendor>Vendor</vendor>\n"
                + "    <description>Description</description>\n"
                + "  </information>\n"
                + "  <information locale=\"en\" os=\"Mac OS X\" arch=\"x86_64\">\n"
                + "    <vendor>Apple</vendor>\n"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.ENGLISH, null, "x86");

        // then
        assertThat(information.getTitle(), is("Title"));
        assertThat(information.getVendor(), is("Apple"));
        assertThat(information.getDescription(), is("Description"));
    }

    @Test
    public void testAssociationsOverwrite() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <association extensions=\"*.html\" mime-type=\"text/html\">"
                + "      <description>Best browser ever</description>"
                + "      <icon href=\"icon.gif\"/>"
                + "    </association>"
                + "  </information>\n"
                + "  <information locale=\"en\">\n"
                + "    <title>English Title</title>\n"
                + "    <association extensions=\"*.html\" mime-type=\"text/html\">"
                + "      <description>Best browser ever</description>"
                + "      <icon href=\"icon.gif\"/>"
                + "    </association>"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.ENGLISH);

        // then
        assertThat(information.getTitle(), is("English Title"));
        assertThat(information.getAssociations(), arrayWithSize(1));
    }


    @Test
    public void testAssociationsExtend() throws MalformedURLException, ParseException {
        // given
        final JNLPFile jnlpFile = setUp("<jnlp>\n"
                + "  <information>\n"
                + "    <title>Title</title>\n"
                + "    <association extensions=\"*.html\" mime-type=\"text/html\">"
                + "      <description>Best browser ever</description>"
                + "      <icon href=\"icon.gif\"/>"
                + "    </association>"
                + "  </information>\n"
                + "  <information locale=\"en\">\n"
                + "    <title>English Title</title>\n"
                + "    <association extensions=\"*.htmlx\" mime-type=\"text/htmlx\">"
                + "      <description>Best browser ever</description>"
                + "      <icon href=\"icon.gif\"/>"
                + "    </association>"
                + "  </information>\n"
                + "  <resources>\n"
                + "  </resources>\n"
                + "</jnlp>\n"
        );

        // when
        final InformationDesc information = jnlpFile.getInformation(Locale.ENGLISH);

        // then
        assertThat(information.getTitle(), is("English Title"));
        assertThat(information.getAssociations(), arrayWithSize(2));
    }

}
