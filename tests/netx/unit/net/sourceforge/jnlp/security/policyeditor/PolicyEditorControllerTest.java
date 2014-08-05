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

import net.sourceforge.jnlp.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PolicyEditorControllerTest {

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
        // #setFile() counts as a change made
        assertTrue("Controller should report changes made initially", controller.changesMade());
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
        final String codebase = "http://example.com";
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        final PolicyEntry policyEntry = new PolicyEntry(codebase, permissions, customPermissions);
        FileUtils.saveFile(policyEntry.toString(), new File(tempFilePath));
        controller.openAndParsePolicyFile();
        final Collection<PolicyEditorPermissions> editorPermissions2 = Collections.singleton(PolicyEditorPermissions.ALL_AWT);
        final PolicyEntry policyEntry2 = new PolicyEntry(codebase, editorPermissions2, customPermissions);
        FileUtils.saveFile(policyEntry2.toString(), new File(tempFilePath));
        assertTrue("File should be marked changed after being externally modified", controller.fileHasChanged());
    }

    @Test
    public void testInitialCodebase() throws Exception {
        final Collection<String> initialCodebases = controller.getCodebases();
        assertEquals("Controller should have no codebases to begin with", 0, initialCodebases.size());
    }

    @Test
    public void testAddCodebase() throws Exception {
        final String urlString = "http://example.com";
        controller.addCodebase(urlString);
        final Collection<String> codebases = controller.getCodebases();
        assertTrue("Controller should have http://example.com", codebases.contains(urlString));
        assertEquals("Controller should only have two codebases", 1, codebases.size());
    }

    @Test
    public void testAddMultipleCodebases() throws Exception {
        final Set<String> toAdd = new HashSet<String>();
        toAdd.add("http://example.com");
        toAdd.add("http://icedtea.classpath.org");
        for (final String cb : toAdd) {
            controller.addCodebase(cb);
        }
        final Collection<String> codebases = controller.getCodebases();
        for (final String codebase : toAdd) {
            assertTrue("Controller should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testRemoveCodebase() throws Exception {
        final String urlString = "http://example.com";
        controller.addCodebase(urlString);
        final Collection<String> codebases = controller.getCodebases();
        assertTrue("Controller should have http://example.com", codebases.contains(urlString));
        assertEquals("Controller should only have one codebase", 1, codebases.size());
        controller.removeCodebase(urlString);
        final Collection<String> afterRemove = controller.getCodebases();
        assertFalse("Controller should not have http://example.com. Contained: " + afterRemove, afterRemove.contains(urlString));
        assertEquals("Controller should have no codebases", 0, afterRemove.size());
    }

    @Test
    public void testCopyPasteCodebase() throws Exception {
        final String copyUrl = "http://example.com";
        final String pasteUrl = "http://example.com/example";
        final PolicyEditorPermissions clipBoard = PolicyEditorPermissions.CLIPBOARD;
        controller.addCodebase(copyUrl);
        controller.setPermission(copyUrl, clipBoard, Boolean.TRUE);
        final Collection<String> beforePasteCodebases = controller.getCodebases();
        assertTrue("Controller should contain original codebase: " + copyUrl, beforePasteCodebases.contains(copyUrl));
        assertTrue(copyUrl + " should have " + clipBoard, controller.getPermissions(copyUrl).get(clipBoard));
        controller.copyCodebaseToClipboard(copyUrl);
        final PolicyEntry clipboardEntry = PolicyEditorController.getPolicyEntryFromClipboard();
        controller.addPolicyEntry(new PolicyEntry(pasteUrl, clipboardEntry.getPermissions(), clipboardEntry.getCustomPermissions()));
        final Collection<String> afterPasteCodebases = controller.getCodebases();
        assertTrue("Controller should still contain original codebase: " + copyUrl, afterPasteCodebases.contains(copyUrl));
        assertTrue("Controller should also contain pasted codebase:" + pasteUrl, afterPasteCodebases.contains(pasteUrl));
        assertTrue(copyUrl + " should have " + clipBoard, controller.getPermissions(copyUrl).get(clipBoard));
        assertTrue(pasteUrl + " should have " + clipBoard, controller.getPermissions(pasteUrl).get(clipBoard));
    }

    @Test
    public void testAddPolicyEntry() throws Exception {
        final String codebase = "http://example.com";
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        final PolicyEntry policyEntry = new PolicyEntry(codebase, permissions, customPermissions);
        controller.addPolicyEntry(policyEntry);
        final Collection<String> codebases = controller.getCodebases();
        assertTrue("Controller should have " + codebase, codebases.contains(codebase));
        assertEquals("Controller should only have one codebase", 1, codebases.size());
        assertTrue("Controller should have granted " + editorPermissions, controller.getPermission(codebase, editorPermissions));
        assertTrue("Controller should have granted " + customPermission, controller.getCustomPermissions(codebase).contains(customPermission));
    }

    @Test
    public void testAddCustomPermissionNoActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPermission customPermission = new CustomPermission("java.lang.RuntimePermission", "createClassLoader");
        controller.addCustomPermission(codebase, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(codebase).contains(customPermission));
    }

    @Test
    public void testAddCustomPermissionEmptyActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPermission customPermission = new CustomPermission("java.lang.RuntimePermission", "createClassLoader", "");
        controller.addCustomPermission(codebase, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(codebase).contains(customPermission));
    }

    @Test
    public void testClearCustomPermissionsNoActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPermission customPermission = new CustomPermission("java.lang.RuntimePermission", "createClassLoader");
        controller.addCustomPermission(codebase, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(codebase).contains(customPermission));
        controller.clearCustomCodebase(codebase);
        assertEquals(0, controller.getCustomPermissions(codebase).size());
    }

    @Test
    public void testClearCustomPermissionsEmptyActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPermission customPermission = new CustomPermission("java.lang.RuntimePermission", "createClassLoader", "");
        controller.addCustomPermission(codebase, customPermission);
        assertTrue("Controller custom permissions should include " + customPermission + " but did not", controller.getCustomPermissions(codebase).contains(customPermission));
        controller.clearCustomCodebase(codebase);
        assertEquals(0, controller.getCustomPermissions(codebase).size());
    }

    @Test
    public void testReturnedCodebasesIsCopy() throws Exception {
        final Collection<String> original = controller.getCodebases();
        original.add("some invalid value");
        original.remove("");
        final Collection<String> second = controller.getCodebases();
        assertEquals("Controller should have no codebases", 0, second.size());
    }

    @Test
    public void testReturnedPermissionsMapIsCopy() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> original = controller.getPermissions("");
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            original.put(perm, true);
        }
        final Map<PolicyEditorPermissions, Boolean> second = controller.getPermissions("");
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : second.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testReturnedCustomPermissionsSetIsCopy() throws Exception {
        final Collection<CustomPermission> original = controller.getCustomPermissions("");
        assertTrue("There should not be any custom permissions to start", original.isEmpty());
        original.add(new CustomPermission("java.io.FilePermission", "*", "write"));
        final Collection<CustomPermission> second = controller.getCustomPermissions("");
        assertTrue("The custom permission should not have been present", second.isEmpty());
    }

    @Test
    public void testDefaultPermissionsAllFalse() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = controller.getPermissions("");
        controller.addCodebase("http://example.com");
        final Map<PolicyEditorPermissions, Boolean> addedMap = controller.getPermissions("http://example.com");
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : defaultMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : addedMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsRepresented() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = controller.getPermissions("");
        controller.addCodebase("http://example.com");
        final Map<PolicyEditorPermissions, Boolean> addedMap = controller.getPermissions("http://example.com");
        assertTrue("Default codebase permissions keyset should be the same size as enum values set",
                defaultMap.keySet().size() == PolicyEditorPermissions.values().length);
        assertTrue("Added codebase permissions keyset should be the same size as enum values set",
                addedMap.keySet().size() == PolicyEditorPermissions.values().length);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue("Permission " + perm + " should be in the editor's codebase keyset", defaultMap.keySet().contains(perm));
        }
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue("Permission " + perm + " should be in the editor's codebase keyset", addedMap.keySet().contains(perm));
        }
    }

    @Test
    public void testSetGetPermission() throws Exception {
        final String codebase = "http://example.com";
        controller.addCodebase(codebase);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        assertFalse("Clipboard permission should not be initially granted", controller.getPermission(codebase, permission));
        controller.setPermission(codebase, permission, true);
        assertTrue("Clipboard permission should be granted after being set", controller.getPermission(codebase, permission));
    }

    @Test
    public void testClearPermission() throws Exception {
        final String codebase = "http://example.com";
        controller.addCodebase(codebase);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        assertFalse("Clipboard permission should not be initially granted", controller.getPermission(codebase, permission));
        controller.setPermission(codebase, permission, true);
        assertTrue("Clipboard permission should be granted after being set", controller.getPermission(codebase, permission));
        controller.clearPermissions();
        for (final String cb : controller.getCodebases()) {
            for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : controller.getPermissions(cb).entrySet()) {
                assertFalse("Permission " + entry.getKey() + " should be false for codebase " + cb, entry.getValue());
            }
        }
        assertEquals(0, controller.getCodebases().size());
    }

    @Test
    public void testCodebaseTrailingSlashesDoNotMatch() throws Exception {
        final Collection<String> toAdd = Arrays.asList("http://redhat.com", "http://redhat.com/");
        for (final String cb : toAdd) {
            controller.addCodebase(cb);
        }
        final Collection<String> codebases = controller.getCodebases();
        for (final String codebase : toAdd) {
            assertTrue("Controller should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testOpenAndParsePolicyFile() throws Exception {
        final String codebase = "http://example.com";
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPermission customPermission = new CustomPermission("com.example.CustomPermission", PermissionTarget.USER_HOME.target, PermissionActions.FILE_ALL.rawString());
        final Collection<CustomPermission> customPermissions = new HashSet<>(Collections.singleton(customPermission));
        final PolicyEntry policyEntry = new PolicyEntry(codebase, permissions, customPermissions);
        FileUtils.saveFile(policyEntry.toString(), new File(tempFilePath));
        controller.openAndParsePolicyFile();
        assertEquals("Controller should have one codebase", 1, controller.getCodebases().size());
        assertTrue("Controller should have codebase " + codebase, controller.getCodebases().contains(codebase));
        assertTrue("Controller should grant " + editorPermissions, controller.getPermission(codebase, editorPermissions));
        assertEquals("Custom permission sets were not equal", customPermissions, controller.getCustomPermissions(codebase));
    }

    @Test
    public void testSavePolicyFile() throws Exception {
        final String codebase = "http://example.com";
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        final PolicyEntry policyEntry = new PolicyEntry(codebase, permissions, customPermissions);
        controller.addPolicyEntry(policyEntry);
        controller.savePolicyFile();
        final String fileContent = FileUtils.loadFileAsString(new File(tempFilePath));
        assertTrue("Saved file should contain policy entry as string", fileContent.contains(policyEntry.toString()));
    }
}
