// Copyright (C) 2010 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.


package net.sourceforge.jnlp.runtime;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.ConfigurationException;

import net.sourceforge.jnlp.ShortcutDesc;

/**
 * Manages the various properties and configuration related to deployment.
 *
 * See:
 * http://download.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/properties.html
 */
public final class DeploymentConfiguration {

    /**
     * Represents a value for a configuration. Provides methods to get the value
     * as well as marking the value as locked.
     */
    private final class ConfigValue {

        private String value;
        private boolean locked;

        ConfigValue(String value) {
            this(value, false);
        }

        ConfigValue(String value, boolean locked) {
            this.value = value;
            this.locked = locked;
        }

        ConfigValue(ConfigValue other) {
            this(other.value, other.locked);
        }

        String get() {
            return value;
        }

        /**
         * Note that setting the value is not enforced - it is the caller's
         * responsibility to check if a value is locked or not before setting a
         * new value
         *
         * @param value the new value
         */
        void set(String value) {
            this.value = value;
        }

        /**
         * @return true if the value has been marked as locked
         */
        boolean isLocked() {
            return locked;
        }

        /**
         * Mark a value as locked
         * @param locked
         */
        void setLocked(boolean locked) {
            this.locked = locked;
        }
    }

    public static final String DEPLOYMENT_DIR = ".icedtea";
    public static final String DEPLOYMENT_CONFIG = "deployment.config";
    public static final String DEPLOYMENT_PROPERTIES = "deployment.properties";

    public static final String DEPLOYMENT_COMMENT = "Netx deployment configuration";

    public static final int JNLP_ASSOCIATION_NEVER = 0;
    public static final int JNLP_ASSOCIATION_NEW_ONLY = 1;
    public static final int JNLP_ASSOCIATION_ASK_USER = 2;
    public static final int JNLP_ASSOCIATION_REPLACE_ASK = 3;

    /*
     * FIXME these should be moved into JavaConsole, but there is a strange
     * dependency in the build system. First all of netx is built. Then the
     * plugin is built. So we cannot refer to plugin code in here :(
     */
    public static final String CONSOLE_HIDE = "HIDE";
    public static final String CONSOLE_SHOW = "SHOW";
    public static final String CONSOLE_DISABLE = "DISABLE";

    /* FIXME these should be moved into the proxy class */
    public static final int PROXY_TYPE_UNKNOWN = -1;
    public static final int PROXY_TYPE_NONE = 0;
    public static final int PROXY_TYPE_MANUAL = 1;
    public static final int PROXY_TYPE_AUTO = 2;
    public static final int PROXY_TYPE_BROWSER = 3;

    public static final String KEY_USER_CACHE_DIR = "deployment.user.cachedir";
    public static final String KEY_USER_PERSISTENCE_CACHE_DIR = "deployment.user.pcachedir";
    public static final String KEY_SYSTEM_CACHE_DIR = "deployment.system.cachedir";
    public static final String KEY_USER_LOG_DIR = "deployment.user.logdir";
    public static final String KEY_USER_TMP_DIR = "deployment.user.tmp";
    /** the directory containing locks for single instance applications */
    public static final String KEY_USER_LOCKS_DIR = "deployment.user.locksdir";
    /**
     * The netx_running file is used to indicate if any instances of netx are
     * running (this file may exist even if no instances are running). All netx
     * instances acquire a shared lock on this file. If this file can be locked
     * (using a {@link FileLock}) in exclusive mode, then other netx instances
     * are not running
     */
    public static final String KEY_USER_NETX_RUNNING_FILE = "deployment.user.runningfile";

    public static final String KEY_USER_TRUSTED_CA_CERTS = "deployment.user.security.trusted.cacerts";
    public static final String KEY_USER_TRUSTED_JSSE_CA_CERTS = "deployment.user.security.trusted.jssecacerts";
    public static final String KEY_USER_TRUSTED_CERTS = "deployment.user.security.trusted.certs";
    public static final String KEY_USER_TRUSTED_JSSE_CERTS = "deployment.user.security.trusted.jssecerts";
    public static final String KEY_USER_TRUSTED_CLIENT_CERTS = "deployment.user.security.trusted.clientauthcerts";

