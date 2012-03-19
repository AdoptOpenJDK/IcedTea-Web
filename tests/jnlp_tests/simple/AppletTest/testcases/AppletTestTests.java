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

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.ProcessResult;
import org.junit.Assert;

import org.junit.Test;

public class AppletTestTests {

    private static ServerAccess server = new ServerAccess();

    @Test
    public void AppletTest() throws Exception {
        System.out.println("connecting AppletTest request");
        System.err.println("connecting AppletTest request");
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/AppletTest.jnlp");
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        evaluateApplet(pr);
        Assert.assertFalse(pr.wasTerminated);
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    private void evaluateApplet(ProcessResult pr) {
        String s3 = "applet was initialised";
        Assert.assertTrue("AppletTest stdout should contains " + s3 + " bud didn't", pr.stdout.contains(s3));
        String s0 = "applet was started";
        Assert.assertTrue("AppletTest stdout should contains " + s0 + " bud didn't", pr.stdout.contains(s0));
        String s1 = "value1";
        Assert.assertTrue("AppletTest stdout should contains " + s1 + " bud didn't", pr.stdout.contains(s1));
        String s2 = "value2";
        Assert.assertTrue("AppletTest stdout should contains " + s2 + " bud didn't", pr.stdout.contains(s2));
        String s4 = "applet was stopped";
        Assert.assertFalse("AppletTest stdout shouldn't contains " + s4 + " bud did", pr.stdout.contains(s4));
        String s5 = "applet will be destroyed";
        Assert.assertFalse("AppletTest stdout shouldn't contains " + s5 + " bud did", pr.stdout.contains(s5));
        String ss = "xception";
        Assert.assertFalse("AppletTest stderr should not contains " + ss + " but did", pr.stderr.contains(ss));
        String s7 = "Aplet killing himself after 2000 ms of life";
        Assert.assertTrue("AppletTest stdout should contains " + s7 + " bud didn't", pr.stdout.contains(s7));
    }

    @Test
    public void AppletInFirefoxTest() throws Exception {
        System.out.println("connecting AppletInFirefoxTest request");
        System.err.println("connecting AppletInFirefoxTest request");
        server.PROCESS_TIMEOUT = 30 * 1000;
        try {
            ServerAccess.ProcessResult pr = server.executeBrowser("/appletAutoTests.html");
            System.out.println(pr.stdout);
            System.err.println(pr.stderr);
            pr.process.destroy();
            evaluateApplet(pr);
            Assert.assertTrue(pr.wasTerminated);
            //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
        } finally {
            server.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }
}
