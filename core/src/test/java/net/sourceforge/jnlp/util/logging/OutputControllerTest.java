/*Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.filelogs.WriterBasedFileLog;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class OutputControllerTest {

    private static boolean originalDebug;
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    private static final String line1 = "I'm logged line one";
    private static final String line2 = "I'm logged line two";
    private static final String line3 = "I'm logged line three";
    private static final String line4 = "I'm logged line four";
    private static final String line5 = "I'm logged line five";
    private static final String line6 = "I'm logged line six";

    @Before
    public void setUp() {
        originalDebug = JNLPRuntime.isSetDebug();
        LogConfig.resetLogConfig();
    }

    @After
    public void tearDown() {
        JNLPRuntime.setDebug(originalDebug);
        LogConfig.resetLogConfig();
        OutputController.getLogger().setInOutErrController(StdInOutErrController.getInstance());
    }

    @Test
    public void isLoggingStdStreams() throws Exception {
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(os1, os2);
        JNLPRuntime.setDebug(false);
        LogConfig.getLogConfig().setEnableLogging(false);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        oc.log(msg(OutputControllerLevel.DEBUG, line1));
        oc.flush();
        Assert.assertFalse((os1.toString(UTF_8).contains(line1)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line1)));
        oc.log(msg(OutputControllerLevel.INFO, line1));
        oc.flush();
        Assert.assertTrue((os1.toString(UTF_8).contains(line1)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line1)));
        oc.log(msg(OutputControllerLevel.ERROR, line1));
        oc.flush();
        Assert.assertTrue((os2.toString(UTF_8).contains(line1)));

        LogConfig.getLogConfig().setEnableLogging(true);
        oc.log(msg(OutputControllerLevel.DEBUG, line2));
        oc.flush();
        Assert.assertTrue((os1.toString(UTF_8).contains(line2)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line2)));
        oc.flush();
        Assert.assertTrue((os1.toString(UTF_8).contains(line2)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line2)));

        oc.log(msg(OutputControllerLevel.DEBUG, line3));
        oc.flush();
        Assert.assertTrue((os1.toString(UTF_8).contains(line3)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line3)));

        LogConfig.getLogConfig().setEnableLogging(false);
        oc.log(msg(OutputControllerLevel.WARN, line5));
        oc.flush();
        Assert.assertTrue((os1.toString(UTF_8).contains(line5)));
        Assert.assertTrue((os2.toString(UTF_8).contains(line5)));
        LogConfig.getLogConfig().setEnableLogging(true);
    }

    private MessageWithHeader msg(OutputControllerLevel level, String msg) {
        return new JavaMessage(new Header(level, false), msg);
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
                    //be sure this pattern is kept in asserts
                    oc.log(msg(OutputControllerLevel.WARN, "thread " + id + " line " + i));
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
    @Ignore("hangs regularly on integration server")
    public void isParallelLoggingWorking() throws Exception {
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        //this was tested with  1-100 iterations and 100 threads. But can cause OutOfMemoryError
        int maxi = 90;
        int minits = 70;
        int maxt = 10;
        //tested with delayable 1-10, but took minutes then
        for (delayable = 1; delayable < 3; delayable++) {
            for (int iterations = minits; iterations < maxi; iterations++) {
                for (int threads = 1; threads < maxt; threads++) {
                    LogConfig.getLogConfig().setLogToFile(false);
                    System.gc();
                    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
                    ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                    OutputController oc = new OutputController(os1, os2);

                    File f = File.createTempFile("replacedFilelogger", "itwTest");
                    f.deleteOnExit();
                    oc.setFileLog(new WriterBasedFileLog(f.getAbsolutePath(), false));
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
                    String s1 = os1.toString(UTF_8);
                    String s2 = os2.toString(UTF_8);
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
    public void isChangingOfStreamsWorking() throws Exception {
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(true);
        LogConfig.getLogConfig().setLogToSysLog(false);
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(os1, os2);
        oc.log(msg(OutputControllerLevel.INFO, line1));
        oc.log(msg(OutputControllerLevel.ERROR, line1));
        oc.flush();
        ByteArrayOutputStream os3 = new ByteArrayOutputStream();
        ByteArrayOutputStream os4 = new ByteArrayOutputStream();
        oc.setInOutErrController(new StdInOutErrController(os3, os2));
        oc.log(msg(OutputControllerLevel.INFO, line2));
        oc.log(msg(OutputControllerLevel.ERROR, line2));
        oc.flush();
        oc.setInOutErrController(new StdInOutErrController(os3, os4));
        oc.log(msg(OutputControllerLevel.INFO, line3));
        oc.log(msg(OutputControllerLevel.ERROR, line3));
        oc.flush();

        Assert.assertTrue((os1.toString(UTF_8).contains(line1)));
        Assert.assertTrue((os2.toString(UTF_8).contains(line1)));
        Assert.assertFalse((os3.toString(UTF_8).contains(line1)));
        Assert.assertFalse((os4.toString(UTF_8).contains(line1)));


        Assert.assertFalse((os1.toString(UTF_8).contains(line2)));
        Assert.assertTrue((os2.toString(UTF_8).contains(line2)));
        Assert.assertTrue((os3.toString(UTF_8).contains(line2)));
        Assert.assertFalse((os4.toString(UTF_8).contains(line2)));

        Assert.assertFalse((os1.toString(UTF_8).contains(line3)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line3)));
        Assert.assertTrue((os3.toString(UTF_8).contains(line3)));
        Assert.assertTrue((os4.toString(UTF_8).contains(line3)));

        LogConfig.getLogConfig().setLogToStreams(false);

        oc.log(msg(OutputControllerLevel.INFO, line4));
        oc.log(msg(OutputControllerLevel.ERROR, line4));

        Assert.assertFalse((os1.toString(UTF_8).contains(line4)));
        Assert.assertFalse((os2.toString(UTF_8).contains(line4)));
        Assert.assertFalse((os3.toString(UTF_8).contains(line4)));
        Assert.assertFalse((os4.toString(UTF_8).contains(line4)));
    }

    @Test
    public void isFileLoggerWorking() throws Exception {
        String s1;
        String s2;
        LogConfig.getLogConfig().setEnableLogging(true);
        LogConfig.getLogConfig().setLogToFile(false);
        LogConfig.getLogConfig().setLogToStreams(false);
        LogConfig.getLogConfig().setLogToSysLog(false);

        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        OutputController oc = new OutputController(os1, os2);
        File f1 = File.createTempFile("replacedFilelogger", "itwTest");
        File f2 = File.createTempFile("replacedFilelogger", "itwTest");
        f1.deleteOnExit();
        f2.deleteOnExit();
        oc.setFileLog(new WriterBasedFileLog(f1.getAbsolutePath(), false));
        LogConfig.getLogConfig().setLogToFile(true);
        oc.log(msg(OutputControllerLevel.INFO, line1));
        oc.log(msg(OutputControllerLevel.ERROR, line2));
        oc.log(msg(OutputControllerLevel.INFO, line3));
        oc.flush();
        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertTrue((s1.contains(line1)));
        Assert.assertFalse((s2.contains(line1)));
        Assert.assertTrue((s1.contains(line2)));
        Assert.assertFalse((s2.contains(line2)));
        Assert.assertTrue((s1.contains(line3)));
        Assert.assertFalse((s2.contains(line3)));

        oc.setFileLog(new WriterBasedFileLog(f2.getAbsolutePath(), false));
        oc.log(msg(OutputControllerLevel.ERROR, line5));
        oc.log(msg(OutputControllerLevel.INFO, line5));
        oc.flush();

        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertTrue((s1.contains(line1)));
        Assert.assertFalse((s2.contains(line1)));
        Assert.assertTrue((s1.contains(line2)));
        Assert.assertFalse((s2.contains(line2)));
        Assert.assertTrue((s1.contains(line3)));
        Assert.assertFalse((s2.contains(line3)));

        Assert.assertFalse((s1.contains(line5)));
        Assert.assertTrue((s2.contains(line5)));

        LogConfig.getLogConfig().setLogToFile(false);
        oc.log(msg(OutputControllerLevel.ERROR, line6));
        oc.log(msg(OutputControllerLevel.INFO, line6));
        oc.flush();

        s1 = StreamUtils.readStreamAsString(new FileInputStream(f1), true);
        s2 = StreamUtils.readStreamAsString(new FileInputStream(f2), true);

        Assert.assertFalse((s1.contains(line6)));
        Assert.assertFalse((s2.contains(line6)));


    }

    /**
     * add syslog once implemented
     */
    @Test
    public void isSysLoggerWorking() {
    }
}
