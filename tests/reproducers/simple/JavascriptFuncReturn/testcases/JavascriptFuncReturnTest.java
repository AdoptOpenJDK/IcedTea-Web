/* JToJSFuncReturnTest.java
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

public class JavascriptFuncReturnTest extends BrowserTest {

    public final boolean doNotRunInOpera = false;

    private final String initStr = "JToJSFuncReturn applet initialized.";
    private final String afterStr = "afterTests";

    private class CountingClosingListenerImpl extends CountingClosingListener {

        @Override
        protected boolean isAlowedToFinish(String s) {
            return (s.contains(initStr) && s.contains(afterStr));
        }
    }

    private void evaluateStdoutContents(String[] expectedStdoutsOR, ProcessResult pr) {
        // Assert that the applet was initialized.
        Assert.assertTrue("JToJSFuncReturnTest stdout should contain " + initStr + " but it didnt.", pr.stdout.contains(initStr));

         // Assert that the values set from JavaScript are ok
        boolean atLeastOne = false;
        for(String s : expectedStdoutsOR){
        	if(pr.stdout.contains(s)) atLeastOne = true;
        }
        Assert.assertTrue("JToJSFuncReturn: the output should include at least one of expected Stdouts, but it didnt.", atLeastOne);
    }

    private void javaToJSFuncReturnTest(String urlEnd, String[] expectedValsOR) throws Exception {

        if( doNotRunInOpera){
            Browsers b = server.getCurrentBrowser().getID();
            if(b == Browsers.opera){
                return;
            }
        }

        String strURL = "/JavascriptFuncReturn.html?" + urlEnd;
        ProcessResult pr = server.executeBrowser(strURL, new CountingClosingListenerImpl(), new CountingClosingListenerImpl());
        evaluateStdoutContents(expectedValsOR, pr);
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncReturn_number_Test() throws Exception {
        javaToJSFuncReturnTest("123", new String[] {"123"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncReturn_boolean_Test() throws Exception {
        javaToJSFuncReturnTest("true", new String[] {"true"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncReturn_String_Test() throws Exception {
        javaToJSFuncReturnTest("\"𠁎〒£$ǣ€𝍖\"", new String[] {"𠁎〒£$ǣ€𝍖"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncReturn_Object_Test() throws Exception {
        javaToJSFuncReturnTest("applet.getNewDummyObject(\"dummy1\")", new String[] {"dummy1"});
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.all })
    @NeedsDisplay
    public void AppletJToJSFuncReturn_JSObject_Test() throws Exception {
        javaToJSFuncReturnTest("window", new String[] {"[object Window]", "[object DOMWindow]"});
    }

}
