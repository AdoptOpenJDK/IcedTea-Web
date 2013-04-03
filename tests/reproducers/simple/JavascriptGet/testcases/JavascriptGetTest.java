/* JToJSGetTest.java
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
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.CountingClosingListener;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.annotations.KnownToFail;
import org.junit.Assert;

import org.junit.Test;

public class JavascriptGetTest extends BrowserTest {

    public final boolean doNotRunInOpera = true;

    private final String initStr = "JToJSGet applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {

            return (s.contains(initStr) && s.contains(afterStr));

        }
    }

    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JToJSGetTest stdout should contain " + initStr
                + " but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values get from JavaScript are ok
        Assert.assertTrue("JToJSGet: the output should include: "+expectedStdout+", but it didnt.", pr.stdout.contains(expectedStdout));
    }

    private void javaToJSGetTest(String funcStr, String paramStr, String expectedVal) throws Exception {

        if( doNotRunInOpera){
            Browsers b = server.getCurrentBrowser().getID();
            if(b == Browsers.opera){
                return;
            }
        }

        String strURL = "/JavascriptGet.html?" + funcStr + ";" + paramStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedVal, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSGet_double_Test() throws Exception {
        javaToJSGetTest("jjsReadDouble", "1.1", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSGet_boolean_Test() throws Exception {
        javaToJSGetTest("jjsReadBoolean", "true", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSGet_string_Test() throws Exception {
        javaToJSGetTest("jjsReadString", "\"teststring\"", "teststring");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSGet_object_Test() throws Exception {
        javaToJSGetTest("jjsReadObject", "applet.getNewDummyObject(\"dummy1\")", "dummy1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail(failsIn={Browsers.midori, Browsers.epiphany, Browsers.googleChrome, Browsers.chromiumBrowser})
    public void AppletJToJSGet_1DArray_Test() throws Exception {
        javaToJSGetTest("jjsRead1DArray", "[1,2,3]", "[1, 2, 3]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail(failsIn={Browsers.midori, Browsers.epiphany, Browsers.googleChrome, Browsers.chromiumBrowser})
    public void AppletJToJSGet_2DArray_Test() throws Exception {
        javaToJSGetTest("jjsRead2DArray", "[[1,2],[3,4]]","[[1, 2], [3, 4]]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSGet_JSObject_Test() throws Exception {
        javaToJSGetTest("jjsReadJSObject", "window","Window]");//[object Window], [object DOMWindow]
    }

}
