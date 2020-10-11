package net.adoptopenjdk.icedteaweb.os;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class OsUtil {

    private static final String WIN = "win";

    private static final String LINUX = "linux";

    /**
     * Returns {@code true} if we are on windows.
     *
     * @return {@code true} if we are on windows.
     */
    public static boolean isWindows() {
        return isOs(WIN);
    }

    public static boolean isLinux() {
        return isOs(LINUX);
    }

    private static boolean isOs(String osName) {
        String operSys = JavaSystemProperties.getOsName().toLowerCase();
        return (operSys.contains(osName));
    }
}
