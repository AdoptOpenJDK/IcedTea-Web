/* CheckServicesTests.java
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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

@Bug(id="http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2012-February/017153.html")
public class CheckServicesTests extends BrowserTest{

    public void evaluateApplet(ProcessResult pr, boolean applet) {
        String s0 = "Codebase for applet was found in constructor";
        Assert.assertTrue("CheckServices stdout should contain `" + s0 + "' but didn't.", pr.stdout.contains(s0));
        String s1 = "Codebase for applet was found in init()";
        Assert.assertTrue("CheckServices stdout should contain `" + s1 + "' but didn't.", pr.stdout.contains(s1));
        String s2 = "Codebase for applet was found in start()";
        Assert.assertTrue("CheckServices stdout should contain `" + s2 + "' but didn't.", pr.stdout.contains(s2));
        if (applet){
            /*this is working correctly in most browser, but not in all. temporarily disabling
        String s3 = "Codebase for applet was found in stop()";
        Assert.assertTrue("CheckServices stdout should contain `" + s3 + "' but didn't.", pr.stdout.contains(s3));
        String s4 = "Codebase for applet was found in destroy()";
        Assert.assertTrue("CheckServices stdout should contain `" + s4 + "' but didn't.", pr.stdout.contains(s4));
             */
        }
        String s5 = "Exception occurred with null codebase in";
        Assert.assertFalse("CheckServices stderr should not contain `" + s5 + "' but did.", pr.stdout.contains(s5));
        String s6 = "Applet killing itself after 2000 ms of life";
        Assert.assertTrue("CheckServices stdout should contain `" + s6 + "' but didn't.", pr.stdout.contains(s6));
    }

    @Test
    @NeedsDisplay
    public void CheckWebstartServices() throws Exception {
        ProcessResult pr = server.executeJavaws(null, "/CheckServices.jnlp");
        evaluateApplet(pr, false);
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer)0, pr.returnValue);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn={Browsers.one})
    public void CheckPluginJNLPHServices() throws Exception {
        ProcessResult pr = server.executeBrowser(null, "/CheckPluginServices.html");
        evaluateApplet(pr,false);
        Assert.assertTrue(pr.wasTerminated);
    }
}
