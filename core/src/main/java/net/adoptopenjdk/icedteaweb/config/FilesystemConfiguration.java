package net.adoptopenjdk.icedteaweb.config;

import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.ConfigurationConstants;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class provided several values that are based on system properties in a thread safe way.
 */
public class FilesystemConfiguration {

    private static final AtomicReference<String> configHome = new AtomicReference<>();
    private static final Lock configHomeLock = new ReentrantLock();

    private static final AtomicReference<String> cacheHome = new AtomicReference<>();
    private static final Lock cacheHomeLock = new ReentrantLock();

    private static final AtomicReference<String> dataHome = new AtomicReference<>();
    private static final Lock dataHomeLock = new ReentrantLock();

    private static final AtomicReference<String> runtimeHome = new AtomicReference<>();
    private static final Lock runtimeHomeLock = new ReentrantLock();

    private static final AtomicReference<String> variablePrefix = new AtomicReference<>();
    private static final Lock variablePrefixLock = new ReentrantLock();

    public static String getConfigHome() {
        if(configHome.get() == null) {
            configHomeLock.lock();
            try {
                if (configHome.get() == null) {
                    final String xdgHome = System.getenv(ConfigurationConstants.XDG_CONFIG_HOME_VAR);
                    final String baseHome = System.getProperty(ConfigurationConstants.HOME_PROP) + File.separator + ".config";
                    configHome.set(Optional.ofNullable(xdgHome).orElse(baseHome));
                }
            } finally {
                configHomeLock.unlock();
            }
        }
        return configHome.get();
    }

    public static String getCacheHome() {
        if(cacheHome.get() == null) {
            cacheHomeLock.lock();
            try {
                if (cacheHome.get() == null) {
                    final String xdgHome = System.getenv(ConfigurationConstants.XDG_CACHE_HOME_VAR);
                    final String baseHome = System.getProperty(ConfigurationConstants.HOME_PROP) + File.separator + ".cache";
                    cacheHome.set(Optional.ofNullable(xdgHome).orElse(baseHome));
                }
            } finally {
                cacheHomeLock.unlock();
            }
        }
        return cacheHome.get();
    }

    public static String getDataHome() {
        if(dataHome.get() == null) {
            dataHomeLock.lock();
            try {
                if (dataHome.get() == null) {
                    final String xdgHome = System.getenv(ConfigurationConstants.XDG_DATA_HOME_VAR);
                    final String baseHome = System.getProperty(ConfigurationConstants.HOME_PROP) +  File.separator + ".local" + File.separator + "share";
                    dataHome.set(Optional.ofNullable(xdgHome).orElse(baseHome));
                }
            } finally {
                dataHomeLock.unlock();
            }
        }
        return dataHome.get();
    }

    public static String getRuntimeHome() {
        if(runtimeHome.get() == null) {
            runtimeHomeLock.lock();
            try {
                if (runtimeHome.get() == null) {
                    final String xdgHome = System.getenv(ConfigurationConstants.XDG_RUNTIME_DIR_VAR);
                    final String baseHome = System.getProperty(ConfigurationConstants.TMP_PROP);
                    runtimeHome.set(Optional.ofNullable(xdgHome).orElse(baseHome));
                }
            } finally {
                runtimeHomeLock.unlock();
            }
        }
        return runtimeHome.get();
    }

    public static String getVariablePrefix() {
        if(variablePrefix.get() == null) {
            variablePrefixLock.lock();
            try {
                if (variablePrefix.get() == null) {
                    variablePrefix.set(OsUtil.isWindows() ? ConfigurationConstants.WIN_VARIABLE_PREFIX : ConfigurationConstants.UNIX_VARIABLE_PREFIX);
                }
            } finally {
                variablePrefixLock.unlock();
            }
        }
        return variablePrefix.get();
    }
}
