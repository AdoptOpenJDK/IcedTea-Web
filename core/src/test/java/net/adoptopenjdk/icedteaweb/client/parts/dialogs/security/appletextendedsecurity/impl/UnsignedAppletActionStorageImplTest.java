/*   Copyright (C) 2015 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl;

import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnsignedAppletActionStorageImplTest {

    private static final String versionLine=UnsignedAppletActionStorageImpl.versionPrefix +UnsignedAppletActionStorageImpl.currentVersion+"\n";
    
    private static File f1;
    private static File f2;
    private static File f3;
    private static File ff1;
    private static File ff2;
    private static File ff3;
    private static File ff4;

    private abstract static class c1 implements RememberableDialog {
    };

    private abstract static class c2 implements RememberableDialog {
    };

    private abstract static class c3 implements RememberableDialog {
    };

    private abstract static class c4 implements RememberableDialog {
    };

    private abstract static class c5 implements RememberableDialog {
    };

    private abstract static class c6 implements RememberableDialog {
    };

    private abstract static class c7 implements RememberableDialog {
    };

    @BeforeClass
    public static void prepareTestFiles() throws IOException {
        f1 = File.createTempFile("itwMatching", "testFile1");
        f2 = File.createTempFile("itwMatching", "testFile2");
        f3 = File.createTempFile("itwMatching", "testFile3");
        ServerAccess.saveFile(versionLine+"c1:A{YES}; 123456 .* .* jar1,jar2", f1);
        ServerAccess.saveFile(versionLine+"c1:N{NO}; 123456 .* \\Qbla\\E jar1,jar2", f2);
        ServerAccess.saveFile(versionLine
                + "c1:A{YES}; 1 \\Qhttp://jmol.sourceforge.net/demo/atoms/\\E \\Qhttp://jmol.sourceforge.net/jmol/\\E JmolApplet0.jar\n"
                + "c1:N{NO}; 1363278653454 \\Qhttp://www.walter-fendt.de/ph14e\\E.* \\Qhttp://www.walter-fendt.de\\E.*\n"
                + "c1:n{NO}; 1363281783104 \\Qhttp://www.walter-fendt.de/ph14e/inclplane.htm\\E \\Qhttp://www.walter-fendt.de/ph14_jar/\\E Ph14English.jar,SchiefeEbene.jar"
                + "c1:y{YES};c2:A{YES}; 1 \\Qhttp://jmol.sourceforge.net/demo/atoms/\\E \\Qhttp://jmol.sourceforge.net/jmol/\\E JmolApplet0.jar\n"
                + "c1:A{YES};c2:A{YES}; 1363278653454 \\Qhttp://www.walter-fendt.de/ph14e\\E.* \\Qhttp://www.walter-fendt.de\\E.*\n"
                + "c1:n{NO};c2:n{NO}; 1363281783104 \\Qhttp://www.walter-fendt.de/ph14e/inclplane.htm\\E \\Qhttp://www.walter-fendt.de/ph14_jar/\\E Ph14English.jar,SchiefeEbene.jar"
                + "", f3);

        ff1 = File.createTempFile("itwMatching", "testFile1");
        ff2 = File.createTempFile("itwMatching", "testFile2");
        ff3 = File.createTempFile("itwMatching", "testFile3");
        ff4 = File.createTempFile("itwMatching", "testFile3");
        ServerAccess.saveFile(versionLine+"c1:A{YES};c3:n{NO}; 123456 .* .* jar1,jar2", ff1);
        ServerAccess.saveFile(versionLine+"c6:y{YES}; 123456 .* \\Qbla\\E jar1,jar2", ff2);
        ServerAccess.saveFile(versionLine+"c6:A{YES}; 123456 .* \\Qbla\\E jar1,jar2", ff4);
        ServerAccess.saveFile(versionLine
                + "c2:A{YES}; 1 \\Qa\\E \\Qb\\E jar1\n"
                + "c1:N{NO};c2:N{NO};c3:A{YES}; 2 \\Qc\\E \\Qd\\E\n"
                + "c1:n{NO};c2:y{YES};c4:y{YES};c5:n{NO}; 3 \\Qe\\E \\Qf\\E j1,j2"
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

    @Test
    public void multipleActionsf4() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff4);
        //reed whatever
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("a", "b", Arrays.asList(new String[]{"jar1"}), c1.class);
        //nonmapped return null
        Assert.assertEquals(null, r1);
    }

    @Test
    public void multipleActionsf3() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff3);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("a", "b", Arrays.asList(new String[]{"jar1"}), c1.class);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("c", "d", Arrays.asList(new String[]{}), c2.class);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("e", "f", Arrays.asList(new String[]{"j1", "j2"}), c3.class);

        Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1,
                new Result(c1.class),
                Result.AY(c2.class),
                new Result(c3.class));

        Assert.assertNotNull("r2 should be found", r2);
        Assert.assertEquals(ExecuteAppletAction.NEVER, r2.getAppletSecurityActions().getActionEntry(c1.class).getAction());
        checkValues(r2,
                Result.NN(c1.class),
                Result.NN(c2.class),
                Result.AY(c3.class));

        Assert.assertNotNull("r3 should be found", r3);
        checkValues(r3, Result.nN(c1.class), Result.yY(c2.class), Result.NUL(c3.class), Result.yY(c4.class), Result.nN(c5.class));
    }
    
     @Test
     public void multipleActionsf2() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff2);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("whatever", "bla", Arrays.asList(new String[]{"jar1", "jar2"}), c6.class);
     Assert.assertNotNull("r1 should be found", r1);
        checkValues(r1, Result.NUL(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.yY(c6.class));
     }

     @Test
     public void multipleActionsf1() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(ff1);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar1", "jar2"}), c1.class);
     Assert.assertNotNull("r1 should be found", r1);
     checkValues(r1, 
              Result.AY(c1.class), Result.NUL(c2.class), Result.nN(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class)
             );
 
     }

     @Test
     public void wildcards1() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f3);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("http://www.walter-fendt.de/ph14e/inclplane.htm", "http://www.walter-fendt.de/ph14_jar/", Arrays.asList(new String[]{"Ph14English.jar", "SchiefeEbene.jar"}), c1.class);
     Assert.assertNotNull("r1 should be found", r1);
     ServerAccess.logOutputReprint(r1.toString());
     //stronger result
checkValues(r1, 
              Result.NN(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class)
             );
     }

     @Test
     public void allMatchingDocAndCode() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar1", "jar2"}), c1.class);
     Assert.assertNotNull("r1 should be found", r1);
     checkValues(r1, 
              Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class)
             );
     UnsignedAppletActionEntry r3 = i1.getMatchingItem("blah", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}), c1.class);
     checkValues(r3, 
              Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class)
             );
     Assert.assertNotNull("r3 should be found", r3);
     UnsignedAppletActionEntry r4 = i1.getMatchingItem("blha", "blaBlam", Arrays.asList(new String[]{"jar2", "wrong_jar"}), c1.class);
     Assert.assertNull("r4 should NOT be found", r4);
     UnsignedAppletActionEntry r5 = i1.getMatchingItem("blaBla", "blaBlaBla", Arrays.asList(new String[]{"jar2"}), c1.class);
     Assert.assertNull("r5 should NOT be found", r5);

     }

     @Test
     public void allMatchingDocAndStrictCode() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f2);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("whatever", "bla", Arrays.asList(new String[]{"jar1", "jar2"}), c1.class);
     Assert.assertNotNull("r1 should be found", r1);
     checkValues(r1, Result.NN(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r3 = i1.getMatchingItem("whatever", null, Arrays.asList(new String[]{"jar2", "jar1"}), c1.class);
     Assert.assertNotNull("r3 should be found", r3);
     checkValues(r3, Result.NN(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBlam", Arrays.asList(new String[]{"jar1", "jar2"}), c1.class);
     Assert.assertNull("r2 should NOT be found", r2);
     UnsignedAppletActionEntry r4 = i1.getMatchingItem(null, "blaBlam", null, c1.class);
     Assert.assertNull("r4 should NOT be found", r4);

     }

     @Test
     public void allMatchingDocAndCodeWithNulls() {
     UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
     UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", null,  c1.class);
     Assert.assertNotNull("r1 should be found", r1);
     checkValues(r1, Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r3 = i1.getMatchingItem("bla", "whatever", null,  c1.class);
     Assert.assertNotNull("r3 should be found", r3);
     checkValues(r3,  Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}),  c1.class);
     Assert.assertNotNull("r2 should be found", r2);
     checkValues(r2,  Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r4 = i1.getMatchingItem("bla", "blaBla", null,  c1.class);
     Assert.assertNotNull("r4 should be found", r4);
     checkValues(r2,  Result.AY(c1.class), Result.NUL(c2.class), Result.NUL(c3.class), Result.NUL(c4.class), Result.NUL(c5.class), Result.NUL(c6.class));
     UnsignedAppletActionEntry r5 = i1.getMatchingItem("", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}),  c1.class);
     Assert.assertNotNull("r5 should be found", r5);
     UnsignedAppletActionEntry r6 = i1.getMatchingItem(null, null, Arrays.asList(new String[]{"jar2", "jar1"}),  c1.class);
     Assert.assertNotNull("r6 should be found", r6);
     UnsignedAppletActionEntry r7 = i1.getMatchingItem(null, null, Arrays.asList(new String[]{"jar2", "jar11"}),  c1.class);
     Assert.assertNull("r7 should NOT be found", r7);

     }
     

    private void checkValues(UnsignedAppletActionEntry item, Result... results) {
        for (Result result : results) {
            if (result.nul) {
                Assert.assertEquals(null, item.getAppletSecurityActions().getActionEntry(result.id));
            } else {
                Assert.assertEquals(result.action, item.getAppletSecurityActions().getActionEntry(result.id).getAction());
                Assert.assertEquals(result.savedValue, item.getAppletSecurityActions().getActionEntry(result.id).getSavedValue());
            }
        }
    }

    private static class Result {

        public final ExecuteAppletAction action;
        public final String savedValue;
        public final boolean nul;
        public final Class<? extends RememberableDialog> id;

        public static Result AY(Class<? extends RememberableDialog> id) {
            return new Result(ExecuteAppletAction.ALWAYS, "YES", id);
        }

        public static Result yY(Class<? extends RememberableDialog> id) {
            return new Result(ExecuteAppletAction.YES, "YES", id);
        }

        public static Result NN(Class<? extends RememberableDialog> id) {
            return new Result(ExecuteAppletAction.NEVER, "NO", id);
        }

        public static Result nN(Class<? extends RememberableDialog> id) {
            return new Result(ExecuteAppletAction.NO, "NO", id);
        }

        public static Result NUL(Class<? extends RememberableDialog> id) {
            return new Result(id);
        }

        public Result(ExecuteAppletAction action, String savedValue, Class<? extends RememberableDialog> id) {
            this.action = action;
            this.savedValue = savedValue;
            this.nul = false;
            this.id = id;
        }

        public Result(Class<? extends RememberableDialog> id) {
            this.action = null;
            this.savedValue = null;
            this.nul = true;
            this.id = id;
        }

    }

}
