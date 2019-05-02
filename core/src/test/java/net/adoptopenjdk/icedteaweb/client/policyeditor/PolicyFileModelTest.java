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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * See PolicyEditorParsingTest, which covers PolicyFileModel#openAndParsePolicyFile(),
 * PolicyFileModel.parsePolicyString(), PolicyFileModel#savePolicyFile()
 */
public class PolicyFileModelTest {

    private static final String EXAMPLE_CODEBASE = "http://example.com";
    private static final String EXAMPLE_CA = "someCA";
    private static final Collection<PolicyParser.PrincipalEntry> EMPTY_PRINCIPALS = Collections.emptyList();
    private static final PolicyIdentifier EXAMPLE_IDENTIFIER = new PolicyIdentifier(EXAMPLE_CA, EMPTY_PRINCIPALS, EXAMPLE_CODEBASE);
    private static final PolicyIdentifier INVALID_IDENTIFIER = new PolicyIdentifier(null, EMPTY_PRINCIPALS, "invalidURL");
    private static final String LINEBREAK = System.getProperty(LINE_SEPARATOR);

    private static final String EXAMPLE_POLICY_1 = "grant {" + LINEBREAK
                                                           + "\tpermission some.java.permission \"somePermission\";" + LINEBREAK
                                                           + "};" + LINEBREAK;
    private static final String EXAMPLE_POLICY_2 = "grant {" + LINEBREAK
                                                           + "\tpermission some.other.java.permission \"somePermission\";" + LINEBREAK
                                                           + "};" + LINEBREAK;

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
    public void testSavePolicyFile() throws Exception {
        File file = new File(tempFilePath);
        BasicFileUtils.saveFile(EXAMPLE_POLICY_1, new File(tempFilePath));
        assertEquals("policy file size", EXAMPLE_POLICY_1.length(), file.length());
        
        model.setFile(file);
        model.openAndParsePolicyFile();
        model.savePolicyFile();

        assertTrue("policy file size isn't at least " + EXAMPLE_POLICY_1.length() + " bytes", file.length() >= EXAMPLE_POLICY_1.length());
    }

    @Test
    public void testHasChangedIsFalseInitially() throws Exception {
        assertFalse("Model should not report changes made initially", model.hasChanged());
    }

    @Test
    public void testFileHasChangedWithChange() throws Exception {
        assertFalse("Model should not report changes made initially", model.hasChanged());
        BasicFileUtils.saveFile(EXAMPLE_POLICY_1, new File(tempFilePath));
        model.openAndParsePolicyFile();
        BasicFileUtils.saveFile(EXAMPLE_POLICY_2, new File(tempFilePath));
        assertTrue("File should be marked changed after being externally modified", model.hasChanged());
    }

