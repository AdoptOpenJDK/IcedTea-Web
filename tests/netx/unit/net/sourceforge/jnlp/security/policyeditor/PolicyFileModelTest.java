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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * See PolicyEditorParsingTest, which covers PolicyFileModel#openAndParsePolicyFile(),
 * PolicyFileModel.parsePolicyString(), PolicyFileModel#savePolicyFile()
 */
public class PolicyFileModelTest {

    private String tempFilePath;
    private PolicyFileModel model;

    @Before
    public void setNewTempfile() throws Exception {
        tempFilePath = File.createTempFile("policyeditor", null).getCanonicalPath();
        model = new PolicyFileModel();
        model.setFile(new File(tempFilePath));
    }

    @Test
    public void testSetGetFile() throws Exception {
        assertEquals(new File(tempFilePath), model.getFile());
    }

    @Test
    public void testSetFileNull() throws Exception {
        model.setFile(null);

        assertEquals(null, model.getFile());
    }

    @Test(expected = NullPointerException.class)
    public void testOpenAndParsePolicyFileWithFileNull() throws Exception {
        model.setFile(null);

        assertEquals(null, model.getFile());
        model.openAndParsePolicyFile();
    }

    @Test(expected = NullPointerException.class)
    public void testSavePolicyFileWithFileNull() throws Exception {
        model.setFile(null);

        assertEquals(null, model.getFile());
        model.savePolicyFile();
    }

    @Test
    public void testHasChangedIsFalseInitially() throws Exception {
        assertFalse("Model should not report changes made initially", model.hasChanged());
    }

    @Test
    public void testFileHasChangedWithChange() throws Exception {
        assertFalse("Model should not report changes made initially", model.hasChanged());
        final String codebase = "http://example.com";
        final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.CLIPBOARD;
        final Collection<PolicyEditorPermissions> permissions = Collections.singleton(editorPermissions);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        final PolicyEntry policyEntry = new PolicyEntry(codebase, permissions, customPermissions);
        FileUtils.saveFile(policyEntry.toString(), new File(tempFilePath));
        model.openAndParsePolicyFile();
        final Collection<PolicyEditorPermissions> editorPermissions2 = Collections.singleton(PolicyEditorPermissions.ALL_AWT);
        final PolicyEntry policyEntry2 = new PolicyEntry(codebase, editorPermissions2, customPermissions);
        FileUtils.saveFile(policyEntry2.toString(), new File(tempFilePath));
        assertTrue("File should be marked changed after being externally modified", model.hasChanged());
    }

    @Test
    public void testAddCodebase() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        assertEquals("Should have the codebase", Collections.singleton(codebase), model.getCodebases());
    }

    @Test
    public void testRemoveCodebase() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        assertEquals("Should have the codebase " + codebase, Collections.singleton(codebase), model.getCodebases());
        model.removeCodebase(codebase);
        assertEquals("Should not have any codebases after removed", Collections.emptySet(), model.getCodebases());
    }

    @Test
    public void testClearPermissions() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        model.setPermission(codebase, permission, true);
        assertTrue("Expected permission " + permission, model.getPermission(codebase, permission));
        model.clearPermissions();
        assertFalse("Expected no permission " + permission, model.getPermission(codebase, permission));
        assertEquals("Expected no permissions ", Collections.emptyMap(), model.getCopyOfPermissions());
    }

    @Test
    public void testSettersAndGettersForPermission() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        model.setPermission(codebase, permission, true);
        assertTrue("Expected permission " + permission, model.getPermission(codebase, permission));
    }

    @Test
    public void testClearCustomCodebase() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(codebase, customPermissions);
        assertEquals("Expected custom permission", customPermissions, model.getCopyOfCustomPermissions().get(codebase));
        model.clearCustomCodebase(codebase);
        assertEquals("Custom permissions were expected to be empty", Collections.emptySet(), model.getCopyOfCustomPermissions().get(codebase));
    }

    @Test
    public void testClearCustomPermission() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(codebase, customPermissions);
        assertEquals("Expected custom permission", customPermissions, model.getCopyOfCustomPermissions().get(codebase));
        model.clearCustomPermissions();
        assertEquals("Custom permissions were expected to be empty", null, model.getCopyOfCustomPermissions().get(codebase));
        assertEquals("All codebase custom permissions were expected to be empty", Collections.emptyMap(), model.getCopyOfCustomPermissions());
    }

    @Test
    public void testAddCustomPermissions() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final CustomPermission customPermission = new CustomPermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<CustomPermission> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(codebase, customPermissions);
        assertEquals("Expected file/user home/all-actions permission ", customPermissions, model.getCopyOfCustomPermissions().get(codebase));
        final CustomPermission customPermission2 = new CustomPermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY);
        final Collection<CustomPermission> customPermissions2 = Collections.singleton(customPermission2);
        model.addCustomPermissions(codebase, customPermissions2);
        assertTrue("Expected audio play permission ", model.getCopyOfCustomPermissions().get(codebase).contains(customPermission2));
        final HashSet<CustomPermission> customPermissionHashSet = new HashSet<>();
        customPermissionHashSet.add(customPermission);
        customPermissionHashSet.add(customPermission2);
        assertEquals("Expected custom permission ", customPermissionHashSet, model.getCopyOfCustomPermissions().get(codebase));
    }

    @Test
    public void testAllPermissionsAreFalseInitially() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final Map<PolicyEditorPermissions, Boolean> policyEditorPermissions = model.getCopyOfPermissions().get(codebase);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : policyEditorPermissions.entrySet()) {
            assertFalse("Expected " + entry.getKey() + " to be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsAreInitialized() throws Exception {
        assertEquals("Should not have any codebases initially", Collections.emptySet(), model.getCodebases());
        final String codebase = "http://example.com";
        model.addCodebase(codebase);
        final Map<PolicyEditorPermissions, Boolean> policyEditorPermissions = model.getCopyOfPermissions().get(codebase);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue(perm + " should have been present as a key", policyEditorPermissions.containsKey(perm));
        }
    }

    @Test
    public void testGetCopyOfPermissionsIsCopy() throws Exception {
        final Map<String, Map<PolicyEditorPermissions, Boolean>> codebasePermissionsMap = model.getCopyOfPermissions();
        assertEquals("Map should be initially empty", Collections.emptyMap(), codebasePermissionsMap);
        codebasePermissionsMap.put("invalid codebase", Collections.singletonMap(PolicyEditorPermissions.CLIPBOARD, true));
        final Map<String, Map<PolicyEditorPermissions, Boolean>> codebasePermissionsMap2 = model.getCopyOfPermissions();
        assertEquals("New copy should be initially empty", Collections.emptyMap(), codebasePermissionsMap2);
        assertNotEquals("Modified map should not equal newly copied map", codebasePermissionsMap, codebasePermissionsMap2);
    }

    @Test
    public void testGetCopyOfCustomPermissionsIsCopy() throws Exception {
        final Map<String, Set<CustomPermission>> codebasePermissionsMap = model.getCopyOfCustomPermissions();
        assertEquals("Map should be initially empty", Collections.emptyMap(), codebasePermissionsMap);
        codebasePermissionsMap.put("invalid codebase", Collections.singleton(new CustomPermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY)));
        final Map<String, Set<CustomPermission>> codebasePermissionsMap2 = model.getCopyOfCustomPermissions();
        assertEquals("New copy should be initially empty", Collections.emptyMap(), codebasePermissionsMap2);
        assertNotEquals("Modified set should not equal newly copied set", codebasePermissionsMap, codebasePermissionsMap2);
    }
}
