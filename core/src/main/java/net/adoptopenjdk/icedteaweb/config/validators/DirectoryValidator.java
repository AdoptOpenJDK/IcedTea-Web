/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryValidator.class);

    private final List<File> dirsToCheck;
    
    /**
     * Creates DirectoryValidator to ensure given directories
     * 
     * @param dirsToCheck dirs to check 
     */
    public DirectoryValidator(final List<File> dirsToCheck) {
        this.dirsToCheck = new ArrayList<>(dirsToCheck);
    }

    /**
     * This method is ensuring, that specified directories will exists after
     * call and will have enough permissions. 
     * <p>
     * This methods is trying to create the directories if they are not present
     * and is testing if can be written inside. All checks are done in bulk. If
     * one or more defect is found, user is warned via dialogue in gui mode
     * (again in bulk). In headless mode stdout/stderr is enough, as application
     * (both gui and headless) should not stop to work, but continue to run with
     * hope that corrupted dirs will not be necessary
     * </p>
     * @return  result of directory checks
     */
    public DirectoryCheckResults ensureDirs() {
        return ensureDirs(dirsToCheck);
    }

    private static DirectoryCheckResults ensureDirs(final List<File> dirs) {
        final List<DirectoryCheckResult> result = new ArrayList<>(dirs.size());
        for (final File f : dirs) {
            if (f.exists()) {
                final DirectoryCheckResult r = testDir(f, true, true);
                result.add(r);
                continue;
            }
            if (!f.mkdirs()) {
                LOG.error("ERROR: Directory {} does not exist and has not been created", f.getAbsolutePath());
            } else {
                LOG.debug("OK: Directory {} did not exist but has been created", f.getAbsolutePath());
            }
            final DirectoryCheckResult r = testDir(f, true, true);
            result.add(r);
        }
        return new DirectoryCheckResults(result);
    }

    /**
     * This method is package private for testing purposes only.
     * <p>
     * This method verify that directory exists, is directory, file and
     * directory can be created, file can be written into, and subdirectory can
     * be written into.
     * </p>
     * <p>
     * Some steps may looks like redundant, but some permission settings really
     * allow to create file but not directory and vice versa. Also some settings
     * can allow to create file or directory which can not be written into. (eg
     * ACL or network disks)
     * </p>
     */
    public static DirectoryCheckResult testDir(final File f, final boolean verbose, final boolean testSubdir) {
        final DirectoryCheckResult result = new DirectoryCheckResult(f);
        if (!f.exists()) {
            if (verbose) {
                LOG.error(DirectoryCheckResult.notExistsMessage(f));
            }
            result.exists = false;
        }
        if (!f.isDirectory()) {
            if (verbose) {
                LOG.error(DirectoryCheckResult.notDirMessage(f));
            }
            result.isDir = false;
        }
        File testFile = null;
        boolean correctPermissions = true;
        try {
            testFile = File.createTempFile("maindir", "check", f);
            if (!testFile.exists()) {
                correctPermissions = false;
            }
            try {
                BasicFileUtils.saveFile("ww", testFile);
                final String s = FileUtils.loadFileAsString(testFile);
                if (!s.trim().equals("ww")) {
                    correctPermissions = false;
                }
            } catch (final Exception ex) {
                if (JNLPRuntime.isDebug()) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                }
                correctPermissions = false;
            }
            final File[] canList = f.listFiles();
            if (canList == null || canList.length == 0) {
                correctPermissions = false;
            }
            testFile.delete();
            if (testFile.exists()) {
                correctPermissions = false;
            } else {
                final boolean created = testFile.mkdir();
                if (!created) {
                    correctPermissions = false;
                }
                if (testFile.exists()) {
                    if (testSubdir) {
                        final DirectoryCheckResult subdirResult = testDir(testFile, verbose, false);
                        if (subdirResult.getFailures() != 0) {
                            result.subDir = subdirResult;
                            correctPermissions = false;
                        }
                        testFile.delete();
                        if (testFile.exists()) {
                            correctPermissions = false;
                        }
                    }
                } else {
                    correctPermissions = false;
                }
            }
        } catch (final Exception ex) {
            if (JNLPRuntime.isDebug()) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
            correctPermissions = false;
        } finally {
            if (testFile != null && testFile.exists()) {
                testFile.delete();
            }
        }
        if (!correctPermissions) {
            if (verbose) {
                LOG.error(DirectoryCheckResult.wrongPermissionsMessage(f));
            }
            result.correctPermissions = false;
        }
        return result;
    }
}
