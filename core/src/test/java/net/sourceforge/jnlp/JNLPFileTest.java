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

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationPermissionLevel;
import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.adoptopenjdk.icedteaweb.testing.mock.MockJNLPFile;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;

public class JNLPFileTest extends NoStdOutErrTest{
    Locale jvmLocale = new Locale("en", "CA", "utf8");
    MockJNLPFile file = new MockJNLPFile(jvmLocale);

    @Test
    public void testCodebaseConstructorWithInputstreamAndCodebase() throws Exception {
        String jnlpContext = "<?xml version=\"1.0\"?>\n" +
                "<jnlp spec=\"1.5+\"\n" +
                "href=\"EmbeddedJnlpFile.jnlp\"\n" +
                "codebase=\"http://icedtea.classpath.org\"\n" +
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

        Assert.assertEquals("http://icedtea.classpath.org/", jnlpFile.getCodeBase().toExternalForm());
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
        String os = System.getProperty(OS_NAME);
        String arch = System.getProperty(OS_ARCH);

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
        Assert.assertEquals(ApplicationPermissionLevel.NONE, jnlpFile.getApplicationPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel2() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+ ApplicationPermissionLevel.ALL.getValue()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(ApplicationPermissionLevel.ALL, jnlpFile.getApplicationPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel3() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(ApplicationPermissionLevel.NONE, jnlpFile.getApplicationPermissionLevel());
    }

    @Test
    public void testGetRequestedPermissionLevel4() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security>unknown</security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(ApplicationPermissionLevel.NONE, jnlpFile.getApplicationPermissionLevel());
    }
    
    @Test
    public void testGetRequestedPermissionLevel5() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+ ApplicationPermissionLevel.J2EE.getValue()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, codeBase, new ParserSettings(false, false, false));
        Assert.assertEquals(ApplicationPermissionLevel.J2EE, jnlpFile.getApplicationPermissionLevel());
    }

    @Test
    public void testGetSourceLocation() throws IOException, ParseException {
        ClassLoader cl = JNLPFileTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        URL jnlpURL = cl.getResource("net/sourceforge/jnlp/minimal.jnlp");
        JNLPFile jnlpFile = new JNLPFile(jnlpURL);
        // no href in JNLP tag: sourceLocation is null
        Assert.assertNull(jnlpFile.getSourceLocation());
    }
    @Test
    public void testGetSourceLocation2() throws IOException, ParseException {
        ClassLoader cl = JNLPFileTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String jnlpResourceName = "net/sourceforge/jnlp/minimalWithHref.jnlp";
        URL jnlpURL = cl.getResource(jnlpResourceName);
        JNLPFile jnlpFile = new JNLPFile(jnlpURL);
        Assert.assertNotNull(jnlpFile.getSourceLocation());
        Assert.assertTrue(jnlpFile.getSourceLocation().getFile().endsWith(jnlpResourceName));
    }
}
