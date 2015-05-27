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

import java.io.File;
import java.net.URL;
import net.sourceforge.jnlp.util.UrlUtils;

import org.junit.Assert;
import org.junit.Test;

public class CacheUtilTest {

    @Test
    public void testNormalizeUrlComparsions() throws Exception {
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
        //stuf behind querry is kept
        final File expected = new File("/tmp/https/example.com/applet/some_weird_applet..jar");
        Assert.assertEquals(expected, CacheUtil.urlToPath(u, "/tmp"));
    }
    
    
    @Test
    public void testUrlToPathWithQuery() throws Exception {
        final URL u = new URL("https://example.com/applet/applet.php?id=applet5");
        //querry is kept and sanitized
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
}
