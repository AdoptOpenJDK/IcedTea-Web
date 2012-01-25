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

import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.ProcessResult;
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
        System.out.println("Currently runnng javas1 " + countJavaInstances());

    }

    @Test
    public void testDeadLockTestTerminated() throws Exception {
        testDeadLockTestTerminatedBody(deadlocktest);
        System.out.println("Currently runnng javas2 " + countJavaInstances());
    }

    @Test
    public void testDeadLockTestTerminated2() throws Exception {
        testDeadLockTestTerminatedBody(deadlocktest_1);
        System.out.println("Currently runnng javas3 " + countJavaInstances());
    }

    public void testDeadLockTestTerminatedBody(String jnlp) throws Exception {
        System.out.println("connecting " + jnlp + " request");
        System.err.println("connecting " + jnlp + " request");
        ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, jnlp);
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        assertDeadlockTestLaunched(pr);
        Assert.assertFalse(pr.stdout.contains("This process is hanging more then 30s. Should be killed"));
//        Assert.assertTrue(pr.stderr.contains("xception"));, exception is thrown by engine,not by application
        Assert.assertTrue("testDeadLockTestTerminated should be terminated, but wasn't", pr.wasTerminated);
        Assert.assertEquals(null, pr.returnValue);//killed process have no value
    }

    @Test
    public void ensureAtLeasOneJavaIsRunning() throws Exception {
        Assert.assertTrue("at least one java should be running, but isnt! Javas are probably counted badly", countJavaInstances() > 0);
        ;

    }

    @Test
    public void testSimpletest1lunchFork() throws Exception {
        System.out.println("connecting " + deadlocktest_1 + " request");
        System.err.println("connecting " + deadlocktest_1 + " request");
        int before = countJavaInstances();
        System.out.println("java4: " + before);
        BackgroundDeadlock bd = new BackgroundDeadlock(deadlocktest_1, null);
        bd.start();
        Thread.sleep(ServerAccess.PROCESS_TIMEOUT * 2 / 3);
        int during = +countJavaInstances();
        System.out.println("java5: " + during);
        waitForBackgroundDeadlock(bd);
        Thread.sleep(500);
        int after = countJavaInstances();
        System.out.println("java6: " + after);
        Assert.assertNotNull("proces inside background deadlock cant be null. Was.", bd.getPr());
        System.out.println(bd.getPr().stdout);
        System.err.println(bd.getPr().stderr);
        assertDeadlockTestLaunched(bd.getPr());
        Assert.assertEquals("lunched JVMs must be exactly 2, was " + (during - before), 2, during - before);
    }

    @Test
    public void testSimpletest1lunchNoFork() throws Exception {
        System.out.println("connecting " + deadlocktest_1 + " Xnofork request");
        System.err.println("connecting " + deadlocktest_1 + " Xnofork request");
        int before = countJavaInstances();
        System.out.println("java7: " + before);
        BackgroundDeadlock bd = new BackgroundDeadlock(deadlocktest_1, Arrays.asList(new String[]{"-Xnofork"}));
        bd.start();
        Thread.sleep(ServerAccess.PROCESS_TIMEOUT * 2 / 3);
        int during = +countJavaInstances();
        System.out.println("java8: " + during);
        waitForBackgroundDeadlock(bd);
        Thread.sleep(500);
        int after = countJavaInstances();
        System.out.println("java9: " + after);
        Assert.assertNotNull("proces inside background deadlock cant be null. Was.", bd.getPr());
        System.out.println(bd.getPr().stdout);
        System.err.println(bd.getPr().stderr);
        assertDeadlockTestLaunched(bd.getPr());
        Assert.assertEquals("lunched JVMs must be exactly 1, was  " + (during - before), 1, during - before);
        ;
    }

    private static int countJavaInstances() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (!(os.contains("linux") || os.contains("unix"))) {
            throw new IllegalStateException("This test can be procesed only on linux like machines");
        }
        ServerAccess.ProcessResult pr = ServerAccess.executeProcess(Arrays.asList(new String[]{"ps", "-A"}));
        Matcher m = Pattern.compile("\\s+java\\s+").matcher(pr.stdout);
        //System.out.println(pr.stdout);
        int i = 0;
        while (m.find()) {
            i++;
        }
        return i;

    }

    private void assertDeadlockTestLaunched(ProcessResult pr) {
        String s = "Deadlock test started";
        Assert.assertTrue("Deadlock test should print out " + s + ", but did not", pr.stdout.contains(s));
        String ss = "xception";
        Assert.assertFalse("Deadlock test should not stderr " + ss + " but did", pr.stderr.contains(ss));
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
                ex.printStackTrace();
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
}
