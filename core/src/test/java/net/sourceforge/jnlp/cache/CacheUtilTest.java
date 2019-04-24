/* CacheUtilTest.java
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

import net.adoptopenjdk.icedteaweb.testing.annotations.Bug;
import net.sourceforge.jnlp.util.UrlUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class CacheUtilTest {

    @Test
    public void testNormalizeUrlComparisons() throws Exception {
        URL[] u = ResourceTrackerTest.getUrls();
        URL[] n = ResourceTrackerTest.getNormalizedUrls();
        for (int i = 0; i < u.length; i++) {
            Assert.assertTrue("url " + i + " must CacheUtil.urlEquals to its normalized form " + i, UrlUtils.urlEquals(u[i], n[i]));
            Assert.assertTrue("normalized form " + i + " must CacheUtil.urlEquals to its original " + i, UrlUtils.urlEquals(n[i], u[i]));
        }
    }

    @Test
    public void testUrlToPath() throws Exception {
        final URL u = new URL("https://example.com/applet/some:weird*applet?.jar");
        //stuff behind query is kept
        final File expected = new File("/tmp/https/example.com/applet/some_weird_applet..jar");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }

    @Test
    @Bug(id = "1190")
    public void testUrlToPathWithPort() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/some:weird*applet?.jar");
        //stuff behind query is kept
        final File expected = new File("/tmp/https/example.com/5050/applet/some_weird_applet..jar");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }

    @Test
    @Bug(id = "3227")
    public void testUrlToPathLonger256() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/uspto-auth.authenticate.jnlp.q_SlNFU1NJT05JRD02OUY1ODVCNkJBOTM1NThCQjdBMTA5RkQyNDZEQjEwRi5wcm9kX3RwdG9tY2F0MjE1X2p2bTsgRW50cnVzdFRydWVQYXNzUmVkaXJlY3RVcmw9Imh0dHBzOi8vZWZzLnVzcHRvLmdvdi9FRlNXZWJVSVJlZ2lzdGVyZWQvRUZTV2ViUmVnaXN0ZXJlZCI7IFRDUFJPRFBQQUlSc2Vzc2lvbj02MjIxMjk0MTguMjA0ODAuMDAwMA__.info");
        final File expected = new File("/tmp/https/example.com/5050/applet/a2ac35576c36d0304c86eb9e645a251ff69dba28646e13f2e81dbb9cc96097f.info");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }
    
    @Test
    @Bug(id = "3227")
    public void testUrlToPathLonger256NoSuffix() throws Exception {
        final URL u = new URL("https://example.com:5050/applet/uspto-auth.authenticate.jnlp.q_SlNFU1NJT05JRD02OUY1ODVCNkJBOTM1NThCQjdBMTA5RkQyNDZEQjEwRi5wcm9kX3RwdG9tY2F0MjE1X2p2bTsgRW50cnVzdFRydWVQYXNzUmVkaXJlY3RVcmw9Imh0dHBzOi8vZWZzLnVzcHRvLmdvdi9FRlNXZWJVSVJlZ2lzdGVyZWQvRUZTV2ViUmVnaXN0ZXJlZCI7IFRDUFJPRFBQQUlSc2Vzc2lvbj02MjIxMjk0MTguMjA0ODAuMDAwMA");
        final File expected = new File("/tmp/https/example.com/5050/applet/e4f3cf11f86f5aa33f424bc3efe3df7a9d20837a6f1a5bbbc60c1f57f3780a4");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }
    
    
    @Test
    public void testUrlToPathWithQuery() throws Exception {
        final URL u = new URL("https://example.com/applet/applet.php?id=applet5");
        //query is kept and sanitized
        final File expected = new File("/tmp/https/example.com/applet/applet.php.id_applet5");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }
    @Test
    public void testUrlToPathWithoutQuery() throws Exception {
        final URL u = new URL("https://example.com/applet/applet.php");
        //no doubledot is caused by patch adding query to file
        final File expected = new File("/tmp/https/example.com/applet/applet.php");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }
    
    @Test
    public void CacheID(){
        CacheUtil.CacheId cj11 = new CacheUtil.CacheJnlpId("a");
        CacheUtil.CacheId cj12 = new CacheUtil.CacheJnlpId("a");
        CacheUtil.CacheId cj2 = new CacheUtil.CacheJnlpId("b");
        CacheUtil.CacheId cj31 = new CacheUtil.CacheJnlpId(null);
        CacheUtil.CacheId cj32 = new CacheUtil.CacheJnlpId(null);
        CacheUtil.CacheId cd11 = new CacheUtil.CacheDomainId("a");
        CacheUtil.CacheId cd12 = new CacheUtil.CacheDomainId("a");
        CacheUtil.CacheId cd2 = new CacheUtil.CacheDomainId("b");
        CacheUtil.CacheId cd31 = new CacheUtil.CacheDomainId(null);
        CacheUtil.CacheId cd32 = new CacheUtil.CacheDomainId(null);
        
        Assert.assertEquals(cj11, cj11);
        Assert.assertEquals(cj11, cj12);
        Assert.assertEquals(cd11, cd11);
        Assert.assertEquals(cd11, cd12);
        Assert.assertEquals(cj31, cj31);
        Assert.assertEquals(cj31, cj32);
        Assert.assertEquals(cd31, cd31);
        Assert.assertEquals(cd31, cd32);
        
        Assert.assertNotEquals(cj11, cj2);
        Assert.assertNotEquals(cj11, cj31);
        Assert.assertNotEquals(cd11, cd2);
        Assert.assertNotEquals(cd11, cd31);
        
        Assert.assertNotEquals(cj11, cd11);
        Assert.assertNotEquals(cj2, cd2);
        Assert.assertNotEquals(cj31, cd31);
        Assert.assertNotEquals(cj32, cd32);
    }
}
