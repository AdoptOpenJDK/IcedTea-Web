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

package net.sourceforge.jnlp.security.dialogs;

import static org.junit.Assert.assertEquals;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioPermission;

import net.sourceforge.jnlp.security.policyeditor.PolicyEditorPermissions;

import org.junit.Test;

public class TemporaryPermissionsTest {

    @Test
    public void testGetPermission() throws Exception {
        final Permission expected = new FilePermission(System.getProperty("java.io.tmpdir"), "read");
        final Permission generated = TemporaryPermissions.getPermission(PolicyEditorPermissions.READ_TMP_FILES);
        assertEquals(expected, generated);
    }

    @Test
    public void testGetPermissionsVarargs() throws Exception {
        final Permission readTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "read");
        final Permission writeTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "write");
        final Set<Permission> expected = new HashSet<>(Arrays.asList(readTmpPermission, writeTmpPermission));
        final Set<Permission> generated = new HashSet<>(TemporaryPermissions.getPermissions(PolicyEditorPermissions.READ_TMP_FILES, PolicyEditorPermissions.WRITE_TMP_FILES));
        assertEquals(expected, generated);
    }

    @Test
    public void testGetPermissionsVarargsArray() throws Exception {
        final Permission readTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "read");
        final Permission writeTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "write");
        final Set<Permission> expected = new HashSet<>(Arrays.asList(readTmpPermission, writeTmpPermission));
        final PolicyEditorPermissions[] arr = new PolicyEditorPermissions[] { PolicyEditorPermissions.READ_TMP_FILES, PolicyEditorPermissions.WRITE_TMP_FILES };
        final Set<Permission> generated = new HashSet<>(TemporaryPermissions.getPermissions(arr));
        assertEquals(expected, generated);
    }

    @Test
    public void testGetPermissionsCollection() throws Exception {
        final Permission readTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "read");
        final Permission writeTmpPermission = new FilePermission(System.getProperty("java.io.tmpdir"), "write");
        final Set<Permission> expected = new HashSet<>(Arrays.asList(readTmpPermission, writeTmpPermission));
        final Collection<PolicyEditorPermissions> coll = Arrays.asList(PolicyEditorPermissions.READ_TMP_FILES, PolicyEditorPermissions.WRITE_TMP_FILES);
        final Set<Permission> generated = new HashSet<>(TemporaryPermissions.getPermissions(coll));
        assertEquals(expected, generated);
    }

    @Test
    public void testGetPermissionsGroup() throws Exception {
        final Permission playAudio = new AudioPermission("play");
        final Permission recordAudio = new AudioPermission("record");
        final Permission print = new RuntimePermission("queuePrintJob");
        final Permission clipboard = new AWTPermission("accessClipboard");
        final Set<Permission> expected = new HashSet<>(Arrays.asList(playAudio, recordAudio, print, clipboard));
        final Set<Permission> generated = new HashSet<>(TemporaryPermissions.getPermissions(PolicyEditorPermissions.Group.MediaAccess));
        assertEquals(expected, generated);
    }

}