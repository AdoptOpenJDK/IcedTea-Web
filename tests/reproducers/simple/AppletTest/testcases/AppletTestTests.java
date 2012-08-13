/* AppletTestTests.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class AppletTestTests extends BrowserTest {

    @Test
    @TestInBrowsers(testIn = {Browsers.googleChrome})
    @NeedsDisplay
    public void doubleChrome() throws Exception {
        ServerAccess.PROCESS_TIMEOUT = 30 * 1000;
        try {
            //System.out.println("connecting AppletInFirefoxTest request in " + getBrowser().toString());
            //just verify loging is recording browser
            ProcessResult pr1 = server.executeBrowser("/appletAutoTests.html");
            if (pr1.process == null) {
                Assert.assertTrue("If proces was null here, then google-chrome had to not exist, and so "
                        + ServerAccess.UNSET_BROWSER
                        + " should be in exception, but exception was "
                        + pr1.deadlyException.getMessage(),
                        pr1.deadlyException.getMessage().contains(ServerAccess.UNSET_BROWSER));
                return;
            }
            evaluateApplet(pr1,false);
            Assert.assertTrue(pr1.wasTerminated);
            //System.out.println("connecting AppletInFirefoxTest request in " + getBrowser().toString());
            // just verify loging is recording browser
            ServerAccess.ProcessResult pr = server.executeBrowser("/appletAutoTests.html");
            evaluateApplet(pr,false);
            Assert.assertTrue(pr.wasTerminated);
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }

    @Test
    @NeedsDisplay
    public void AppletTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/AppletTest.jnlp");
        evaluateApplet(pr,true);
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    private void evaluateApplet(ProcessResult pr, boolean javawsApplet) {
        String s3 = "applet was initialised";
        Assert.assertTrue("AppletTest stdout should contains " + s3 + " bud didn't", pr.stdout.contains(s3));
        String s0 = "applet was started";
        Assert.assertTrue("AppletTest stdout should contains " + s0 + " bud didn't", pr.stdout.contains(s0));
        String s1 = "value1";
        Assert.assertTrue("AppletTest stdout should contains " + s1 + " bud didn't", pr.stdout.contains(s1));
        String s2 = "value2";
        Assert.assertTrue("AppletTest stdout should contains " + s2 + " bud didn't", pr.stdout.contains(s2));
        String ss = "xception";
        Assert.assertFalse("AppletTest stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
        String s7 = "Aplet killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTest stdout should contains " + s7 + " bud didn't", pr.stdout.contains(s7));
        if (!javawsApplet) {
            /*this is working correctly in most browser, but not in all. temporarily disabling
        String s4 = "applet was stopped";
        Assert.assertTrue("AppletTest stdout should contains " + s4 + " bud did't", pr.stdout.contains(s4));
        String s5 = "applet will be destroyed";
        Assert.assertTrue("AppletTest stdout should contains " + s5 + " bud did't", pr.stdout.contains(s5));
             */
        }
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.all})
    @NeedsDisplay
    public void AppletInBrowserTest() throws Exception {
        //System.out.println("connecting AppletInFirefoxTest request in " + getBrowser().toString());
        //just verify loging is recordingb rowser
        ServerAccess.PROCESS_TIMEOUT = 30 * 1000;
        try {
            ProcessResult pr = server.executeBrowser("/appletAutoTests2.html");
            evaluateApplet(pr,false);
            Assert.assertTrue(pr.wasTerminated);
            //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }

    @TestInBrowsers(testIn = {Browsers.all})
    @NeedsDisplay
    public void AppletInBrowserTestXslowX() throws Exception {
        //System.out.println("connecting AppletInFirefoxTest request in " + getBrowser().toString());
        //just verify loging is recording browser
        ServerAccess.PROCESS_TIMEOUT = 30 * 1000;
        try {
            ProcessResult pr = server.executeBrowser("/appletAutoTests.html");
            pr.process.destroy();
            evaluateApplet(pr,false);
            Assert.assertTrue(pr.wasTerminated);
            //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }
}
