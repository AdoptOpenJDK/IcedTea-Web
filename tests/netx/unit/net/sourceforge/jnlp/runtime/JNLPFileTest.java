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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.mock.DummyJNLPFileWithJar;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.sourceforge.jnlp.util.FileTestUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JNLPFileTest extends NoStdOutErrTest {

    private static AppletSecurityLevel level;
    private static List<ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK> attCheckValue;

    @BeforeClass
    public static void setPermissions() {
        level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        attCheckValue = ManifestAttributesChecker.getAttributesCheck();
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.toChars());
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, String.valueOf(ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL));
    }

    @AfterClass
    public static void resetPermissions() {
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL, level.toChars());
        JNLPRuntime.getConfiguration().setProperty(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, String.valueOf(attCheckValue));
    }


    @Test
    public void newSecurityAttributesTestNotSet() throws Exception {
        //oreder is tested in removeTitle
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

        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_LIBRARY_ALLOWABLE)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CALLER_ALLOWABLE)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY)));
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.ENTRY_POINT)));

        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getMainClass());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getApplicationName());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getCallerAllowableCodebase());
        Assert.assertNull("classloader attached, but should be null", jnlpFile.getManifestsAttributes().getCodebase());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isSandboxForced());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isTrustedLibrary());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isTrustedOnly());
    }

    @Test
    public void newSecurityAttributesTest() throws Exception {
        //oreder is tested in removeTitle
        //here we go with pure loading and aprsing of them
        File tempDirectory = FileTestUtils.createTempDirectory();
        tempDirectory.deleteOnExit();
        File jarLocation6 = new File(tempDirectory, "test6.jar");
        File jarLocation7 = new File(tempDirectory, "test7.jar");
        Manifest manifest6 = new Manifest();
        manifest6.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass1"); //see DummyJNLPFileWithJar constructor with int
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME), "DummyClass1 title");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.ENTRY_POINT), "main1 main2");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_LIBRARY_ALLOWABLE), "*.com  https://*.cz");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.CALLER_ALLOWABLE), "*.net  ftp://*uu.co.uk");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE), "*.com *.net *.cz *.co.uk");
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
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS), "all-permissions");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY), "false");
        manifest6.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY), "false");

        Manifest manifest7 = new Manifest(); //6 must e main
        manifest7.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass2");
        /*
         *  "sandbox" or "all-permissions"
         */
        manifest7.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS), "erroronous one");
        manifest7.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY), "erroronous one");
        manifest7.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY), "erroronous one");

        FileTestUtils.createJarWithContents(jarLocation6, manifest6);
        FileTestUtils.createJarWithContents(jarLocation7, manifest7);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(1, jarLocation7, jarLocation6); //jar 6 should be main. Jar 7 have wrong items, but they are never laoded as in main jar are the correct one
        final DummyJNLPFileWithJar errorJnlpFile = new DummyJNLPFileWithJar(0, jarLocation7); //jar 7 should be main
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.ENTRY_POINT)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_LIBRARY_ALLOWABLE)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CALLER_ALLOWABLE)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY)));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY)));

        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getApplicationName());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getCallerAllowableCodebase());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getCodebase());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isSandboxForced());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isTrustedLibrary());
        Assert.assertEquals("no classloader attached, should be null", JNLPFile.ManifestBoolean.UNDEFINED, jnlpFile.getManifestsAttributes().isTrustedOnly());

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

        Assert.assertEquals("DummyClass1 title", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));
        Assert.assertEquals("main1 main2", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.ENTRY_POINT)));
        Assert.assertEquals("*.com  https://*.cz", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_LIBRARY_ALLOWABLE)));
        Assert.assertEquals("*.net  ftp://*uu.co.uk", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CALLER_ALLOWABLE)));
        Assert.assertEquals("*.com *.net *.cz *.co.uk", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE)));
        // Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString(), jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS))); /* commented due to DummyJNLP being "signed" */
        Assert.assertEquals(SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString(), jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS)));
        Assert.assertEquals("false", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY)));
        Assert.assertEquals("false", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY)));


        Assert.assertNull(errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));
        Assert.assertNull(errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.ENTRY_POINT)));
        Assert.assertNull(errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_LIBRARY_ALLOWABLE)));
        Assert.assertNull(errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CALLER_ALLOWABLE)));
        Assert.assertNull(errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE)));
        Assert.assertEquals("erroronous one", errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.PERMISSIONS)));
        Assert.assertEquals("erroronous one", errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_LIBRARY)));
        Assert.assertEquals("erroronous one", errorJnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.TRUSTED_ONLY)));

        Assert.assertEquals("DummyClass1 title", jnlpFile.getManifestsAttributes().getApplicationName());
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase().matches(new URL("http://aa.com")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase().matches(new URL("https://aa.cz")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase().matches(new URL("https://aa.com")));
        Assert.assertEquals(false, jnlpFile.getManifestsAttributes().getApplicationLibraryAllowableCodebase().matches(new URL("http://aa.cz")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getCallerAllowableCodebase().matches(new URL("http://aa.net")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getCallerAllowableCodebase().matches(new URL("ftp://aa.uu.co.uk")));
        Assert.assertEquals(false, jnlpFile.getManifestsAttributes().getCallerAllowableCodebase().matches(new URL("http://aa.uu.co.uk")));
        Assert.assertEquals("*.com *.net *.cz *.co.uk", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.CODEBASE)));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getCodebase().matches(new URL("http://aa.com")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getCodebase().matches(new URL("ftp://aa.bb.net")));
        Assert.assertEquals(true, jnlpFile.getManifestsAttributes().getCodebase().matches(new URL("https://x.net")));
        Assert.assertEquals(false, jnlpFile.getManifestsAttributes().getCodebase().matches(new URL("http://aa.bb/com")));
        // Assert.assertEquals(JNLPFile.ManifestBoolean.TRUE, jnlpFile.getManifestsAttributes().isSandboxForced()); /* commented due to DummyJNLP being "signed" */
        Assert.assertEquals(JNLPFile.ManifestBoolean.FALSE, jnlpFile.getManifestsAttributes().isSandboxForced());
        Assert.assertEquals(JNLPFile.ManifestBoolean.FALSE, jnlpFile.getManifestsAttributes().isTrustedLibrary());
        Assert.assertEquals(JNLPFile.ManifestBoolean.FALSE, jnlpFile.getManifestsAttributes().isTrustedOnly());

        ex = null;
        try {
            errorJnlpFile.getManifestsAttributes().isSandboxForced();
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            Assert.assertEquals("erroronous one", errorJnlpFile.getManifestsAttributes().isTrustedLibrary());
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
        try {
            Assert.assertEquals("erroronous one", errorJnlpFile.getManifestsAttributes().isTrustedOnly());
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);


    }
   
    @Test
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
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "it"); //jsut once in not main jar, see DummyJNLPFileWithJar constructor with int
        manifest3.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "rh2");

        Manifest manifest4 = new Manifest();
        manifest4.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "DummyClass2"); //see jnlpFile.setMainJar(3);
        manifest4.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url2"); //see DummyJNLPFileWithJar constructor with int

        //first jar
        Manifest manifest5 = new Manifest();
        manifest5.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_URL, "some url1"); //see DummyJNLPFileWithJar constructor with int
        manifest5.getMainAttributes().put(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME), "Manifested Name");


        FileTestUtils.createJarWithContents(jarLocation1, manifest1);
        FileTestUtils.createJarWithContents(jarLocation2, manifest2);
        FileTestUtils.createJarWithContents(jarLocation3, manifest3);
        FileTestUtils.createJarWithContents(jarLocation4, manifest4);
        FileTestUtils.createJarWithContents(jarLocation5, manifest5);

        final DummyJNLPFileWithJar jnlpFile = new DummyJNLPFileWithJar(3, jarLocation5, jarLocation3, jarLocation4, jarLocation1, jarLocation2); //jar 1 should be main
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getMainClass());
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNull("no classloader attached, should be null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));

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
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestsAttributes().getMainClass());
        Assert.assertNull("defined twice, shoud be null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertNull("not deffined, should benull", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR_ID));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertNotNull("classloader attached, should be not null", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));
        //correct values are also tested in JnlpClassloaderTest
        Assert.assertEquals("classloader attached, should be not null", "it", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_TITLE));
        Assert.assertEquals("classloader attached, should be not null", "DummyClass1", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.MAIN_CLASS));
        Assert.assertEquals("classloader attached, should be not null", "some url1", jnlpFile.getManifestsAttributes().getAttribute(Attributes.Name.IMPLEMENTATION_URL));
        Assert.assertEquals("classloader attached, should be not null", "Manifested Name", jnlpFile.getManifestsAttributes().getAttribute(new Attributes.Name(JNLPFile.ManifestsAttributes.APP_NAME)));

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
