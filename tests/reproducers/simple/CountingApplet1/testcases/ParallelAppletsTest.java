/* ParallelAppletsTest.java
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
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import org.junit.Assert;

import org.junit.Test;

public class ParallelAppletsTest extends BrowserTest {

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1Ex2s() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1EE_x_2s.html");
        checkSimpleSignedStarted(pr);
        checkNotInitialised(pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1x2E() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1_x_2EE.html");
        checkExactCounts(1, 10, pr);
        checkNotInitialised(pr);

    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1x2e() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1_x_2e.html");
        checkExactCounts(1, 10, pr);
        checkException(pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1ex2s() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1e_x_2s.html");
        checkSimpleSignedStarted(pr);
        checkException(pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1sx2() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1s_x_2.html");
        checkAppletStarted(pr);
        checkSimpleSignedStarted(pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1sx2s() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1s_x_2s.html");
        int found=countCounts(SimpleSignedStarted, pr.stdout);
        assertExactCount(SimpleSignedStarted, 2, found);


    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1sx2ssk() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1s_x_2ss.html");
        checkSimpleSigned2Started(pr);
        checkSimpleSignedStarted(pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1x2sk() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1_x_2sk.html");
        checkExitNotAllowed(pr);
        checkAtLeastCounts(1, 10, pr);
        checkExactCounts(2, 5, pr);

    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1kx2() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1k_x_2.html");
        checkExitNotAllowed(pr);
        checkAtLeastCounts(1, 10, pr);
        checkExactCounts(2, 5, pr);

    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1x2() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1_x_2.html");
        checkExactCounts(2, 10, pr);
    }

    @Test
    @TestInBrowsers(testIn = {Browsers.one})
    @NeedsDisplay
    public void testParallelAppletsTest1x1() throws Exception {
        ProcessResult pr = server.executeBrowser("ParallelAppletsTest_1_x_1.html");
        checkExactCounts(2, 10, pr);
    }
    private static final String ACE = "java.security.AccessControlException";
    private static final String Sexit = "System.exit()";
    private static final String LE1 = "net.sourceforge.jnlp.LaunchException";
    private static final String LE2 = "Fatal: Initialization Error";
    private static final String Cinit = "Could not initialize applet";
    private static final String CountStub = "counting... ";
    private static final String SimpleSignedStarted = "AppletTestSigned was started";
    private static final String SimpleSigned2Started = "AppletTestSigned2 was started";
    private static final String AppletStarted = "applet was started";
    private static final String AppletThrowedException = "java.lang.RuntimeException: Correct exception";




    private void checkExitNotAllowed(ProcessResult pr) {
        Assert.assertTrue("Applets cant call " + Sexit, pr.stderr.matches("(?s).*" + ACE + ".*" + Sexit + ".*"));
    }

    private void checkNotInitialised(ProcessResult pr) {
        Assert.assertTrue("Applets should not be initialised ", pr.stderr.matches("(?s).*" + LE1 + ".*" + LE2 + ".*" + Cinit + ".*"));
    }

    private void checkSimpleSignedStarted(ProcessResult pr) {
        Assert.assertTrue("Applet's start should be confirmed by " + SimpleSignedStarted, pr.stdout.contains(SimpleSignedStarted));
    }
    private void checkSimpleSigned2Started(ProcessResult pr) {
        Assert.assertTrue("Applet's start should be confirmed by " + SimpleSigned2Started, pr.stdout.contains(SimpleSigned2Started));
    }
    private void checkAppletStarted(ProcessResult pr) {
        Assert.assertTrue("Applet's start should be confirmed by " + AppletStarted, pr.stdout.contains(AppletStarted));
    }
      private void checkException(ProcessResult pr) {
         Assert.assertTrue("Applet's exception should be confirmed by " + AppletThrowedException, pr.stderr.contains(AppletThrowedException));
    }

    private void checkExactCounts(int howManyTimes, int countIdTill, ProcessResult pr) {
        for (int i = 0; i <= countIdTill; i++) {
            String countId = CountStub + i+"\n";
            int found = countCounts(countId, pr.stdout);
            assertExactCount(countId, howManyTimes, found);
        }

    }

    private void assertExactCount(String what, int howManyTimes, int found) {
        Assert.assertEquals(what + " was expected exactly " + howManyTimes + " but was found " + found, howManyTimes, found);
    }

    private void checkAtLeastCounts(int howManyTimes, int countIdTill, ProcessResult pr) {
        for (int i = 0; i <= countIdTill; i++) {
            String countId = CountStub + i;
            int found = countCounts(countId, pr.stdout);
            Assert.assertTrue(countId + " was expected et least " + howManyTimes + " but was found " + found, found >= howManyTimes);
        }

    }

    private int countCounts(String what, String where) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = where.indexOf(what, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += what.length();
            }
        }
        return count;

    }


}
