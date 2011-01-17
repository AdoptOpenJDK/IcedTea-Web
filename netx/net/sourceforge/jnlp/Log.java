package net.sourceforge.jnlp;

import java.io.File;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * This file provides the information required to do logging.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
abstract class Log {

    // Directory where the logs are stored.
    protected static String icedteaLogDir;

    protected static boolean enableLogging = false;
    protected static boolean enableTracing = false;

    // Prepare for logging.
    static {
        DeploymentConfiguration config = JNLPRuntime.getConfiguration();

        // Check whether logging and tracing is enabled.
        enableLogging = Boolean.parseBoolean(config.getProperty(DeploymentConfiguration.KEY_ENABLE_LOGGING));
        enableTracing = Boolean.parseBoolean(config.getProperty(DeploymentConfiguration.KEY_ENABLE_TRACING));

        // Get log directory, create it if it doesn't exist. If unable to create and doesn't exist, don't log.
        icedteaLogDir = config.getProperty(DeploymentConfiguration.KEY_USER_LOG_DIR);
        if (icedteaLogDir != null) {
            File f = new File(icedteaLogDir);
            if (f.isDirectory() || f.mkdirs())
                icedteaLogDir += File.separator;
            else {
                enableLogging = false;
                enableTracing = false;
            }
        }
    }
}
