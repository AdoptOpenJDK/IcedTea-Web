/* JToJSSetTest.java
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

public class JavascriptSetTest extends BrowserTest {

    public final boolean doNotRunInOpera = false;

    private final String initStr = "JToJSSet applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String[] expectedStdoutsOR, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JToJSSetTest stdout should contain " + initStr + " but it didnt.", pr.stdout.contains(initStr));

        // Assert that the values set from JavaScript are ok
        boolean atLeastOne = false;
        for (String s : expectedStdoutsOR) {
            if (pr.stdout.contains(s)) {
                atLeastOne = true;
            }
        }
        Assert.assertTrue("JToJSSet: the output should include at least one of expected Stdouts, but it didnt.", atLeastOne);
    }

    private void javaToJSSetTest(String urlEnd, String[] expectedValsOR) throws Exception {

        if (doNotRunInOpera) {
            Browsers b = server.getCurrentBrowser().getID();
            if (b == Browsers.opera) {
                return;
            }
        }

        String strURL = "/JavascriptSet.html?" + urlEnd;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedValsOR, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_int_Test() throws Exception {
        javaToJSSetTest("jjsSetInt", new String[] { "1" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Integer_Test() throws Exception {
        javaToJSSetTest("jjsSetInteger", new String[] { "2" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_double_Test() throws Exception {
        javaToJSSetTest("jjsSetdouble", new String[] { "2.5" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Double_Test() throws Exception {
        javaToJSSetTest("jjsSetDouble", new String[] { "2.5" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_float_Test() throws Exception {
        javaToJSSetTest("jjsSetfloat", new String[] { "2.5" }); // 2.3->2.2999...
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Float_Test() throws Exception {
        javaToJSSetTest("jjsSetFloat", new String[] { "2.5" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_long_Test() throws Exception {
        javaToJSSetTest("jjsSetlong", new String[] { "4294967296" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Long_Test() throws Exception {
        javaToJSSetTest("jjsSetLong", new String[] { "4294967297" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_short_Test() throws Exception {
        javaToJSSetTest("jjsSetshort", new String[] { "3" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Short_Test() throws Exception {
        javaToJSSetTest("jjsSetShort", new String[] { "4" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_byte_Test() throws Exception {
        javaToJSSetTest("jjsSetbyte", new String[] { "5" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Byte_Test() throws Exception {
        javaToJSSetTest("jjsSetByte", new String[] { "6" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_char_Test() throws Exception {
        javaToJSSetTest("jjsSetchar", new String[] { "97" }); // i.e. 'a'
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Character_Test() throws Exception {
        javaToJSSetTest("jjsSetCharacter", new String[] { "97" }); // i.e. 'a'
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_boolean_Test() throws Exception {
        javaToJSSetTest("jjsSetboolean", new String[] { "true" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_Boolean_Test() throws Exception {
        javaToJSSetTest("jjsSetBoolean", new String[] { "true" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_String_Test() throws Exception {
        javaToJSSetTest("jjsSetString", new String[] { "𠁎〒£$ǣ€𝍖" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_object_Test() throws Exception {
        javaToJSSetTest("jjsSetObject", new String[] { "DummyObject2" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_1DArrayElement_Test() throws Exception {
        javaToJSSetTest("jjsSet1DArray", new String[] { "100" });
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSSet_2DArrayElement_Test() throws Exception {
        javaToJSSetTest("jjsSet2DArray", new String[] { "200" });
    }
}
