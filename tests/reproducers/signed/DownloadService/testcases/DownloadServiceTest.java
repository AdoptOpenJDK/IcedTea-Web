/* DownloadServiceTest.java
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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DownloadServiceTest {
    private static ServerAccess server = new ServerAccess();
    private final String exitString = "Exiting DownloadService..";
    private static List<String> checkCache = new ArrayList<String>();
    private static List<String> manageJnlpResources = new ArrayList<String>();
    private static List<String> manageExternalResources = new ArrayList<String>();

    @BeforeClass
    public static void initalizeClass() throws MalformedURLException {
        //Check Cache
        checkCache.add(server.getJavawsLocation());
        checkCache.add("-arg");
        checkCache.add(server.getUrl().toString() + "/");
        checkCache.add("-arg");
        checkCache.add("checkCache");
        checkCache.add("-Xtrustall");
        checkCache.add(ServerAccess.HEADLES_OPTION);
        checkCache.add(server.getUrl() + "/DownloadService.jnlp");

        //Manage Jnlp Resouces
        manageJnlpResources.add(server.getJavawsLocation());
        manageJnlpResources.add("-arg");
        manageJnlpResources.add(server.getUrl().toString() + "/");
        manageJnlpResources.add("-arg");
        manageJnlpResources.add("manageJnlpJars");
        manageJnlpResources.add("-Xtrustall");
        manageJnlpResources.add(ServerAccess.HEADLES_OPTION);
        manageJnlpResources.add(server.getUrl() + "/DownloadService.jnlp");

        //Manage External Resources
        manageExternalResources.add(server.getJavawsLocation());
        manageExternalResources.add("-arg");
        manageExternalResources.add(server.getUrl().toString() + "/");
        manageExternalResources.add("-arg");
        manageExternalResources.add("manageExternalJars");
        manageExternalResources.add("-Xtrustall");
        manageExternalResources.add(ServerAccess.HEADLES_OPTION);
        manageExternalResources.add(server.getUrl() + "/DownloadService.jnlp");
    }

    /**
     * Executes reproducer to checks if DownloadServices's cache checks are working correctly.
     * @return stdout of reproducer.
     */
    private String runCacheCheckTests() throws Exception {
        //Check cache test
        ProcessResult processResult = ServerAccess.executeProcess(checkCache);
        String stdoutCheckCache = processResult.stdout;
        Assert.assertTrue("CheckCache - DownloadServiceRunner instance did not close as expected, this test may fail.",
                stdoutCheckCache.contains(exitString));

        return stdoutCheckCache;
    }

    /**
     * Executes reproducer to checks if DownloadServices's management of external jars are working correctly.
     * @return stdout of reproducer.
     */
    private String runExternalTests() throws Exception {
        ProcessResult processResult = ServerAccess.executeProcess(manageExternalResources);
        String stdoutExternalResources = processResult.stdout;
        Assert.assertTrue("ManageExternalResources - DownloadServiceRunner instance did not close as expected, this test may fail.",
                stdoutExternalResources.contains(exitString));

        return stdoutExternalResources;
    }

    /**
     * Executes reproducer to checks if DownloadServices's management of jnlp jars are working correctly.
     * @return stdout of reproducer.
     */
    private String runJnlpResourceTests() throws Exception {
        ProcessResult processResult = ServerAccess.executeProcess(manageJnlpResources);
        String stdoutJnlpResources = processResult.stdout;
        Assert.assertTrue("ManageJnlpResources - DownloadServiceRunner instance did not close as expected, this test may fail.",
                stdoutJnlpResources.contains(exitString));

        return stdoutJnlpResources;
    }

    @Test
    public void checkIfRequiredResourcesExist() {
        //Jnlp files
        Assert.assertTrue("DownloadService.jnlp is a required resource that's missing.",
                new File(server.getDir().getAbsolutePath() + "/DownloadService.jnlp").isFile());
        Assert.assertTrue("DownloadServiceExtension.jnlp is a required resource that's missing.", new File(server.getDir().getAbsolutePath()
                + "/DownloadServiceExtension.jnlp").isFile());

        //Jar files
        Assert.assertTrue("DownloadService.jar is a required resource that's missing.",
                new File(server.getDir().getAbsolutePath() + "/DownloadService.jar").isFile());
        Assert.assertTrue("SignedJnlpResource.jar is a required resource that's missing.", new File(server.getDir().getAbsolutePath()
                + "/SignedJnlpResource.jar").isFile());
        Assert.assertTrue("SignedJarResource.jar is a required resource that's missing.",
                new File(server.getDir().getAbsolutePath() + "/SignedJarResource.jar").isFile());
        Assert.assertTrue("MultiJar-NoSignedJnlp.jar is a required resource that's missing.", new File(server.getDir().getAbsolutePath()
                + "/MultiJar-NoSignedJnlp.jar").isFile());
    }

    @Test
    public void testcheckCaches() throws Exception {
        String stdoutCheckCache = runCacheCheckTests();

        //Stdout validations
        String s = "CHECKCACHE-isPartCached: LaunchPartOne: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKCACHE-isPartCached: LaunchPartTwo: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKCACHE-isPartCached: NonExistingPart: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));
    }

    @Test
    public void testcheckCachesUsingArray() throws Exception {
        String stdoutCheckCache = runCacheCheckTests();

        //Stdout validations
        String s = "CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): ValidLaunchParts: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): HalfValidLaunchParts: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKCACHEUSINGMUTIPLEPARTS-isPartCached(Array): InvalidParts: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));
    }

    @Test
    public void testExtensioncheckCaches() throws Exception {
        String stdoutCheckCache = runCacheCheckTests();

        //Stdout validations
        String s = "CHECKEXTENSIONCACHE-isExtensionPartCached: ExtensionPartOne: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKEXTENSIONCACHE-isExtensionPartCached: NonExistingPart: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKEXTENSIONCACHE-isExtensionPartCached: NonExistingUrl: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));
    }

    @Test
    public void testExtensioncheckCachesUsingArray() throws Exception {
        String stdoutCheckCache = runCacheCheckTests();

        //Stdout validations
        String s = "CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): ValidExtensionParts: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): HalfValidExtensionParts: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

        s = "CHECKEXTENSIONCACHEUSINGMUTIPLEPARTS-isExtensionPartCached(Array): InvalidParts: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutCheckCache.contains(s));

    }

    @Test
    public void testExternalResourceChecks() throws Exception {
        runCacheCheckTests();
        String stdoutExternalResources = runExternalTests();

        //Stdout validations
        //This is automatically cached from the test engine because the .jar exists
        String s = "CHECKEXTERNALCACHE-isResourceCached: UrlToExternalResource: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

        s = "CHECKEXTERNALCACHE-isResourceCached: NonExistingUrl: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));
    }

    @Test
    public void testRemovePart() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        String s = "REMOVEPART-removePart: LaunchPartOne-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
        s = "REMOVEPART-removePart: LaunchPartOne-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "REMOVEPART-removePart: LaunchPartTwo-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
        s = "REMOVEPART-removePart: LaunchPartTwo-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testRemoveExtensionPart() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        String s = "REMOVEEXTENSIONPART-removeExtensionPart: ExtensionPartOne-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "REMOVEEXTENSIONPART-removeExtensionPart: ExtensionPartOne-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testRemoveExtensionPartUsingArray() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        String s = "REMOVEEXTENSIONUSINGVALIDPARTINARRAY-removeExtensionPart(Array): ValidExtensionParts-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "REMOVEEXTENSIONUSINGVALIDPARTINARRAY-removeExtensionPart(Array): ValidExtensionParts-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "REMOVEEXTENSIONUSINGHALFVALIDPARTINARRAY-removeExtensionPart(Array): HalfValidExtensionParts-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "REMOVEEXTENSIONUSINGHALFVALIDPARTINARRAY-removeExtensionPart(Array): HalfValidExtensionParts-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testRemoveExternalResource() throws Exception {
        runCacheCheckTests();
        String stdoutExternalResources = runExternalTests();

        //Stdout validations
        String s = "REMOVEEXTERNALPART-removeResource: UrlToExternalResource-BEFORE: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

        s = "REMOVEEXTERNALPART-removeResource: UrlToExternalResource-AFTER: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

    }

    @Test
    public void testLoadPart() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        //Part 'one'
        String s = "LOADPART-loadPart: LaunchPartOne-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
        s = "LOADPART-loadPart: LaunchPartOne-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        //Part 'two'
        s = "LOADPART-loadPart: LaunchPartTwo-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
        s = "LOADPART-loadPart: LaunchPartTwo-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testLoadExtensionPart() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        String s = "LOADEXTENSIONPART-loadExtensionPart: ExtensionPartOne-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "LOADEXTENSIONPART-loadExtensionPart: ExtensionPartOne-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testLoadExtensionPartUsingArray() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        String s = "LOADEXTENSIONUSINGVALIDPARTINARRAY-loadExtensionPart(Array): ValidExtensionParts-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "LOADEXTENSIONUSINGVALIDPARTINARRAY-loadExtensionPart(Array): ValidExtensionParts-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "LOADEXTENSIONUSINGHALFVALIDPARTINARRAY-loadExtensionPart(Array): HalfValidExtensionParts-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "LOADEXTENSIONUSINGHALFVALIDPARTINARRAY-loadExtensionPart(Array): HalfValidExtensionParts-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));
    }

    @Test
    public void testLoadExternalResource() throws Exception {
        runCacheCheckTests();
        String stdoutExternalResources = runExternalTests();

        //Stdout validations
        String s = "LOADEXTERNALRESOURCE-loadResource: UrlToExternalResource-BEFORE: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

        s = "LOADEXTERNALRESOURCE-loadResource: UrlToExternalResource-AFTER: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

    }

    @Test
    public void testRepeatedlyLoadingAndUnloadingJnlpResources() throws Exception {
        runCacheCheckTests();
        String stdoutJnlpResources = runJnlpResourceTests();

        //Stdout validations
        String s = "MULTIPLEMETHODCALLS - removePart: LaunchPartOne: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

        s = "MULTIPLEMETHODCALLS - loadPart: LaunchPartOne: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutJnlpResources.contains(s));

    }

    @Test
    public void testRepeatedlyLoadingAndUnloadingExternalResources() throws Exception {
        runCacheCheckTests();
        String stdoutExternalResources = runExternalTests();

        //Stdout validations
        String s = "MULTIPLEMETHODCALLS - removeResource: UrlToExternalResource: false";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));

        s = "MULTIPLEMETHODCALLS - loadResource: UrlToExternalResource: true";
        Assert.assertTrue("stdout should contain \"" + s + "\" but did not.", stdoutExternalResources.contains(s));
    }
}
