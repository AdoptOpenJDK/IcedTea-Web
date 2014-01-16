/* ClasspathManifestTest.java
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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

public class ClasspathManifestTest extends BrowserTest {

    private static String s1 = "Searching for CheckForClasspath.";
    private static String s2 = "CheckForClasspath found on classpath.";
    private static String ss = "xception";

    public void checkAppFails(ProcessResult pr, String testName) {
        Assert.assertTrue("ClasspathManifest." + testName + " stdout should contain " + s1 + " but didn't", pr.stdout.contains(s1));
        Assert.assertFalse("ClasspathManifest." + testName + " stdout should not contain " + s2 + " but did", pr.stdout.contains(s2));
        Assert.assertTrue("ClasspathManifest." + testName + " stderr should contain " + ss + " but didn't", pr.stderr.contains(ss));
    }

    @NeedsDisplay
    @Test
    public void ApplicationJNLPRemoteTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ClasspathManifestApplicationTest.jnlp");
        checkAppFails(pr, "ApplicationJNLPRemoteTest");
    }

    @NeedsDisplay
    @KnownToFail
    @Test
    public void ApplicationJNLPLocalTest() throws Exception {
        List<String> commands=new ArrayList<String>(3);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("ClasspathManifestApplicationTest.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        checkAppFails(pr, "ApplicationJNLPLocalTest");
    }

    @NeedsDisplay
    @Test
    public void AppletJNLPRemoteTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/ClasspathManifestAppletTest.jnlp");
        checkAppFails(pr, "AppletJNLPRemoteTest");
    }

    @NeedsDisplay
    @KnownToFail
    @Test
    public void AppletJNLPRLocalTest() throws Exception {
        List<String> commands=new ArrayList<String>(3);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("ClasspathManifestAppletTest.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        checkAppFails(pr, "AppletJNLPRLocalTest");
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserJNLPHrefRemoteTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ClasspathManifestJNLPHrefTest.html");
        checkAppFails(pr, "BrowserJNLPHrefRemoteTest");
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @KnownToFail
    @Test
    public void BrowserJNLPHrefLocalTest() throws Exception {
        List<String> commands=new ArrayList<String>(2);
        commands.add(server.getBrowserLocation());
        commands.add("ClasspathManifestJNLPHrefTest.html");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        checkAppFails(pr, "BrowserJNLPHrefLocalTest");
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserAppletRemoteTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/ClasspathManifestAppletTest.html");
        Assert.assertTrue("ClasspathManifest.BrowserAppletRemoteTest stdout should contain " + s1 + " but didn't", pr.stdout.contains(s1));
        // Should be the only one to search manifest for classpath.
        Assert.assertTrue("ClasspathManifest.BrowserAppletRemoteTest stdout should contain " + s2 + " but didn't", pr.stdout.contains(s2));
    }
}
