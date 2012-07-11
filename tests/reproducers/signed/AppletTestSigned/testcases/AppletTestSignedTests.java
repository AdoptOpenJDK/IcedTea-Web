/* AppletTestSignedTests.java
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class AppletTestSignedTests extends BrowserTest {

    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall"}));

    @Test
    public void AppletTestSignedTest() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(l, "/AppletTestSigned.jnlp");
        evaluateSignedApplet(pr, true);
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    private void evaluateSignedApplet(ProcessResult pr, boolean javawsApplet) {
        String s3 = "AppletTestSigned was initialised";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s3 + " but didn't", pr.stdout.contains(s3));
        String s0 = "AppletTestSigned was started";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s0 + " but didn't", pr.stdout.contains(s0));
        String s1 = "value1";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s1 + " but didn't", pr.stdout.contains(s1));
        String s2 = "value2";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s2 + " but didn't", pr.stdout.contains(s2));
        String ss = "xception";
        Assert.assertFalse("AppletTestSigned stderr should not contain " + ss + " but did", pr.stderr.contains(ss));
        String s7 = "AppletTestSigned killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTestSigned stdout should contain " + s7 + " but didn't", pr.stdout.contains(s7));
        if (!javawsApplet) {
            /*this is working correctly in most browser, but not in all. temporarily disabling
            String s4 = "AppletTestSigned was stopped";
            Assert.assertTrue("AppletTestSigned stdout shouldt contain " + s4 + " but did", pr.stdout.contains(s4));
            String s5 = "AppletTestSigned will be destroyed";
            Assert.assertTrue("AppletTestSigned stdout shouldt contain " + s5 + " but did", pr.stdout.contains(s5));
             */
        }
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.all})
    public void AppletTestSignedFirefoxTest() throws Exception {
        ServerAccess.PROCESS_TIMEOUT = 30 * 1000;
        try {
            ProcessResult pr = server.executeBrowser("/AppletTestSigned.html");
            evaluateSignedApplet(pr, false);
            Assert.assertTrue(pr.wasTerminated);
            //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }
}
