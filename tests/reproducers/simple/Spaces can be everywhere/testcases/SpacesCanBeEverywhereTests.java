/* SpacesCanBeEverywhereTests.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import org.junit.Assert;
import org.junit.Test;

@Bug(id={"http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2011-October/016127.html","PR804","PR811"})
public class SpacesCanBeEverywhereTests extends BrowserTest {

public static final String s = "Spaces can be everywhere.jsr was launched correctly";
    @Bug(id="PR811")
    @Test
    @NeedsDisplay
    public void SpacesCanBeEverywhereLocalAppletTestsJnlp2() throws Exception {
        List<String> commands=new ArrayList<String>(1);
        commands.add(server.getJavawsLocation());
        commands.add(server.getDir()+"/NotOnly spaces can kill ěščřž too.jnlp");
        /*                                         Change of dir is cousing the Exception bellow
         * ServerAccess.ProcessResult pr =  ServerAccess.executeProcess(commands,server.getDir());
         * No X11 DISPLAY variable was set, but this program performed an operation which requires it.
         * at java.awt.GraphicsEnvironment.checkHeadless(GraphicsEnvironment.java:173)
         * at java.awt.Window.<init>(Window.java:476)
         * at java.awt.Frame.<init>(Frame.java:419)
         * at java.awt.Frame.<init>(Frame.java:384)
         * at javax.swing.SwingUtilities$SharedOwnerFrame.<init>(SwingUtilities.java:1754)
         * at javax.swing.SwingUtilities.getSharedOwnerFrame(SwingUtilities.java:1831)
         * at javax.swing.JWindow.<init>(JWindow.java:185)
         * at javax.swing.JWindow.<init>(JWindow.java:137)
         * at net.sourceforge.jnlp.runtime.JNLPSecurityManager.<init>(JNLPSecurityManager.java:121)
         * at net.sourceforge.jnlp.runtime.JNLPRuntime.initialize(JNLPRuntime.java:202)
         * at net.sourceforge.jnlp.runtime.Boot.run(Boot.java:177)
         * at net.sourceforge.jnlp.runtime.Boot.run(Boot.java:51)
         * at java.security.AccessController.doPrivileged(Native Method)
         * at net.sourceforge.jnlp.runtime.Boot.main(Boot.java:168)
         *
         * Thats why there is absolute path to the file.
         *
         * This is also why SpacesCanBeEverywhereLocalTests1Signed is passing -
         * it is in headless mode. This can be considered as bug, but because it is
         * only on ocal files, and probably only from test run - it can be ignored
         */
        ProcessResult pr =  ServerAccess.executeProcess(commands);
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    @NeedsDisplay
    public void SpacesCanBeEverywhereRemoteAppletTestsJnlp2() throws Exception {
        ProcessResult pr = server.executeJavaws("/NotOnly%20spaces%20can%20kill%20%C4%9B%C5%A1%C4%8D%C5%99%C5%BE%20too.jnlp");
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        Assert.assertFalse("should NOT be terminated, but was", pr.wasTerminated);
    }

    @Bug(id="PR811")
    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.all})
    public void SpacesCanBeEverywhereRemoteAppletTestsHtml2() throws Exception {
        ProcessResult pr = server.executeBrowser("/spaces+applet+Tests.html", Arrays.asList(new ContentReaderListener[] {new StringBasedClosingListener(s)}), null);
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        Assert.assertTrue("should be terminated, but was not", pr.wasTerminated);
    }


    @Bug(id={"PR811","http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2011-October/016144.html"})
    @Test
    public void SpacesCanBeEverywhereRemoteTests1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere1.jnlp");
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere2.jnlp");
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests2_withQuery1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere2.jnlp?test=10");
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }


    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests2_withQuery2() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/Spaces%20can%20be%20everywhere2.jnlp?test%3D10");
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR811")
    @Test
    public void SpacesCanBeEverywhereRemoteTests3() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/SpacesCanBeEverywhere1.jnlp");
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }


    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests1() throws Exception {
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("Spaces can be everywhere1.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("stdout should contains `" + s + "`, but did not", pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests2() throws Exception {
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("Spaces can be everywhere2.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests4() throws Exception {
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add(server.getDir()+"/Spaces can be everywhere2.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands);
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR804")
    @Test
    public void SpacesCanBeEverywhereLocalTests3() throws Exception {
        List<String> commands=new ArrayList<String>(4);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add("SpacesCanBeEverywhere1.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands,server.getDir());
        Assert.assertTrue("stdout should contains `"+s+"`, but did not",pr.stdout.contains(s));
        String cc = "ClassNotFoundException";
        Assert.assertFalse("stderr should NOT contains `" + cc + "`, but did", pr.stderr.contains(cc));
        Assert.assertFalse("should not be terminated, but was", pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }
}
