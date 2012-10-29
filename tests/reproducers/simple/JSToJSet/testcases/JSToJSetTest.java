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
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class JSToJSetTest extends BrowserTest {

    private final String exceptionStr = "xception";
    private final String errorStr = "rror";
    private final String initStr = "JSToJSet applet initialized.";
    private final String afterStr = "afterTests";
        
    private class CountingClosingListenerImpl extends CountingClosingListener {

            @Override
            protected boolean isAlowedToFinish(String s) {
                if (s.contains(exceptionStr) || s.contains(errorStr)) {
                    return true;
                }
                return (s.contains(initStr) && s.contains(afterStr));
            }
        }
    
    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJSetTest stdout should contain \"" + initStr
                + "\" but it didn't.", pr.stdout.contains(initStr));

        // Assert that the values set by JavaScript are ok
        Assert.assertTrue("The output should include: "+expectedStdout+", but is: "+pr.stdout,
                pr.stdout.contains(expectedStdout));

    }

    private void jsToJavaSetNormalTest(String fieldStr, String valueStr) throws Exception {
        String strURL = "/JSToJSet.html?" + fieldStr + ";" + valueStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        String expectedStdout = "New value is: " + valueStr;
        evaluateStdoutContents(expectedStdout, pr);
    }
    
    private void jsToJavaSetSpecialTest(String fieldStr, String valueStr, int testType) throws Exception {
        String strURL = "/JSToJSet.html?";
        String expectedStdout = "";
        switch( testType ){
        case 0://array element
            strURL += fieldStr + ";" + valueStr;
            expectedStdout = "New array value is: "+valueStr;
            break;
        case 1://whole array, set 1st element
            strURL += fieldStr + ";[" + valueStr;
            expectedStdout = "New array value is: "+valueStr;
            break;
        case 2://char et al - to be set at JS side
            strURL += fieldStr + ";JavaScript";
            expectedStdout = "New value is: "+valueStr;
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
        jsToJavaSetSpecialTest("_char", "a", 2);
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
    public void AppletJSToJSet_intArrayElement_Test() throws Exception {
        jsToJavaSetSpecialTest("_intArray[0]", "1", 0);
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
        jsToJavaSetSpecialTest("_specialString", "𠁎〒£$ǣ€𝍖", 2);
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
    public void AppletJSToJSet_Integer_Test() throws Exception {
        jsToJavaSetNormalTest("_Integer", "1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_Double_Test() throws Exception {
        jsToJavaSetNormalTest("_Double", "1.0");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_Float_Test() throws Exception {
        jsToJavaSetNormalTest("_Float", "1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
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
        jsToJavaSetSpecialTest("_Character", "A", 2);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_Byte_Test() throws Exception {
        jsToJavaSetNormalTest("_Byte", "100");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_DoubleArrayElement_Test() throws Exception {
        jsToJavaSetSpecialTest("_DoubleArray[0]", "1.1", 0);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJSet_DoubleFullArray_Test() throws Exception {
        jsToJavaSetSpecialTest("_DoubleArray2", "0.1", 1);
    }

}
