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

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import org.junit.Before;
import org.junit.Test;
import sun.security.provider.PolicyParser;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolicyEditorControllerTest {

    private static final String SIGNED_BY = "someCA";
    private static final String CODEBASE = "http://example.com";
    private static final List<PolicyParser.PrincipalEntry> EMPTY_PRINCIPALS = Collections.emptyList();
    private static final PolicyIdentifier DEFAULT_IDENTIFIER = new PolicyIdentifier(SIGNED_BY, EMPTY_PRINCIPALS, CODEBASE);
    private static final String LINEBREAK = System.getProperty(LINE_SEPARATOR);

    private static final String EXAMPLE_POLICY_1 = "grant {" + LINEBREAK
            + "permission some.java.permission \"somePermission\";" + LINEBREAK
            + "};" + LINEBREAK;
    private static final String EXAMPLE_POLICY_2 = "grant {" + LINEBREAK
            + "permission some.other.java.permission \"somePermission\";" + LINEBREAK
            + "};" + LINEBREAK;

    private static final String CLIPBOARD_POLICY = "grant codeBase \"http://example.com\" {" + LINEBREAK
            + "permission java.awt.AWTPermission \"accessClipboard\";" + LINEBREAK
           + "};" + LINEBREAK;

    private String tempFilePath;
    private PolicyEditorController controller;

    @Before
    public void setNewTempfile() throws Exception {
        tempFilePath = File.createTempFile("policyeditor", null).getCanonicalPath();
        controller = new PolicyEditorController();
        controller.setFile(new File(tempFilePath));
    }

    @Test
    public void testChangesMadeInitiallyFalse() throws Exception {
        assertFalse("Controller should report changes made initially", controller.changesMade());
    }

    @Test
    public void testChangesMade() throws Exception {
        controller.setChangesMade(false);
        assertFalse("Controller should have changes made marked false after being explicitly set", controller.changesMade());
    }

    @Test
    public void testFileHasChanged() throws Exception {
        assertFalse("File should not have been changed initially", controller.fileHasChanged());
    }

    @Test
    public void testFileHasChangedWithChange() throws Exception {
        assertFalse("Controller should report file has changed initially", controller.fileHasChanged());
        BasicFileUtils.saveFile(EXAMPLE_POLICY_1, new File(tempFilePath));
        controller.openAndParsePolicyFile();
        BasicFileUtils.saveFile(EXAMPLE_POLICY_2, new File(tempFilePath));
        assertTrue("File should be marked changed after being externally modified", controller.fileHasChanged());
    }

    @Test
    public void testInitialIdentifier() throws Exception {
        final Collection<PolicyIdentifier> initialIdentifiers = controller.getIdentifiers();
        assertEquals("Controller should have no identifiers to begin with", 0, initialIdentifiers.size());
    }

    @Test
    public void testAddIdentifier() throws Exception {
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final Collection<PolicyIdentifier> identifiers = controller.getIdentifiers();
        assertTrue("Controller should have " + DEFAULT_IDENTIFIER, identifiers.contains(DEFAULT_IDENTIFIER));
        assertEquals("Controller should only have two identifiers", 1, identifiers.size());
    }

    @Test
    public void testAddMultipleIdentifiers() throws Exception {
        final Set<PolicyIdentifier> toAdd = new HashSet<>();
        toAdd.add(DEFAULT_IDENTIFIER);
        final PolicyIdentifier newIdentifier = new PolicyIdentifier(null, EMPTY_PRINCIPALS, "http://icedtea.classpath.org");
        toAdd.add(newIdentifier);
        for (final PolicyIdentifier id : toAdd) {
            controller.addIdentifier(id);
        }
        final Collection<PolicyIdentifier> identifiers = controller.getIdentifiers();
        for (final PolicyIdentifier id : toAdd) {
            assertTrue("Controller should have " + id, identifiers.contains(id));
        }
    }

    @Test
    public void testRemoveIdentifier() throws Exception {
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final Collection<PolicyIdentifier> identifiers = controller.getIdentifiers();
        assertTrue("Controller should have " + DEFAULT_IDENTIFIER, identifiers.contains(DEFAULT_IDENTIFIER));
        assertEquals("Controller should only have one identifier", 1, identifiers.size());
        controller.removeIdentifier(DEFAULT_IDENTIFIER);
        final Collection<PolicyIdentifier> afterRemove = controller.getIdentifiers();
        assertFalse("Controller should not have " + DEFAULT_IDENTIFIER + ". Contained: " + afterRemove, afterRemove.contains(DEFAULT_IDENTIFIER));
        assertEquals("Controller should have no identifiers", Collections.<PolicyIdentifier>emptySet(), afterRemove);
    }

    @Test
    public void testCopyPasteIdentifiers() throws Exception {
        final String pasteUrl = "http://example.com/example";
        final PolicyIdentifier pasteIdentifier = new PolicyIdentifier(null, EMPTY_PRINCIPALS, pasteUrl);
        final PolicyEditorPermissions clipBoard = PolicyEditorPermissions.CLIPBOARD;
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        controller.setPermission(DEFAULT_IDENTIFIER, clipBoard, Boolean.TRUE);
        final Collection<PolicyIdentifier> beforePasteIdentifiers = controller.getIdentifiers();
        assertTrue("Controller should contain original identifier: " + DEFAULT_IDENTIFIER, beforePasteIdentifiers.contains(DEFAULT_IDENTIFIER));
        assertTrue(DEFAULT_IDENTIFIER + " should have " + clipBoard, controller.getPermissions(DEFAULT_IDENTIFIER).get(clipBoard));
        controller.copyPolicyEntryToClipboard(DEFAULT_IDENTIFIER);
        final PolicyEntry clipboardEntry = PolicyEditorController.getPolicyEntryFromClipboard();
        final PolicyEntry newEntry = new PolicyEntry.Builder()
                .codebase(pasteUrl)
                .permissions(clipboardEntry.getPermissions())
                .customPermissions(clipboardEntry.getCustomPermissions())
                .build();
        controller.addPolicyEntry(newEntry);
        final Collection<PolicyIdentifier> afterPasteIdentifiers = controller.getIdentifiers();
        assertTrue("Controller should still contain original identifier: " + DEFAULT_IDENTIFIER, afterPasteIdentifiers.contains(DEFAULT_IDENTIFIER));
        assertTrue("Controller should also contain pasted identifier:" + pasteIdentifier, afterPasteIdentifiers.contains(pasteIdentifier));
        assertTrue(DEFAULT_IDENTIFIER + " should have " + clipBoard, controller.getPermissions(DEFAULT_IDENTIFIER).get(clipBoard));
        assertTrue(pasteIdentifier + " should have " + clipBoard, controller.getPermissions(pasteIdentifier).get(clipBoard));
    }

    @Test
    public void testAddPolicyEntry() throws Exception {
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPolicyViewer.DisplayablePermission> customPermissions = Collections.singleton(customPermission);
        final PolicyEntry policyEntry = new PolicyEntry.Builder()
                .identifier(DEFAULT_IDENTIFIER)
                .permissions(permissions)
                .customPermissions(customPermissions)
                .build();
        controller.addPolicyEntry(policyEntry);
        final Collection<PolicyIdentifier> identifiers = controller.getIdentifiers();
        assertTrue("Controller should have " + DEFAULT_IDENTIFIER, identifiers.contains(DEFAULT_IDENTIFIER));
        assertEquals("Controller should only have one identifier", 1, identifiers.size());
        assertTrue("Controller should have granted " + editorPermissions, controller.getPermission(DEFAULT_IDENTIFIER, editorPermissions));
        assertTrue("Controller should have granted " + customPermission, controller.getCustomPermissions(DEFAULT_IDENTIFIER).contains(customPermission));
    }

    @Test
    public void testAddCustomPermissionNoActions() throws Exception {
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader");
        controller.addCustomPermission(DEFAULT_IDENTIFIER, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(DEFAULT_IDENTIFIER).contains(customPermission));
    }

    @Test
    public void testAddCustomPermissionEmptyActions() throws Exception {
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader", "");
        controller.addCustomPermission(DEFAULT_IDENTIFIER, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(DEFAULT_IDENTIFIER).contains(customPermission));
    }

    @Test
    public void testClearCustomPermissionsNoActions() throws Exception {
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader");
        controller.addCustomPermission(DEFAULT_IDENTIFIER, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(DEFAULT_IDENTIFIER).contains(customPermission));
        controller.clearCustomIdentifier(DEFAULT_IDENTIFIER);
        assertEquals(0, controller.getCustomPermissions(DEFAULT_IDENTIFIER).size());
    }

    @Test
    public void testClearCustomPermissionsEmptyActions() throws Exception {
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader", "");
        controller.addCustomPermission(DEFAULT_IDENTIFIER, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(DEFAULT_IDENTIFIER).contains(customPermission));
        controller.clearCustomIdentifier(DEFAULT_IDENTIFIER);
        assertEquals(0, controller.getCustomPermissions(DEFAULT_IDENTIFIER).size());
    }

    @Test
    public void testReturnedIdentifiersIsCopy() throws Exception {
        final Collection<PolicyIdentifier> original = controller.getIdentifiers();
        original.add(new PolicyIdentifier("invalidSigner", EMPTY_PRINCIPALS, "invalidURL"));
        original.remove(DEFAULT_IDENTIFIER);
        final Collection<PolicyIdentifier> second = controller.getIdentifiers();
        assertEquals("Controller should have no identifiers", 0, second.size());
    }

    @Test
    public void testReturnedPermissionsMapIsCopy() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> original = controller.getPermissions(DEFAULT_IDENTIFIER);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            original.put(perm, true);
        }
        final Map<PolicyEditorPermissions, Boolean> second = controller.getPermissions(DEFAULT_IDENTIFIER);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : second.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testReturnedCustomPermissionsSetIsCopy() throws Exception {
        final Collection<PolicyParser.PermissionEntry> original = controller.getCustomPermissions(DEFAULT_IDENTIFIER);
        assertTrue("There should not be any custom permissions to start", original.isEmpty());
        original.add(new CustomPolicyViewer.DisplayablePermission("java.io.FilePermission", "*", "write"));
        final Collection<PolicyParser.PermissionEntry> second = controller.getCustomPermissions(DEFAULT_IDENTIFIER);
        assertTrue("The custom permission should not have been present", second.isEmpty());
    }

    @Test
    public void testDefaultPermissionsAllFalse() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = controller.getPermissions(DEFAULT_IDENTIFIER);
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final Map<PolicyEditorPermissions, Boolean> addedMap = controller.getPermissions(DEFAULT_IDENTIFIER);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : defaultMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : addedMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsRepresented() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = controller.getPermissions(DEFAULT_IDENTIFIER);
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final Map<PolicyEditorPermissions, Boolean> addedMap = controller.getPermissions(DEFAULT_IDENTIFIER);
        assertTrue("Default identifier permissions keyset should be the same size as enum values set",
                defaultMap.keySet().size() == PolicyEditorPermissions.values().length);
        assertTrue("Added identifier permissions keyset should be the same size as enum values set",
                addedMap.keySet().size() == PolicyEditorPermissions.values().length);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue("Permission " + perm + " should be in the editor's identifier keyset", defaultMap.keySet().contains(perm));
        }
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue("Permission " + perm + " should be in the editor's identifier keyset", addedMap.keySet().contains(perm));
        }
    }

    @Test
    public void testSetGetPermission() throws Exception {
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        assertFalse("Clipboard permission should not be initially granted", controller.getPermission(DEFAULT_IDENTIFIER, permission));
        controller.setPermission(DEFAULT_IDENTIFIER, permission, true);
        assertTrue("Clipboard permission should be granted after being set", controller.getPermission(DEFAULT_IDENTIFIER, permission));
    }

    @Test
    public void testClearPermission() throws Exception {
        controller.addIdentifier(DEFAULT_IDENTIFIER);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        assertFalse("Clipboard permission should not be initially granted", controller.getPermission(DEFAULT_IDENTIFIER, permission));
        controller.setPermission(DEFAULT_IDENTIFIER, permission, true);
        assertTrue("Clipboard permission should be granted after being set", controller.getPermission(DEFAULT_IDENTIFIER, permission));
        controller.clearPermissions();
        for (final PolicyIdentifier id : controller.getIdentifiers()) {
            for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : controller.getPermissions(id).entrySet()) {
                assertFalse("Permission " + entry.getKey() + " should be false for identifier " + id, entry.getValue());
            }
        }
        assertEquals(0, controller.getIdentifiers().size());
    }

    @Test
    public void testIdentifierCodebaseTrailingSlashesDoNotMatch() throws Exception {
        final PolicyIdentifier firstId = new PolicyIdentifier(SIGNED_BY, EMPTY_PRINCIPALS, "http://example.com");
        final PolicyIdentifier secondId = new PolicyIdentifier(SIGNED_BY, EMPTY_PRINCIPALS, "http://example.com");
        final Collection<PolicyIdentifier> toAdd = Arrays.asList(firstId, secondId);
        for (final PolicyIdentifier id : toAdd) {
            controller.addIdentifier(id);
        }
        final Collection<PolicyIdentifier> identifiers = controller.getIdentifiers();
        for (final PolicyIdentifier id : toAdd) {
            assertTrue("Controller should have " + id, identifiers.contains(id));
        }
    }

    @Test
    public void testOpenAndParsePolicyFile() throws Exception {
        final PolicyIdentifier exampleIdentifier = new PolicyIdentifier(null, Collections.<PolicyParser.PrincipalEntry>emptyList(), "http://example.com");
        BasicFileUtils.saveFile(CLIPBOARD_POLICY, new File(tempFilePath));
        controller.openAndParsePolicyFile();
        assertEquals("Controller should have one identifier", 1, controller.getIdentifiers().size());
        assertTrue("Controller should have identifier " + exampleIdentifier, controller.getIdentifiers().contains(exampleIdentifier));
        assertTrue("Controller should grant " + PolicyEditorPermissions.CLIPBOARD + " got: " + controller.getPermissions(exampleIdentifier) + " and: " + controller.getCustomPermissions(exampleIdentifier), controller.getPermission(exampleIdentifier, PolicyEditorPermissions.CLIPBOARD));
        assertEquals("Custom permission set should have been empty", Collections.<PolicyParser.PermissionEntry>emptySet(), controller.getCustomPermissions(exampleIdentifier));
    }

}
