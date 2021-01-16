/* JNLPFileTest.java
   Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment;
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

public class JNLPFileTest extends NoStdOutErrTest{
    Locale jvmLocale = new Locale("en", "CA", "utf8");
    MockJNLPFile file = new MockJNLPFile(jvmLocale);
    JNLPFileFactory jnlpFileFactory = new JNLPFileFactory();

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

        JNLPFile jnlpFile = new JNLPFile(is, null, null, new ParserSettings(false,false,false), null);

        Assert.assertEquals("http://icedtea.classpath.org/", jnlpFile.getCodeBase().toExternalForm());
        Assert.assertEquals("redhat.embeddedjnlp", jnlpFile.getApplet().getMainClass());
        Assert.assertEquals("Sample Test", jnlpFile.getTitle());
        Assert.assertEquals(2, jnlpFile.getResources().getJARs().length);
    }

    @Bug(id={"PR1533"})
    @Test
    public void testDownloadOptionsAppliedEverywhere() throws MalformedURLException, ParseException {
        String os = JavaSystemProperties.getOsName();
        String arch = JavaSystemProperties.getOsArch();

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
                "  <resources>" +
                "    <java version='1+'/>" +
                "  </resources>\n" +
                "  <installer-desc/>\n" +
                "</jnlp>";

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false,false,false), null);
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
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false,false,false), null);
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
    public void testGetRequestedEnvironment1() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "");
        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false, false, false), null);
        Assert.assertEquals(ApplicationEnvironment.SANDBOX, jnlpFile.getApplicationEnvironment());
    }

    @Test
    public void testGetRequestedEnvironment2() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+ ApplicationEnvironment.ALL.getValue()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false, false, false), null);
        Assert.assertEquals(ApplicationEnvironment.ALL, jnlpFile.getApplicationEnvironment());
    }

    @Test
    public void testGetRequestedEnvironment3() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false, false, false), null);
        Assert.assertEquals(ApplicationEnvironment.SANDBOX, jnlpFile.getApplicationEnvironment());
    }

    @Test
    public void testGetRequestedEnvironment4() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security>unknown</security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false, false, false), null);
        Assert.assertEquals(ApplicationEnvironment.SANDBOX, jnlpFile.getApplicationEnvironment());
    }
    
    @Test
    public void testGetRequestedEnvironment5() throws MalformedURLException, ParseException {
        String jnlpContents = minimalJnlp.replace("SECURITY", "<security><"+ ApplicationEnvironment.J2EE.getValue()+"/></security>");

        URL codeBase = new URL("http://icedtea.classpath.org");
        InputStream is = new ByteArrayInputStream(jnlpContents.getBytes());
        JNLPFile jnlpFile = new JNLPFile(is, null, codeBase, new ParserSettings(false, false, false), null);
        Assert.assertEquals(ApplicationEnvironment.J2EE, jnlpFile.getApplicationEnvironment());
    }

    @Test
    public void testGetSourceLocation() throws IOException, ParseException {
        ClassLoader cl = JNLPFileTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String jnlpResourceName = "net/sourceforge/jnlp/minimal.jnlp";
        URL jnlpURL = cl.getResource(jnlpResourceName);
        JNLPFile jnlpFile = jnlpFileFactory.create(jnlpURL);
        Assert.assertNotNull(jnlpFile.getSourceLocation());
        Assert.assertTrue(jnlpFile.getSourceLocation().getFile().endsWith(jnlpResourceName));
    }
    @Test
    public void testGetSourceLocation2() throws IOException, ParseException {
        ClassLoader cl = JNLPFileTest.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String jnlpResourceName = "net/sourceforge/jnlp/minimalWithHref.jnlp";
        URL jnlpURL = cl.getResource(jnlpResourceName);
        JNLPFile jnlpFile = jnlpFileFactory.create(jnlpURL);
        Assert.assertNotNull(jnlpFile.getSourceLocation());
        Assert.assertTrue(jnlpFile.getSourceLocation().getFile().endsWith(jnlpResourceName));
    }
}
