
package net.adoptopenjdk.icedteaweb.lockingfile;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_NAME;

/**
 * @author jvanek
 */
public class WindowsLockableFileTest {

    private static String os;

    @BeforeClass
    public static void smuggleOs() {
        os = JavaSystemProperties.getOsName();
        System.setProperty(OS_NAME, "Windows for itw");
    }

    @AfterClass
    public static void restoreOs() {
        System.setProperty(OS_NAME, os);
    }

    @Test
    public void testLockUnlockOkExists() throws IOException {
        File f = Files.createTempFile("itw", "lockingFile").toFile();
        f.deleteOnExit();
        LockableFile lf = LockableFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

    @Test
    public void testLockUnlockOkNotExists() throws IOException {
        File f = Files.createTempFile("itw", "lockingFile").toFile();
        f.delete();
        LockableFile lf = LockableFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

    @Test
    public void testLockUnlockNoOkNotExists() throws IOException {
        File parent = Files.createTempFile("itw", "lockingFile").toFile();
        parent.deleteOnExit();
        File f = new File(parent, "itwLcokingRelict");
        f.delete();
        parent.setReadOnly();
        LockableFile lf = LockableFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

    @Test
    public void testLockUnlockNotOkExists() throws IOException {
        File f = new File("/some/definitely/not/existing/file.itw");
        LockableFile lf = LockableFile.getInstance(f);
        lf.lock();
        lf.unlock();
    }

}
