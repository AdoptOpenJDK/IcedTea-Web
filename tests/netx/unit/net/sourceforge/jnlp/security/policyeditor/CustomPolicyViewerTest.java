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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import net.sourceforge.jnlp.security.policyeditor.CustomPermission;
import net.sourceforge.jnlp.security.policyeditor.CustomPolicyViewer;
import net.sourceforge.jnlp.security.policyeditor.PolicyEditor;

import org.junit.Before;
import org.junit.Test;

public class CustomPolicyViewerTest {

    private CustomPolicyViewer viewer;
    private static final String CODEBASE = "http://example.com";
    private static final CustomPermission PERMISSION = new CustomPermission("java.lang.RuntimePermission", "createClassLoader");

    @Before
    public void setupViewer() {
        viewer = new CustomPolicyViewer(new PolicyEditor(null), CODEBASE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullPolicyEditor() throws Exception {
        new CustomPolicyViewer(null, CODEBASE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCodebase() throws Exception {
        new CustomPolicyViewer(new PolicyEditor(null), null);
    }

    @Test
    public void testPermissionsSetInitiallyEmpty() throws Exception {
        assertEquals("Permissions set should be empty", 0, viewer.getCopyOfCustomPermissions().size());
    }

    @Test
    public void testAddCustomPermission() throws Exception {
        viewer.addCustomPermission(PERMISSION);
        assertEquals("Permissions set size mismatch", 1, viewer.getCopyOfCustomPermissions().size());
        assertTrue("Permissions set should contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
    }

    @Test
    public void testAddCustomPermissionDuplicates() throws Exception {
        viewer.addCustomPermission(PERMISSION);
        assertEquals("Permissions set size mismatch", 1, viewer.getCopyOfCustomPermissions().size());
        assertTrue("Permissions set should contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
        viewer.addCustomPermission(PERMISSION);
        assertEquals("Permissions set size should not have changed", 1, viewer.getCopyOfCustomPermissions().size());
        assertTrue("Permissions set should still contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
    }

    @Test(expected = NullPointerException.class)
    public void testAddCustomPermissionNull() throws Exception {
        viewer.addCustomPermission(null);
    }

    @Test
    public void testRemoveCustomPermission() throws Exception {
        viewer.addCustomPermission(PERMISSION);
        assertEquals("Permissions set size mismatch", 1, viewer.getCopyOfCustomPermissions().size());
        assertTrue("Permissions set should contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
        viewer.removeCustomPermission(PERMISSION);
        assertEquals("Permissions set should be empty", 0, viewer.getCopyOfCustomPermissions().size());
        assertFalse("Permissions set should not contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveCustomPermissionNull() throws Exception {
        viewer.removeCustomPermission(null);
    }

    @Test
    public void testGetCopyOfCustomPermissionsNotNull() throws Exception {
        assertNotNull(viewer.getCopyOfCustomPermissions());
    }

    @Test
    public void testGetCopyOfCustomPermissionsReturnsCopy() throws Exception {
        final Collection<CustomPermission> permissions = viewer.getCopyOfCustomPermissions();
        permissions.add(PERMISSION);
        assertNotEquals("Sets should be distinct", viewer.getCopyOfCustomPermissions(), permissions);
        assertNotEquals("Sizes should not match", viewer.getCopyOfCustomPermissions().size(), permissions.size());
        assertTrue("Copy should contain " + PERMISSION, permissions.contains(PERMISSION));
        assertFalse("Viewer should not contain " + PERMISSION, viewer.getCopyOfCustomPermissions().contains(PERMISSION));
    }

}
