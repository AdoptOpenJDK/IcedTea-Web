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

import net.sourceforge.jnlp.ClosingListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.closinglisteners.StringMatchClosingListener;
import org.junit.Assert;

import org.junit.Test;

public class AddShutdownHookTest extends BrowserTest {

    public static final String s = "(?s).*java.security.AccessControlException.{0,5}access denied.{0,5}java.lang.RuntimePermission.{0,5}" + "shutdownHooks" + ".*";
    public static final String cnfString = "ClassNotFoundException";
    public static final String confirmFailure = "WRONG - ShutdownHook was probably added";
    public static final RulesFolowingClosingListener.MatchesRule mr = new RulesFolowingClosingListener.MatchesRule(s);
    public static final RulesFolowingClosingListener.ContainsRule cnf = new RulesFolowingClosingListener.ContainsRule(cnfString);
    public static final RulesFolowingClosingListener.ContainsRule cf = new RulesFolowingClosingListener.ContainsRule(confirmFailure);
    public static final RulesFolowingClosingListener rfc = new RulesFolowingClosingListener(mr);

    @Test
    public void AddShutdownHookTestLunch1() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(null, "/AddShutdownHook.jnlp");
        Assert.assertTrue("stderr " + mr.toPassingString(), mr.evaluate(pr.stderr));
        Assert.assertFalse("stderr " + cnf.toFailingString(), cnf.evaluate(pr.stderr));
        Assert.assertFalse("AddShutdownHookTestLunch1 should not be terminated, but was", pr.wasTerminated);
        Assert.assertFalse("stderr " + cf.toFailingString(), cf.evaluate(pr.stderr));
        Assert.assertEquals((Integer) 0, pr.returnValue);
    }

    @Test
    @TestInBrowsers(testIn = Browsers.one)
    public void AddShutdownHookApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/AddShutdownHook.html", null, rfc);

        if (server.getCurrentBrowsers() == Browsers.firefox) {
            //lookslike only firefox is able to recieve this
            Assert.assertTrue("stderr " + mr.toPassingString(), mr.evaluate(pr.stderr));
        }
        Assert.assertFalse("stderr " + cnf.toFailingString(), cnf.evaluate(pr.stderr));
        Assert.assertFalse("stderr " + cf.toFailingString(), cf.evaluate(pr.stderr));
    }
}
