package net.adoptopenjdk.icedteaweb.os;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class OsUtil {

    private final static String WIN = "win";

    /**
     * Returns {@code true} if we are on windows.
     * @return {@code true} if we are on windows.
     */
    public static boolean isWindows() {
        String operSys = System.getProperty(OS_NAME).toLowerCase();
        return (operSys.contains(WIN));
    }
}
