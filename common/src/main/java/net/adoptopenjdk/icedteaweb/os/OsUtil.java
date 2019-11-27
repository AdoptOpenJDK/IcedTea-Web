package net.adoptopenjdk.icedteaweb.os;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class OsUtil {

    private final static String WIN = "win";

    private final static String MAC = "mac";


    /**
     * Returns {@code true} if we are on windows.
     * @return {@code true} if we are on windows.
     */
    public static boolean isWindows() {
        String operSys = JavaSystemProperties.getOsName().toLowerCase();
        return (operSys.contains(WIN));
    }

    public static boolean isMac() {
        String operSys = JavaSystemProperties.getOsName().toLowerCase();
        return (operSys.contains(MAC));
    }
}
