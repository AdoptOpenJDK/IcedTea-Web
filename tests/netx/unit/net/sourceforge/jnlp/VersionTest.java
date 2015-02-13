/* 
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
package net.sourceforge.jnlp;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    private static boolean[] results = {true,
        true,
        false,
        true,
        false,
        true,
        false,
        true,
        false,
        false,
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        false,
        true};
    private static Version jvms[] = {
        new Version("1.1* 1.3*"),
        new Version("1.2+"),};
    private static Version versions[] = {
        new Version("1.1"),
        new Version("1.1.8"),
        new Version("1.2"),
        new Version("1.3"),
        new Version("2.0"),
        new Version("1.3.1"),
        new Version("1.2.1"),
        new Version("1.3.1-beta"),
        new Version("1.1 1.2"),
        new Version("1.2 1.3"),};

    @Test
    public void testMatches() {

        int i = 0;
        for (int j = 0; j < jvms.length; j++) {
            for (int v = 0; v < versions.length; v++) {
                i++;
                String debugOutput = i + " " + jvms[j].toString() + " ";
                if (!jvms[j].matches(versions[v])) {
                    debugOutput += "!";
                }
                debugOutput += "matches " + versions[v].toString();
                ServerAccess.logOutputReprint(debugOutput);
                Assert.assertEquals(results[i - 1], jvms[j].matches(versions[v]));
            }
        }


    }
    
    
      @Test
    public void cornerCases() {
        Assert.assertTrue(new Version("1.5").matches("1.5"));
        Assert.assertTrue(new Version("1.5+").matches("1.5"));
        Assert.assertTrue(new Version("1.5+").matches("1.6"));
        Assert.assertFalse(new Version("1.5+").matches("1.4"));
        Assert.assertFalse(new Version("1.5").matches("1.4"));
        Assert.assertFalse(new Version("1.5").matches("1.6"));
    }

    @Test
    public void testMatchesMinus() {
        Assert.assertTrue(new Version("1.5-").matches("1.5"));
        //this fails, do we need to patch it?
        //Assert.assertTrue(new Version("1.5-").matches("1.4"));
        //not until somebody complains
        Assert.assertFalse(new Version("1.5-").matches("1.6"));

    }

    @Test
    public void multiplePossibilities() {
        Assert.assertTrue(new Version("1.4 1.5").matches("1.5"));
        Assert.assertFalse(new Version("1.3 1.4").matches("1.5"));
    }

    @Test
    public void jreVersionTestOk() {
        //no exception occures
        //head support jdk 7+, so this statements should be always true
        Version.JreVersion jreVersion = new Version.JreVersion("1.4 1.5+", true, true);
        Version.JreVersion jreVersion1 = new Version.JreVersion("1.6+", true, true);
    }

    @Test(expected = RuntimeException.class)
    public void jreVersionTestFails1() {
        //head support jdk 7+, so this statements should be always false
        Version.JreVersion jreVersion = new Version.JreVersion("2", true, true);
    }

    @Test(expected = RuntimeException.class)
    public void jreVersionTestFails2() {
        //head support jdk 7+, so this statements should be always false
        Version.JreVersion jreVersion = new Version.JreVersion("1.4", true, true);
    }
}
