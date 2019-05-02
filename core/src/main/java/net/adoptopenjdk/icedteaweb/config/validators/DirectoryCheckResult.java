package net.adoptopenjdk.icedteaweb.config.validators;

import java.io.File;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Is storing result of directory validation.
 *
 * validated are existence of directory
 * whether it is directory
 * if it have read/write permissions
 */
public class DirectoryCheckResult {

    //do exist?
    public boolean exists = true;

    //is dir?
    public boolean isDir = true;

    //can be read, written to?
    public boolean correctPermissions = true;

    //have correct subdir? - this implies soe rules, when subdirectory of some
    //particular directory have weaker permissions
    public DirectoryCheckResult subDir = null;

    //actual tested directory
    private final File testedDir;

    public DirectoryCheckResult(final File testedDir) {
        this.testedDir = testedDir;
    }

    public static String notExistsMessage(final File f) {
        return R("DCmaindircheckNotexists", f.getAbsolutePath());
    }

    public static String notDirMessage(final File f) {
        return R("DCmaindircheckNotdir", f.getAbsolutePath());
    }

    public static String wrongPermissionsMessage(final File f) {
        return R("DCmaindircheckRwproblem", f.getAbsolutePath());
    }

    private static int booleanToInt(final boolean b) {
        if (b) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * count passes of this result (0-3, both inclusive).
     * @return  how many times it passed
     */
    public int getPasses() {
        int subdirs = 0;
        if (subDir != null) {
            subdirs = subDir.getPasses();
        }
        return booleanToInt(exists)
                + booleanToInt(isDir)
                + booleanToInt(correctPermissions)
                + subdirs;
    }

    /**
     * count failures of this result (0-3, both inclusive).
     * @return how many failures appeared
     */
    public int getFailures() {
        int max = 3;
        if (subDir != null) {
            max = 2 * max;
        }
        return max - getPasses();
    }

    /**
     * Convert results to string.
     * Each failure by line. PAsses are not mentioned
     * The subdirectory (and it subdirectories are included to )
     *
     * @return  string with \n, or/and ended by \n
     */
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        if (!exists) {
            sb.append(notExistsMessage(testedDir)).append("\n");
        }
        if (!isDir) {
            sb.append(notDirMessage(testedDir)).append("\n");
        }
        if (!correctPermissions) {
            sb.append(wrongPermissionsMessage(testedDir)).append("\n");
        }

        if (subDir != null) {
            final String s = subDir.getMessage();
            if (!s.isEmpty()) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
