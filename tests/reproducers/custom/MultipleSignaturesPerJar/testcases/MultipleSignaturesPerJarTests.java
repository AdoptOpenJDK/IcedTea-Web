/* MultipleSignaturesTestTests.java
Copyright (C) 20121 Red Hat, Inc.

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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Test;

@Bug(id = { "PR822" })
public class MultipleSignaturesPerJarTests extends BrowserTest {
    private final List<String> TRUST_ALL = Collections.unmodifiableList(Arrays.asList(new String[] { "-Xtrustall" }));

    public static final String CORRECT_FINISH = "Test has finished.";
    public static final String CNFEXCEPTION = "ClassNotFoundException";
    public static final String DIFF_CERTS_EXCEPTION = "Fatal: Application Error: The JNLP application is not fully signed by a single cert.";
    public static final String ACEXCEPTION = "java.security.AccessControlException: access denied";

    @Test
    @NeedsDisplay
    public void multipleSignaturesPerJarMatchingJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(TRUST_ALL, "/MultipleSignaturesPerJarMatching.jnlp");
        // Assert relevant exceptions did not occur
        assertFalse("stderr should NOT contain `" + CNFEXCEPTION + "`, but did",
                pr.stderr.contains(CNFEXCEPTION));
        assertFalse("stderr should NOT contain `" + ACEXCEPTION + "`, but did",
                pr.stderr.contains(ACEXCEPTION));
        assertFalse("stderr should NOT contain `" + DIFF_CERTS_EXCEPTION + "`, but did",
                pr.stderr.contains(DIFF_CERTS_EXCEPTION));

        // Assert that we correctly finish
        assertTrue("stdout should contain `" + CORRECT_FINISH + "`, but did not",
                pr.stdout.contains(CORRECT_FINISH));
    }

    @Test
    @NeedsDisplay
    public void multipleSignaturesPerJarMismatchingJNLP() throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(TRUST_ALL, "/MultipleSignaturesPerJarMismatching.jnlp");
        // Assert only for the expected exception
        assertTrue("stderr should contain `" + DIFF_CERTS_EXCEPTION + "`, but did not",
                pr.stderr.contains(DIFF_CERTS_EXCEPTION));

        // Assert that we did not correctly finish
        assertFalse("stdout should NOT contain " + CORRECT_FINISH + " but did",
                pr.stdout.contains(CORRECT_FINISH));
    }

    private static void testForCorrectAppletExecution(ProcessResult pr) {

        // Assert relevant exceptions did not occur
        assertFalse("stderr should NOT contain `" + CNFEXCEPTION + "`, but did",
                pr.stderr.contains(CNFEXCEPTION));
        assertFalse("stderr should NOT contain `" + ACEXCEPTION + "`, but did",
                pr.stderr.contains(ACEXCEPTION));
        assertFalse("stderr should NOT contain `" + DIFF_CERTS_EXCEPTION + "`, but did",
                pr.stderr.contains(DIFF_CERTS_EXCEPTION));

        // Assert that we correctly finish
        // It is difficult to check for user.home's value here, so we only check for the ending message:
        assertTrue("stdout should contain `" + CORRECT_FINISH + "`, but did not",
                pr.stdout.contains(CORRECT_FINISH));
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    @Bug(id = { "PR822" })
    public void multipleSignaturesPerJarMismatchingApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/MultipleSignaturesPerJarMismatching.html", AutoClose.CLOSE_ON_CORRECT_END);
        // NB: Both this and the matching applet should pass
        // Unlike JNLPs, applets pass as long as all their parts are signed by *something*
        testForCorrectAppletExecution(pr);
    }

    @Test
    @NeedsDisplay
    @TestInBrowsers(testIn = Browsers.one)
    public void multipleSignaturesPerJarMatchingApplet() throws Exception {
        ProcessResult pr = server.executeBrowser("/MultipleSignaturesPerJarMatching.html", AutoClose.CLOSE_ON_CORRECT_END);
        testForCorrectAppletExecution(pr);
    }

}