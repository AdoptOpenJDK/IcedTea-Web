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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class PolicyEditorTest {

    private String tempFilePath;
    private PolicyEditor editor;

    @Before
    public void setNewTempfile() throws Exception {
        tempFilePath = File.createTempFile("policyeditor", null).getCanonicalPath();
        editor = new PolicyEditor(tempFilePath);
    }

    @Test
    public void testInitialCodebase() throws Exception {
        final Collection<String> initialCodebases = editor.getCodebases();
        assertTrue("Editor should have one codebase to begin with", initialCodebases.size() == 1);
        assertTrue("Editor's initial codebase should be \"\" (empty string)",
                initialCodebases.toArray(new String[0])[0].equals(""));
    }

    @Test
    public void testAddCodebase() throws Exception {
        final String urlString = "http://example.com";
        editor.addNewCodebase(urlString);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        assertTrue("Editor should have http://example.com", codebases.contains(urlString));
        assertTrue("Editor should only have two codebases", codebases.size() == 2);
    }

    @Test
    public void addMultipleCodebases() throws Exception {
        final Set<String> toAdd = new HashSet<String>();
        toAdd.add("http://example.com");
        toAdd.add("http://icedtea.classpath.org");
        editor.addNewCodebases(toAdd);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        for (final String codebase : toAdd) {
            assertTrue("Editor should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testAddInvalidUrlCodebase() throws Exception {
        final String invalidUrl = "url.com"; // missing protocol -> invalid
        editor.addNewCodebase(invalidUrl);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        assertTrue("Editor should only have default codebase", codebases.size() == 1);
    }

    @Test
    public void testReturnedCodebasesAreCopy() throws Exception {
        final Collection<String> original = editor.getCodebases();
        original.add("some invalid value");
        original.remove("");
        final Collection<String> second = editor.getCodebases();
        assertTrue("Editor should have default codebase", second.contains(""));
        assertTrue("Editor should only have default codebase", second.size() == 1);
    }

    @Test
    public void testReturnedPermissionsMapIsCopy() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> original = editor.getPermissions("");
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            original.put(perm, true);
        }
        final Map<PolicyEditorPermissions, Boolean> second = editor.getPermissions("");
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : second.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testReturnedCustomPermissionsSetIsCopy() throws Exception {
        final Collection<CustomPermission> original = editor.getCustomPermissions("");
        original.add(new CustomPermission("java.io.FilePermission", "*", "write"));
        final Collection<CustomPermission> second = editor.getCustomPermissions("");
        assertTrue("There should not be any custom permissions", second.isEmpty());
    }

    @Test
    public void testDefaultPermissionsAllFalse() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = editor.getPermissions("");
        editor.addNewCodebase("http://example.com");
        final Map<PolicyEditorPermissions, Boolean> addedMap = editor.getPermissions("http://example.com");
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : defaultMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : addedMap.entrySet()) {
            assertFalse("Permission " + entry.getKey() + " should be false", entry.getValue());
        }
    }

    @Test
    public void testAllPermissionsRepresented() throws Exception {
        final Map<PolicyEditorPermissions, Boolean> defaultMap = editor.getPermissions("");
        editor.addNewCodebase("http://example.com");
        final Map<PolicyEditorPermissions, Boolean> addedMap = editor.getPermissions("http://example.com");
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
        final Set<String> toAdd = new HashSet<String>();
        toAdd.add("http://redhat.com");
        toAdd.add("http://redhat.com/");
        editor.addNewCodebases(toAdd);
        final Collection<String> codebases = editor.getCodebases();
        assertTrue("Editor should have default codebase", codebases.contains(""));
        for (final String codebase : toAdd) {
            assertTrue("Editor should have " + codebase, codebases.contains(codebase));
        }
    }

    @Test
    public void testArgsToMap() throws Exception {
        final String[] args = new String[] {
                "-codebase", "http://example.com http://icedtea.classpath.org",
                "-file", "/tmp/some-policy-file.tmp",
                "-help"
        };
        Map<String, String> map = PolicyEditor.argsToMap(args);
        assertTrue("Args map should contain help flag", map.containsKey("-help"));
        assertTrue("Value for -help should be null but was " + map.get("-help"),
                map.get("-help") == null);
        assertTrue("Args map should contain file flag", map.containsKey("-file"));
        assertTrue("Value for -file should be /tmp/some-policy-file.tmp but was " + map.get("-file"),
                map.get("-file").equals("/tmp/some-policy-file.tmp"));
        assertTrue("Args map should contain codebase flag", map.containsKey("-codebase"));
        assertTrue("Value for codebase flag should be \"http://example.com http://icedtea.classpath.org\" but was " + map.get("-codebase"),
                map.get("-codebase").equals("http://example.com http://icedtea.classpath.org"));
    }

}