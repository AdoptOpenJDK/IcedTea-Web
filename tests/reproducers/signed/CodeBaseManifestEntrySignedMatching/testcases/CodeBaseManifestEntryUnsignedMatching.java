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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class CodeBaseManifestEntryUnsignedMatching extends BrowserTest {

    RulesFolowingClosingListener.ContainsRule aokr = new RulesFolowingClosingListener.ContainsRule(AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING);
    public static final String GENERAL_NAME = "CodeBaseManifestEntry";
    public static final String SIGNATURE = "UnsignedMatching";

    public void checkMessage(ProcessResult pr, int i) {
        CodeBaseManifestEntrySignedMatching.checkMessage(pr, i);
    }

    @NeedsDisplay
    @Test
    public void ApplicationJNLPRemoteTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, GENERAL_NAME + SIGNATURE + ".jnlp");
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 2);
    }

    @Test
    public void ApplicationJNLPLocalTest() throws Exception {
        List<String> commands = new ArrayList<String>(3);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add(GENERAL_NAME + SIGNATURE + ".jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 0);
    }

    private static void prepareCopyFile() throws IOException {
        CodeBaseManifestEntrySignedMatching.prepareCopyFile(GENERAL_NAME + SIGNATURE);
    }
    @Test
    public void ApplicationJNLPLocalTestWithRemoteCodebase() throws Exception {
        prepareCopyFile();
        List<String> commands = new ArrayList<String>(3);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add(GENERAL_NAME + SIGNATURE + "_copy.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 2);
    }

    @NeedsDisplay
    @Test
    public void AppletJNLPRemoteTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, GENERAL_NAME + SIGNATURE + "Applet.jnlp");
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 2);
    }

    @NeedsDisplay
    @Test
    public void AppletJNLPRLocalTest() throws Exception {
        List<String> commands = new ArrayList<String>(3);
        commands.add(server.getJavawsLocation());
        commands.add(ServerAccess.HEADLES_OPTION);
        commands.add(GENERAL_NAME + SIGNATURE + "Applet.jnlp");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir());
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 0);
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserJNLPHrefRemoteTest() throws Exception {
        ProcessResult pr = server.executeBrowser(GENERAL_NAME + SIGNATURE + "Jnlp.html", ServerAccess.AutoClose.CLOSE_ON_CORRECT_END);
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 2);
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserJNLPHrefLocalTest() throws Exception {
        List<String> commands = new ArrayList<String>(2);
        commands.add(server.getBrowserLocation());
        commands.add(GENERAL_NAME + SIGNATURE + "Jnlp.html");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir(), new AutoOkClosingListener(), null);
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 0);
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserAppletLocalTest() throws Exception {
        List<String> commands = new ArrayList<String>(2);
        commands.add(server.getBrowserLocation());
        commands.add(GENERAL_NAME + SIGNATURE + ".html");
        ProcessResult pr = ServerAccess.executeProcess(commands, server.getDir(), new AutoOkClosingListener(), null);
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 0);
    }

    @NeedsDisplay
    @TestInBrowsers(testIn = {Browsers.one})
    @Test
    public void BrowserAppletRemoteTest() throws Exception {
        ProcessResult pr = server.executeBrowser(GENERAL_NAME + SIGNATURE + ".html", ServerAccess.AutoClose.CLOSE_ON_CORRECT_END);
        Assert.assertTrue(aokr.toPassingString(), aokr.evaluate(pr.stdout));
        checkMessage(pr, 2);
    }
}