    public static final String KEY_SYSTEM_TRUSTED_CA_CERTS = "deployment.system.security.cacerts";
    public static final String KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS = "deployment.system.security.jssecacerts";
    public static final String KEY_SYSTEM_TRUSTED_CERTS = "deployment.system.security.trusted.certs";
    public static final String KEY_SYSTEM_TRUSTED_JSSE_CERTS = "deployment.system.security.trusted.jssecerts";
    public static final String KEY_SYSTEM_TRUSTED_CLIENT_CERTS = "deployment.system.security.trusted.clientautcerts";

    public enum ConfigType {
        System, User
    }

    /** is it mandatory to load the system properties? */
    private boolean systemPropertiesMandatory = false;

    /** The system's deployment.config file */
    private File systemPropertiesFile = null;
    /** The user's deployment.config file */
    private File userPropertiesFile = null;

    /** the current deployment properties */
    private Map<String, ConfigValue> currentConfiguration;

    /** the deployment properties that cannot be changed */
    private Map<String, ConfigValue> unchangeableConfiguration;

    public DeploymentConfiguration() {
        currentConfiguration = new HashMap<String,ConfigValue>();
        unchangeableConfiguration = new HashMap<String, ConfigValue>();
    }

    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @throws DeploymentException if it encounters a fatal error.
     */
    public void load() throws ConfigurationException {
        // make sure no state leaks if security check fails later on
        File userFile = new File(System.getProperty("user.home") + File.separator + DEPLOYMENT_DIR
                + File.separator + DEPLOYMENT_PROPERTIES);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(userFile.toString());
        }

        Map<String, ConfigValue> initialProperties = loadDefaultProperties();

        Map<String, ConfigValue> systemProperties = null;

        /*
         * First, try to read the system's deployment.config file to find if
         * there is a system-level deployment.poperties file
         */

