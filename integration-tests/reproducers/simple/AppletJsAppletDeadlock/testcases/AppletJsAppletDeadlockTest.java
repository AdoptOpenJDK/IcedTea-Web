/* 
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
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppletJsAppletDeadlockTest extends BrowserTest {

    private static final String called = "Callback function called";
    private static final String started = "AppletJsAppletDeadlock started";
    private static final String finished = "JS call finished";
    private static final RulesFolowingClosingListener.ContainsRule calledRule = new RulesFolowingClosingListener.ContainsRule(called);
    private static final RulesFolowingClosingListener.ContainsRule startedRule = new RulesFolowingClosingListener.ContainsRule(started);
    private static final RulesFolowingClosingListener.ContainsRule finishedRule = new RulesFolowingClosingListener.ContainsRule(finished);
    private static final long defaultTimeout = ServerAccess.PROCESS_TIMEOUT;

    @BeforeClass
    public static void setTimeout() {
        //the timeout is js call is 60s 
        //see sun.applet.PluginAppletViewer.REQUEST_TIMEOUT
        //so wee need to have little longer timooute here
        ServerAccess.PROCESS_TIMEOUT = 120000;//120 s 
    }

    @AfterClass
    public static void resetTimeout() {
        ServerAccess.PROCESS_TIMEOUT = defaultTimeout;
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void callAppletJsAppletNotDeadlock() throws Exception {
        ProcessResult processResult = server.executeBrowser("AppletJsAppletDeadlock.html", new RulesFolowingClosingListener(finishedRule), null);
        Assert.assertTrue(startedRule.toPassingString(), startedRule.evaluate(processResult.stdout));
        Assert.assertTrue(finishedRule.toPassingString(), finishedRule.evaluate(processResult.stdout));
        //this is representing another error, not sure now it is worthy to be fixed
        //Assert.assertTrue(calledRule.toPassingString(), calledRule.evaluate(processResult.stdout));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    @KnownToFail
    public void callAppletJsAppletSuccessfullyEvaluated() throws Exception {
        ProcessResult processResult = server.executeBrowser("AppletJsAppletDeadlock.html", new RulesFolowingClosingListener(finishedRule), null);
        Assert.assertTrue(startedRule.toPassingString(), startedRule.evaluate(processResult.stdout));
        Assert.assertTrue(finishedRule.toPassingString(), finishedRule.evaluate(processResult.stdout));
        Assert.assertTrue(calledRule.toPassingString(), calledRule.evaluate(processResult.stdout));
    }
}
