/* JSToJTypeConvTest.java
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

public class JSToJTypeConvTest extends BrowserTest {

    //the JS<->J tests tend to make Opera unusable
    public final boolean doNotRunInOpera = true;

    private final String initStr = "JSToJTypeConv applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String[] expectedStdouts, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJTypeConv: the stdout should contain " + initStr
                + ", but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values set by JavaScript are ok
        for(String str : expectedStdouts){

            String xmlStr = "new value";

            if(str.contains("nonXML char"))
            {
                str = str.substring(12);
            }else{
                xmlStr = str;
            }

            Assert.assertTrue("JSToJTypeConv: the output should include "+xmlStr+", but it didnt.", pr.stdout.contains(str));
        }
    }

    private void jsToJavaTypeConvTest(String fieldStr, String valueStr, String[] expectedValueAndOutputs) throws Exception {

        if( doNotRunInOpera){
            if(server.getCurrentBrowser().getID() == Browsers.opera){
                return;
            }
        }

        String strURL = "/JSToJTypeConv.html?" + fieldStr + ";" + valueStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        String[] expectedStdouts = expectedValueAndOutputs;
        evaluateStdoutContents(expectedStdouts, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToJavaStringInteger_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "1", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToJavaStringDouble_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "1.1", new String[] {"1.1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToJavaObjectInteger_Test() throws Exception {
        jsToJavaTypeConvTest("_Object", "1.0", new String[] {"1","superclass is java.lang.Number"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToJavaObjectDouble_Test() throws Exception {
        jsToJavaTypeConvTest("_Object", "1.1", new String[] {"1.1","superclass is java.lang.Number"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToboolean0_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "0", new String[] {"false"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_NumToboolean1dot1_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "1.1", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToJavaBoolTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_Boolean", "true", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToJavaBoolFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_Boolean", "false", new String[] {"false"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToJavaObject_Test() throws Exception {
        jsToJavaTypeConvTest("_Object", "true", new String[] {"true", "class is java.lang.Boolean"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToJavaString_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "true", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTobyteTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_byte", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTocharTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_char", "true", new String[] { "nonXML char "+((char)1) });
     }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToshortTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_short", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTointTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_int", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTolongTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_long", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTofloatTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_float", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTodoubleTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_double", "true", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTobyteFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_byte", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTocharFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_char", "false", new String[] { "nonXML char "+((char)0) });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolToshortFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_short", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTointFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_int", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTolongFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_long", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTofloatFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_float", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_BoolTodoubleFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_double", "false", new String[] {"0"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringToObject_Test() throws Exception {
        jsToJavaTypeConvTest("_Object", "\"𠁎〒£$ǣ€𝍖\"", new String[] {"𠁎〒£$ǣ€𝍖"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTobyte_Test() throws Exception {
        jsToJavaTypeConvTest("_byte", "\'1\'", new String[] {"1"}); //JS string 'str' or "str" both ok
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringToshort_Test() throws Exception {
        jsToJavaTypeConvTest("_short", "\"1\"", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringToint_Test() throws Exception {
        jsToJavaTypeConvTest("_int", "\"1\"", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTolong_Test() throws Exception {
        jsToJavaTypeConvTest("_long", "\"1\"", new String[] {"1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTofloat_Test() throws Exception {
        jsToJavaTypeConvTest("_float", "\"1.1\"", new String[] {"1.1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTodouble_Test() throws Exception {
        jsToJavaTypeConvTest("_double", "\"1.1\"", new String[] {"1.1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTochar_Test() throws Exception {
        jsToJavaTypeConvTest("_char", "\"1\"", new String[] { "nonXML char "+((char)1) });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTobooleanEmptyFalse_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "\"\"", new String[] {"false"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTobooleanNonemptyTrue_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "\"a nonempty string\"", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTobyteArr_Test() throws Exception {
        jsToJavaTypeConvTest("_byteArray", "[1,null,2]", new String[] {"[1,0,2]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTocharArr_Test() throws Exception {
        jsToJavaTypeConvTest("_charArray", "[97,null,98]", new String[] {"nonXML char [a,"+((char)0) +",b]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayToshortArr_Test() throws Exception {
        jsToJavaTypeConvTest("_shortArray", "[1,null,2]", new String[] {"[1,0,2]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTointArr_Test() throws Exception {
        jsToJavaTypeConvTest("_intArray", "[1,null,2]", new String[] {"[1,0,2]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTolongArr_Test() throws Exception {
        jsToJavaTypeConvTest("_longArray", "[1,null,2]", new String[] {"[1,0,2]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTofloatArr_Test() throws Exception {
        jsToJavaTypeConvTest("_floatArray", "[1,null,2]", new String[] {"[1.0,0.0,2.0]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTodoubleArr_Test() throws Exception {
        jsToJavaTypeConvTest("_doubleArray", "[1,null,2]", new String[] {"[1.0,0.0,2.0]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayToStringArr_Test() throws Exception {
        jsToJavaTypeConvTest("_StringArray", "[1,null,2]", new String[] {"[1,null,2]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayTocharArrArr_Test() throws Exception {
        jsToJavaTypeConvTest("_charArray2D", "[[\"97\",null,\"98\"],[],[\"99\",\"100\",null,\"101\"]]", new String[] {"nonXML char [[a,"+((char)0)+",b],[],[c,d,"+((char)0)+",e]]"}); //Error on Java side: array element type mismatch
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayToStringArrArr_Test() throws Exception {
        jsToJavaTypeConvTest("_StringArray2D", "[[\"00\",null,\"02\"],[],[\"20\",\"21\",null,\"23\"]]", new String[] {"[[00,null,02],[],[20,21,null,23]]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_ArrayToString_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "[[\"00\",null,\"02\"],[],[\"20\",\"21\",null,\"23\"]]", new String[] {"00,,02,,20,21,,23"}); //Error on Java side: array element type mismatch
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_JSObjectToJSObject_Test() throws Exception {
        jsToJavaTypeConvTest("_JSObject", "window", new String[] {"[object Window]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_JSObjectToString_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "window", new String[] {"[object Window]"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_JavaObjectToJavaObject_Test() throws Exception {
        jsToJavaTypeConvTest("_Object", "new applet.Packages.java.lang.Float(1.1)", new String[] {"1.1","class is java.lang.Float"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_JavaObjectToString_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "applet.getNewDummyObject(\"dummy1\")", new String[] {"dummy1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_nullToJavaObjectString_Test() throws Exception {
        jsToJavaTypeConvTest("_String", "null", new String[] {"null"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTobooleanFalseStr_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "\"false\"", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringTobooleanTrueStr_Test() throws Exception {
        jsToJavaTypeConvTest("_boolean", "\"true\"", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringToBooleanFalseStr_Test() throws Exception {
        jsToJavaTypeConvTest("_Boolean", "\"false\"", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJTypeConv_StringToBooleanTrueStr_Test() throws Exception {
        jsToJavaTypeConvTest("_Boolean", "\"true\"", new String[] {"true"});
    }

}