        File systemConfigFile = findSystemConfigFile();
        if (systemConfigFile != null) {
            if (loadSystemConfiguration(systemConfigFile)) {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("System level " + DEPLOYMENT_CONFIG + " is mandatory: " + systemPropertiesMandatory);
                }
                /* Second, read the System level deployment.properties file */
                systemProperties = loadProperties(ConfigType.System, systemPropertiesFile,
                        systemPropertiesMandatory);
            }
            if (systemProperties != null) {
                mergeMaps(initialProperties, systemProperties);
            }
        }

        /* need a copy of the original when we have to save */
        unchangeableConfiguration = new HashMap<String, ConfigValue>();
        Set<String> keys = initialProperties.keySet();
        for (String key : keys) {
            unchangeableConfiguration.put(key, new ConfigValue(initialProperties.get(key)));
        }

        /*
         * Third, read the user's deployment.properties file
         */
        userPropertiesFile = userFile;
        Map<String, ConfigValue> userProperties = loadProperties(ConfigType.User, userPropertiesFile,
                false);
        if (userProperties != null) {
            mergeMaps(initialProperties, userProperties);
        }

        currentConfiguration = initialProperties;
    }

    /**
     * Get the value for the given key
     *
     * @param key the property key
     * @return the value for the key, or null if it can not be found
     */
    public String getProperty(String key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkRead(userPropertiesFile.toString());
            }
        }

        return currentConfiguration.get(key).get();
    }

    /**
     * @return a Set containing all the property names
     */
    public Set<String> getAllPropertyNames() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkRead(userPropertiesFile.toString());
            }
        }

        return currentConfiguration.keySet();
    }

    /**
     * Sets the value of corresponding to the key. If the value has been marked
     * as locked, it is not changed
     *
     * @param key the key
     * @param value the value to be associated with the key
     */
    public void setProperty(String key, String value) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkWrite(userPropertiesFile.toString());
            }
        }

        ConfigValue currentValue = currentConfiguration.get(key);
        if (currentValue != null) {
            if (!currentValue.isLocked()) {
                currentValue.set(value);
            }
        } else {
            currentValue = new ConfigValue(value);
            currentConfiguration.put(key, currentValue);
        }
    }

    /**
     * Loads the default properties for deployment
     */
    private Map<String, ConfigValue> loadDefaultProperties() {

        final String SYSTEM_HOME = System.getProperty("java.home");
        final String SYSTEM_SECURITY = SYSTEM_HOME + File.separator + "lib" + File.separator
                + "security";

        final String USER_HOME = System.getProperty("user.home") + File.separator + DEPLOYMENT_DIR;
        final String USER_SECURITY = USER_HOME + File.separator + "security";

        final String LOCKS_DIR = System.getProperty("java.io.tmpdir") + File.separator
                + System.getProperty("user.name") + File.separator + "netx" + File.separator
                + "locks";

        /*
         * This is more or less a straight copy from the deployment
         * configuration page, with occasional replacements of "" or no-defaults
         * with null
         */

        String[][] defaults = new String[][] {
            /* infrastructure */
            { KEY_USER_CACHE_DIR, USER_HOME + File.separator + "cache" },
            { KEY_USER_PERSISTENCE_CACHE_DIR, USER_HOME + File.separator + "pcache" },
            { KEY_SYSTEM_CACHE_DIR, null },
            { KEY_USER_LOG_DIR, USER_HOME + File.separator + "log" },
            { KEY_USER_TMP_DIR, USER_HOME + File.separator + "tmp" },
            { KEY_USER_LOCKS_DIR, LOCKS_DIR },
            { KEY_USER_NETX_RUNNING_FILE, LOCKS_DIR + File.separator + "netx_running" },
            /* certificates and policy files */
            { "deployment.user.security.policy", "file://" + USER_SECURITY + File.separator + "java.policy" },
            { KEY_USER_TRUSTED_CA_CERTS, USER_SECURITY + File.separator + "trusted.cacerts" },
            { KEY_USER_TRUSTED_JSSE_CA_CERTS, USER_SECURITY + File.separator + "trusted.jssecacerts" },
            { KEY_USER_TRUSTED_CERTS, USER_SECURITY + File.separator + "trusted.certs" },
            { KEY_USER_TRUSTED_JSSE_CERTS, USER_SECURITY + File.separator + "trusted.jssecerts"},
            { KEY_USER_TRUSTED_CLIENT_CERTS, USER_SECURITY + File.separator + "trusted.clientcerts" },
            { "deployment.system.security.policy", null },
            { KEY_SYSTEM_TRUSTED_CA_CERTS , SYSTEM_SECURITY + File.separator + "cacerts" },
            { KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS, SYSTEM_SECURITY + File.separator + "jssecacerts" },
            { KEY_SYSTEM_TRUSTED_CERTS, SYSTEM_SECURITY + File.separator + "trusted.certs" },
            { KEY_SYSTEM_TRUSTED_JSSE_CERTS, SYSTEM_SECURITY + File.separator + "trusted.jssecerts" },
            { KEY_SYSTEM_TRUSTED_CLIENT_CERTS, SYSTEM_SECURITY + File.separator + "trusted.clientcerts" },
            /* security access and control */
            { "deployment.security.askgrantdialog.show", String.valueOf(true) },
            { "deployment.security.askgrantdialog.notinca", String.valueOf(true) },
            { "deployment.security.notinca.warning", String.valueOf(true) },
            { "deployment.security.expired.warning", String.valueOf(true) },
            { "deployment.security.jsse.hostmismatch.warning", String.valueOf(true) },
            { "deployment.security.trusted.policy", null },
            { "deployment.security.sandbox.awtwarningwindow", String.valueOf(true) },
            { "deployment.security.sandbox.jnlp.enhanced", String.valueOf(true) },
            { "deployment.security.authenticator", String.valueOf(true) },
            /* networking */
            { "deployment.proxy.type", String.valueOf(PROXY_TYPE_BROWSER) },
            { "deployment.proxy.same", String.valueOf(false) },
            { "deployment.proxy.auto.config.url", null },
            { "deployment.proxy.bypass.list", null },
            { "deployment.proxy.bypass.local", null },
            { "deployment.proxy.http.host", null },
            { "deployment.proxy.http.port", null },
            { "deployment.proxy.https.host", null },
            { "deployment.proxy.https.port", null },
            { "deployment.proxy.ftp.host", null },
            { "deployment.proxy.ftp.port", null },
            { "deployment.proxy.socks.host", null },
            { "deployment.proxy.socks.port", null },
            { "deployment.proxy.override.hosts", null },
            /* cache and optional package repository */
            { "deployment.cache.max.size", String.valueOf("-1") },
            { "deployment.cache.jarcompresson", String.valueOf(0) },
            { "deployment.javapi.cache.enabled", String.valueOf(false) },
            /* java console */
            { "deployment.console.startup.mode", CONSOLE_HIDE },
            /* tracing and logging */
            { "deployment.trace", String.valueOf(false) },
            { "deployment.log", String.valueOf(false) },
            /* JNLP association */
            { "deployment.javaws.associations", String.valueOf(JNLP_ASSOCIATION_ASK_USER) },
            /* desktop integration */
            { "deployment.javaws.shortcut", ShortcutDesc.SHORTCUT_ASK_USER_IF_HINTED},
            /* jre selection */
            { "deployment.javaws.installURL", null },
            /* jre management */
            { "deployment.javaws.autodownload", null },
            /* browser selection */
            { "deployment.browser.path", null },
            /* check for update timeout */
            { "deployment.javaws.update.timeout", String.valueOf(500) }
        };

        HashMap<String, ConfigValue> result = new HashMap<String, ConfigValue>();
        for (int i = 0; i < defaults.length; i++) {
            String key = defaults[i][0];
            String actualValue = defaults[i][1];
            boolean locked = false;
            ConfigValue value = new ConfigValue(actualValue, locked);
            result.put(key, value);
        }

        return result;
    }

    /**
     * @return the location of system-level deployment.config file, or null if none can be found
     */
    private File findSystemConfigFile() {
        File etcFile = new File(File.separator + "etc" + File.separator + ".java" + File.separator
                + "deployment" + File.separator + DEPLOYMENT_CONFIG);
        if (etcFile.isFile()) {
            return etcFile;
        }

        File jreFile = new File(System.getProperty("java.home") + File.separator + "lib"
                + File.separator + DEPLOYMENT_CONFIG);
        if (jreFile.isFile()) {
            return jreFile;
        }

        return null;
    }

    /**
     * Reads the system configuration file and sets the relevant
     * system-properties related variables
     */
    private boolean loadSystemConfiguration(File configFile) {

        if (JNLPRuntime.isDebug()) {
            System.out.println("Loading system configuation from: " + configFile);
        }

        Map<String, ConfigValue> systemConfiguration = new HashMap<String, ConfigValue>();
        try {
            systemConfiguration = parsePropertiesFile(configFile);
        } catch (IOException e) {
            if (JNLPRuntime.isDebug()) {
                System.out.println("No System level " + DEPLOYMENT_PROPERTIES + " found.");
            }
            return false;
        }

        /*
         * at this point, we have read the system deployment.config file
         * completely
         */

        try {
            String urlString = systemConfiguration.get("deployment.system.config").get();
            if (urlString == null) {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("No System level " + DEPLOYMENT_PROPERTIES + " found.");
                }
                return false;
            }
            URL url = new URL(urlString);
            if (url.getProtocol().equals("file")) {
                systemPropertiesFile = new File(url.getFile());
                if (JNLPRuntime.isDebug()) {
                    System.out.println("Using System level" + DEPLOYMENT_PROPERTIES + ": "
                            + systemPropertiesFile);
                }
                ConfigValue mandatory = systemConfiguration.get("deployment.system.config.mandatory");
                systemPropertiesMandatory = Boolean.valueOf(mandatory == null? null: mandatory.get());
                return true;
            } else {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("Remote + " + DEPLOYMENT_PROPERTIES + " not supported");
                }
                return false;
            }
        } catch (MalformedURLException e) {
            if (JNLPRuntime.isDebug()) {
                System.out.println("Invalid url for " + DEPLOYMENT_PROPERTIES);
            }
            return false;
        }
    }

    /**
     * Loads the properties file, if one exists
     *
     * @param type the ConfigType to load
     * @param file the File to load Properties from
     * @param mandatory indicates if reading this file is mandatory
     *
     * @throws ConfigurationException if the file is mandatory but cannot be read
     */
    private Map<String, ConfigValue> loadProperties(ConfigType type, File file, boolean mandatory)
            throws ConfigurationException {
        if (file == null || !file.isFile()) {
            if (JNLPRuntime.isDebug()) {
                System.out.println("No " + type.toString() + " level " + DEPLOYMENT_PROPERTIES + " found.");
            }
            if (!mandatory) {
                return null;
            } else {
                throw new ConfigurationException();
            }
        }

        if (JNLPRuntime.isDebug()) {
            System.out.println("Loading " + type.toString() + " level properties from: " + file);
        }
        try {
            return parsePropertiesFile(file);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Saves all properties that are not part of default or system properties
     *
     * @throws IOException if unable to save the file
     * @throws IllegalStateException if save() is called before load()
     */
    public void save() throws IOException {
        if (userPropertiesFile == null) {
            throw new IllegalStateException("must load() before save()");
        }

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkWrite(userPropertiesFile.toString());
        }

        if (JNLPRuntime.isDebug()) {
            System.out.println("Saving properties into " + userPropertiesFile.toString());
        }
        Properties toSave = new Properties();

        for (String key : currentConfiguration.keySet()) {
            String oldValue = unchangeableConfiguration.get(key) == null ? null
                    : unchangeableConfiguration.get(key).get();
            String newValue = currentConfiguration.get(key) == null ? null : currentConfiguration
                    .get(key).get();
            if (oldValue == null && newValue == null) {
                continue;
            } else if (oldValue == null && newValue != null) {
                toSave.setProperty(key, newValue);
            } else if (oldValue != null && newValue == null) {
                toSave.setProperty(key, newValue);
            } else { // oldValue != null && newValue != null
                if (!oldValue.equals(newValue)) {
                    toSave.setProperty(key, newValue);
                }
            }
        }

        File backupPropertiesFile = new File(userPropertiesFile.toString() + ".old");
        if (userPropertiesFile.isFile()) {
            if (!userPropertiesFile.renameTo(backupPropertiesFile)) {
                throw new IOException("Error saving backup copy of " + userPropertiesFile);
            }
        }

        userPropertiesFile.getParentFile().mkdirs();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(userPropertiesFile));
        try {
            toSave.store(out, DEPLOYMENT_COMMENT);
        } finally {
            out.close();
        }
    }

    /**
     * Reads a properties file and returns a map representing the properties
     *
     * @param propertiesFile the file to read Properties from
     * @param destination the map to which all the properties should be added
     * @throws IOException if an IO problem occurs
     */
    private Map<String, ConfigValue> parsePropertiesFile(File propertiesFile) throws IOException {
        Map<String, ConfigValue> result = new HashMap<String, ConfigValue>();

        Properties properties = new Properties();

        Reader reader = new BufferedReader(new FileReader(propertiesFile));
        try {
            properties.load(reader);
        } finally {
            reader.close();
        }

        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            if (key.endsWith(".locked")) {
                String realKey = key.substring(0, key.length() - ".locked".length());
                ConfigValue configValue = result.get(realKey);
                if (configValue == null) {
                    configValue = new ConfigValue(null, true);
                    result.put(realKey, configValue);
                } else {
                    configValue.setLocked(true);
                }
            } else {
                /* when parsing a properties we set value without checking if it is locked or not */
                String newValue = properties.getProperty(key);
                ConfigValue configValue = result.get(key);
                if (configValue == null) {
                    configValue = new ConfigValue(newValue);
                    result.put(key, configValue);
                } else {
                    configValue.set(newValue);
                }
            }
        }
        return result;
    }

    /**
     * Merges two maps while respecting whether the values have been locked or
     * not. All values from srcMap are put into finalMap, replacing values in
     * finalMap if necessary, unless the value is present and marked as locked
     * in finalMap
     *
     * @param finalMap the destination for putting values
     * @param srcMap the source for reading key value pairs
     */
    private void mergeMaps(Map<String, ConfigValue> finalMap, Map<String, ConfigValue> srcMap) {
        for (String key: srcMap.keySet()) {
            ConfigValue configValue = finalMap.get(key);
            if (configValue == null) {
                configValue = srcMap.get(key);
                finalMap.put(key, configValue);
            } else {
                if (!configValue.isLocked()) {
                    configValue.set(srcMap.get(key).get());
                }
            }
        }
    }

    /**
     * Dumps the configuration to the PrintStream
     *
     * @param config a map of key,value pairs representing the configuration to
     * dump
     * @param out the PrintStream to write data to
     */
    @SuppressWarnings("unused")
    private static void dumpConfiguration(Map<String, ConfigValue> config, PrintStream out) {
        System.out.println("KEY: VALUE [Locked]");

        for (String key : config.keySet()) {
            ConfigValue value = config.get(key);
            out.println("'" + key + "': '" + value.get() + "'"
                    + (value.isLocked() ? " [LOCKED]" : ""));
        }
    }
}
