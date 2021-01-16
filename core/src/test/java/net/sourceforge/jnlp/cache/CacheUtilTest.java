/* CacheUtilTest.java
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
package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.resources.DefaultResourceTrackerTest;
import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.sourceforge.jnlp.util.UrlUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;

public class CacheUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File cacheDir;

    @Before
    public final void setUp() throws Exception {
        cacheDir = temporaryFolder.newFolder();
    }

    @Test
    public void testNormalizeUrlComparisons() throws Exception {
        URL[] u = DefaultResourceTrackerTest.getUrls();
        URL[] n = DefaultResourceTrackerTest.getNormalizedUrls();
        for (int i = 0; i < u.length; i++) {
            Assert.assertTrue("url " + i + " must CacheUtil.urlEquals to its normalized form " + i, UrlUtils.urlEquals(u[i], n[i]));
            Assert.assertTrue("normalized form " + i + " must CacheUtil.urlEquals to its original " + i, UrlUtils.urlEquals(n[i], u[i]));
        }
    }

    @Test
    public void testUrlToPath() throws Exception {
        final URL u = new URL("https://example.com/applet/some:weird*applet?.jar");
        //stuff behind query is kept
        final File expected = new File(cacheDir, "https/example.com/443/applet/some_weird_applet..jar");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }

    @Test
    @Bug(id = "1190")
    public void testUrlToPathWithPort() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/some:weird*applet?.jar");
        //stuff behind query is kept
        final File expected = new File(cacheDir, "https/example.com/5050/applet/some_weird_applet..jar");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }

    @Test
    @Bug(id = "3227")
    public void testUrlToPathLonger256() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/uspto-auth.authenticate.jnlp.q_SlNFU1NJT05JRD02OUY1ODVCNkJBOTM1NThCQjdBMTA5RkQyNDZEQjEwRi5wcm9kX3RwdG9tY2F0MjE1X2p2bTsgRW50cnVzdFRydWVQYXNzUmVkaXJlY3RVcmw9Imh0dHBzOi8vZWZzLnVzcHRvLmdvdi9FRlNXZWJVSVJlZ2lzdGVyZWQvRUZTV2ViUmVnaXN0ZXJlZCI7IFRDUFJPRFBQQUlSc2Vzc2lvbj02MjIxMjk0MTguMjA0ODAuMDAwMA__.info");
        final File expected = new File(cacheDir, "https/example.com/5050/applet/a2ac35576c36d0304c86eb9e645a251ff69dba28646e13f2e81dbb9cc96097f.info");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }

    @Test
    @Bug(id = "3227")
    public void testUrlToPathLonger256NoSuffix() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/uspto-auth.authenticate.jnlp.q_SlNFU1NJT05JRD02OUY1ODVCNkJBOTM1NThCQjdBMTA5RkQyNDZEQjEwRi5wcm9kX3RwdG9tY2F0MjE1X2p2bTsgRW50cnVzdFRydWVQYXNzUmVkaXJlY3RVcmw9Imh0dHBzOi8vZWZzLnVzcHRvLmdvdi9FRlNXZWJVSVJlZ2lzdGVyZWQvRUZTV2ViUmVnaXN0ZXJlZCI7IFRDUFJPRFBQQUlSc2Vzc2lvbj02MjIxMjk0MTguMjA0ODAuMDAwMA");
        final File expected = new File(cacheDir, "https/example.com/5050/applet/e4f3cf11f86f5aa33f424bc3efe3df7a9d20837a6f1a5bbbc60c1f57f3780a4");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }

    @Test
    public void testPathUpNoGoBasic() throws Exception {
        final URL u = new URL("https://example.com/applet/../my.jar");
        final File expected = new File(cacheDir, "https/example.com/443/abca4723622ed60db3dea12cbe2402622a74f7a49b73e23b55988e4eee5ded.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testPathUpNoGoBasicLong() throws Exception {
        final URL u = new URL("https://example.com/applet/../my.jar.q_SlNFU1NJT05JRD02OUY1ODVCNkJBOTM1NThCQjdBMTA5RkQyNDZEQjEwRi5wcm9kX3RwdG9tY2F0MjE1X2p2bTsgRW50cnVzdFRydWVQYXNzUmVkaXJlY3RVcmw9Imh0dHBzOi8vZWZzLnVzcHRvLmdvdi9FRlNXZWJVSVJlZ2lzdGVyZWQvRUZTV2ViUmVnaXN0ZXJlZCI7IFRDUFJPRFBQQUlSc2Vzc2lvbj02MjIxMjk0MTguMjA0ODAuMDAwMA\"");
        final File expected = new File(cacheDir, "https/example.com/443/ec97413e3f6eee8215ecc8375478cc1ae5f44f18241b9375361d5dfcd7b0ec");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testPathUpNoGoBasic2() throws Exception {
        final URL u = new URL("https://example.com/../my.jar");
        final File expected = new File(cacheDir, "https/example.com/443/eb1a56bed34523dbe7ad84d893ebc31a8bbbba9ce3f370e42741b6a5f067c140.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testPathUpNoGoBasicEvil() throws Exception {
        final URL u = new URL("https://example.com/../../my.jar");
        final File expected = new File(cacheDir, "https/example.com/443/db464f11d68af73e37eefaef674517b6be23f0e4a5738aaee774ecf5b58f1bfc.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testPathUpNoGoBasicEvil2() throws Exception {
        final URL u = new URL("https://example.com:99/../../../my.jar");
        final File expected = new File(cacheDir, "https/example.com/99/95401524c345e0d554d4d77330e86c98a77b9bb58a0f93094204df446b356.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testPathUpNoGoBasicEvilest() throws Exception {
        final URL u = new URL("https://example2.com/something/../../../../../../../../../../../my.jar");
        final File expected = new File(cacheDir, "https/example2.com/443/a8df64388f5b84d5f635e4d6dea5f4d2f692ae5381f8ec6736825ff8d6ff2c0.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testQueryGotHAshedToo() throws Exception {
        final URL u = new URL("https://example2.com/something/my.jar?../../harm");
        final File expected = new File(cacheDir, "https/example2.com/443/2844b3c690ea355159ed61de6e727f2e9169ab55bf58b8fa3f4b64f6a25bd7.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testNameGotHashedToo() throws Exception {
        final URL u = new URL("https://example2.com/something/..my.jar");
        final File expected = new File(cacheDir, "https/example2.com/443/c0c13e25fbe26876938eecd15227ab6299f9a4b8c11746e5451fb88cb775cb0.jar");
        File r = CacheUtil.urlToPath(u, cacheDir.getAbsolutePath());
        Assert.assertEquals(expected, r);
    }

    @Test
    public void testUrlToPathWithQuery() throws Exception {
        final URL u = new URL("https://example.com/applet/applet.php?id=applet5");
        //query is kept and sanitized
        final File expected = new File(cacheDir, "https/example.com/443/applet/applet.php.id_applet5");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }

    @Test
    public void testUrlToPathWithoutQuery() throws Exception {
        final URL u = new URL("https://example.com/applet/applet.php");
        //no doubledot is caused by patch adding query to file
        final File expected = new File(cacheDir, "https/example.com/443/applet/applet.php");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, cacheDir.getAbsolutePath()));
    }
}
