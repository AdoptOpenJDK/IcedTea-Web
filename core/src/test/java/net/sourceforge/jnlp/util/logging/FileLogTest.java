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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import net.sourceforge.jnlp.util.StreamUtils;
import net.sourceforge.jnlp.util.logging.filelogs.LogBasedFileLog;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileLogTest {

    private static final File[] loggingTargets = new File[12];
    private static final String line1 = "I'm logged line one";
    private static final String line2 = "I'm logged line two";
    private static final String line3 = "I'm logged line three";
    private static final RulesFolowingClosingListener.ContainsRule r1 = new RulesFolowingClosingListener.ContainsRule(line1);
    private static final RulesFolowingClosingListener.ContainsRule r2 = new RulesFolowingClosingListener.ContainsRule(line2);
    private static final RulesFolowingClosingListener.ContainsRule r3 = new RulesFolowingClosingListener.ContainsRule(line3);

    @BeforeClass
    public static void prepareTmpFiles() throws IOException {
        for (int i = 0; i < loggingTargets.length; i++) {
            loggingTargets[i] = File.createTempFile("fileLogger", "iteTest");
            loggingTargets[i].deleteOnExit();
        }
        //delete first half of the files, logger should handle both casses
        for (int i = 0; i < loggingTargets.length / 2; i++) {
            loggingTargets[i].delete();
        }

    }

    @AfterClass
    public static void cleanTmpFiles() throws IOException {
        for (int i = 0; i < loggingTargets.length; i++) {
            loggingTargets[i].delete();
        }
    }

    @Test
    public void isAppendingLoggerLoggingOnNotExisitngFile() throws Exception {
        int i = 0;
        LogBasedFileLog l = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l.log(line1);
        String s = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r1.evaluate(s));
    }

    @Test
    public void isRewritingLoggerLoggingOnNotExisitngFile() throws Exception {
        int i = 1;
        LogBasedFileLog l = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l.log(line1);
        String s = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r1.evaluate(s));
    }

    @Test
    public void isRewritingLoggerRewritingOnNotExisitngFile() throws Exception {
        int i = 2;
        LogBasedFileLog l1 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l1.log(line2);
        String s1 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s1));
        l1.close();
        LogBasedFileLog l2 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l2.log(line3);
        String s2 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertFalse(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s2));

    }

    @Test
    public void isAppendingLoggerAppendingOnNotExisitngFile() throws Exception {
        int i = 4;
        LogBasedFileLog l1 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l1.log(line2);
        String s1 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s1));
        l1.close();
        LogBasedFileLog l2 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l2.log(line3);
        String s2 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s2));

    }

    //************
    @Test
    public void isAppendingLoggerLoggingOnExisitngFile() throws Exception {
        int i = 6;
        LogBasedFileLog l = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l.log(line1);
        String s = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r1.evaluate(s));
    }

    @Test
    public void isRewritingLoggerLoggingOnExisitngFile() throws Exception {
        int i = 7;
        LogBasedFileLog l = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l.log(line1);
        String s = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r1.evaluate(s));
    }

    @Test
    public void isRewritingLoggerRewritingOnExisitngFile() throws Exception {
        int i = 8;
        LogBasedFileLog l1 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l1.log(line2);
        String s1 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s1));
        l1.close();
        LogBasedFileLog l2 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), false);
        l2.log(line3);
        String s2 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertFalse(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s2));

    }

    @Test
    public void isAppendingLoggerAppendingOnExisitngFile() throws Exception {
        int i = 10;
        LogBasedFileLog l1 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l1.log(line2);
        String s1 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s1));
        l1.close();
        LogBasedFileLog l2 = new LogBasedFileLog(loggingTargets[i].getAbsolutePath(), true);
        l2.log(line3);
        String s2 = StreamUtils.readStreamAsString(new FileInputStream(loggingTargets[i]), true);
        Assert.assertTrue(r2.evaluate(s2));
        Assert.assertTrue(r3.evaluate(s2));

    }
}
