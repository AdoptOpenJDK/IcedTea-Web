/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class DirectoryValidator {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryValidator.class);
    
    /**
     * This class is holding results of directory validation.
     * Various errors like can not read, write create dir can appear
     * For summaries of results are here getPasses, getFailures methods
     * <p>
     * Individual results can be read from results field, or converted to string
     * </p>
     */
    public static class DirectoryCheckResults {
        public final List<DirectoryCheckResult> results;

        /**
         * Wraps results so we can make some statistics or convert to message 
         * @param results results to be checked
         */
        public DirectoryCheckResults(List<DirectoryCheckResult> results) {
            this.results = results;
        }
        
        /**
         * @return sum of passed checks, 0-3 per result
         */
         public int getPasses() {
            int passes = 0;
             for (DirectoryCheckResult directoryCheckResult : results) {
                 passes += directoryCheckResult.getPasses();
             }
             return passes;
        }

         /**
          * @return  sum of failed checks, 0-3 per results
          */
        public int getFailures() {
           int failures = 0;
             for (DirectoryCheckResult directoryCheckResult : results) {
                 failures += directoryCheckResult.getFailures();
             }
             return failures;
        }
        
        /**
         * The result have one result per line, separated by \n 
         * as is inherited from result.getMessage() method.
         *
         * @return all results connected. 
         */
        public String getMessage() {
            return resultsToString(results);
        }

        /**
         * using getMessage
         * @return a text representation of a {@code DirectoryValidator} object
         */
        @Override
        public String toString() {
            return getMessage();
        }
        
        
        
        public  static String resultsToString(List<DirectoryCheckResult> results) {
            StringBuilder sb = new StringBuilder();
            for (DirectoryCheckResult r : results) {
                if (r.getFailures() > 0) {
                    sb.append(r.getMessage());
                }
            }
            return sb.toString();
        }
    }

    /**
     * Is storing result of directory validation.
     * 
     * validated are existence of directory
     * whether it is directory
     * if it have read/write permissions
     */
    public static class DirectoryCheckResult {

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

        public DirectoryCheckResult(File testedDir) {
            this.testedDir = testedDir;
        }

        public static String notExistsMessage(File f) {
            return R("DCmaindircheckNotexists", f.getAbsolutePath());
        }

        public static String notDirMessage(File f) {
            return R("DCmaindircheckNotdir", f.getAbsolutePath());
        }

        public static String wrongPermissionsMessage(File f) {
            return R("DCmaindircheckRwproblem", f.getAbsolutePath());
        }

        private static int booleanToInt(boolean b) {
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
            StringBuilder sb = new StringBuilder();
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
                String s = subDir.getMessage();
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
    
    
    private final List<File> dirsToCheck;

    
    /**
     * Creates DirectoryValidator to ensure given directories
     * 
     * @param dirsToCheck dirs to check 
     */
    public DirectoryValidator(List<File> dirsToCheck) {
        this.dirsToCheck = dirsToCheck;
    }

    
    /**
     * Creates DirectoryValidator to ensure directories read from
     * user (if any - default otherwise) settings via keys:
     * <ul>
     *     <li>{@link DeploymentConfiguration#KEY_USER_CACHE_DIR}</li>
     *     <li>{@link DeploymentConfiguration#KEY_USER_PERSISTENCE_CACHE_DIR}</li>
     *     <li>{@link DeploymentConfiguration#KEY_SYSTEM_CACHE_DIR}</li>
     *     <li>{@link DeploymentConfiguration#KEY_USER_LOG_DIR}</li>
     *     <li>{@link DeploymentConfiguration#KEY_USER_TMP_DIR}</li>
     *     <li>{@link DeploymentConfiguration#KEY_USER_LOCKS_DIR}</li>
     * </ul>
     */
    private DirectoryValidator() {
        dirsToCheck = new ArrayList<>(6);
        DeploymentConfiguration dc = JNLPRuntime.getConfiguration();
        String[] keys = new String[]{
            DeploymentConfiguration.KEY_USER_CACHE_DIR,
            DeploymentConfiguration.KEY_USER_PERSISTENCE_CACHE_DIR,
            DeploymentConfiguration.KEY_SYSTEM_CACHE_DIR,
            DeploymentConfiguration.KEY_USER_LOG_DIR,
            DeploymentConfiguration.KEY_USER_TMP_DIR,
            DeploymentConfiguration.KEY_USER_LOCKS_DIR};
        for (String key : keys) {
            String value = dc.getProperty(key);
            if (value == null) {
                LOG.warn("WARNING: key {} has no value, setting to default value", key);
                value = Defaults.getDefaults().get(key).getValue();
            }
            if (value == null) {
                if (JNLPRuntime.isDebug()) {
                    LOG.warn("WARNING: key {} has no value, skipping", key);
                }
                continue;
            }
            File f = new File(value);
            dirsToCheck.add(f);
        }
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

    private static DirectoryCheckResults ensureDirs(List<File> dirs) {
        List<DirectoryCheckResult> result = new ArrayList<>(dirs.size());
        for (File f : dirs) {
            if (f.exists()) {
                DirectoryCheckResult r = testDir(f, true, true);
                result.add(r);
                continue;
            }
            if (!f.mkdirs()) {
                LOG.error("ERROR: Directory {} does not exist and has not been created", f.getAbsolutePath());
            } else {
                LOG.debug("OK: Directory {} did not exist but has been created", f.getAbsolutePath());
            }
            DirectoryCheckResult r = testDir(f, true, true);
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
    static DirectoryCheckResult testDir(File f, boolean verbose, boolean testSubdir) {
        DirectoryCheckResult result = new DirectoryCheckResult(f);
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
                String s = FileUtils.loadFileAsString(testFile);
                if (!s.trim().equals("ww")) {
                    correctPermissions = false;
                }
            } catch (Exception ex) {
                if (JNLPRuntime.isDebug()) {
                    ex.printStackTrace();
                }
                correctPermissions = false;
            }
            File[] canList = f.listFiles();
            if (canList == null || canList.length == 0) {
                correctPermissions = false;
            }
            testFile.delete();
            if (testFile.exists()) {
                correctPermissions = false;
            } else {
                boolean created = testFile.mkdir();
                if (!created) {
                    correctPermissions = false;
                }
                if (testFile.exists()) {
                    if (testSubdir) {
                        DirectoryCheckResult subdirResult = testDir(testFile, verbose, false);
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
        } catch (Exception ex) {
            if (JNLPRuntime.isDebug()) {
                ex.printStackTrace();
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
