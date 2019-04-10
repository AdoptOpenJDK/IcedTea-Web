package net.sourceforge.jnlp.util.legacy;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.DirectoryValidator;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to migrate legacy to current structures when changes are required in new versions of the application.
 * @deprecated
 */
@Deprecated
public class MigrationUtils {
    private final static Logger LOG = LoggerFactory.getLogger(MigrationUtils.class);

    public static void move14AndOlderFilesTo15StructureCatched() {
        try {
            move14AndOlderFilesTo15Structure();
        } catch (Throwable t) {
            LOG.error("Critical error during converting old files to new. Continuing", t);
        }
    }

    private static void move14AndOlderFilesTo15Structure() {
        int errors = 0;
        final String PRE_15_DEPLOYMENT_DIR = ".icedtea";
        final String LEGACY_USER_HOME = System.getProperty("user.home") + File.separator + PRE_15_DEPLOYMENT_DIR;
        final String legacyProperties = LEGACY_USER_HOME + File.separator + DeploymentConfiguration.DEPLOYMENT_PROPERTIES;
        final File configDir = new File(PathsAndFiles.USER_CONFIG_HOME);
        final File cacheDir = new File(PathsAndFiles.USER_CACHE_HOME);
        final File legacyUserDir = new File(LEGACY_USER_HOME);
        if (legacyUserDir.exists()) {
            LOG.info(DeploymentConfiguration.TRANSFER_TITLE);
            LOG.info(PathsAndFiles.USER_CONFIG_HOME + " and " + PathsAndFiles.USER_CACHE_HOME);
            LOG.info("You should not see this message next time you run icedtea-web!");
            LOG.info("Your custom dirs will not be touched and will work");
            LOG.info("-----------------------------------------------");

            LOG.info("Preparing new directories:");
            LOG.info("{}", PathsAndFiles.USER_CONFIG_HOME);
            errors += resultToStd(configDir.mkdirs());
            LOG.info("{}", PathsAndFiles.USER_CACHE_HOME);
            errors += resultToStd(cacheDir.mkdirs());
            //move this first, the creation of config singleton may happen anytime...
            //but must not before USER_DEPLOYMENT_FILE is moved and should not in this block
            String currentProperties = PathsAndFiles.USER_DEPLOYMENT_FILE.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyProperties, currentProperties);

            String legacyPropertiesOld = LEGACY_USER_HOME + File.separator + DeploymentConfiguration.DEPLOYMENT_PROPERTIES + ".old";
            String currentPropertiesOld = currentProperties + ".old";
            errors += moveLegacyToCurrent(legacyPropertiesOld, currentPropertiesOld);

            String legacySecurity = LEGACY_USER_HOME + File.separator + "security";
            String currentSecurity = PathsAndFiles.USER_DEFAULT_SECURITY_DIR;
            errors += moveLegacyToCurrent(legacySecurity, currentSecurity);

            String legacyAppletTrust = LEGACY_USER_HOME + File.separator + DeploymentConfiguration.APPLET_TRUST_SETTINGS;
            String currentAppletTrust = PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyAppletTrust, currentAppletTrust);

            //note - all here use default path. Any call to getFullPAth will invoke creation of config singleton
            // but: we DO copy only defaults. There is no need to copy nondefaults!
            // nond-efault will be used thanks to config singleton read from copied deployment.properties

            String legacyCache = LEGACY_USER_HOME + File.separator + "cache";
            String currentCache = PathsAndFiles.CACHE_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyCache, currentCache);
            LOG.info("Adapting {} to new destination", PathsAndFiles.CACHE_INDEX_FILE_NAME);
            //replace all legacyCache by currentCache in new recently_used
            try {
                File f = PathsAndFiles.getRecentlyUsedFile().getDefaultFile();
                String s = FileUtils.loadFileAsString(f);
                s = s.replace(legacyCache, currentCache);
                FileUtils.saveFile(s, f);
            } catch (IOException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                errors++;
            }

            final String legacyPcahceDir = LEGACY_USER_HOME + File.separator + "pcache";
            final String currentPcacheDir = PathsAndFiles.PCACHE_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyPcahceDir, currentPcacheDir);

            final String legacyLogDir = LEGACY_USER_HOME + File.separator + "log";
            final String currentLogDir = PathsAndFiles.LOG_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyLogDir, currentLogDir);

            final String legacyTmp = LEGACY_USER_HOME + File.separator + "tmp";
            final String currentTmp = PathsAndFiles.TMP_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyTmp, currentTmp);

            LOG.info("Removing now empty {}", LEGACY_USER_HOME);
            errors += resultToStd(legacyUserDir.delete());

            if (errors != 0) {
                LOG.info("There occureed {} errors", errors);
                LOG.info("Please double check content of old data in {} with ", LEGACY_USER_HOME);
                LOG.info("new {} and {}", PathsAndFiles.USER_CONFIG_HOME, PathsAndFiles.USER_CACHE_HOME);
                LOG.info("To disable this check again, please remove {}", LEGACY_USER_HOME);
            }

        } else {
            LOG.info("System is already following XDG .cache and .config specifications");
            try {
                LOG.info("config: {} file exists: {}", PathsAndFiles.USER_CONFIG_HOME, configDir.exists());
            } catch (Exception ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
            try {
                LOG.info("cache: {} file exists: {}", PathsAndFiles.USER_CACHE_HOME, cacheDir.exists());
            } catch (Exception ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
        }
        //this call should endure even if (ever) will migration code be removed
        final DirectoryValidator.DirectoryCheckResults r = new DirectoryValidator().ensureDirs();
        if (r.getFailures() > 0) {
            LOG.info(r.getMessage());
            if (!JNLPRuntime.isHeadless()) {
                JOptionPane.showMessageDialog(null, r.getMessage());
            }
        }

    }

    private static int moveLegacyToCurrent(final String legacy, final String current) {
        LOG.info("Moving {} to {}", legacy, current);
        final File cf = new File(current);
        final File old = new File(legacy);
        if (cf.exists()) {
            LOG.warn("Warning! Destination {} exists!", current);
        }
        if (old.exists()) {
            boolean moved = old.renameTo(cf);
            return resultToStd(moved);
        } else {
            LOG.info("Source {} do not exists, nothing to do", legacy);
            return 0;
        }
    }

    private static int resultToStd(boolean securityMove) {
        if (securityMove) {
            LOG.debug("OK");
            return 0;
        } else {
            LOG.debug(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE);
            return 1;
        }
    }
}
