/* JSToJFuncParamTest.java
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
import org.junit.Assert;

import org.junit.Test;

public class JSToJFuncParamTest extends BrowserTest {

    //the JS<->J tests tend to make Opera unusable
    public final boolean doNotRunInOpera = true;

    private final String initStr = "JSToJFuncParam applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

            @Override
            protected boolean isAlowedToFinish(String s) {
                return (s.contains(initStr) && s.contains(afterStr));
            }
    }

    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJFuncParam: the stdout should contain " + initStr
                + ", but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values set by JavaScript are ok
        Assert.assertTrue("JSToJFuncParam: the output should include: "+expectedStdout+", but it didnt.", pr.stdout.contains(expectedStdout));

    }

    private void jsToJavaFuncParamTest(String funcStr, String paramStr, String expectedVal) throws Exception {

        if( doNotRunInOpera){
            if(server.getCurrentBrowser().getID() == Browsers.opera){
                return;
            }
        }

        String strURL = "/JSToJFuncParam.html?" + funcStr + ";" + paramStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        String expectedStdout = funcStr + " " + expectedVal;
        evaluateStdoutContents(expectedStdout, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_int_Test() throws Exception {
        jsToJavaFuncParamTest("intParam", "1", "1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_double_Test() throws Exception {
        jsToJavaFuncParamTest("doubleParam", "1.1", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_float_Test() throws Exception {
        jsToJavaFuncParamTest("floatParam", "1.1", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_long_Test() throws Exception {
        jsToJavaFuncParamTest("longParam", "10000", "10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_boolean_Test() throws Exception {
        jsToJavaFuncParamTest("booleanParam", "true", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_char_Test() throws Exception {
        jsToJavaFuncParamTest("charParam", "97", "a");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_byte_Test() throws Exception {
        jsToJavaFuncParamTest("byteParam", "10", "10");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_charArray_Test() throws Exception {
        jsToJavaFuncParamTest("charArrayParam", "[97,98,99]", "[a, b, c]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_String_Test() throws Exception {
        jsToJavaFuncParamTest("StringParam", "\"test\"", "test");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Integer_Test() throws Exception {
        jsToJavaFuncParamTest("IntegerParam", "1", "1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Double_Test() throws Exception {
        jsToJavaFuncParamTest("DoubleParam", "1.1", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Float_Test() throws Exception {
        jsToJavaFuncParamTest("FloatParam", "1.1", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Long_Test() throws Exception {
        jsToJavaFuncParamTest("LongParam", "10000", "10000");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Boolean_Test() throws Exception {
        jsToJavaFuncParamTest("BooleanParam", "true", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Character_Test() throws Exception {
        jsToJavaFuncParamTest("CharacterParam", "new applet.Packages.java.lang.Character(65)", "A");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_Byte_Test() throws Exception {
        jsToJavaFuncParamTest("ByteParam", "10", "10");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_StringIntMixed_Test() throws Exception {
        jsToJavaFuncParamTest("StringIntMixedParam", "[\"test\",123]", "[test, 123]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_DummyObjectArray_Test() throws Exception {
        jsToJavaFuncParamTest("DummyObjectArrayParam", "[applet.getNewDummyObject(\"Dummy1\"),applet.getNewDummyObject(\"Dummy2\")]", "[Dummy1, Dummy2]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_JSObject_Test() throws Exception {
        jsToJavaFuncParamTest("JSObjectParam", "new JSCar(100,\"red\")", "100, red");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_booleanFalseStr_Test() throws Exception {
        jsToJavaFuncParamTest("booleanParam", "false", "true");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncParam_BooleanFalseStr_Test() throws Exception {
        jsToJavaFuncParamTest("BooleanParam", "false", "true");
    }

}
