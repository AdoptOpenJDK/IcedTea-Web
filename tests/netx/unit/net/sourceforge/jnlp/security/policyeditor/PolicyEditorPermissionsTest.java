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

import java.util.regex.Pattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PolicyEditorPermissionsTest {

    @Test
    public void assertAllPermissionsHaveNames() throws Exception {
        for (PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertFalse("Permission " + perm + " should have a defined name", perm.getName().contains("UNDEFINED"));
        }
    }

    @Test
    public void assertAllPermissionsHaveDescriptions() throws Exception {
        for (PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertFalse("Permission " + perm + " should have a defined description", perm.getDescription().contains("UNDEFINED"));
        }
    }

    @Test
    public void assertAllPermissionsHavePermissionStrings() throws Exception {
        for (PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertFalse("Permission " + perm + " should have a defined permission string",
                    perm.toPermissionString().trim().isEmpty());
        }
    }

    @Test
    public void testActionsRegex() throws Exception {
        final Pattern pattern = CustomPermission.ACTIONS_PERMISSION;

        final String actionsPermission = "permission java.io.FilePermission \"${user.home}\", \"read\";";
        final String targetPermission = "permission java.io.RuntimePermission \"queuePrintJob\";";
        final String badPermission = "permission java.io.FilePermission user.home read;";

        assertTrue(actionsPermission + " should match", pattern.matcher(actionsPermission).matches());
        assertFalse(targetPermission + " should not match", pattern.matcher(targetPermission).matches());
        assertFalse(badPermission + " should not match", pattern.matcher(badPermission).matches());
    }

    @Test
    public void testTargetRegex() throws Exception {
        final Pattern pattern = CustomPermission.TARGET_PERMISSION;

        final String actionsPermission = "permission java.io.FilePermission \"${user.home}\", \"read\";";
        final String targetPermission = "permission java.io.RuntimePermission \"queuePrintJob\";";
        final String badPermission = "permission java.io.FilePermission user.home read;";

        assertFalse(actionsPermission + " should not match", pattern.matcher(actionsPermission).matches());
        assertTrue(targetPermission + " should match", pattern.matcher(targetPermission).matches());
        assertFalse(badPermission + " should not match", pattern.matcher(badPermission).matches());
    }

    @Test
    public void testRegexesAgainstBadPermissionNames() throws Exception {
        final Pattern targetPattern = CustomPermission.TARGET_PERMISSION;
        final Pattern actionsPattern = CustomPermission.ACTIONS_PERMISSION;
        final String badPermission = "permission abc123^$% \"target\", \"actions\"";

        assertFalse(badPermission + " should not match", targetPattern.matcher(badPermission).matches());
        assertFalse(badPermission + " should not match", actionsPattern.matcher(badPermission).matches());
    }
}
