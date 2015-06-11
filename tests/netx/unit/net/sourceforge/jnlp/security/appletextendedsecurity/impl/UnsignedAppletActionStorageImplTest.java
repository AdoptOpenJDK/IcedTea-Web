/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity.impl;

import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.security.dialogs.remember.AppletSecurityActions;
import net.sourceforge.jnlp.security.dialogs.remember.ExecuteAppletAction;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnsignedAppletActionStorageImplTest {

    private static File f1;
    private static File f2;
    private static File f3;
    private static File ff1;
    private static File ff2;
    private static File ff3;
    private static File ff4;

    public static final int ZERO = AppletSecurityActions.UNSIGNED_APPLET_ACTION;
    public static final int ONE = AppletSecurityActions.MATCHING_ALACA_ACTION;

    @BeforeClass
    public static void preapreTestFiles() throws IOException {
        f1 = File.createTempFile("itwMatching", "testFile1");
        f2 = File.createTempFile("itwMatching", "testFile2");
        f3 = File.createTempFile("itwMatching", "testFile3");
        ServerAccess.saveFile("A 123456 .* .* jar1,jar2", f1);
        ServerAccess.saveFile("N 123456 .* \\Qbla\\E jar1,jar2", f2);
        ServerAccess.saveFile(""
                + "A 1 \\Qhttp://jmol.sourceforge.net/demo/atoms/\\E \\Qhttp://jmol.sourceforge.net/jmol/\\E JmolApplet0.jar\n"
                + "N 1363278653454 \\Qhttp://www.walter-fendt.de/ph14e\\E.* \\Qhttp://www.walter-fendt.de\\E.*\n"
                + "n 1363281783104 \\Qhttp://www.walter-fendt.de/ph14e/inclplane.htm\\E \\Qhttp://www.walter-fendt.de/ph14_jar/\\E Ph14English.jar,SchiefeEbene.jar"
                + "yA 1 \\Qhttp://jmol.sourceforge.net/demo/atoms/\\E \\Qhttp://jmol.sourceforge.net/jmol/\\E JmolApplet0.jar\n"
                + "Ay 1363278653454 \\Qhttp://www.walter-fendt.de/ph14e\\E.* \\Qhttp://www.walter-fendt.de\\E.*\n"
                + "nn 1363281783104 \\Qhttp://www.walter-fendt.de/ph14e/inclplane.htm\\E \\Qhttp://www.walter-fendt.de/ph14_jar/\\E Ph14English.jar,SchiefeEbene.jar"
                + "", f3);

        ff1 = File.createTempFile("itwMatching", "testFile1");
        ff2 = File.createTempFile("itwMatching", "testFile2");
        ff3 = File.createTempFile("itwMatching", "testFile3");
        ff4 = File.createTempFile("itwMatching", "testFile3");
        ServerAccess.saveFile("AXn 123456 .* .* jar1,jar2", ff1);
        ServerAccess.saveFile("XXXXXy 123456 .* \\Qbla\\E jar1,jar2", ff2);
        ServerAccess.saveFile("XXXXXY 123456 .* \\Qbla\\E jar1,jar2", ff4);//errornous
        ServerAccess.saveFile(""
                + "XA 1 \\Qa\\E \\Qb\\E jar1\n"
                + "NNA 2 \\Qc\\E \\Qd\\E\n"
                + "nyXyn 3 \\Qe\\E \\Qf\\E j1,j2"
                + "", ff3);
    }

    @AfterClass
    public static void removeTestFiles() throws IOException {
        f1.delete();
        f2.delete();
        f3.delete();
        ff1.delete();
        ff2.delete();
        ff3.delete();
        ff4.delete();
    }

    @Test
    public void multipleActionsf4JustLoad() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff4);
        //pass
    }

    @Test(expected = RuntimeException.class)
    public void multipleActionsf4() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff4);
        //reed whatever
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("a", "b", Arrays.asList(new String[]{"jar1"}), ZERO);
        //fail
    }

    @Test
    public void multipleActionsf3() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff3);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("a", "b", Arrays.asList(new String[]{"jar1"}), ONE);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("c", "d", Arrays.asList(new String[]{}), ONE);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("e", "f", Arrays.asList(new String[]{"j1", "j2"}), ONE);

        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.UNSET, ExecuteAppletAction.ALWAYS);

        Assert.assertNotNull("r2 should be found", r2);
        checkValues(r2, ExecuteAppletAction.NEVER, ExecuteAppletAction.NEVER, ExecuteAppletAction.ALWAYS);

        Assert.assertNotNull("r3 should be found", r3);
        checkValues(r3, ExecuteAppletAction.NO, ExecuteAppletAction.YES, ExecuteAppletAction.UNSET, ExecuteAppletAction.YES, ExecuteAppletAction.NO);
    }

    @Test
    public void multipleActionsf2() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff2);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("whatever", "bla", Arrays.asList(new String[]{"jar1", "jar2"}), ONE);
        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.UNSET, ExecuteAppletAction.UNSET, ExecuteAppletAction.UNSET, ExecuteAppletAction.UNSET, ExecuteAppletAction.UNSET, ExecuteAppletAction.YES);
    }

    @Test
    public void multipleActionsf1() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff1);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar1", "jar2"}), ONE);
        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.ALWAYS, ExecuteAppletAction.UNSET, ExecuteAppletAction.NO);

    }

    @Test
    public void wildcards1() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f3);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("http://www.walter-fendt.de/ph14e/inclplane.htm", "http://www.walter-fendt.de/ph14_jar/", Arrays.asList(new String[]{"Ph14English.jar", "SchiefeEbene.jar"}), ZERO);
        Assert.assertNotNull("r1 should be found", r1);
        ServerAccess.logOutputReprint(r1.toString());
        checkValues(r1, ExecuteAppletAction.NEVER);
    }

    @Test
    public void allMatchingDocAndCode() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar1", "jar2"}), ZERO);
        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.ALWAYS);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("blah", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}), ZERO);
        checkValues(r3, ExecuteAppletAction.ALWAYS);
        Assert.assertNotNull("r3 should be found", r3);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem("blha", "blaBlam", Arrays.asList(new String[]{"jar2", "wrong_jar"}), ZERO);
        Assert.assertNull("r4 should NOT be found", r4);
        UnsignedAppletActionEntry r5 = i1.getMatchingItem("blaBla", "blaBlaBla", Arrays.asList(new String[]{"jar2"}), ZERO);
        Assert.assertNull("r5 should NOT be found", r5);

    }

    @Test
    public void allMatchingDocAndStrictCode() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f2);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("whatever", "bla", Arrays.asList(new String[]{"jar1", "jar2"}), ZERO);
        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.NEVER);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("whatever", null, Arrays.asList(new String[]{"jar2", "jar1"}), ZERO);
        Assert.assertNotNull("r3 should be found", r3);
        checkValues(r3, ExecuteAppletAction.NEVER);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBlam", Arrays.asList(new String[]{"jar1", "jar2"}), ZERO);
        Assert.assertNull("r2 should NOT be found", r2);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem(null, "blaBlam", null, ZERO);
        Assert.assertNull("r4 should NOT be found", r4);

    }

    @Test
    public void allMatchingDocAndCodeWithNulls() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", null, ZERO);
        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, ExecuteAppletAction.ALWAYS);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("bla", "whatever", null, ZERO);
        Assert.assertNotNull("r3 should be found", r3);
        checkValues(r3, ExecuteAppletAction.ALWAYS);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}), ZERO);
        Assert.assertNotNull("r2 should be found", r2);
        checkValues(r2, ExecuteAppletAction.ALWAYS);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem("bla", "blaBla", null, ZERO);
        Assert.assertNotNull("r4 should be found", r4);
        checkValues(r2, ExecuteAppletAction.ALWAYS);
        UnsignedAppletActionEntry r5 = i1.getMatchingItem("", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}), ZERO);
        Assert.assertNotNull("r5 should be found", r5);
        UnsignedAppletActionEntry r6 = i1.getMatchingItem(null, null, Arrays.asList(new String[]{"jar2", "jar1"}), ZERO);
        Assert.assertNotNull("r6 should be found", r6);
        UnsignedAppletActionEntry r7 = i1.getMatchingItem(null, null, Arrays.asList(new String[]{"jar2", "jar11"}), ZERO);
        Assert.assertNull("r7 should NOT be found", r7);

    }

    private void checkValues(UnsignedAppletActionEntry item, ExecuteAppletAction... items) {
        AppletSecurityActions as = item.getAppletSecurityActions();
        for (int i = 0; i < items.length; i++) {
            Assert.assertEquals(items[i], as.getAction(i));

        }
        for (int i = items.length; i < 1000; i++) {
            Assert.assertEquals(ExecuteAppletAction.UNSET, as.getAction(i));

        }

    }

}
