/* DeadLockTestTest.java
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

import java.util.ArrayList;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.BeforeClass;

import org.junit.Test;

public class DeadLockTestTest {

    private static ServerAccess server = new ServerAccess();
    private static String deadlocktest_1 = "/deadlocktest_1.jnlp";
    private static String deadlocktest = "/deadlocktest.jnlp";

    @BeforeClass
    public static void printJavas() throws Exception {
        ServerAccess.logOutputReprint("Currently runnng javas1 " + countJavaInstances());

    }

    @Test
    public void testDeadLockTestTerminated() throws Exception {
        testDeadLockTestTerminatedBody(deadlocktest);
        testDeadLockTestTerminatedBody(deadlocktest);
        ServerAccess.logOutputReprint("Currently running javas12 " + countJavaInstances());
    }

    @Test
    public void testDeadLockTestTerminated2() throws Exception {
        testDeadLockTestTerminatedBody(deadlocktest_1);
        testDeadLockTestTerminatedBody(deadlocktest_1);
        /**
         * this happens, when p.p.destroy is called before p.interrupt. and destroyed variable is removedI have no idea why, but it is incorrect.
        Assert.assertNotNull("return can not be null in no fork process. Was ",pr.returnValue);//in this case forking is forbiden, and sojava throws an exception after destroy
         */
        ServerAccess.logOutputReprint("Currently running javas13 " + countJavaInstances());
    }

    public void testDeadLockTestTerminatedBody(String jnlp) throws Exception {
        List<String> before = countJavaInstances();
        ServerAccess.logOutputReprint("java1 " + jnlp + " : " + before.size());
        ProcessResult pr = server.executeJavawsHeadless(null, jnlp);
        assertDeadlockTestLaunched(pr);
        List<String> after = countJavaInstances();
        ServerAccess.logOutputReprint("java2 " + jnlp + " : " + after.size());
        killDiff(before, after);
        String ss = "This process is hanging more than 30s. Should be killed";
        Assert.assertFalse("stdout should not contains: " + ss + ", but did", pr.stdout.contains(ss));
//        as we are tryng to terminate process as harmless as possible those two are no longer valid in all cases
//        Assert.assertTrue("testDeadLockTestTerminated should be terminated, but wasn't", pr.wasTerminated);
//        Assert.assertNull("Killed process must have null return value. Have not - ", pr.returnValue);
        List<String> afterKill = countJavaInstances();
        ServerAccess.logOutputReprint("java3 " + jnlp + " : " + afterKill.size());
        Assert.assertEquals("assert that just old javas remians", 0, (before.size() - afterKill.size()));
    }

    @Test
    public void ensureAtLeasOneJavaIsRunning() throws Exception {
        Assert.assertTrue("at least one java should be running, but isn't! Javas are probably counted badly", countJavaInstances().size() > 0);

    }

    @Test
    public void testSimpletest1lunchFork() throws Exception {
        List<String> before = countJavaInstances();
        ServerAccess.logOutputReprint("java4: " + before.size());
        BackgroundDeadlock bd = new BackgroundDeadlock(deadlocktest_1, null);
        bd.start();
        Thread.sleep(ServerAccess.PROCESS_TIMEOUT * 2 / 3);
        List<String> during = countJavaInstances();
        ServerAccess.logOutputReprint("java5: " + during.size());
        waitForBackgroundDeadlock(bd);
        List<String> after = countJavaInstances();
        ServerAccess.logOutputReprint("java6: " + after.size());
        Assert.assertNotNull("proces inside background deadlock cant be null. It was.", bd.getPr());
        assertDeadlockTestLaunched(bd.getPr());
        killDiff(before, during);
        List<String> afterKill = countJavaInstances();
        ServerAccess.logOutputReprint("java66: " + afterKill.size());
        Assert.assertEquals("assert that just old javas remians", 0, (before.size() - afterKill.size()));
        // div by two is caused by jav in java process hierarchy
        Assert.assertEquals("launched JVMs must be exactly 2, was " + (during.size() - before.size()), 2, (during.size() - before.size()));
    }

    @Test
    public void testSimpletest1lunchNoFork() throws Exception {
        List<String> before = countJavaInstances();
        ServerAccess.logOutputReprint("java7: " + before.size());
        BackgroundDeadlock bd = new BackgroundDeadlock(deadlocktest_1, Arrays.asList(new String[]{"-Xnofork"}));
        bd.start();
        Thread.sleep(ServerAccess.PROCESS_TIMEOUT * 2 / 3);
        List<String> during = countJavaInstances();
        ServerAccess.logOutputReprint("java8: " + during.size());
        waitForBackgroundDeadlock(bd);
        List<String> after = countJavaInstances();
        ServerAccess.logOutputReprint("java9: " + after.size());
        Assert.assertNotNull("proces inside background deadlock cant be null. It was.", bd.getPr());
        assertDeadlockTestLaunched(bd.getPr());
        killDiff(before, during);
        List<String> afterKill = countJavaInstances();
        ServerAccess.logOutputReprint("java99: " + afterKill.size());
        Assert.assertEquals("assert that just old javas remians", 0, (before.size() - afterKill.size()));
        // div by two is caused by jav in java process hierarchy
        Assert.assertEquals("launched JVMs must be exactly 1, was  " + (during.size() - before.size()), 1, (during.size() - before.size()));
    }

    /**
     * by process assasin destroyed processes are hanging random amount of time as zombies.
     * Kill -9 is handling zombies pretty well.
     *
     * This function kills or  processes which are in nw but are not in old
     * (eq.to killing new zombies:) )
     *
     * @param old
     * @param nw
     * @return
     * @throws Exception
     */
    private static List<String> killDiff(List<String> old, List<String> nw) throws Exception {
        ensureLinux();
        List<String> result = new ArrayList<String>();
        for (String string : nw) {
            if (old.contains(string)) {
                continue;
            }
            ServerAccess.logOutputReprint("Killing " + string);
            ServerAccess.PROCESS_LOG = false;
            try {
                ProcessResult pr = ServerAccess.executeProcess(Arrays.asList(new String[]{"kill", "-9", string}));
            } finally {
                ServerAccess.PROCESS_LOG = true;
            }
            result.add(string);
            ServerAccess.logOutputReprint("Killed " + string);
        }
        return result;
    }

    private static List<String> countJavaInstances() throws Exception {
        ensureLinux();
        List<String> result = new ArrayList<String>();
        ServerAccess.PROCESS_LOG = false;
        try {
            ProcessResult pr = ServerAccess.executeProcess(Arrays.asList(new String[]{"ps", "-eo", "pid,ppid,stat,fname"}));
            Matcher m = Pattern.compile("\\s*\\d+\\s+\\d+ .+ java\\s*").matcher(pr.stdout);
            int i = 0;
            while (m.find()) {
                i++;
                String ss = m.group();
                //ServerAccess.logOutputReprint(i+": "+ss);
                result.add(ss.trim().split("\\s+")[0]);
            }
        } finally {
            ServerAccess.PROCESS_LOG = true;
        }
        return result;

    }

    public static void main(String[] args) throws Exception {
        ServerAccess.logOutputReprint("" + countJavaInstances());
    }

    private void assertDeadlockTestLaunched(ProcessResult pr) {
        String s = "Deadlock test started";
        Assert.assertTrue("Deadlock test should print out " + s + ", but did not", pr.stdout.contains(s));
        //each 3500 seconds deadlock test stdout something
        //timeout is 20s
        //so it should write out FIVE sentences, but is mostly just three or four. Last is nearly always consumed by termination
        for (int i = 1; i <= 3; i++) {
            String sentence = i + " Deadlock sleeping";
            Assert.assertTrue(
                    "stdout should contains: " + sentence + ", didn't, so framework have consumed to much during termination",
                    pr.stdout.contains(sentence));
        }
    }

    private void waitForBackgroundDeadlock(final BackgroundDeadlock bd) throws InterruptedException {
        while (!bd.isFinished()) {
            Thread.sleep(500);

        }
    }

    private static class BackgroundDeadlock extends Thread {

        private boolean finished = false;
        private ProcessResult pr = null;
        String jnlp;
        List<String> args;

        public BackgroundDeadlock(String jnlp, List<String> args) {
            this.jnlp = jnlp;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                pr = server.executeJavawsHeadless(args, jnlp);
            } catch (Exception ex) {
                ServerAccess.logException(ex);
            } finally {
                finished = true;
            }

        }

        public ProcessResult getPr() {
            return pr;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    private static void ensureLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!(os.contains("linux") || os.contains("unix"))) {
            throw new IllegalStateException("This test can be procesed only on linux like machines");
        }
    }
}
