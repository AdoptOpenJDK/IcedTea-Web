/* JSToJSetTest.java
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
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

@Bug(id = { "PR1298" })
public class JSToJSetTest extends BrowserTest {

    // the JS<->J tests tend to make Opera unusable
    public final boolean doNotRunInOpera = false;

    private final String initStr = "JSToJSet applet initialized.";
    private final String afterStr = "afterTests";

    public enum TestType {
        ARRAY_ELEMENT, WHOLE_ARRAY, NORMAL_VALUE
    }

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJSet: the stdout should contain " + initStr + ", but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values set by JavaScript are ok
        Assert.assertTrue("JSToJSet: the output should include: " + expectedStdout + ", but it didnt.", pr.stdout.contains(expectedStdout));

    }

    private void jsToJavaSetNormalTest(String fieldStr, String valueStr) throws Exception {

        if (doNotRunInOpera) {
            if (server.getCurrentBrowser().getID() == Browsers.opera) {
                return;
            }
        }

        String strURL = "/JSToJSet.html?" + fieldStr + ";" + valueStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        String expectedStdout = "New value is: " + valueStr;
        evaluateStdoutContents(expectedStdout, pr);
    }

    private void jsToJavaSetSpecialTest(String fieldStr, String valueStr, TestType testType) throws Exception {

        if (doNotRunInOpera) {
            Browsers b = server.getCurrentBrowser().getID();
            if (b == Browsers.opera) {
                return;
            }
        }

        String strURL = "/JSToJSet.html?";
        String expectedStdout = "";
        switch (testType) {
        case ARRAY_ELEMENT:// array element
            strURL += fieldStr + ";" + valueStr;
            expectedStdout = "New array value is: " + valueStr;
            break;
        case WHOLE_ARRAY:// whole array, set 1st element
            strURL += fieldStr + ";[" + valueStr;
            expectedStdout = "New array value is: " + valueStr;
            break;
        case NORMAL_VALUE:// char et al - to be set at JS side
            strURL += fieldStr + ";JavaScript";
            expectedStdout = "New value is: " + valueStr;
            break;
        default:
            break;
        }

        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedStdout, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_int_Test() throws Exception {
        jsToJavaSetNormalTest("_int", "1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_double_Test() throws Exception {
        jsToJavaSetNormalTest("_double", "1.0");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_float_Test() throws Exception {
        jsToJavaSetNormalTest("_float", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_long_Test() throws Exception {
        jsToJavaSetNormalTest("_long", "10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_boolean_Test() throws Exception {
        jsToJavaSetNormalTest("_boolean", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_char_Test() throws Exception {
        jsToJavaSetSpecialTest("_char", "a", TestType.NORMAL_VALUE);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_byte_Test() throws Exception {
        jsToJavaSetNormalTest("_byte", "10");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @Bug(id = { "PR1298" })
    public void AppletJSToJSet_intArrayElement_Test() throws Exception {
        jsToJavaSetSpecialTest("_intArray[0]", "1", TestType.ARRAY_ELEMENT);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_regularString_Test() throws Exception {
        jsToJavaSetNormalTest("_String", "teststring");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_specialCharsString_Test() throws Exception {
        jsToJavaSetSpecialTest("_specialString", "𠁎〒£$ǣ€𝍖", TestType.NORMAL_VALUE);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_null_Test() throws Exception {
        jsToJavaSetNormalTest("_Object", "null");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_Integer_Test() throws Exception {
        jsToJavaSetNormalTest("_Integer", "1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_Double_Test() throws Exception {
        jsToJavaSetNormalTest("_Double", "1.0");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_Float_Test() throws Exception {
        jsToJavaSetNormalTest("_Float", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_Long_Test() throws Exception {
        jsToJavaSetNormalTest("_Long", "10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_Boolean_Test() throws Exception {
        jsToJavaSetNormalTest("_Boolean", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_Character_Test() throws Exception {
        jsToJavaSetSpecialTest("_Character", "A", TestType.NORMAL_VALUE);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_Byte_Test() throws Exception {
        jsToJavaSetNormalTest("_Byte", "100");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    @Bug(id = { "PR1298" })
    public void AppletJSToJSet_DoubleArrayElement_Test() throws Exception {
        jsToJavaSetSpecialTest("_DoubleArray[0]", "1.1", TestType.ARRAY_ELEMENT);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJSet_DoubleFullArray_Test() throws Exception {
        jsToJavaSetSpecialTest("_DoubleArray2", "0.1", TestType.WHOLE_ARRAY);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_JSObject_Test() throws Exception {
        jsToJavaSetSpecialTest("_JSObject", "100, red", TestType.NORMAL_VALUE);
    }
}
