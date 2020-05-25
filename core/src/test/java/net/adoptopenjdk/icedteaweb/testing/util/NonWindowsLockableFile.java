
package net.adoptopenjdk.icedteaweb.testing.util;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.lockingfile.WindowsLockableFileTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_NAME;

/**
 *
 * @author jvanek
 */
public class NonWindowsLockableFile extends WindowsLockableFileTest {

    private static String os;

    @BeforeClass
    public static void smuggleOs() {
        os = JavaSystemProperties.getOsName();
        System.setProperty(OS_NAME, "No Microsoft OS for itw");
    }

    @AfterClass
    public static void restoreOs() {
        System.setProperty(OS_NAME, os);
    }

}
