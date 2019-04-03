/* CustomPoliciesTest.java
Copyright (C) 2014 Red Hat, Inc.

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* Test that adding permission for all codesources to read the user.home property
 * results in an unsigned applet being able to perform this action
 */
public class CustomPoliciesTest extends BrowserTest {

    private static DeploymentConfiguration config = JNLPRuntime.getConfiguration();
    private static File policy, policyBackup;

    @BeforeClass
    public static void setPolicyLocation() throws Exception {
        policy = new File(new URL(config.getProperty(DeploymentConfiguration.KEY_USER_SECURITY_POLICY)).getPath());
        File securityDir = policy.getParentFile();
        File[] previousBackups = securityDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("java.policy.bak");
            }
        });
        for (File backup : previousBackups) {
            ServerAccess.logErrorReprint("Warning: found previous policy file backup at " + backup);
        }
    }

    @Before
    public void backupPolicy() throws Exception {
        if (policy.isFile()) {
            policyBackup = File.createTempFile("java.policy.bak", null, policy.getParentFile());
            if (!policy.renameTo(policyBackup)) {
                ServerAccess.logErrorReprint("Could not back up existing policy file");
                throw new RuntimeException("Could not back up existing policy file");
            }
        }

    }

    @After
    public void restorePolicy() {
        policy.delete();
        if (policyBackup != null && policyBackup.isFile()) {
            policyBackup.renameTo(policy);
        }
    }

    private void writePolicy() throws IOException {
        FileWriter out = new FileWriter(policy);
        try {
            String policyText="grant {\n  permission java.util.PropertyPermission \"user.home\", \"read\";\n};\n";
            out.write(policyText, 0, policyText.length());
        } finally {
            out.close();
        }
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testHtmlLaunchWithPolicy() throws Exception {
        writePolicy();
        assertPolicyExists();
        RulesFolowingClosingListener listener = new RulesFolowingClosingListener();
        listener.addContainsRule("CustomPolicies applet read:");
        ProcessResult pr = server.executeBrowser("CustomPolicies.html", listener, null);
        assertInit(pr);
        assertReadProps(pr);
        assertNoAccessControlException(pr);
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn={Browsers.one})
    public void testHtmlJnlpHrefLaunchWithPolicy() throws Exception {
        writePolicy();
        assertPolicyExists();
        RulesFolowingClosingListener listener = new RulesFolowingClosingListener();
        listener.addContainsRule("CustomPolicies applet read:");
        ProcessResult pr = server.executeBrowser("CustomPoliciesJnlpHref.html", listener, null);
        assertInit(pr);
        assertReadProps(pr);
        assertNoAccessControlException(pr);
    }

    @Test
    public void testJnlpAppletLaunchWithPolicy() throws Exception {
        writePolicy();
        assertPolicyExists();
        ProcessResult pr = server.executeJavawsHeadless("CustomPoliciesApplet.jnlp");
        assertInit(pr);
        assertReadProps(pr);
        assertNoAccessControlException(pr);
    }

    @Test
    public void testJnlpApplicationLaunchWithPolicy() throws Exception {
        writePolicy();
        assertPolicyExists();
        ProcessResult pr = server.executeJavawsHeadless("CustomPoliciesApplication.jnlp");
        assertInit(pr);
        assertReadProps(pr);
        assertNoAccessControlException(pr);
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    public void testHtmlLaunch() throws Exception {
        assertNoPolicyExists();
        RulesFolowingClosingListener listener = new RulesFolowingClosingListener();
        listener.addContainsRule("CustomPolicies applet read:");
        ProcessResult pr = server.executeBrowser("CustomPolicies.html", listener, null);
        assertInit(pr);
        assertAccessControlException(pr);
    }

    @NeedsDisplay
    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    public void testHtmlJnlpHrefLaunch() throws Exception {
        assertNoPolicyExists();
        RulesFolowingClosingListener listener = new RulesFolowingClosingListener();
        listener.addContainsRule("CustomPolicies applet read:");
        ProcessResult pr = server.executeBrowser("CustomPoliciesJnlpHref.html", listener, null);
        assertInit(pr);
        assertAccessControlException(pr);
    }

    @Test
    public void testJnlpAppletLaunch() throws Exception {
        assertNoPolicyExists();
        ProcessResult pr = server.executeJavawsHeadless("CustomPoliciesApplet.jnlp");
        assertInit(pr);
        assertAccessControlException(pr);
    }

    @Test
    public void testJnlpApplicationLaunch() throws Exception {
        assertNoPolicyExists();
        ProcessResult pr = server.executeJavawsHeadless("CustomPoliciesApplication.jnlp");
        assertInit(pr);
        assertAccessControlException(pr);
    }

    private void assertAccessControlException(ProcessResult pr) {
        assertTrue("Applet should not have been able to read user.home", pr.stdout.contains("AccessControlException: access denied"));
    }

    private void assertPolicyExists() {
        assertTrue("A user policy file should be installed", policy.isFile());
    }

    private void assertNoPolicyExists() {
        assertFalse("A user policy file should not be installed", policy.isFile());
    }

    private void assertInit(ProcessResult pr) {
        assertTrue("Applet should have initialized", pr.stdout.contains("CustomPolicies applet read:"));
    }

    private void assertReadProps(ProcessResult pr) {
        assertTrue("stdout should contain user.home", pr.stdout.contains(System.getProperty("user.home")));
    }

    private void assertNoAccessControlException(ProcessResult pr) {
        assertFalse("Applet should have been able to read user.home", pr.stdout.contains("AccessControlException: access denied"));
    }

}
