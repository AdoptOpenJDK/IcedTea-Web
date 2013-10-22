/*Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Random;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.util.StreamUtils;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OutputControllerTest {

    private static String line1 = "I'm logged line one";
    private static String line2 = "I'm logged line two";
    private static String line3 = "I'm logged line three";
    private static String line4 = "I'm logged line four";
    private static String line5 = "I'm logged line five";
    private static String line6 = "I'm logged line six";
    private static RulesFolowingClosingListener.ContainsRule r1 = new RulesFolowingClosingListener.ContainsRule(line1);
    private static RulesFolowingClosingListener.ContainsRule r2 = new RulesFolowingClosingListener.ContainsRule(line2);
    private static RulesFolowingClosingListener.ContainsRule r3 = new RulesFolowingClosingListener.ContainsRule(line3);
    private static RulesFolowingClosingListener.ContainsRule r4 = new RulesFolowingClosingListener.ContainsRule(line4);
    private static RulesFolowingClosingListener.ContainsRule r5 = new RulesFolowingClosingListener.ContainsRule(line5);
    private static RulesFolowingClosingListener.ContainsRule r6 = new RulesFolowingClosingListener.ContainsRule(line6);

    private static class AccessiblePrintStream extends PrintStream {

        public AccessiblePrintStream(ByteArrayOutputStream out) {
            super(out);
        }

        public ByteArrayOutputStream getOut() {
            return (ByteArrayOutputStream) out;
        }
    }

    @Before
    public void setUp() {
        LogConfig.resetLogConfig();
    }

    @After
    public void tearDown() {
        LogConfig.resetLogConfig();
    }

    @Test
    public void isLoggingStdStreams() throws Exception {
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(new PrintStream(os1), new PrintStream(os2));
        LogConfig.getLogConfig().setEnableLogging(false);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        oc.log(OutputController.Level.MESSAGE_DEBUG, line1);
        oc.log(OutputController.Level.ERROR_DEBUG, line1);
        oc.flush();
        Assert.assertFalse(r1.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r1.evaluate(os2.toString("utf-8")));
        oc.log(OutputController.Level.MESSAGE_ALL, line1);
        oc.log(OutputController.Level.ERROR_DEBUG, line1);
        oc.flush();
        Assert.assertTrue(r1.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r1.evaluate(os2.toString("utf-8")));
        oc.log(OutputController.Level.ERROR_ALL, line1);
        oc.flush();
        Assert.assertTrue(r1.evaluate(os2.toString("utf-8")));

        LogConfig.getLogConfig().setEnableLogging(true);
        oc.log(OutputController.Level.MESSAGE_DEBUG, line2);
        oc.flush();
        Assert.assertTrue(r2.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r2.evaluate(os2.toString("utf-8")));
        oc.log(OutputController.Level.ERROR_DEBUG, line2);
        oc.flush();
        Assert.assertTrue(r2.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(os2.toString("utf-8")));

        oc.log(OutputController.Level.ERROR_DEBUG, line3);
        oc.flush();
        Assert.assertFalse(r3.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r3.evaluate(os2.toString("utf-8")));
        oc.log(OutputController.Level.MESSAGE_DEBUG, line3);
        oc.flush();
        Assert.assertTrue(r3.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r3.evaluate(os2.toString("utf-8")));

        LogConfig.getLogConfig().setEnableLogging(false);
        oc.log(OutputController.Level.WARNING_DEBUG, line4);
        oc.flush();
        Assert.assertFalse(r4.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r4.evaluate(os2.toString("utf-8")));
        oc.log(OutputController.Level.WARNING_ALL, line5);
        oc.flush();
        Assert.assertTrue(r5.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r5.evaluate(os2.toString("utf-8")));
        LogConfig.getLogConfig().setEnableLogging(true);
        oc.log(OutputController.Level.WARNING_DEBUG, line4);
        oc.flush();
        Assert.assertTrue(r4.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r4.evaluate(os2.toString("utf-8")));

    }
    private static final Random random = new Random();
    private int delayable = 0;

    private class IdedRunnable implements Runnable {

        private final int id;
        private final OutputController oc;
        private boolean done = false;
        private final int iterations;

        public IdedRunnable(int id, OutputController oc, int iterations) {
            this.id = id;
            this.oc = oc;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            for (int i = 0; i < iterations; i++) {
                try {
                    //be sure this pattern is kept in assers
                    oc.log(OutputController.Level.WARNING_ALL, "thread " + id + " line " + i);
                    Thread.sleep(random.nextInt(delayable));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            done = true;
        }

        public boolean isDone() {
            return done;
        }
    }

    /**
     * todo - include syslog once implemented
     */
    @Test
    public void isParalelLogingWorking() throws Exception {
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        String s = "";
        //this was tested with  1-100 iterations and 100 threads. But can couse OutOfMemoryError
        int maxi = 90;
        int minits = 70;
        int maxt = 10;
        //tested with delayable 1-10, but took minutes then
        for (delayable = 1; delayable < 1; delayable++) {
            for (int iterations = minits; iterations < maxi; iterations++) {
                for (int threads = 1; threads < maxt; threads++) {
                    LogConfig.getLogConfig().setLogToFile(false);
                    System.gc();
                    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
                    ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                    OutputController oc = new OutputController(new PrintStream(os1), new PrintStream(os2));

                    File f = File.createTempFile("replacedFilelogger", "itwTest");
                    f.deleteOnExit();
                    oc.setFileLog(new FileLog(f.getAbsolutePath(), false));
                    LogConfig.getLogConfig().setLogToFile(true);

                    ThreadGroup tg = new ThreadGroup("TerribleGroup");
                    IdedRunnable[] idedRunnables = new IdedRunnable[threads];
                    Thread[] xt = new Thread[threads];
                    for (int i = 0; i < threads; i++) {
                        Thread.sleep(random.nextInt(delayable));
                        idedRunnables[i] = new IdedRunnable(i, oc, iterations);
                        xt[i] = new Thread(tg, idedRunnables[i], "iterations = " + iterations + "; threads = " + threads + "; delayable = " + delayable);
                        xt[i].start();
                    }
                    while (true) {
                        boolean ok = true;
                        for (IdedRunnable idedRunnable : idedRunnables) {
                            if (!idedRunnable.isDone()) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            break;
                        }
                    }
                    oc.flush();
                    String s1 = os1.toString("utf-8");
                    String s2 = os2.toString("utf-8");
                    String s3 = StreamUtils.readStreamAsString(new FileInputStream(f), true);
                    for (int i = minits; i < maxi; i++) {
                        for (int t = 0; t < maxt; t++) {
                            //be sure this pattern is kept in IdedRunnable
                            String expected = "thread " + t + " line " + i;
                            if (i >= iterations || t >= threads) {
                                Assert.assertFalse(s1.contains(expected));
                                Assert.assertFalse(s2.contains(expected));
                                Assert.assertFalse(s3.contains(expected));
                            } else {
                                Assert.assertTrue(s1.contains(expected));
                                Assert.assertTrue(s2.contains(expected));
                                Assert.assertTrue(s3.contains(expected));
                            }

                        }
                    }
                    tg.destroy();

                }
            }
        }



    }

    @Test
    public void isChangingOfStreasmWorking() throws Exception {
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(new PrintStream(os1), new PrintStream(os2));
        oc.log(OutputController.Level.MESSAGE_ALL, line1);
        oc.log(OutputController.Level.ERROR_ALL, line1);
        oc.flush();
        ByteArrayOutputStream os3 = new ByteArrayOutputStream();
        ByteArrayOutputStream os4 = new ByteArrayOutputStream();
        oc.setOut(new PrintStream(os3));
        oc.log(OutputController.Level.MESSAGE_ALL, line2);
        oc.log(OutputController.Level.ERROR_ALL, line2);
        oc.flush();
        oc.setErr(new PrintStream(os4));
        oc.log(OutputController.Level.MESSAGE_ALL, line3);
        oc.log(OutputController.Level.ERROR_ALL, line3);
        oc.flush();

        Assert.assertTrue(r1.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r1.evaluate(os2.toString("utf-8")));
        Assert.assertFalse(r1.evaluate(os3.toString("utf-8")));
        Assert.assertFalse(r1.evaluate(os4.toString("utf-8")));


        Assert.assertFalse(r2.evaluate(os1.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(os2.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(os3.toString("utf-8")));
        Assert.assertFalse(r2.evaluate(os4.toString("utf-8")));

        Assert.assertFalse(r3.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r3.evaluate(os2.toString("utf-8")));
        Assert.assertTrue(r3.evaluate(os3.toString("utf-8")));
        Assert.assertTrue(r3.evaluate(os4.toString("utf-8")));

        LogConfig.getLogConfig().setLogToStreams(false);

        oc.log(OutputController.Level.MESSAGE_ALL, line4);
        oc.log(OutputController.Level.ERROR_ALL, line4);

        Assert.assertFalse(r4.evaluate(os1.toString("utf-8")));
        Assert.assertFalse(r4.evaluate(os2.toString("utf-8")));
        Assert.assertFalse(r4.evaluate(os3.toString("utf-8")));
        Assert.assertFalse(r4.evaluate(os4.toString("utf-8")));

    }

    @Test
    public void isFileLoggerWorking() throws Exception {
        String s1 = "";
        String s2 = "";
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(false);
        LogConfig.getLogConfig().setLogToSysLog(false);

        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(new PrintStream(os1), new PrintStream(os2));
        File f1 = File.createTempFile("replacedFilelogger", "itwTest");
        File f2 = File.createTempFile("replacedFilelogger", "itwTest");
        f1.deleteOnExit();
        f2.deleteOnExit();
        oc.setFileLog(new FileLog(f1.getAbsolutePath(), false));
        LogConfig.getLogConfig().setLogToFile(true);
        oc.log(OutputController.Level.MESSAGE_ALL, line1);
        oc.log(OutputController.Level.ERROR_ALL, line2);
        oc.log(OutputController.Level.MESSAGE_ALL, line3);
        oc.flush();
        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertTrue(r1.evaluate(s1));
        Assert.assertFalse(r1.evaluate(s2));
        Assert.assertTrue(r2.evaluate(s1));
        Assert.assertFalse(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s1));
        Assert.assertFalse(r3.evaluate(s2));

        oc.setFileLog(new FileLog(f2.getAbsolutePath(), false));
        oc.log(OutputController.Level.ERROR_ALL, line5);
        oc.log(OutputController.Level.MESSAGE_ALL, line5);
        oc.flush();

        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertTrue(r1.evaluate(s1));
        Assert.assertFalse(r1.evaluate(s2));
        Assert.assertTrue(r2.evaluate(s1));
        Assert.assertFalse(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s1));
        Assert.assertFalse(r3.evaluate(s2));

        Assert.assertFalse(r5.evaluate(s1));
        Assert.assertTrue(r5.evaluate(s2));

        LogConfig.getLogConfig().setLogToFile(false);
        oc.log(OutputController.Level.ERROR_ALL, line6);
        oc.log(OutputController.Level.MESSAGE_ALL, line6);
        oc.flush();
        
        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertFalse(r6.evaluate(s1));
        Assert.assertFalse(r6.evaluate(s2));


    }

    /**
     * add syslog once implemented
     */
    @Test
    public void isSysLoggerWorking() throws Exception {
    }
}
