/* 
Copyright (C) 2014 Red Hat, Inc.

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
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.closinglisteners.CountingClosingListener;

import org.junit.Assert;
import org.junit.Test;

public class JToJSStringTest extends BrowserTest {
    // the JS<->J tests tend to make Opera unusable
    private static final boolean doNotRunInOpera = false;

    private static final String initStr = "init";
    private static final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {
        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private static void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        Assert.assertTrue("JSToJSet: the stdout should contain " + initStr + ", but it didnt.", pr.stdout.contains(initStr));
        Assert.assertTrue("JSToJSet: the output should include: " + expectedStdout + ", but it didnt.", pr.stdout.contains(expectedStdout));
    }

    private void javaToJSTest(String url, String expectedStdout) throws Exception {
        if (doNotRunInOpera) {
            if (server.getCurrentBrowser().getID() == Browsers.opera) {
                return;
            }
            ProcessResult pr = server.executeBrowser(url, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
            evaluateStdoutContents(expectedStdout, pr);
        }
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1794" })
    public void StringArrayTest() throws Exception {
        javaToJSTest("/JToJSString.html", "abc");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void StringTest() throws Exception {
        javaToJSTest("/JToJSString.html", "notarrayitem");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1794" })
    public void jnlpStringArrayAppletTest() throws Exception {
        javaToJSTest("/JToJSStringJnlpApplet.html", "abc");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1794" })
    public void jnlpStringAppletTest() throws Exception {
        javaToJSTest("/JToJSStringJnlpApplet.html", "notarrayitem");
    }
}
