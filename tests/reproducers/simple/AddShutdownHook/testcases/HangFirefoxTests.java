/* AddShutdownHookTest.java
 Copyright (C) 2011 Red Hat, Inc.

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
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.CountingClosingListener;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.Assert;

import org.junit.Test;

/**

If this test has failed, you may try the following to reproduce the problem more consistently:

-    private static final int MAX_WORKERS = MAX_PARALLEL_INITS * 4;
-    private static final int PRIORITY_WORKERS = MAX_PARALLEL_INITS * 2;
+    private static final int MAX_WORKERS = MAX_PARALLEL_INITS * 2;
+    private static final int PRIORITY_WORKERS = MAX_PARALLEL_INITS * 1;

in PluginMessageConsumer.java


*/
public class HangFirefoxTests extends BrowserTest {

    String leString = "LaunchException";
    ;
    String startedString = "applet was started";
    RulesFolowingClosingListener.ContainsRule leRule = new RulesFolowingClosingListener.ContainsRule(leString);
    RulesFolowingClosingListener.ContainsRule appleStartedRule = new RulesFolowingClosingListener.ContainsRule(startedString);

    @Test
    @TestInBrowsers(testIn = Browsers.firefox)
    public void HangFirefoxWithRuntimeExceptionTests() throws Exception {
        ProcessResult pr = server.executeBrowser("/AddShutdownHook.html", new RulesFolowingClosingListener(appleStartedRule), new CountingClosingListener() {
            private boolean launched = false;

            @Override
            protected boolean isAlowedToFinish(String content) {
                if (AddShutdownHookTest.mr.evaluate(content) && !launched) {
                    launched = true;
                    try {
                        server.executeBrowser("/appletAutoTests2.html", null, (CountingClosingListener) null);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return false;
            }
        });
        Assert.assertTrue("stderr " + AddShutdownHookTest.mr.toPassingString(), AddShutdownHookTest.mr.evaluate(pr.stderr));
        Assert.assertTrue("stdout " + appleStartedRule.toPassingString(), appleStartedRule.evaluate(pr.stdout));
        Assert.assertFalse("stderr " + AddShutdownHookTest.cnf.toFailingString(), AddShutdownHookTest.cnf.evaluate(pr.stderr));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.firefox)
    public void HangFirefoxWithLaunchException() throws Exception {
        ProcessResult pr = server.executeBrowser("/AddShutdownHook_wrong.html", new RulesFolowingClosingListener(appleStartedRule), new CountingClosingListener() {
            private boolean launched = false;

            @Override
            protected boolean isAlowedToFinish(String content) {
                if (leRule.evaluate(content) && !launched) {
                    launched = true;
                    try {
                        server.executeBrowser("/appletAutoTests2.html", null, (CountingClosingListener) null);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return false;
            }
        });
        Assert.assertTrue("stderr " + leRule.toPassingString(), leRule.evaluate(pr.stderr));
        Assert.assertTrue("stdout " + appleStartedRule.toPassingString(), appleStartedRule.evaluate(pr.stdout));
        Assert.assertFalse("stderr " + AddShutdownHookTest.cnf.toFailingString(), AddShutdownHookTest.cnf.evaluate(pr.stderr));
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void TestAddShutdownHookWrong() throws Exception {
        ProcessResult pr = server.executeBrowser("/AddShutdownHook_wrong.html", null, new RulesFolowingClosingListener(leRule));
        Assert.assertTrue("stderr " + leRule.toPassingString(), leRule.evaluate(pr.stderr));
        Assert.assertFalse("stderr " + AddShutdownHookTest.cnf.toFailingString(), AddShutdownHookTest.cnf.evaluate(pr.stderr));
    }
}
