/* JSToJFuncReturnTest.java
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
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class JSToJFuncReturnTest extends BrowserTest {

    private final String initStr = "JSToJFuncReturn applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJFuncReturnTest stdout should contain "+ initStr + " but it didnt.", pr.stdout.contains(initStr));

        // Assert that the tests have passed.
        Assert.assertTrue("JSToJFuncReturnTest stdout should contain "+ expectedStdout + " but it didnt.", pr.stdout.contains(expectedStdout));
    }


    private void jsToJavaFuncReturnNormalTest(String methodStr, String expectedStdout) throws Exception {
        String strURL = "/JSToJFuncReturn.html?" + methodStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedStdout, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_int_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_int", "number 1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_double_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_double", "number 1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_float_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_float", "number 1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_long_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_long", "number 10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_boolean_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_boolean", "boolean true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_char_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_char", "number 97"); //'a'
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_byte_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_byte", "number 10");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_charArrayElement_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_charArrayElement", "number 97");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_void_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_void", "undefined undefined");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_regularString_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_regularString", "string test");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_specialCharsString_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_specialString", "string 𠁎〒£$ǣ€𝍖");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_null_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_null", "object null");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Integer_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Integer", "object 1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Double_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Double", "object 1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Float_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Float", "object 1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Long_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Long", "object 10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Boolean_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Boolean", "object true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Character_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Character", "object A");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_Byte_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_Byte", "object 10");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncReturn_CharArrayElement_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_CharacterArrayElement", "object A");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_CharFullArray_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_CharacterArray", "object [Ljava.lang.Character;@");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncReturn_JSObject_Test() throws Exception {
        jsToJavaFuncReturnNormalTest("_JSObject", "object value1");
    }


}
