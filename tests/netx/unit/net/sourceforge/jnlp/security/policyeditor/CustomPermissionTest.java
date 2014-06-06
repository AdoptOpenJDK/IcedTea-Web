/*Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.policyeditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CustomPermissionTest {

    @Test
    public void assertFieldsPopulateCorrectly() throws Exception {
        final CustomPermission cp = new CustomPermission("type", "target", "some,actions");
        assertTrue("Permission type should be \"type\"", cp.type.equals("type"));
        assertTrue("Permission target should be \"target\"", cp.target.equals("target"));
        assertTrue("Permission actions should be \"some,actions\"", cp.actions.equals("some,actions"));
    }

    @Test
    public void testFromStringWithoutActions() throws Exception {
        final CustomPermission cp = CustomPermission.fromString("permission java.lang.RuntimePermission \"queuePrintJob\";");
        assertTrue("Permission type should be \"java.lang.RuntimePermission\"", cp.type.equals("java.lang.RuntimePermission"));
        assertTrue("Permission target should be \"queuePrintJob\"", cp.target.equals("queuePrintJob"));
        assertTrue("Permission actions should be empty", cp.actions.isEmpty());
    }

    @Test
    public void testFromStringWithActions() throws Exception {
        final CustomPermission cp = CustomPermission.fromString("permission java.io.FilePermission \"*\", \"read,write\";");
        assertTrue("Permission type should be \"java.io.FilePermission\"", cp.type.equals("java.io.FilePermission"));
        assertTrue("Permission target should be \"*\"", cp.target.equals("*"));
        assertTrue("Permission actions should be \"read,write\"", cp.actions.equals("read,write"));
    }

    @Test
    public void testMissingQuotationMarks() throws Exception {
        final CustomPermission cp = CustomPermission.fromString("permission java.io.FilePermission *, read,write;");
        assertTrue("Custom permission should be null", cp == null);
    }

    @Test
    public void testActionsMissingComma() throws Exception {
        final String missingComma = "permission java.io.FilePermission \"*\" \"read,write\";";
        final CustomPermission cp1 = CustomPermission.fromString(missingComma);
        assertTrue("Custom permission for " + missingComma + " should be null", cp1 == null);
    }

    @Test
    public void testActionsMissingFirstQuote() throws Exception {
        final String missingFirstQuote = "permission java.io.FilePermission \"*\", read,write\";";
        final CustomPermission cp2 = CustomPermission.fromString(missingFirstQuote);
        assertTrue("Custom permission for " + missingFirstQuote + " should be null", cp2 == null);
    }

    @Test
    public void testActionsMissingSecondQuote() throws Exception {
        final String missingSecondQuote = "permission java.io.FilePermission \"*\", \"read,write;";
        final CustomPermission cp3 = CustomPermission.fromString(missingSecondQuote);
        assertTrue("Custom permission for " + missingSecondQuote + " should be null", cp3 == null);
    }

    @Test
    public void testActionsMissingBothQuotes() throws Exception {
        final String missingBothQuotes = "permission java.io.FilePermission \"*\", read,write;";
        final CustomPermission cp4 = CustomPermission.fromString(missingBothQuotes);
        assertTrue("Custom permission for " + missingBothQuotes + " should be null", cp4 == null);
    }

    @Test
    public void testActionsMissingAllPunctuation() throws Exception {
        final String missingAll = "permission java.io.FilePermission \"*\" read,write;";
        final CustomPermission cp5 = CustomPermission.fromString(missingAll);
        assertTrue("Custom permission for " + missingAll + " should be null", cp5 == null);
    }

    @Test
    public void testToString() throws Exception {
        final CustomPermission cp = new CustomPermission("java.io.FilePermission", "*", "read");
        final String expected = "permission java.io.FilePermission \"*\", \"read\";";
        assertTrue("Permissions string should have equalled " + expected, cp.toString().equals(expected));
    }

    @Test
    public void testToStringWithoutActions() throws Exception {
        final CustomPermission cp = new CustomPermission("java.lang.RuntimePermission", "createClassLoader", "");
        final String expected = "permission java.lang.RuntimePermission \"createClassLoader\";";
        assertEquals(expected, cp.toString());
    }

    @Test
    public void testCompareTo() throws Exception {
        final CustomPermission cp1 = new CustomPermission("java.io.FilePermission", "*", "read");
        final CustomPermission cp2 = new CustomPermission("java.io.FilePermission", "${user.home}${/}*", "read");
        final CustomPermission cp3 = new CustomPermission("java.lang.RuntimePermission", "queuePrintJob", "");
        assertTrue("cp1.compareTo(cp2) should be > 0", cp1.compareTo(cp2) > 0);
        assertTrue("cp1.compareTo(cp1) should be 0", cp1.compareTo(cp1) == 0);
        assertTrue("cp2.compareTo(cp3) should be < 0", cp2.compareTo(cp3) < 0);
    }
}
