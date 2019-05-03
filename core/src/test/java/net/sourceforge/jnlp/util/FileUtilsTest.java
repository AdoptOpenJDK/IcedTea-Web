/* FileUtilsTest.java
Copyright (C) 2014 Red Hat, Inc.

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
package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.os.OsUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {

    public static final List<Character> INVALID_PATH = Arrays.asList(new Character[]{':', '*', '?', '"', '<', '>', '|', '[', ']', '\'', ';', '=', ','});
    public static final List<Character> INVALID_NAME = new ArrayList<>(INVALID_PATH);

    static {
        INVALID_NAME.add(0, '\\');
        INVALID_NAME.add(0, '/');
    }

    @Test
    public void testSanitizePath() throws Exception {
        for (char ch : INVALID_PATH) {
            String str = "/tmp/test" + ch + "path";
            String sanitized = FileUtils.sanitizePath(str);
            assertFalse(ch + " should be sanitized from " + sanitized, ch != File.separatorChar && sanitized.contains(Character.toString(ch)));
            assertEquals("/tmp/test_path", sanitized);
        }
    }

    @Test
    public void testSanitizeMoreDoubleDots() throws Exception {
        String str = "C:/some:dir/some:file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some_dir/some_file", sanitized);
        } else {
            assertEquals("C_/some_dir/some_file", sanitized);
        }
    }

    @Test
    public void testSanitizePathWindowsLinuxSlashes() throws Exception {
        String str = "C:/some.dir/some.file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some.dir/some.file", sanitized);
        } else {
            assertEquals("C_/some.dir/some.file", sanitized);
        }
    }

    @Test
    public void testSanitizePathWindowsWinSlashes() throws Exception {
        String str = "C:\\some.dir\\some.file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some.dir/some.file", sanitized);
        } else {
            assertEquals("C_/some.dir/some.file", sanitized);
        }
    }

    @Test
    public void testSanitizeFilename() throws Exception {
        for (char ch : INVALID_PATH) {
            String str = "file" + ch + "name";
            String sanitized = FileUtils.sanitizeFileName(str);
            assertFalse(ch + " should be sanitized from " + sanitized, sanitized.contains(Character.toString(ch)));
            assertEquals("file_name", sanitized);
        }
    }

    @Test
    public void testSanitizeFilenameSlashes() throws Exception {
        for (char ch : new char[]{'/', '\\'}) {
            String str = "file" + ch + "name";
            String sanitized = FileUtils.sanitizeFileName(str);
            assertFalse(ch + " should be sanitized from " + sanitized, sanitized.contains(Character.toString(ch)));
            assertEquals("file_name", sanitized);
        }
    }

    @Test
    public void testCreateParentDir() throws Exception {
        final File tmpdir = new File(System.getProperty(JAVA_IO_TMPDIR)), testParent = new File(tmpdir, "itw_test_create_parent_dir"), testChild = new File(testParent, "test_child_dir");
        testChild.deleteOnExit();
        testParent.deleteOnExit();
        FileUtils.createParentDir(testChild);
        assertTrue(tmpdir.isDirectory());
        assertTrue(testParent.isDirectory());
        assertFalse(testChild.exists());
    }

    @Test
    public void testCreateRestrictedFile() throws Exception {
        if (!OsUtil.isWindows()) {
            return;
        }
        final File tmpdir = new File(System.getProperty(JAVA_IO_TMPDIR)), testfile = new File(tmpdir, "itw_test_create_restricted_file");
        if (testfile.exists()) {
            assertTrue(testfile.delete());
        }
        testfile.deleteOnExit();
        FileUtils.createRestrictedFile(testfile, true);
        boolean hasOwner = false;
        AclFileAttributeView view = Files.getFileAttributeView(testfile.toPath(), AclFileAttributeView.class);
        for (AclEntry ae : view.getAcl()) {
            if (view.getOwner().getName().equals(ae.principal().getName())) {
                assertFalse("Duplicate owner entry", hasOwner);
                hasOwner = true;
                assertEquals("Owner must have all permissions",14, ae.permissions().size());
            }
        }
        assertTrue("No owner entry", hasOwner);
    }

}
