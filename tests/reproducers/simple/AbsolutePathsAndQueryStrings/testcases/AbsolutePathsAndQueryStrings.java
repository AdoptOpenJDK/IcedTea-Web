/* AbsolutePathsAndQueryStrings.java
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

import java.io.File;
import java.net.URL;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;

public class AbsolutePathsAndQueryStrings extends BrowserTest {

    private static final String appletCloseString = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;

    @Bug(id="PR1204")
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testAbsolutePathAndQueryStringBrowser() throws Exception {
        /* HTML specifies absolute path and path params, ensure that this is able to launch correctly */
        ProcessResult pr = server.executeBrowser("/AbsolutePathsAndQueryStrings.html", AutoClose.CLOSE_ON_BOTH);
        Assert.assertTrue("stdout should contain " + appletCloseString + " but did not", pr.stdout.contains(appletCloseString));
    }

    @Bug(id="PR1204")
    @NeedsDisplay
    @Test
    public void testAbsolutePathAndQueryStringWebstart() throws Exception {
        /* JNLP specifies absolute path and path params, ensure that this is able to launch correctly */
        ProcessResult pr = server.executeJavawsHeadless("/AbsolutePathsAndQueryStrings.jnlp");
        Assert.assertTrue("stdout should contain \"running\"but did not", pr.stdout.contains("running"));
    }

    @Bug(id="PR1204")
    @Test
    public void testCaching() throws Exception {
        /* Test that caching ignores path parameters and double-slash issue from absolute codebase paths
         */
        URL plainLocation = new URL("http://localhost:1234/StripHttpPathParams.jar");
        URL paramLocation = new URL("http://localhost:1234/StripHttpPathParams.jar?i=abcd");
        URL absoluteLocation = new URL("http://localhost:1234//StripHttpPathParams.jar");
        URL absoluteParamLocation = new URL("http://localhost:1234//StripHttpPathParams.jar?i=abcd");

        DeploymentConfiguration config = JNLPRuntime.getConfiguration();
        config.load();
        String cacheLocation = config.getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR) + File.separator;
        File cacheDir = new File(cacheLocation);
        Assert.assertTrue(cacheDir.isDirectory());

        boolean hasCachedCopy = false;
        for (File cache : cacheDir.listFiles()) {
            File[] cacheFiles = new File[] {
                CacheUtil.urlToPath(plainLocation, cache.getPath()),
                CacheUtil.urlToPath(paramLocation, cache.getPath()),
                CacheUtil.urlToPath(absoluteLocation, cache.getPath()),
                CacheUtil.urlToPath(absoluteParamLocation, cache.getPath()),
            };
            for (File f : cacheFiles) {
                if (f.isFile())
                    hasCachedCopy = true;
                for (File g : cacheFiles) {
                    Assert.assertEquals(f.getPath(), g.getPath());
                }
            }
        }
        Assert.assertTrue(hasCachedCopy);
    }

}
