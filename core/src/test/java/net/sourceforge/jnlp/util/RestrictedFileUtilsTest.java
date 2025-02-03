package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;

import static net.sourceforge.jnlp.util.RestrictedFileUtils.getSIDForPrincipal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ...
 */
public class RestrictedFileUtilsTest {

    @Test
    public void testCreateRestrictedFile() throws Exception {
        if (!OsUtil.isWindows()) {
            return;
        }
        final File tmpdir = new File(JavaSystemProperties.getJavaTempDir()), testfile = new File(tmpdir, "itw_test_create_restricted_file");
        if (testfile.exists()) {
            assertTrue(testfile.delete());
        }
        testfile.deleteOnExit();
        RestrictedFileUtils.createRestrictedFile(testfile);
        boolean hasOwner = false;
        AclFileAttributeView view = Files.getFileAttributeView(testfile.toPath(), AclFileAttributeView.class);
        for (AclEntry ae : view.getAcl()) {
            if (view.getOwner().getName().equals(ae.principal().getName()) || getSIDForPrincipal(ae.principal()).orElse("").equals(RestrictedFileUtils.NT_AUTHENTICATED_USER_SID)) {
                assertFalse("Duplicate owner entry", hasOwner);
                hasOwner = true;
                assertEquals("Owner must have all permissions", 14, ae.permissions().size());
            }
        }
        assertTrue("No owner entry", hasOwner);
    }
}
