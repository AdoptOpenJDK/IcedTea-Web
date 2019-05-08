/* 
 Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributes;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.adoptopenjdk.icedteaweb.manifest.ManifestBoolean;
import net.adoptopenjdk.icedteaweb.testing.mock.DummyJNLPFileWithJar;
import net.adoptopenjdk.icedteaweb.testing.util.FileTestUtils;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JNLPFileTest extends NoStdOutErrTest {

    private static AppletSecurityLevel level;
    private static List<ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK> attCheckValue;

    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        attCheckValue = ManifestAttributesChecker.getAttributesCheck();
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, String.valueOf(ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL));
    }

    @AfterClass
    public static void resetPermissions() {
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, level.toChars());
        JNLPRuntime.getConfiguration().setProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, String.valueOf(attCheckValue));
    }


    @Test
    public void newSecurityAttributesTestNotSet() throws Exception {
        //order is tested in removeTitle
        //here we go with pure loading and parsing of them
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();
        File jarLocation66 = new File(tempDirectory, "test66.jar");
        File jarLocation77 = new File(tempDirectory, "test77.jar");
        Manifest manifest77 = new Manifest();

        FileTestUtils.createJarWithContents(jarLocation66); //no manifest
        FileTestUtils.createJarWithContents(jarLocation77, manifest77);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(0, jarLocation66, jarLocation77); //jar 6 should be main
        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);//jnlp file got its instance in classloaders constructor
        //jnlpFile.getManifestsAttributes().setLoader(classLoader); //classloader set, but no att specified

        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getApplicationName());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getPermissions());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString())));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString())));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.ENTRY_POINT.toString())));

        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getMainClass());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getApplicationName());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestAttributesReader().getCodebase());
        Assert.assertEquals("no classloader attached, should be null", ManifestBoolean.UNDEFINED, jnlpFile.getManifestAttributesReader().isTrustedLibrary());
        Assert.assertEquals("no classloader attached, should be null", ManifestBoolean.UNDEFINED, jnlpFile.getManifestAttributesReader().isTrustedOnly());
    }

    @Test
    public void newSecurityAttributesTest() throws Exception {
        //order is tested in removeTitle
        //here we go with pure loading and parsing of them
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();
        File jarLocation6 = new File(tempDirectory, "test6.jar");
        File jarLocation7 = new File(tempDirectory, "test7.jar");
        Manifest manifest6 = new Manifest();
        manifest6.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass1"); //see DummyJNLPFileWithJar constructor with int
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString()), "DummyClass1 title");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.ENTRY_POINT.toString()), "main1 main2");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString()), "*.com  https://*.cz");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString()), "*.net  ftp://*uu.co.uk");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.CODEBASE.toString()), "*.com *.net *.cz *.co.uk");
        /*
         *  "sandbox" or "all-permissions"
         */
        /* TODO: Commented lines with "sandbox" permissions specified are causing failures after
         * PR1769 ("Permissions: sandbox" manifest attribute) patch is applied. The problem
         * appears to be that the JarCertVerifier thinks that DummyJNLPFileWithJars are
         * signed (jcv.isFullySigned() falls into the isTriviallySigned() case) even though
         * they are completely unsigned. This *may* be only be an issue with DummyJNLPFiles.
         */
        // manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS), "sandbox"); /* commented due to DummyJNLP being "signed" */
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.PERMISSIONS.toString()), "all-permissions");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString()), "false");
        manifest6.getMainAttributes().put(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString()), "false");

        Manifest manifest7 = new Manifest(); //6 must e main
        manifest7.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass2");
        /*
         *  "sandbox" or "all-permissions"
         */
        manifest7.getMainAttributes().put(new Attributes.Name(ManifestAttributes.PERMISSIONS.toString()), "erroneous one");
        manifest7.getMainAttributes().put(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString()), "erroneous one");
        manifest7.getMainAttributes().put(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString()), "erroneous one");

        FileTestUtils.createJarWithContents(jarLocation6, manifest6);
        FileTestUtils.createJarWithContents(jarLocation7, manifest7);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(1, jarLocation7, jarLocation6); //jar 6 should be main. Jar 7 have wrong items, but they are never loaded as in main jar are the correct one
        final DummyJNLPFileWithJar errorJnlpFile = new DummyJNLPFileWithJar(0, jarLocation7); //jar 7 should be main
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getApplicationName());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.ENTRY_POINT.toString())));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString())));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString())));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name((ManifestAttributes.CODEBASE.toString()))));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.PERMISSIONS.toString())));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString())));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString())));

        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getApplicationName());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getCodebase());
        Assert.assertEquals("no classloader attached, should be null", ManifestBoolean.UNDEFINED, jnlpFile.getManifestAttributesReader().isTrustedLibrary());
        Assert.assertEquals("no classloader attached, should be null", ManifestBoolean.UNDEFINED, jnlpFile.getManifestAttributesReader().isTrustedOnly());

        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS); //jnlp file got its instance in classloaders constructor
        //jnlpFile.getManifestsAttributes().setLoader(classLoader);

        Exception ex = null;
        try {
           final JNLPClassLoader errorClassLoader = new JNLPClassLoader(errorJnlpFile, UpdatePolicy.ALWAYS);//jnlp file got its instance in classloaders constructor
           //errorJnlpFile.getManifestsAttributes().setLoader(errorClassLoader);
        } catch (Exception e){
            //correct exception
            ex = e;
        }
        Assert.assertNotNull(ex);

        Assert.assertEquals("DummyClass1 title", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString())));
        Assert.assertEquals("main1 main2", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.ENTRY_POINT.toString())));
        Assert.assertEquals("*.com  https://*.cz", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString())));
        Assert.assertEquals("*.net  ftp://*uu.co.uk", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString())));
        Assert.assertEquals("*.com *.net *.cz *.co.uk", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CODEBASE.toString())));
        // Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString(), jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS))); /* commented due to DummyJNLP being "signed" */
        Assert.assertEquals(AppletPermissionLevel.ALL.getValue(), jnlpFile.getManifestAttributesReader().getPermissions());
        Assert.assertEquals("false", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString())));
        Assert.assertEquals("false", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString())));


        Assert.assertNull(errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString())));
        Assert.assertNull(errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.ENTRY_POINT.toString())));
        Assert.assertNull(errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString())));
        Assert.assertNull(errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString())));
        Assert.assertNull(errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CODEBASE.toString())));
        Assert.assertEquals("erroneous one", errorJnlpFile.getManifestAttributesReader().getPermissions());
        Assert.assertEquals("erroneous one", errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_LIBRARY.toString())));
        Assert.assertEquals("erroneous one", errorJnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.TRUSTED_ONLY.toString())));

        Assert.assertEquals("DummyClass1 title", jnlpFile.getManifestAttributesReader().getApplicationName());
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase().matches(new URL("http://aa.com")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase().matches(new URL("https://aa.cz")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase().matches(new URL("https://aa.com")));
        Assert.assertEquals(false, jnlpFile.getManifestAttributesReader().getApplicationLibraryAllowableCodebase().matches(new URL("http://aa.cz")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase().matches(new URL("http://aa.net")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase().matches(new URL("ftp://aa.uu.co.uk")));
        Assert.assertEquals(false, jnlpFile.getManifestAttributesReader().getCallerAllowableCodebase().matches(new URL("http://aa.uu.co.uk")));
        Assert.assertEquals("*.com *.net *.cz *.co.uk", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.CODEBASE.toString())));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getCodebase().matches(new URL("http://aa.com")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getCodebase().matches(new URL("ftp://aa.bb.net")));
        Assert.assertEquals(true, jnlpFile.getManifestAttributesReader().getCodebase().matches(new URL("https://x.net")));
        Assert.assertEquals(false, jnlpFile.getManifestAttributesReader().getCodebase().matches(new URL("http://aa.bb/com")));
        // Assert.assertEquals(JNLPFile.ManifestBoolean.TRUE, jnlpFile.getManifestsAttributes().isSandboxForced()); /* commented due to DummyJNLP being "signed" */
        Assert.assertEquals(ManifestBoolean.FALSE, jnlpFile.getManifestAttributesReader().isTrustedLibrary());
        Assert.assertEquals(ManifestBoolean.FALSE, jnlpFile.getManifestAttributesReader().isTrustedOnly());

        ex = null;
        try {
            Assert.assertEquals("erroneous one", errorJnlpFile.getManifestAttributesReader().isTrustedLibrary());
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            Assert.assertEquals("erroneous one", errorJnlpFile.getManifestAttributesReader().isTrustedOnly());
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);


    }
   
    @Test
    @Ignore
    public void removeTitle() throws Exception {
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();
        File jarLocation1 = new File(tempDirectory, "test1.jar");
        File jarLocation2 = new File(tempDirectory, "test2.jar");
        File jarLocation3 = new File(tempDirectory, "test3.jar");
        File jarLocation4 = new File(tempDirectory, "test4.jar");
        File jarLocation5 = new File(tempDirectory, "test5.jar");

        /* Test with various attributes in manifest!s! */
        Manifest manifest1 = new Manifest();
        manifest1.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass1"); //two times, but one in main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest2 = new Manifest();
        manifest2.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh1"); //two times, both in not main jar, see DummyJNLPFileWithJar constructor with int

        Manifest manifest3 = new Manifest();
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "it"); //just once in not main jar, see DummyJNLPFileWithJar constructor with int
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh2");

        Manifest manifest4 = new Manifest();
        manifest4.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass2"); //see jnlpFile.setMainJar(3);
        manifest4.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url2"); //see DummyJNLPFileWithJar constructor with int

        //first jar
        Manifest manifest5 = new Manifest();
        manifest5.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url1"); //see DummyJNLPFileWithJar constructor with int
        manifest5.getMainAttributes().put(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString()), "Manifested Name");


        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);
        FileTestUtils.createJarWithContents(jarLocation4, manifest4);
        FileTestUtils.createJarWithContents(jarLocation5, manifest5);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(3, jarLocation5, jarLocation3, jarLocation4, jarLocation1, jarLocation2); //jar 1 should be main
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getMainClass());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString())));

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertNull(jnlpFile.getTitle());

        setTitle(jnlpFile);

        Assert.assertEquals("jnlp title", jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertEquals("jnlp title", jnlpFile.getTitle());

        removeTitle(jnlpFile);

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertNull(jnlpFile.getTitleFromManifest());
        Assert.assertNull(jnlpFile.getTitle());

        final JNLPClassLoader classLoader = new JNLPClassLoader(jnlpFile, UpdatePolicy.ALWAYS);//jnlp file got its instance in classloaders constructor
        //jnlpFile.getManifestsAttributes().setLoader(classLoader);
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestAttributesReader().getMainClass());
        Assert.assertNull("defined twice, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("not defined, should be null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString())));
        //correct values are also tested in JnlpClassloaderTest
        Assert.assertEquals("classloader attached, should be not null", "it", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertEquals("classloader attached, should be not null", "DummyClass1", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertEquals("classloader attached, should be not null", "some url1", jnlpFile.getManifestAttributesReader().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertEquals("classloader attached, should be not null", "Manifested Name", jnlpFile.getManifestAttributesReader().getAttribute(new Attributes.Name(ManifestAttributes.APPLICATION_NAME.toString())));

        Assert.assertNull(jnlpFile.getTitleFromJnlp());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitleFromManifest());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitle());

        setTitle(jnlpFile);

        Assert.assertEquals("jnlp title", jnlpFile.getTitleFromJnlp());
        Assert.assertEquals("Manifested Name", jnlpFile.getTitleFromManifest());
        Assert.assertEquals("jnlp title (Manifested Name)", jnlpFile.getTitle());

    }

    private void setTitle(final DummyJNLPFileWithJar jnlpFile) {
        setTitle(jnlpFile, "jnlp title");
    }

    private void setTitle(final DummyJNLPFileWithJar jnlpFile, final String title) {
        jnlpFile.setInfo(Arrays.asList(new InformationDesc[]{
                    new InformationDesc(new Locale[]{}, false) {
                        @Override
                        public String getTitle() {
                            return title;
                        }
                    }
                }));
    }

    private void removeTitle(final DummyJNLPFileWithJar jnlpFile) {
        jnlpFile.setInfo(Arrays.asList(new InformationDesc[]{}));
    }
}
