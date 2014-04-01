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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.jnlp.JNLPFile.Match;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.mock.MockJNLPFile;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;

import org.junit.Assert;
import org.junit.Test;

public class JNLPFileTest extends NoStdOutErrTest{
    Locale jvmLocale = new Locale("en", "CA", "utf8");
    MockJNLPFile file = new MockJNLPFile(jvmLocale);

    @Test
    public void testCompareAll() {
        Locale[] correctAvailable = { new Locale("en", "CA", "utf8") };
        Assert.assertTrue("Entire locale should match but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY_VARIANT));

        Locale[] mismatchedAvailable = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match variant but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY_VARIANT));
    }

    @Test
    public void testLangAndCountry() {
        Locale[] correctAvailable = { new Locale("en", "CA") };
        Assert.assertTrue("Should match language and country, ignoring variant but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY));

        Locale[] mismatchedAvailable = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match country but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY));

        Locale[] extraMismatched = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match because of extra variant but did.",
                file.localeMatches(jvmLocale, extraMismatched, Match.LANG_COUNTRY));
    }

    @Test
    public void testLangOnly() {
        Locale[] correctAvailable = { new Locale("en") };
        Assert.assertTrue("Should match only language but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG));

        Locale[] mismatchedAvailable = { new Locale("fr", "CA", "utf8") };
        Assert.assertFalse("Should not match language but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG));

        Locale[] extraMismatched = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match because of extra country but did.",
                file.localeMatches(jvmLocale, extraMismatched, Match.LANG));
    }

    @Test
    public void testNoLocalAvailable() {
        Assert.assertTrue("Null locales should match but did not.",
                file.localeMatches(jvmLocale, null, Match.GENERALIZED));

        Locale[] emptyAvailable = {};
        Assert.assertTrue("Empty locales list should match but did not.",
                file.localeMatches(jvmLocale, emptyAvailable, Match.GENERALIZED));

        Locale[] mismatchAvailable = { new Locale("fr", "FR", "utf16") };
        Assert.assertFalse("Locales list should not match generalized case but did.",
                file.localeMatches(jvmLocale, mismatchAvailable, Match.GENERALIZED));
    }

    @Test
    public void testCodebaseConstructorWithInputstreamAndCodebase() throws Exception {
        String jnlpContext = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                "codebase=\"http://icedtea.claspath.org\"\n" +
                ">\n" +
                "" +
                "<information>\n" +
                "<title>Sample Test</title>\n" +
                "<vendor>RedHat</vendor>\n" +
                "<offline-allowed/>\n" +
                "</information>\n" +
                "\n" +
                "<resources>\n" +
                "<j2se version='1.6+' />\n" +
                "<jar href='EmbeddedJnlpJarOne.jar' main='true'/>\n" +
                "<jar href='EmbeddedJnlpJarTwo.jar' main='true'/>\n" +
                "</resources>\n" +
                "\n" +
                "<applet-desc\n" +
                "documentBase=\".\"\n" +
                "name=\"redhat.embeddedjnlp\"\n" +
                "main-class=\"redhat.embeddedjnlp\"\n" +
                "width=\"0\"\n" +
                "height=\"0\"\n" +
                "/>\n" +
                "</jnlp>";

        URL codeBase = new URL("http://www.redhat.com/");

        InputStream is = new ByteArrayInputStream(jnlpContext.getBytes());

        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false,false,false));

        Assert.assertEquals("http://icedtea.claspath.org/", jnlpFile.getCodeBase().toExternalForm());
        Assert.assertEquals("redhat.embeddedjnlp", jnlpFile.getApplet().getMainClass());
        Assert.assertEquals("Sample Test", jnlpFile.getTitle());
        Assert.assertEquals(2, jnlpFile.getResources().getJARs().length);
    }

    @Test
    public void testPropertyRestrictions() throws MalformedURLException, ParseException {
        String jnlpContents = "<?xml version='1.0'?>\n" +
                "<jnlp spec='1.5' href='foo' codebase='bar'>\n" +
                "  <information>\n" +
                "    <title>Parsing Test</title>\n" +
                "    <vendor>IcedTea</vendor>\n" +
                "    <offline-allowed/>\n" +
                "  </information>\n" +
                "  <resources>\n" +
                "    <property name='general' value='general'/>\n" +
                "    <property name='os' value='general'/>\n" +
                "    <property name='arch' value='general'/>\n" +
                "  </resources>\n" +
                "  <resources os='os1'>" +
                "    <property name='os' value='os1'/>\n" +
                "  </resources>\n" +
                "  <resources os='os1' arch='arch1'>" +
                "    <property name='arch' value='arch1'/>\n" +
                "  </resources>\n" +
                "  <resources os='os2' arch='arch2'>\n" +
                "    <property name='os' value='os2'/>\n" +
                "    <property name='arch' value='arch2'/>\n" +
                "  </resources>\n" +
                "  <installer-desc/>\n" +
                "</jnlp>";

        URL codeBase = new URL("http://www.redhat.com/");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false,false,false));

        ResourcesDesc resources;
        Map<String,String> properties;

        resources = jnlpFile.getResources(Locale.getDefault(), "os0", "arch0");
        properties = resources.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("general", properties.get("os"));
        Assert.assertEquals("general", properties.get("arch"));

        resources = jnlpFile.getResources(Locale.getDefault(), "os1", "arch0");
        properties = resources.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os1", properties.get("os"));
        Assert.assertEquals("general", properties.get("arch"));

        resources = jnlpFile.getResources(Locale.getDefault(), "os1", "arch1");
        properties = resources.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os1", properties.get("os"));
        Assert.assertEquals("arch1", properties.get("arch"));

        resources = jnlpFile.getResources(Locale.getDefault(), "os2", "arch2");
        properties = resources.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os2", properties.get("os"));
        Assert.assertEquals("arch2", properties.get("arch"));
    }

    @Bug(id={"PR1533"})
    @Test
    public void testDownloadOptionsAppliedEverywhere() throws MalformedURLException, ParseException {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");

        String jnlpContents = "<?xml version='1.0'?>\n" +
                "<jnlp spec='1.5' href='foo' codebase='bar'>\n" +
                "  <information>\n" +
                "    <title>Parsing Test</title>\n" +
                "    <vendor>IcedTea</vendor>\n" +
                "    <offline-allowed/>\n" +
                "  </information>\n" +
                "  <resources>\n" +
                "    <property name='jnlp.packEnabled' value='false'/>" +
                "    <property name='jnlp.versionEnabled' value='false'/>" +
                "  </resources>\n" +
                "  <resources os='" + os + "'>" +
                "    <property name='jnlp.packEnabled' value='true'/>" +
                "  </resources>\n" +
                "  <resources arch='" + arch + "'>" +
                "    <property name='jnlp.versionEnabled' value='true'/>" +
                "  </resources>\n" +
                "  <installer-desc/>\n" +
                "</jnlp>";

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false,false,false));
        DownloadOptions downloadOptions = jnlpFile.getDownloadOptions();

        Assert.assertTrue(downloadOptions.useExplicitPack());
        Assert.assertTrue(downloadOptions.useExplicitVersion());
    }

    @Bug(id={"PR1533"})
    @Test
    public void testDownloadOptionsFilteredOut() throws MalformedURLException, ParseException {
         String jnlpContents = "<?xml version='1.0'?>\n" +
                "<jnlp spec='1.5' href='foo' codebase='bar'>\n" +
                "  <information>\n" +
                "    <title>Parsing Test</title>\n" +
                "    <vendor>IcedTea</vendor>\n" +
                "    <offline-allowed/>\n" +
                "  </information>\n" +
                "  <resources>\n" +
                "    <property name='jnlp.packEnabled' value='false'/>" +
                "    <property name='jnlp.versionEnabled' value='false'/>" +
                "  </resources>\n" +
                "  <resources os='someOtherOs'>" +
                "    <property name='jnlp.packEnabled' value='true'/>" +
                "  </resources>\n" +
                "  <resources arch='someOtherArch'>" +
                "    <property name='jnlp.versionEnabled' value='true'/>" +
                "  </resources>\n" +
                "  <installer-desc/>\n" +
                "</jnlp>";

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false,false,false));
        DownloadOptions downloadOptions = jnlpFile.getDownloadOptions();

        Assert.assertFalse(downloadOptions.useExplicitPack());
        Assert.assertFalse(downloadOptions.useExplicitVersion());
    }
    
    
    public static final String minimalJnlp = "<?xml version='1.0'?>\n"
            + "<jnlp spec='1.5' href='foo' codebase='.'>\n"
            + "  <information>\n"
            + "    <title>Parsing Test</title>\n"
            + "    <vendor>IcedTea</vendor>\n"
            + "  </information>\n"
            + "<resources>\n"
            + "  </resources>\n"
            + "SECURITY"
            + "</jnlp>";

    @Test
    public void testGetRequestedPermissionLevel1() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "");
        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.NONE, jnlpFile.getRequestedPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel2() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+SecurityDesc.RequestedPermissionLevel.ALL.toJnlpString()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.ALL, jnlpFile.getRequestedPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel3() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.NONE, jnlpFile.getRequestedPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel4() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security>whatever</security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.NONE, jnlpFile.getRequestedPermissionLevel());
    }
    
    @Test
    public void testGetRequestedPermissionLevel5() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+SecurityDesc.RequestedPermissionLevel.J2EE.toJnlpString()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.J2EE, jnlpFile.getRequestedPermissionLevel());
    }
    
    @Test
    //unknown for jnlp
    public void testGetRequestedPermissionLevel6() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><" + SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString() + "/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.NONE, jnlpFile.getRequestedPermissionLevel());
    }
    
    @Test
    //unknown for jnlp
    public void testGetRequestedPermissionLevel7() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><" + SecurityDesc.RequestedPermissionLevel.DEFAULT.toHtmlString() + "/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.NONE, jnlpFile.getRequestedPermissionLevel());
    }
}
