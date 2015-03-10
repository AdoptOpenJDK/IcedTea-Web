/* JToJSFuncParamTest.java
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

public class JavascriptFuncParamTest extends BrowserTest {

    public final boolean doNotRunInOpera = false;

    private final String initStr = "JToJSFuncParam applet initialized.";
    private final String afterStr = "afterTests";
    private final String globStart = "Call with ";
    private final String globEnd = " from JS";
    private final String jEnd = " from J";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JToJSFuncParamTest stdout should contain " + initStr + " but it didnt.", pr.stdout.contains(initStr));

        // Assert that the results of two calls of js func are the same
        
        int gs = pr.stdout.indexOf(globStart);
        int ge = pr.stdout.indexOf(globEnd);
        int je = pr.stdout.indexOf(jEnd);
        int jss = je + jEnd.length() + 1;
        
        String javaOutput = pr.stdout.substring(gs, je);
        String jsOutput = pr.stdout.substring(jss, ge);
        
        Assert.assertTrue("JToJSFuncParam: the J and JS outputs are not equal!", javaOutput.equals(jsOutput));
    }

    private void javaToJSFuncParamTest(String funcStr) throws Exception {

        if( doNotRunInOpera){
            Browsers b = server.getCurrentBrowser().getID();
            if(b == Browsers.opera){
                return;
            }
        }

        String strURL = "/JavascriptFuncParam.html?" + funcStr;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_int_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallintParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_double_Test() throws Exception {
        javaToJSFuncParamTest("jjsCalldoubleParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_float_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallfloatParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_long_Test() throws Exception {
        javaToJSFuncParamTest("jjsCalllongParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_short_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallshortParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_byte_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallbyteParam");
    }


    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_char_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallcharParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_boolean_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallbooleanParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Integer_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallIntegerParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Double_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallDoubleParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Float_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallFloatParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Long_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallLongParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Short_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallShortParam");
    }    

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Byte_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallByteParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Boolean_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallBooleanParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_Character_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallCharacterParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_String_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallStringParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncParam_DummyObject_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallDummyObjectParam");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    @KnownToFail(failsIn={Browsers.googleChrome, Browsers.chromiumBrowser})
    public void AppletJToJSFuncParam_JSObject_Test() throws Exception {
        javaToJSFuncParamTest("jjsCallJSObjectParam");
    }
}
