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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.commandline.UnevenParameterException;
import net.sourceforge.jnlp.config.PathsAndFiles;
import org.junit.Before;
import org.junit.Test;
import sun.security.provider.PolicyParser;

import static net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.getCodebaseArgument;
import static net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.getFilePathArgument;
import static net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.getPrincipalsArgument;
import static net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.getSignedByArgument;
import static net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.identifierFromCodebase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolicyEditorTest {

    private String tempFilePath;
    private PolicyEditor editor;

    @Before
    public void setNewTempfile() throws Exception {
        tempFilePath = File.createTempFile("policyeditor", null).getCanonicalPath();
        editor = new PolicyEditor(tempFilePath);
        editor.openPolicyFileSynchronously();
    }

    @Test
    public void testInitialCodebase() throws Exception {
        final Collection<String> initialCodebases = editor.getCodebases();
        assertTrue("Editor should have one codebase to begin with", initialCodebases.size() == 1);
        assertTrue("Editor's initial codebase should be \"\" (empty string)",
                initialCodebases.toArray(new String[initialCodebases.size()])[0].equals(""));
    }

    @Test
    public void testAddCodebase() throws Exception {
        final String urlString = "http://example.com";
        final PolicyIdentifier identifier = identifierFromCodebase(urlString);
        editor.addNewEntry(identifier);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        assertTrue("Editor should have http://example.com", codebases.contains(urlString));
        assertTrue("Editor should only have two codebases", codebases.size() == 2);
    }

    @Test
    public void addMultipleCodebases() throws Exception {
        final Set<String> toAdd = new HashSet<>();
        toAdd.add("http://example.com");
        toAdd.add("http://icedtea.classpath.org");
        for (final String cb : toAdd) {
            editor.addNewEntry(identifierFromCodebase(cb));
        }
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        for (final String codebase : toAdd) {
            assertTrue("Editor should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testAddInvalidUrlCodebase() throws Exception {
        final String invalidUrl = "url.com"; // missing protocol -> invalid
        editor.addNewEntry(identifierFromCodebase(invalidUrl));
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        assertTrue("Editor should only have default codebase", codebases.size() == 1);
    }

    @Test
    public void testRemoveCodebase() throws Exception {
        final String urlString = "http://example.com";
        final PolicyIdentifier identifier = identifierFromCodebase(urlString);
        editor.addNewEntry(identifier);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        assertTrue("Editor should have http://example.com", codebases.contains(urlString));
        assertEquals("Editor should only have two codebases", codebases.size(), 2);
        editor.removeIdentifier(identifier);
        final Collection<String> afterRemove = editor.getCodebases();
        assertTrue("Editor should have default codebase", afterRemove.contains(""));
        assertFalse("Editor should not have http://example.com. Contained: " + afterRemove, afterRemove.contains(urlString));
        assertEquals("Editor should only have one codebase", afterRemove.size(), 1);
    }

    @Test
    public void testRenameCodebase() throws Exception {
        final String originalUrl = "http://example.com";
        final String renamedUrl = "http://example.com/example";
        final PolicyEditorPermissions clipBoard = PolicyEditorPermissions.CLIPBOARD;
        final PolicyIdentifier identifier = identifierFromCodebase(originalUrl);
        editor.addNewEntry(identifier);
        editor.setPermission(identifier, clipBoard, Boolean.TRUE);
        final Collection<String> beforeRenameCodebases = editor.getCodebases();
        assertTrue("Editor should contain " + originalUrl, beforeRenameCodebases.contains(originalUrl));
        assertTrue(originalUrl + " should have " + clipBoard, editor.getPermissions(identifier).get(clipBoard));
        editor.modifyCodebase(identifier, renamedUrl);
        final Collection<String> afterRenamedCodebases = editor.getCodebases();
        assertFalse("Editor should not contain old codebase: " + originalUrl, afterRenamedCodebases.contains(originalUrl));
        assertTrue("Editor should contain new codebase name: " + renamedUrl, afterRenamedCodebases.contains(renamedUrl));
        final PolicyIdentifier renamedIdentifier = identifierFromCodebase(renamedUrl);
        assertTrue("Renamed " + renamedUrl + " should have " + clipBoard, editor.getPermissions(renamedIdentifier).get(clipBoard));
    }

    @Test
    public void testCopyPasteCodebase() throws Exception {
        final String copyUrl = "http://example.com";
        final String pasteUrl = "http://example.com/example";
        final PolicyEditorPermissions clipBoard = PolicyEditorPermissions.CLIPBOARD;
        final PolicyIdentifier identifier = identifierFromCodebase(copyUrl);
        editor.addNewEntry(identifier);
        editor.setPermission(identifier, clipBoard, Boolean.TRUE);
        final Collection<String> beforePasteCodebases = editor.getCodebases();
        assertTrue("Editor should contain original codebase: " + copyUrl, beforePasteCodebases.contains(copyUrl));
        assertTrue(copyUrl + " should have " + clipBoard, editor.getPermissions(identifier).get(clipBoard));
        editor.copyEntry(identifier);
        final PolicyIdentifier pastedIdentifier = identifierFromCodebase(pasteUrl);
        editor.pasteEntry(pastedIdentifier);
        final Collection<String> afterPasteCodebases = editor.getCodebases();
        assertTrue("Editor should still contain original codebase: " + copyUrl, afterPasteCodebases.contains(copyUrl));
        assertTrue("Editor should also contain pasted codebase:" + pasteUrl, afterPasteCodebases.contains(pasteUrl));
        assertTrue(copyUrl + " should have " + clipBoard, editor.getPermissions(identifier).get(clipBoard));
        assertTrue(pasteUrl + " should have " + clipBoard, editor.getPermissions(pastedIdentifier).get(clipBoard));
    }

    @Test
    public void testAddCustomPermissionNoActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader");
        final PolicyIdentifier identifier = identifierFromCodebase(codebase);
        editor.addCustomPermission(identifier, customPermission);
        assertTrue("Editor custom permissions should include " + customPermission + " but did not", editor.getCustomPermissions(identifier).contains(customPermission));
    }

    @Test
    public void testAddCustomPermissionEmptyActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader", "");
        final PolicyIdentifier identifier = identifierFromCodebase(codebase);
        editor.addCustomPermission(identifier, customPermission);
        assertTrue("Editor custom permissions should include " + customPermission + " but did not", editor.getCustomPermissions(identifier).contains(customPermission));
    }

    @Test
    public void testClearCustomPermissionsNoActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader");
        final PolicyIdentifier identifier = identifierFromCodebase(codebase);
        editor.addCustomPermission(identifier, customPermission);
        assertTrue("Editor custom permissions should include " + customPermission + " but did not", editor.getCustomPermissions(identifier).contains(customPermission));
        editor.clearCustomPermissions(identifier);
        assertEquals(0, editor.getCustomPermissions(identifier).size());
    }

    @Test
    public void testClearCustomPermissionsEmptyActions() throws Exception {
        final String codebase = "http://example.com";
        final CustomPolicyViewer.DisplayablePermission customPermission = new CustomPolicyViewer.DisplayablePermission("java.lang.RuntimePermission", "createClassLoader", "");
        final PolicyIdentifier identifier = identifierFromCodebase(codebase);
        editor.addCustomPermission(identifier, customPermission);
        assertTrue("Editor custom permissions should include " + customPermission + " but did not", editor.getCustomPermissions(identifier).contains(customPermission));
        editor.clearCustomPermissions(identifier);
        assertEquals(0, editor.getCustomPermissions(identifier).size());
    }

    @Test
    public void testReturnedCodebasesIsCopy() throws Exception {
        final Collection<String> original = editor.getCodebases();
        original.add("some invalid value");
        original.remove("");
        final Collection<String> second = editor.getCodebases();
        assertTrue("Editor should have default codebase", second.contains(""));
        assertEquals("Editor should only have default codebase", 1, second.size());
    }

    @Test
    public void testReturnedPermissionsMapIsCopy() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> original = editor.getPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            original.put(perm, true);
        }
        final Map<PolicyEditorPermissions, Boolean> second = editor.getPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : second.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testReturnedCustomPermissionsSetIsCopy() throws Exception {
        final Collection<PolicyParser.PermissionEntry> original = editor.getCustomPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        assertTrue("There should not be any custom permissions to start", original.isEmpty());
        original.add(new CustomPolicyViewer.DisplayablePermission("java.io.FilePermission", "*", "write"));
        final Collection<PolicyParser.PermissionEntry> second = editor.getCustomPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        assertTrue("The custom permission should not have been present", second.isEmpty());
    }

    @Test
    public void testDefaultPermissionsAllFalse() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = editor.getPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        final PolicyIdentifier exampleIdentifier = identifierFromCodebase("http://example.com");
        editor.addNewEntry(exampleIdentifier);
        final Map<PolicyEditorPermissions, Boolean> addedMap = editor.getPermissions(exampleIdentifier);
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : defaultMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : addedMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsRepresented() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = editor.getPermissions(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        final PolicyIdentifier exampleIdentifier = identifierFromCodebase("http://example.com");
        editor.addNewEntry(exampleIdentifier);
        final Map<PolicyEditorPermissions, Boolean> addedMap = editor.getPermissions(exampleIdentifier);
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
    public void testCodebaseTrailingSlashesDoNotMatch() throws Exception {
        final Set<String> toAdd = new HashSet<>();
        toAdd.add("http://example.com");
        toAdd.add("http://example.com/");
        for (final String cb : toAdd) {
            editor.addNewEntry(identifierFromCodebase(cb));
        }
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        for (final String codebase : toAdd) {
            assertTrue("Editor should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testFilePathArgumentMainArg() {
        String[] args = new String[] { "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals("foo"));
    }

    @Test
    public void testFilePathArgumentMainArg2() {
        String[] args = new String[] { "-codebase", "http://example.com", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals("foo"));
    }

    @Test
    public void testFilePathArgumentFileSwitch() {
        String[] args = new String[] { "-file", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals("foo"));
    }

    @Test
    public void testFilePathArgumentFileSwitch2() {
        String[] args = new String[] { "-codebase", "http://example.com", "-file", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals("foo"));
    }

    @Test
    public void testFilePathArgumentDefaultFileSwitch() {
        String[] args = new String[] { "-defaultfile" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals(new File(PathsAndFiles.JAVA_POLICY.getFullPath()).getAbsolutePath()));
    }

    @Test
    public void testFilePathArgumentDefaultFileSwitch2() {
        String[] args = new String[] { "-codebase", "http://example.com", "-defaultfile" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getFilePathArgument(optionParser);
        assertTrue(result.equals(new File(PathsAndFiles.JAVA_POLICY.getFullPath()).getAbsolutePath()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMainArgAndFileSwitch() {
        String[] args = new String[] { "-file", "foo", "bar" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMainArgAndFileSwitch2() {
        String[] args = new String[] { "bar", "-file", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultFileSwitchAndMainArg() {
        String[] args = new String[] { "-defaultfile", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultFileSwitchAndMainArg2() {
        String[] args = new String[] { "foo", "-defaultfile" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultFileSwitchAndMainArgAndFileSwitch() {
        String[] args = new String[] { "-defaultfile", "-file", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultFileSwitchAndMainArgAndFileSwitch2() {
        String[] args = new String[] { "-file", "foo", "-defaultfile" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getFilePathArgument(optionParser);
    }

    @Test
    public void testGetCodebaseArgument() {
        String[] args = new String[] { "-codebase", "http://example.com" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getCodebaseArgument(optionParser);
        assertTrue(result.equals("http://example.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCodebaseArgument2() {
        String[] args = new String[] { "-codebase", "" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getCodebaseArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCodebaseArgument3() {
        String[] args = new String[] { "-codebase", "example.com" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getCodebaseArgument(optionParser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCodebaseArgumentWhenNotProvided() {
        String[] args = new String[] { "-codebase" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getCodebaseArgument(optionParser);
    }

    @Test
    public void testGetPrincipalsArgument() {
        String[] args = new String[] { "-principals", "aa=bb" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        Set<PolicyParser.PrincipalEntry> result = getPrincipalsArgument(optionParser);
        assertTrue(result.size() == 1);
        assertTrue(result.contains(new PolicyParser.PrincipalEntry("aa", "bb")));
    }

    @Test
    public void testGetPrincipalsArgument2() {
        String[] args = new String[] { "-principals", "aa", "bb" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        Set<PolicyParser.PrincipalEntry> result = getPrincipalsArgument(optionParser);
        assertTrue(result.size() == 1);
        assertTrue(result.contains(new PolicyParser.PrincipalEntry("aa", "bb")));
    }

    @Test(expected = UnevenParameterException.class)
    public void testGetPrincipalsArgumentWhenUnevenArgumentsProvided() {
        String[] args = new String[] { "-principals", "aa=bb", "cc" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getPrincipalsArgument(optionParser);
    }

    @Test
    public void testGetPrincipalsArgumentWhenNotProvided() {
        String[] args = new String[] { "-principals" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        Set<PolicyParser.PrincipalEntry> result = getPrincipalsArgument(optionParser);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSignedByArgument() {
        String[] args = new String[] { "-signedby", "foo" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        String result = getSignedByArgument(optionParser);
        assertTrue(result.equals("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSignedByArgumentWhenNotProvided() {
        String[] args = new String[] { "-signedby" };
        CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());
        getSignedByArgument(optionParser);
    }
}
