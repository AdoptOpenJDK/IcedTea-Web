package net.adoptopenjdk.icedteaweb.io;

import net.adoptopenjdk.icedteaweb.os.OsUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static net.adoptopenjdk.icedteaweb.io.FileUtils.INVALID_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ...
 */
public class FileUtilsTest {

    @Test
    public void testSanitizePath() {
        for (char ch : INVALID_PATH) {
            String str = "/tmp/test" + ch + "path";
            String sanitized = FileUtils.sanitizePath(str);
            assertFalse(ch + " should be sanitized from " + sanitized, ch != File.separatorChar && sanitized.contains(Character.toString(ch)));
            assertEquals("/tmp/test_path", sanitized);
        }
    }

    @Test
    public void testSanitizeMoreDoubleDots() {
        String str = "C:/some:dir/some:file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some_dir/some_file", sanitized);
        } else {
            assertEquals("C_/some_dir/some_file", sanitized);
        }
    }

    @Test
    public void testSanitizePathWindowsLinuxSlashes() {
        String str = "C:/some.dir/some.file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some.dir/some.file", sanitized);
        } else {
            assertEquals("C_/some.dir/some.file", sanitized);
        }
    }

    @Test
    public void testSanitizePathWindowsWinSlashes() {
        String str = "C:\\some.dir\\some.file";
        String sanitized = FileUtils.sanitizePath(str);
        if (OsUtil.isWindows()) {
            assertEquals("C:/some.dir/some.file", sanitized);
        } else {
            assertEquals("C_/some.dir/some.file", sanitized);
        }
    }

    @Test
    public void testSanitizeFilename() {
        for (char ch : INVALID_PATH) {
            String str = "file" + ch + "name";
            String sanitized = FileUtils.sanitizeFileName(str);
            assertFalse(ch + " should be sanitized from " + sanitized, sanitized.contains(Character.toString(ch)));
            assertEquals("file_name", sanitized);
        }
    }

    @Test
    public void testSanitizeFilenameSlashes() {
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
