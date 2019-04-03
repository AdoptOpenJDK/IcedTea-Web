/* JSToJGetTest.java
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

public class JSToJGetTest extends BrowserTest {

    // the JS<->J tests tend to make Opera unusable
    public final boolean doNotRunInOpera = false;

    public String passStr = " - passed.";
    public String failValStr = " - failed, value mismatch.";
    public String failTypeStr = " - failed, type mismatch.";
    public String expStr = "expected:[";
    public String foundStr = "] found:[";
    public String endStr = "].";

    private final String initStr = "JSToJGet applet initialized.";
    private final String setupStr = "JSToJGet applet set up for GET tests.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(setupStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String testStr, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJGetTest stdout should contain " + initStr + " but it didnt.", pr.stdout.contains(initStr));

        // Assert that the applet was set up for the GM tests.
        Assert.assertTrue("JSToJGetTest stdout should contain " + setupStr + " but it didnt.", pr.stdout.contains(setupStr));

        // Assert that the tests have passed.
        String s0 = testStr + passStr;
        String s1 = testStr + failValStr;
        String s2 = testStr + failTypeStr;

        int ind0 = pr.stdout.indexOf(s0);
        int ind1 = pr.stdout.indexOf(s1);
        int ind2 = pr.stdout.indexOf(s2);
        int indBegin = pr.stdout.indexOf(setupStr);
        if (indBegin != -1) {
            indBegin += setupStr.length();
        } else {
            indBegin = 0;
        }

        String failStr = "JSToJGet " + testStr + ": passed not found in the applet stdout.";

        if (ind1 != -1) {
            // int inde = pr.stdout.indexOf(expStr);
            // int indf = pr.stdout.indexOf(foundStr);
            // int indend = pr.stdout.indexOf(endStr);
            failStr = "JSToJGet: value mismatch in " + testStr;
        }

        if (ind2 != -1) {
            // int inde = pr.stdout.indexOf(expStr);
            // int indf = pr.stdout.indexOf(foundStr);
            // int indend = pr.stdout.indexOf(endStr);
            failStr = "JSToJGet: type mismatch in " + testStr;
        }

        Assert.assertTrue(failStr, (ind1 == -1));// no value mismatch
        Assert.assertTrue(failStr, (ind2 == -1));// no type mismatch
        Assert.assertTrue(failStr, (ind0 != -1));// test passed

    }

    private void jsToJavaGetTest(String urlEnd, String testStr) throws Exception {

        if (doNotRunInOpera) {
            if (server.getCurrentBrowser().getID() == Browsers.opera) {
                return;
            }
        }

        String strURL = "/JSToJGet.html?" + urlEnd;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(testStr, pr);

    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_int_Test() throws Exception {
        jsToJavaGetTest("int", "Test no. 1 - (int)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_double_Test() throws Exception {
        jsToJavaGetTest("double", "Test no. 2 - (double)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_float_Test() throws Exception {
        jsToJavaGetTest("float", "Test no. 3 - (float)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_long_Test() throws Exception {
        jsToJavaGetTest("long", "Test no. 4 - (long)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_boolean_Test() throws Exception {
        jsToJavaGetTest("boolean", "Test no. 5 - (boolean)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_char_Test() throws Exception {
        jsToJavaGetTest("char", "Test no. 6 - (char)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_byte_Test() throws Exception {
        jsToJavaGetTest("byte", "Test no. 7 - (byte)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_intArrayElement_Test() throws Exception {
        jsToJavaGetTest("intArrayElement", "Test no. 8 - (int[] - element access)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_intArrayBeyond_Test() throws Exception {
        jsToJavaGetTest("intArrayBeyond", "Test no. 9 - (int[] - beyond length)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_regularString_Test() throws Exception {
        jsToJavaGetTest("regularString", "Test no.10 - (regular string)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_specialCharsString_Test() throws Exception {
        jsToJavaGetTest("specialCharsString", "Test no.11 - (string with special characters)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_null_Test() throws Exception {
        jsToJavaGetTest("null", "Test no.12 - (null)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Integer_Test() throws Exception {
        jsToJavaGetTest("Integer", "Test no.13 - (Integer)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Double_Test() throws Exception {
        jsToJavaGetTest("Double", "Test no.14 - (Double)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Float_Test() throws Exception {
        jsToJavaGetTest("Float", "Test no.15 - (Float)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Long_Test() throws Exception {
        jsToJavaGetTest("Long", "Test no.16 - (Long)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Boolean_Test() throws Exception {
        jsToJavaGetTest("Boolean", "Test no.17 - (Boolean)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Character_Test() throws Exception {
        jsToJavaGetTest("Character", "Test no.18 - (Character)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_Byte_Test() throws Exception {
        jsToJavaGetTest("Byte", "Test no.19 - (Byte)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_DoubleArrayElement_Test() throws Exception {
        jsToJavaGetTest("DoubleArrayElement", "Test no.20 - (Double[] - element access)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJGet_DoubleFullArray_Test() throws Exception {
        jsToJavaGetTest("DoubleFullArray", "Test no.21 - (Double[] - full array)");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJGet_JSObject_Test() throws Exception {
        jsToJavaGetTest("JSObject", "Test no.22 - (JSObject)");
    }
}
