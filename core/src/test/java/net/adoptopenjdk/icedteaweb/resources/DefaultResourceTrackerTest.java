/* ResourceTrackerTest.java
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
package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import net.adoptopenjdk.icedteaweb.testing.ServerLauncher;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.StdInOutErrController;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultResourceTrackerTest extends NoStdOutErrTest {

    public static ServerLauncher downloadServer;
    private static ByteArrayOutputStream currentErrorStream;


    private static Resource createResource(final String name) throws MalformedURLException {
        return Resource.createResource(new URL("http://example.com/" + name + ".jar"), VersionString.fromString("1.0"), null, UpdatePolicy.ALWAYS);
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
    public static void redirectErr() {
        currentErrorStream = new ByteArrayOutputStream();
        OutputController.getLogger().setInOutErrController(new StdInOutErrController(currentErrorStream, currentErrorStream));
    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString(UTF_8.name()));
        OutputController.getLogger().setInOutErrController(StdInOutErrController.getInstance());
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
        File dir = new File(JavaSystemProperties.getJavaTempDir(), "itw-down");
        dir.mkdirs();
        dir.deleteOnExit();
        redirectErr();
        downloadServer = ServerAccess.getIndependentInstance(dir.getAbsolutePath(), ServerAccess.findFreePort());
        redirectErrBack();

        cacheDir = PathsAndFiles.CACHE_DIR.getFullPath();
        PathsAndFiles.CACHE_DIR.setValue(JavaSystemProperties.getJavaTempDir() + File.separator + "tempcache");
    }

    @AfterClass
    public static void teardownDownloadServer() {
        downloadServer.stop();

        Cache.clearCache();
        PathsAndFiles.CACHE_DIR.setValue(cacheDir);
    }

    @Test
    @Ignore
    public void testDownloadResource() throws IOException {
        String s = "hello";
        File f = downloadServer.getDir();
        File temp = new File(f, "resource");
        temp.createNewFile();
        Files.write(temp.toPath(), s.getBytes());
        temp.deleteOnExit();

        URL url = downloadServer.getUrl("resource");

        ResourceTracker rt = new DefaultResourceTracker();
        rt.addResource(url, (VersionString) null, UpdatePolicy.FORCE);
        File downloadFile = rt.getCacheFile(url);

        assertTrue(downloadFile.exists() && downloadFile.isFile());

        String output = new String(Files.readAllBytes(downloadFile.toPath()));
        assertEquals(s, output);
    }
}
