/* AppletTestSignedTests.java
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

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ProcessWrapper;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.closinglisteners.StringBasedClosingListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener.*;

public class AppletTestSignedTests extends BrowserTest {

    private static final String s0 = "AppletTestSigned was started";
    private static final String s1 = "value1";
    private static final String s2 = "value2";
    private static final String s3 = "AppletTestSigned was initialised";
    private static final String s7 = "AppletTestSigned killing himself after 2000 ms of life";
    private static final ContainsRule startedRule = new ContainsRule(s0);
    private static final ContainsRule variable1Rule = new ContainsRule(s1);
    private static final ContainsRule variable2Rule = new ContainsRule(s2);
    private static final ContainsRule initialisedRule = new ContainsRule(s3);
    private static final ContainsRule killedRule = new ContainsRule(s7);
    private static final RulesFolowingClosingListener okListener=new RulesFolowingClosingListener(startedRule, variable1Rule, variable2Rule, initialisedRule, killedRule);

    @Test
    public void AppletTestSignedTest() throws Exception {
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option}), server.getUrl("AppletTestSigned.jnlp"));
        pw.setWriter("YES\nYES\n");
        pw.addStdOutListener(new StringBasedClosingListener("killer was started"));
        ProcessResult pr = pw.execute();
        evaluateSignedApplet(pr, true);
        //closing listener affected those two
        //Assert.assertFalse(pr.wasTerminated);
        //Assert.assertEquals((Integer) 0, pr.returnValue);
    }
    
    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    public void AppletTestSignedTestBrowser() throws Exception {
        ProcessWrapper pw = new ProcessWrapper(server.getBrowserLocation(), Arrays.asList(new String[]{}), server.getUrl("AppletTestSigned.html"));
        pw.setWriter("YES\nYES\n");
        pw.addStdOutListener(new StringBasedClosingListener("killer was started"));
        ProcessResult pr = pw.execute();
        evaluateSignedApplet(pr, true);
    }

    @Test
    public void AppletTestSignedTestHtmlSwitch() throws Exception {
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option, OptionsDefinitions.OPTIONS.HTML.option}), server.getUrl("AppletTestSigned.html"));
        pw.setWriter("YES\nYES\n");
        pw.addStdOutListener(new StringBasedClosingListener("killer was started"));
        ProcessResult pr = pw.execute();
        evaluateSignedApplet(pr, true);
    }

    @Test
    public void AppletTestSignedTestHtmlSwitchNotAffectedByJnlpProtocol() throws Exception {
        String jnlpUrl = "jnlp://localhost:" + server.getPort() + "/AppletTestSigned.html";
        ProcessWrapper pw = new ProcessWrapper(server.getJavawsLocation(), Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option, OptionsDefinitions.OPTIONS.HTML.option}), jnlpUrl);
        pw.setWriter("YES\nYES\n");
        pw.addStdOutListener(new StringBasedClosingListener("killer was started"));
        ProcessResult pr = pw.execute();
        Assert.assertTrue("jnlp protocol should not be recognized", pr.stderr.contains("Invalid jnlp file "+jnlpUrl));
        //applet should not start
        Assert.assertFalse("AppletTestSigned stdout " + initialisedRule.toFailingString()+ " but didn't", initialisedRule.evaluate(pr.stdout));
        Assert.assertFalse("AppletTestSigned stdout " + startedRule.toFailingString() + " but didn't", startedRule.evaluate(pr.stdout));
        Assert.assertFalse("AppletTestSigned stdout " + variable1Rule.toFailingString() + " but didn't", variable1Rule.evaluate(pr.stdout));
        Assert.assertFalse("AppletTestSigned stdout " + variable2Rule.toFailingString() + " but didn't", variable2Rule.evaluate(pr.stdout));
    }

    private void evaluateSignedApplet(ProcessResult pr, boolean javawsApplet) {
        Assert.assertTrue("AppletTestSigned stdout " + initialisedRule.toPassingString() + " but didn't", initialisedRule.evaluate(pr.stdout));
        Assert.assertTrue("AppletTestSigned stdout " + startedRule.toPassingString() + " but didn't", startedRule.evaluate(pr.stdout));
        Assert.assertTrue("AppletTestSigned stdout " + variable1Rule.toPassingString() + " but didn't", variable1Rule.evaluate(pr.stdout));
        Assert.assertTrue("AppletTestSigned stdout " + variable2Rule.toPassingString() + " but didn't", variable2Rule.evaluate(pr.stdout));
        //Assert.assertTrue("AppletTestSigned stdout " + killedRule.toPassingString() + " but didn't", killedRule.evaluate(pr.stdout));
        if (!javawsApplet) {
            /*this is working correctly in most firefox, but not in all. temporarily disabling
            String s4 = "AppletTestSigned was stopped";
            Assert.assertTrue("AppletTestSigned stdout shouldt contain " + s4 + " but did", pr.stdout.contains(s4));
            String s5 = "AppletTestSigned will be destroyed";
            Assert.assertTrue("AppletTestSigned stdout shouldt contain " + s5 + " but did", pr.stdout.contains(s5));
             */
        }
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    public void AppletTestSignedFirefoxTestXslowX() throws Exception {
        ServerAccess.PROCESS_TIMEOUT = 30 * 1000;
        try {
            ProcessResult pr = server.executeBrowser("/AppletTestSigned2.html", okListener, null);
            evaluateSignedApplet(pr, false);
            //Assert.assertTrue(pr.wasTerminated);
            //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
        } finally {
            ServerAccess.PROCESS_TIMEOUT = 20 * 1000; //back to normal
        }
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    public void AppletTestSignedFirefoxTest() throws Exception {
        ProcessResult pr = server.executeBrowser("/AppletTestSigned.html", ServerAccess.AutoClose.CLOSE_ON_CORRECT_END);
        evaluateSignedApplet(pr, false);
        //Assert.assertTrue(pr.wasTerminated);
        //Assert.assertEquals((Integer) 0, pr.returnValue); due to destroy is null
    }
}
