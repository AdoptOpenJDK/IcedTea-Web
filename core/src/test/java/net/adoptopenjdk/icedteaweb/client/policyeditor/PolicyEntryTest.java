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

package net.adoptopenjdk.icedteaweb.client.policyeditor;

import org.junit.Test;
import sun.security.provider.PolicyParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * PolicyEntryTest does not test the various parsing scenarios as those are tested in PolicyEditorParsingTest
 */
public class PolicyEntryTest {

    @Test
    public void testGetCodebase() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                .codebase(codebase)
                .permissions(permissions)
                .customPermissions(customPermissions)
                .build();
        assertEquals("Codebase should equal input", codebase, policyEntry.getPolicyIdentifier().getCodebase());
    }

    @Test
    public void testNullCodebaseConvertsToEmpty() throws Exception {
        final String codebase = null;
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertEquals("Null codebase should produce empty string", "", policyEntry.getPolicyIdentifier().getCodebase());
    }

    @Test
    public void testGetPermissions() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertEquals("Permissions set should equal input", permissions, policyEntry.getPermissions());
    }


    @Test
    public void testGetPermissions2() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = new HashSet<PolicyEditorPermissions>() {{
            add(PolicyEditorPermissions.CLIPBOARD);
            add(PolicyEditorPermissions.NETWORK);
        }};
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertEquals("Permissions set should equal input", permissions, policyEntry.getPermissions());
    }

    @Test
    public void testGetCustomPermissions() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<? extends PolicyParser.PermissionEntry> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertEquals("Custom permissions set should equal input", customPermissions, policyEntry.getCustomPermissions());
    }

    @Test
    public void testGetCustomPermissions2() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<? extends PolicyParser.PermissionEntry> customPermissions = new HashSet<CustomPolicyViewer.DisplayablePermission>(){{
            add(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
            add(new CustomPolicyViewer.DisplayablePermission(PermissionType.RUNTIME_PERMISSION, PermissionTarget.CLASSLOADER));
        }};
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertEquals("Custom permissions set should equal input", customPermissions, policyEntry.getCustomPermissions());
    }

    @Test(expected = NullPointerException.class)
    public void testEntryWithNullPermissions() throws Exception {
        final String codebase = "http://example.com";
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        new PolicyEntry.Builder()
                .codebase(codebase)
                .permissions(null)
                .customPermissions(customPermissions)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testEntryWithNullCustomPermissions() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        new PolicyEntry.Builder()
                .codebase(codebase)
                .permissions(permissions)
                .customPermissions(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullPermissionsNotAllowed() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(null);
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY));
        new PolicyEntry.Builder()
            .codebase(codebase)
            .permissions(permissions)
            .customPermissions(customPermissions)
            .build();
    }

    @Test
    public void testNullCustomPermissionsNotAllowed() throws Exception {
        final String codebase = "http://example.com";
        final Set<PolicyEditorPermissions> permissions = Collections.singleton(PolicyEditorPermissions.CLIPBOARD);
        final Set<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(null);
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                                                .codebase(codebase)
                                                .permissions(permissions)
                                                .customPermissions(customPermissions)
                                                .build();
        assertFalse("Custom permissions set should not contain null element", policyEntry.getCustomPermissions().contains(null));
    }

}
