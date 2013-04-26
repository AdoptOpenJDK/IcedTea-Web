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

import net.sourceforge.jnlp.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnsignedAppletActionStorageImplTest {

    private static File f1;
    private static File f2;
    private static File f3;
    private static File f4;

    @BeforeClass
    public static void preapreTestFiles() throws IOException {
        f1 = File.createTempFile("itwMatching", "testFile1");
        f2 = File.createTempFile("itwMatching", "testFile2");
        f3 = File.createTempFile("itwMatching", "testFile3");
        f4 = File.createTempFile("itwMatching", "testFile4");
        ServerAccess.saveFile("A 123456 .* .* jar1,jar2", f1);
        ServerAccess.saveFile("A 123456 .* \\Qbla\\E jar1,jar2", f2);
        ServerAccess.saveFile(""
                + "A 1 \\Qhttp://jmol.sourceforge.net/demo/atoms/\\E \\Qhttp://jmol.sourceforge.net/jmol/\\E JmolApplet0.jar\n"
                + "A 1363278653454 \\Qhttp://www.walter-fendt.de/ph14e\\E.* \\Qhttp://www.walter-fendt.de\\E.*\n"
                + "n 1363281783104 \\Qhttp://www.walter-fendt.de/ph14e/inclplane.htm\\E \\Qhttp://www.walter-fendt.de/ph14_jar/\\E Ph14English.jar,SchiefeEbene.jar"
                + "", f3);
    }

     @AfterClass
    public static void removeTestFiles() throws IOException {
         f1.delete();
         f2.delete();
         f3.delete();
     }


     @Test
    public void wildcards1() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f3);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("http://www.walter-fendt.de/ph14e/inclplane.htm", "http://www.walter-fendt.de/ph14_jar/", Arrays.asList(new String[]{"Ph14English.jar","SchiefeEbene.jar"}));
        ServerAccess.logOutputReprint(r1.toString());
     }
     @Test
    public void allMatchingDocAndCode() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar1", "jar2"}));
        Assert.assertNotNull("r1 should be found", r1);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("blah", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}));
        Assert.assertNotNull("r3 should be found", r1);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem("blha", "blaBlam",  Arrays.asList(new String[]{"jar2", "wrong_jar"}));
        Assert.assertNull("r4 should NOT be found", r4);
        UnsignedAppletActionEntry r5 = i1.getMatchingItem("blaBla", "blaBlaBla",  Arrays.asList(new String[]{"jar2"}));
        Assert.assertNull("r5 should NOT be found", r5);

    }

     @Test
    public void allMatchingDocAndStrictCode() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f2);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("whatever", "bla",  Arrays.asList(new String[]{"jar1", "jar2"}));
        Assert.assertNotNull("r1 should be found", r1);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("whatever", null,  Arrays.asList(new String[]{"jar2", "jar1"}));
        Assert.assertNotNull("r3 should be found", r1);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBlam",  Arrays.asList(new String[]{"jar1", "jar2"}));
        Assert.assertNull("r2 should NOT be found", r2);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem(null, "blaBlam", null);
        Assert.assertNull("r4 should NOT be found", r4);

    }

     @Test
    public void allMatchingDocAndCodeWithNulls() {
        UnsignedAppletActionStorageImpl i1 = new UnsignedAppletActionStorageImpl(f1);
        UnsignedAppletActionEntry r1 = i1.getMatchingItem("bla", "blaBla",  null);
        Assert.assertNotNull("r1 should be found", r1);
        UnsignedAppletActionEntry r3 = i1.getMatchingItem("bla", "whatever", null);
        Assert.assertNotNull("r3 should be found", r1);
        UnsignedAppletActionEntry r2 = i1.getMatchingItem("bla", "blaBla", Arrays.asList(new String[]{"jar2", "jar1"}));
        Assert.assertNotNull("r2 should be found", r2);
        UnsignedAppletActionEntry r4 = i1.getMatchingItem("bla", "blaBla",  null);
        Assert.assertNotNull("r4 should be found", r4);
        UnsignedAppletActionEntry r5 = i1.getMatchingItem("", "blaBla",  Arrays.asList(new String[]{"jar2", "jar1"}));
        Assert.assertNotNull("r5 should be found", r5);
        UnsignedAppletActionEntry r6 = i1.getMatchingItem(null, null,  Arrays.asList(new String[]{"jar2", "jar1"}));
        Assert.assertNotNull("r6 should be found", r6);
        UnsignedAppletActionEntry r7 = i1.getMatchingItem(null, null,  Arrays.asList(new String[]{"jar2", "jar11"}));
        Assert.assertNull("r7 should NOT be found", r7);


    }
}
