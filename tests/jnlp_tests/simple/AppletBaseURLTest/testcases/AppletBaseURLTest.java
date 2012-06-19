/* AppletBaseURLTest.java
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

import net.sourceforge.jnlp.ServerAccess.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

public class AppletBaseURLTest extends BrowserTest{

    private void evaluateApplet(ProcessResult pr, String baseName) {
        String s8 = "(?s).*Codebase is http://localhost:[0-9]{5}/ for this applet(?s).*";
        Assert.assertTrue("AppletBaseURL stdout should match" + s8 + " but didn't", pr.stdout.matches(s8));
        String s9 = "(?s).*Document base is http://localhost:[0-9]{5}/" + baseName + " for this applet(?s).*";
        Assert.assertTrue("AppletBaseURL stdout should match" + s9 + " but didn't", pr.stdout.matches(s9));
        String ss = "xception";
        Assert.assertFalse("AppletBaseURL stderr should not contain" + ss + " but did", pr.stderr.contains(ss));
    }

    @NeedsDisplay
    @Test
    public void AppletWebstartBaseURLTest() throws Exception {
        ProcessResult pr = server.executeJavaws(null, "/AppletBaseURLTest.jnlp");
        evaluateApplet(pr, "");
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Bug(id="PR855")
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void AppletInFirefoxTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/AppletBaseURLTest.html");
        pr.process.destroy();
        evaluateApplet(pr, "AppletBaseURLTest.html");
        Assert.assertTrue(pr.wasTerminated);
    }

    @Bug(id="PR855")
    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void AppletWithJNLPHrefTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/AppletJNLPHrefBaseURLTest.html");
        pr.process.destroy();
        evaluateApplet(pr, "AppletJNLPHrefBaseURLTest.html");
        Assert.assertTrue(pr.wasTerminated);
    }
}
