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
import java.io.PrintStream;
import net.sourceforge.jnlp.closinglisteners.RulesFolowingClosingListener;
import org.junit.Assert;
import org.junit.Test;

public class PrintStreamLoggerTest {

    private static String line1 = "I'm logged line one";
    private static String line2 = "I'm logged line two";
    private static RulesFolowingClosingListener.ContainsRule r1 = new RulesFolowingClosingListener.ContainsRule(line1);
    private static RulesFolowingClosingListener.ContainsRule r2 = new RulesFolowingClosingListener.ContainsRule(line2);
    
    private static class  AccessiblePrintStream extends PrintStream{

        public AccessiblePrintStream(ByteArrayOutputStream out) {
            super(out);
        }
        
        
        
            public ByteArrayOutputStream getOut() {
                return (ByteArrayOutputStream) out;
            }
    }

    @Test
    public void isLoggingAtAll() throws Exception {
        int i = 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStreamLogger l = new PrintStreamLogger(new PrintStream(output));
        l.log(line1);
        Assert.assertTrue(r1.evaluate(output.toString("utf-8")));
        l.log(line2);
        Assert.assertTrue(r1.evaluate(output.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(output.toString("utf-8")));
    }

    @Test
    public void isReturningStream() throws Exception {
        int i = 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AccessiblePrintStream ps = new AccessiblePrintStream(output);
        PrintStreamLogger l = new PrintStreamLogger(ps);
        l.log(line1);
        Assert.assertTrue(r1.evaluate(output.toString("utf-8")));
        AccessiblePrintStream got = (AccessiblePrintStream) l.getStream();
        Assert.assertTrue(r1.evaluate(got.getOut().toString("utf-8")));
        l.log(line2);
        Assert.assertTrue(r1.evaluate(output.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(output.toString("utf-8")));
        Assert.assertTrue(r1.evaluate(got.getOut().toString("utf-8")));
        Assert.assertTrue(r2.evaluate(got.getOut().toString("utf-8")));
        Assert.assertTrue(got == ps);
    }
    
    @Test
     public void isSettingStream() throws Exception {
        int i = 0;
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        AccessiblePrintStream ps = new AccessiblePrintStream(output1);
        PrintStreamLogger l = new PrintStreamLogger(ps);
        l.log(line1);
        Assert.assertTrue(r1.evaluate(output1.toString("utf-8")));
        AccessiblePrintStream set = new AccessiblePrintStream(output2);
        l.setStream(set);
        l.log(line2);
        Assert.assertFalse(r1.evaluate(output2.toString("utf-8")));
        Assert.assertTrue(r2.evaluate(output2.toString("utf-8")));
        Assert.assertFalse(r1.evaluate(set.getOut().toString("utf-8")));
        Assert.assertTrue(r2.evaluate(set.getOut().toString("utf-8")));
        Assert.assertTrue(set != ps);
        Assert.assertTrue(set == l.getStream());
    }
}