    @Test
    public void testAddIdentifier() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        assertEquals("Should have the identifier", Collections.singleton(EXAMPLE_IDENTIFIER), model.getIdentifiers());
    }

    @Test
    public void testRemoveIdentifier() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        assertEquals("Should have the identifier " + EXAMPLE_IDENTIFIER, Collections.singleton(EXAMPLE_IDENTIFIER), model.getIdentifiers());
        model.removeIdentifier(EXAMPLE_IDENTIFIER);
        assertEquals("Should not have any identifiers after removed", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
    }

    @Test
    public void testClearPermissions() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        model.setPermission(EXAMPLE_IDENTIFIER, permission, true);
        assertTrue("Expected permission " + permission, model.getPermission(EXAMPLE_IDENTIFIER, permission));
        model.clearPermissions();
        assertFalse("Expected no permission " + permission, model.getPermission(EXAMPLE_IDENTIFIER, permission));
        assertEquals("Expected no permissions ", Collections.<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>>emptyMap(), model.getCopyOfPermissions());
    }

    @Test
    public void testSettersAndGettersForPermission() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final PolicyEditorPermissions permission = PolicyEditorPermissions.CLIPBOARD;
        model.setPermission(EXAMPLE_IDENTIFIER, permission, true);
        assertTrue("Expected permission " + permission, model.getPermission(EXAMPLE_IDENTIFIER, permission));
    }

    @Test
    public void testClearCustomCodebase() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final PolicyParser.PermissionEntry customPermission = new CustomPolicyViewer.DisplayablePermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<PolicyParser.PermissionEntry> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(EXAMPLE_IDENTIFIER, customPermissions);
        assertEquals("Expected custom permission", customPermissions, model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER));
        model.clearCustomIdentifier(EXAMPLE_IDENTIFIER);
        final Set<PolicyParser.PermissionEntry> result = model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER);
        assertTrue("Custom permissions were expected to be empty, was: " + result, result.isEmpty());
    }

    @Test
    public void testClearCustomPermission() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final PolicyParser.PermissionEntry customPermission = new CustomPolicyViewer.DisplayablePermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<PolicyParser.PermissionEntry> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(EXAMPLE_IDENTIFIER, customPermissions);
        assertEquals("Expected custom permission", customPermissions, model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER));
        model.clearCustomPermissions();
        assertEquals("Custom permissions were expected to be empty", null, model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER));
        final Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> result = model.getCopyOfCustomPermissions();
        assertTrue("All identifier custom permissions were expected to be empty, was: " + result, result.isEmpty());
    }

    @Test
    public void testAddCustomPermissions() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final PolicyParser.PermissionEntry customPermission = new CustomPolicyViewer.DisplayablePermission(PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.FILE_ALL);
        final Collection<PolicyParser.PermissionEntry> customPermissions = Collections.singleton(customPermission);
        model.addCustomPermissions(EXAMPLE_IDENTIFIER, customPermissions);
        assertEquals("Expected file/user home/all-actions permission ", customPermissions, model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER));
        final CustomPolicyViewer.DisplayablePermission customPermission2 = new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY);
        final Collection<CustomPolicyViewer.DisplayablePermission> customPermissions2 = Collections.singleton(customPermission2);
        model.addCustomPermissions(EXAMPLE_IDENTIFIER, customPermissions2);
        assertTrue("Expected audio play permission ", model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER).contains(customPermission2));
        final HashSet<PolicyParser.PermissionEntry> customPermissionHashSet = new HashSet<>();
        customPermissionHashSet.add(customPermission);
        customPermissionHashSet.add(customPermission2);
        assertEquals("Expected custom permission ", customPermissionHashSet, model.getCopyOfCustomPermissions().get(EXAMPLE_IDENTIFIER));
    }

    @Test
    public void testAllPermissionsAreFalseInitially() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final Map<PolicyEditorPermissions, Boolean> policyEditorPermissions = model.getCopyOfPermissions().get(EXAMPLE_IDENTIFIER);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : policyEditorPermissions.entrySet()) {
            assertFalse("Expected " + entry.getKey() + " to be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsAreInitialized() throws Exception {
        assertEquals("Should not have any identifiers initially", Collections.<PolicyIdentifier>emptySet(), model.getIdentifiers());
        model.addIdentifier(EXAMPLE_IDENTIFIER);
        final Map<PolicyEditorPermissions, Boolean> policyEditorPermissions = model.getCopyOfPermissions().get(EXAMPLE_IDENTIFIER);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            assertTrue(perm + " should have been present as a key", policyEditorPermissions.containsKey(perm));
        }
    }

    @Test
    public void testGetCopyOfPermissionsIsCopy() throws Exception {
        final Map<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>> codebasePermissionsMap = model.getCopyOfPermissions();
        assertEquals("Map should be initially empty", Collections.<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>>emptyMap(), codebasePermissionsMap);
        codebasePermissionsMap.put(INVALID_IDENTIFIER, Collections.singletonMap(PolicyEditorPermissions.CLIPBOARD, true));
        final Map<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>> codebasePermissionsMap2 = model.getCopyOfPermissions();
        assertEquals("New copy should be initially empty", Collections.<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>>emptyMap(), codebasePermissionsMap2);
        assertNotEquals("Modified map should not equal newly copied map", codebasePermissionsMap, codebasePermissionsMap2);
    }

    @Test
    public void testGetCopyOfCustomPermissionsIsCopy() throws Exception {
        final Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> codebasePermissionsMap = model.getCopyOfCustomPermissions();
        assertEquals("Map should be initially empty", Collections.<PolicyIdentifier, Set<PolicyParser.PermissionEntry>>emptyMap(), codebasePermissionsMap);
        codebasePermissionsMap.put(INVALID_IDENTIFIER, Collections.singleton((PolicyParser.PermissionEntry) new CustomPolicyViewer.DisplayablePermission(PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY)));
        final Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> codebasePermissionsMap2 = model.getCopyOfCustomPermissions();
        assertEquals("New copy should be initially empty", Collections.<PolicyIdentifier, Set<PolicyParser.PermissionEntry>>emptyMap(), codebasePermissionsMap2);
        assertNotEquals("Modified set should not equal newly copied set", codebasePermissionsMap, codebasePermissionsMap2);
    }
}
