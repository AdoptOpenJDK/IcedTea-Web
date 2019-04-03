/* ResourceTrackerTest.java
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
package net.sourceforge.jnlp.cache;

import static net.sourceforge.jnlp.cache.Resource.Status.CONNECTED;
import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADING;
import static net.sourceforge.jnlp.cache.Resource.Status.ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;

public class ResourceTrackerTest extends NoStdOutErrTest{

    public static ServerLauncher downloadServer;
    private static final PrintStream[] backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;


    private static Resource createResource(final String name) throws MalformedURLException {
        return Resource.getResource(new URL("http://example.com/" + name + ".jar"), new Version("1.0"), UpdatePolicy.ALWAYS);
    }

    @Test
    public void testSelectByStatusOneMatchingResource() throws Exception {
        Resource resource = createResource("oneMatchingResource");
        Assert.assertNotNull(resource);
        resource.setStatusFlag(DOWNLOADING);
        List<Resource> resources = Arrays.asList(resource);
        Resource result = ResourceTracker.selectByStatus(resources, DOWNLOADING, ERROR);
        Assert.assertEquals(resource, result);
    }

    @Test
    public void testSelectByStatusNoMatchingResource() throws Exception {
        Resource resource = createResource("noMatchingResource");
        Assert.assertNotNull(resource);
        List<Resource> resources = Arrays.asList(resource);
        Resource result = ResourceTracker.selectByStatus(resources, DOWNLOADING, ERROR);
        Assert.assertNull(result);
    }

    @Test
    public void testSelectByStatusExcludedResources() throws Exception {
        Resource resource = createResource("excludedResources");
        Assert.assertNotNull(resource);
        resource.setStatusFlag(ERROR);
        List<Resource> resources = Arrays.asList(resource);
        Resource result = ResourceTracker.selectByStatus(resources, DOWNLOADING, ERROR);
        Assert.assertNull(result);
    }

    @Test
    public void testSelectByStatusMixedResources() throws Exception {
        Resource r1 = createResource("mixedResources1");
        Assert.assertNotNull(r1);
        r1.setStatusFlag(CONNECTED);
        r1.setStatusFlag(DOWNLOADING);
        Resource r2 = createResource("mixedResources2");
        Assert.assertNotNull(r2);
        r2.setStatusFlag(CONNECTED);
        r2.setStatusFlag(DOWNLOADING);
        r2.setStatusFlag(ERROR);
        List<Resource> resources = Arrays.asList(r1, r2);
        Resource result = ResourceTracker.selectByStatus(resources, EnumSet.of(CONNECTED, DOWNLOADING), EnumSet.of(ERROR));
        Assert.assertEquals(r1, result);
    }

    @Test
    public void testSelectByFilterUninitialized() throws Exception {
        Resource resource = createResource("filterUninitialized");
        Assert.assertNotNull(resource);
        List<Resource> resources = Arrays.asList(resource);
        Resource result = ResourceTracker.selectByFilter(resources, new ResourceTracker.Filter<Resource>() {
            @Override
            public boolean test(Resource t) {
                return !t.isInitialized();
            }
        });
        Assert.assertEquals(resource, result);
    }

    @Test
    public void testNormalizeUrl() throws Exception {
        URL[] u = getUrls();

        URL[] n = getNormalizedUrls();

        Assert.assertNull("first url should be null", u[0]);
        Assert.assertNull("first normalized url should be null", n[0]);
        for (int i = 1; i < CHANGE_BORDER; i++) {
            Assert.assertTrue("url " + i + " must be equals too normalized url " + i, u[i].equals(n[i]));
        }
        for (int i = CHANGE_BORDER; i < n.length; i++) {
            Assert.assertFalse("url " + i + " must be normalized (and so not equals) too normalized url " + i, u[i].equals(n[i]));
        }
    }
    public static final int CHANGE_BORDER = 8;

    public static URL[] getUrls() throws MalformedURLException {
        URL[] u = {
            /*constant*/
            null,
            new URL("file:///home/jvanek/Desktop/icedtea-web/tests.build/jnlp_test_server/Spaces%20can%20be%20everywhere2.jnlp"),
            new URL("http://localhost:44321/SpacesCanBeEverywhere1.jnlp"),
            new URL("http:///SpacesCanBeEverywhere1.jnlp"),
            new URL("file://localhost/home/jvanek/Desktop/icedtea-web/tests.build/jnlp_test_server/Spaces can be everywhere2.jnlp"),
            new URL("http://localhost:44321/testpage.jnlp?applicationID=25"),
            new URL("http://localhost:44321/Spaces%20Can%20Be%20Everyw%2Fhere1.jnlp"),
            new URL("http://localhost/Spaces+Can+Be+Everywhere1.jnlp"),
            /*changing*/
            new URL("http://localhost/SpacesC anBeEverywhere1.jnlp?a=5&b=10#df"),
            new URL("http:///oook.jnlp?a=5&b=ahoj šš dd#df"),
            new URL("http://localhost/Spacesěčšžšřýžčřú can !@^*(){}[].jnlp?a=5&ahoj šš dd#df"),
            new URL("http://localhost:44321/SpaŠcesCan Be Everywhere1.jnlp"),
            new URL("http:/SpacesCanB eEverywhere1.jnlp")};
        return u;
    }

    public static URL[] getNormalizedUrls() throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        URL[] u = getUrls();

        URL[] n = new URL[u.length];
        for (int i = 0; i < n.length; i++) {
            n[i] = UrlUtils.normalizeUrl(u[i]);
        }
        return n;

    }

    @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() throws IOException {
        for (int i = 0; i < backedUpStream.length; i++) {
            if (backedUpStream[i] == null) {
                switch (i) {
                    case 0:
                        backedUpStream[i] = System.out;
                        break;
                    case 1:
                        backedUpStream[i] = System.err;
                        break;
                    case 2:
                        backedUpStream[i] = OutputController.getLogger().getOut();
                        break;
                    case 3:
                        backedUpStream[i] = OutputController.getLogger().getErr();
                        break;
                }

            }

        }
        currentErrorStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(currentErrorStream));
        System.setErr(new PrintStream(currentErrorStream));
        OutputController.getLogger().setOut(new PrintStream(currentErrorStream));
        OutputController.getLogger().setErr(new PrintStream(currentErrorStream));


    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString("utf-8"));
        System.setOut(backedUpStream[0]);
        System.setErr(backedUpStream[1]);
        OutputController.getLogger().setOut(backedUpStream[2]);
        OutputController.getLogger().setErr(backedUpStream[3]);
    }

    @BeforeClass
    public static void onDebug() {
        JNLPRuntime.setDebug(true);
    }

    @AfterClass
    public static void offDebug() {
        JNLPRuntime.setDebug(false);
    }

    private static String cacheDir;

    @BeforeClass
    public static void setupDownloadServer() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"), "itw-down");
        dir.mkdirs();
        dir.deleteOnExit();
        redirectErr();
        downloadServer = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), ServerAccess.findFreePort());
        redirectErrBack();

        cacheDir = PathsAndFiles.CACHE_DIR.getFullPath();
        PathsAndFiles.CACHE_DIR.setValue(System.getProperty("java.io.tmpdir") + File.separator + "tempcache");
    }

    @AfterClass
    public static void teardownDownloadServer() {
        downloadServer.stop();

        CacheUtil.clearCache();
        PathsAndFiles.CACHE_DIR.setValue(cacheDir);
    }

    @Test
    public void testDownloadResource() throws IOException {
        String s = "hello";
        File f = downloadServer.getDir();
        File temp = new File(f, "resource");
        temp.createNewFile();
        Files.write(temp.toPath(), s.getBytes());
        temp.deleteOnExit();

        URL url = downloadServer.getUrl("resource");

        ResourceTracker rt = new ResourceTracker();
        rt.addResource(url, null, null, UpdatePolicy.FORCE);
        File downloadFile = rt.getCacheFile(url);

        assertTrue(downloadFile.exists() && downloadFile.isFile());

        String output = new String(Files.readAllBytes(downloadFile.toPath()));
        assertEquals(s, output);
    }
}
