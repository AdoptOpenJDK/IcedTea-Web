/* JSToJFuncResolTest.java
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

public class JSToJFuncResolTest extends BrowserTest {

    private final String initStr = "JSToJFuncResol applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {

            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String expectedStdout, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JSToJFuncResol: the stdout should contain " + initStr
                + ", but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values set by JavaScript are ok
        Assert.assertTrue("JSToJFuncResol: the output should include: "+expectedStdout+", but it didnt.",
        pr.stdout.contains(expectedStdout));

    }

    private void jsToJavaFuncResolTest( String methodStr, String valueStr, String expectedStdout) throws Exception {
        String strURL = "/JSToJFuncResol.html?" + methodStr + ";" + valueStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedStdout, pr);
    }

    /****** Primitive (numeric) value resolutions ******/
    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_numeric_Test() throws Exception {
        jsToJavaFuncResolTest("numeric", "1", "numeric(int) with 1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_numericToDifferentNumeric_Test() throws Exception {
        jsToJavaFuncResolTest("numericToDifferentNumeric", "1.1", "numericToDifferentNumeric(double) with 1.1");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail
    public void AppletJSToJFuncResol_numericToDouble_Test() throws Exception {
        jsToJavaFuncResolTest("numericToDouble", "1.1", "numericToDouble(double) with 1.1");
    }

    /****** Null resolutions ******/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_nullToInteger_Test() throws Exception {
        jsToJavaFuncResolTest("nullToInteger", "null", "nullToInteger(Integer) with null");
    }

    /****** Java inherited class resolutions ******/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_inheritedClass_Test() throws Exception {
        jsToJavaFuncResolTest("inheritedClass", "applet.getNewOverloadTestHelper2()", "inheritedClass(OverloadTestHelper2) with JSToJFuncResol$OverloadTestHelper2@");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_inheritedClassToParent1_Test() throws Exception {
        jsToJavaFuncResolTest("inheritedClassToParent1", "applet.getNewOverloadTestHelper3()", "inheritedClassToParent1(OverloadTestHelper2) with JSToJFuncResol$OverloadTestHelper3@");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_inheritedClassToParent2_Test() throws Exception {
        jsToJavaFuncResolTest("inheritedClassToParent2", "applet.getNewOverloadTestHelper2()", "inheritedClassToParent2(OverloadTestHelper1) with JSToJFuncResol$OverloadTestHelper2@");
    }

    /****** Java object resolutions ******/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_javaObjectToString_Test() throws Exception {
        jsToJavaFuncResolTest("javaObjectToString", "applet.getNewOverloadTestHelper1()", "javaObjectToString(String) with JSToJFuncResol$OverloadTestHelper1@");
    }

    /****** String resolutions ******/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_javascriptStringToNumeric_Test() throws Exception {
        jsToJavaFuncResolTest("javascriptStringToNumeric", "\"1.1\"", "javascriptStringToNumeric(double) with 1.1");
    }

    /****** Javascript object resolutions ******/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_javascriptObject_Test() throws Exception {
        jsToJavaFuncResolTest("javascriptObject", "window", "javascriptObject(JSObject) with [object Window]");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_javascriptObjectToArray_Test() throws Exception {
        jsToJavaFuncResolTest("javascriptObjectToArray", "[10]", "javascriptObjectToArray(int[]) with [I@");
    }

    /****** The unsupported resolutions: *****/

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_nullToPrimitive_Test() throws Exception {
        jsToJavaFuncResolTest("nullToPrimitive", "null", "Error on Java side: No suitable method named nullToPrimitive with matching args found");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_javascriptObjectToUnrelatedType_Test()
            throws Exception {
        jsToJavaFuncResolTest("javascriptObjectToUnrelatedType", "window", "Error on Java side: No suitable method named javascriptObjectToUnrelatedType with matching args found");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJSToJFuncResol_unsupported_Test() throws Exception {
        jsToJavaFuncResolTest("unsupported", "25", "Error on Java side: No suitable method named unsupported with matching args found");
    }

}
