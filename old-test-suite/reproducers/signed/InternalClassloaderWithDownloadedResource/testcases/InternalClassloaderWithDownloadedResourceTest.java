/* InternalClassloaderWithDownloadedResourceTest.java
Copyright (C) 2011 Red Hat, Inc.

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Bug(id = {"RH816592","PR858"})
public class InternalClassloaderWithDownloadedResourceTest extends BrowserTest {

    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[]{"-verbose", "-Xtrustall", "-J-Dserveraccess.port=" + server.getPort()}));
    private static final File portsFile = new File(System.getProperty("java.io.tmpdir"), "serveraccess.port");

    @Before
    public void setUp() {
        try {
            ServerAccess.logOutputReprint("Writeing " + server.getPort() + " to " + portsFile.getAbsolutePath());
            ServerAccess.saveFile("" + server.getPort(), portsFile);
            ServerAccess.logOutputReprint("done");
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        }
    }

    @After
    public void tearDown() {
        ServerAccess.logOutputReprint("Deleting " + portsFile.getAbsolutePath());
        boolean b = portsFile.delete();
        ServerAccess.logOutputReprint("Deletion state (should be true) is " + b);
    }

    @Test
    @Bug(id = {"RH816592","PR858"})
    public void launchInternalClassloaderWithDownloadedResourceAsJnlpApplication() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/InternalClassloaderWithDownloadedResource-new.jnlp");
        evaluate(pr);
        Assert.assertFalse("should not be terminated but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    private void evaluate(ProcessResult pr) {
        String ss = "Good simple javaws exapmle";
        Assert.assertTrue("Stdout should  contains " + ss + " but didn't", pr.stdout.contains(ss));
    }

    @Test
    @Bug(id = {"RH816592","PR858"})
    public void launchInternalClassloaderWithDownloadedResourceAsJnlpApplet() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/InternalClassloaderWithDownloadedResource-applet-new.jnlp");
        evaluate(pr);
        Assert.assertFalse("should not be terminated but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    @Bug(id = {"RH816592","PR858"})
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.all})
    public void launchInternalClassloaderWithDownloadedResourceAsHtmlApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/InternalClassloaderWithDownloadedResource-new.html");
        evaluate(pr);
        Assert.assertTrue("should be terminated but was not", pr.wasTerminated);
    }

    @Test
    @Bug(id = {"http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-May/018737.html"})
    public void launchInternalClassloaderWithDownloadedResourceAsJnlpApplicationHack() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/InternalClassloaderWithDownloadedResource-hack.jnlp");
        evaluate(pr);
        Assert.assertFalse("should not be terminated but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    @Bug(id = {"http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-May/018737.html"})
    public void launchInternalClassloaderWithDownloadedResourceAsJnlpAppletHack() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/InternalClassloaderWithDownloadedResource-applet-hack.jnlp");
        evaluate(pr);
        Assert.assertFalse("should not be terminated but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    @NeedsDisplay
    @Bug(id = {"http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-May/018737.html"})
    @TestInBrowsers(testIn={Browsers.all})
    public void launchInternalClassloaderWithDownloadedResourceAsHtmlAppletHack() throws Exception {
        ProcessResult pr = server.executeBrowser("/InternalClassloaderWithDownloadedResource-hack.html");
        evaluate(pr);
        Assert.assertTrue("should be terminated but was not", pr.wasTerminated);
    }
}
