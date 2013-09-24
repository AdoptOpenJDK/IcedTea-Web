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

package net.sourceforge.jnlp.config;

import static net.sourceforge.jnlp.runtime.Translator.R;

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

import net.sourceforge.jnlp.cache.CacheLRUWrapper;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * Manages the various properties and configuration related to deployment.
 *
 * See:
 * http://download.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/properties.html
 */
public final class DeploymentConfiguration {

    public static final String DEPLOYMENT_SUBDIR_DIR = "icedtea-web";
    public static final String DEPLOYMENT_CACHE_DIR = ".cache" + File.separator + DEPLOYMENT_SUBDIR_DIR;
    public static final String DEPLOYMENT_CONFIG_DIR = ".config" + File.separator + DEPLOYMENT_SUBDIR_DIR;
    public static final String DEPLOYMENT_CONFIG_FILE = "deployment.config";
    public static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    public static final String APPLET_TRUST_SETTINGS = ".appletTrustSettings";

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

    public static final String KEY_USER_SECURITY_POLICY = "deployment.user.security.policy";
    public static final String KEY_USER_TRUSTED_CA_CERTS = "deployment.user.security.trusted.cacerts";
    public static final String KEY_USER_TRUSTED_JSSE_CA_CERTS = "deployment.user.security.trusted.jssecacerts";
    public static final String KEY_USER_TRUSTED_CERTS = "deployment.user.security.trusted.certs";
    public static final String KEY_USER_TRUSTED_JSSE_CERTS = "deployment.user.security.trusted.jssecerts";
    public static final String KEY_USER_TRUSTED_CLIENT_CERTS = "deployment.user.security.trusted.clientauthcerts";

    public static final String KEY_SYSTEM_SECURITY_POLICY = "deployment.system.security.policy";
    public static final String KEY_SYSTEM_TRUSTED_CA_CERTS = "deployment.system.security.cacerts";
    public static final String KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS = "deployment.system.security.jssecacerts";
    public static final String KEY_SYSTEM_TRUSTED_CERTS = "deployment.system.security.trusted.certs";
    public static final String KEY_SYSTEM_TRUSTED_JSSE_CERTS = "deployment.system.security.trusted.jssecerts";
    public static final String KEY_SYSTEM_TRUSTED_CLIENT_CERTS = "deployment.system.security.trusted.clientautcerts";

    /*
     * Security and access control
     */

    /** Boolean. Only show security prompts to user if true */
    public static final String KEY_SECURITY_PROMPT_USER = "deployment.security.askgrantdialog.show";

    //enum of AppletSecurityLevel in result
    public static final String KEY_SECURITY_LEVEL = "deployment.security.level";

    public static final String KEY_SECURITY_TRUSTED_POLICY = "deployment.security.trusted.policy";

    /** Boolean. Only give AWTPermission("showWindowWithoutWarningBanner") if true */
    public static final String KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING = "deployment.security.sandbox.awtwarningwindow";

    /** Boolean. Only prompt user for granting any JNLP permissions if true */
    public static final String KEY_SECURITY_PROMPT_USER_FOR_JNLP = "deployment.security.sandbox.jnlp.enhanced";

    /** Boolean. Only install the custom authenticator if true */
    public static final String KEY_SECURITY_INSTALL_AUTHENTICATOR = "deployment.security.authenticator";

    /*
     * Networking
     */

    /** the proxy type. possible values are {@code JNLPProxySelector.PROXY_TYPE_*} */
    public static final String KEY_PROXY_TYPE = "deployment.proxy.type";

    /** Boolean. If true, the http host/port should be used for https and ftp as well */
    public static final String KEY_PROXY_SAME = "deployment.proxy.same";

    public static final String KEY_PROXY_AUTO_CONFIG_URL = "deployment.proxy.auto.config.url";
    public static final String KEY_PROXY_BYPASS_LIST = "deployment.proxy.bypass.list";
    public static final String KEY_PROXY_BYPASS_LOCAL = "deployment.proxy.bypass.local";
    public static final String KEY_PROXY_HTTP_HOST = "deployment.proxy.http.host";
    public static final String KEY_PROXY_HTTP_PORT = "deployment.proxy.http.port";
    public static final String KEY_PROXY_HTTPS_HOST = "deployment.proxy.https.host";
    public static final String KEY_PROXY_HTTPS_PORT = "deployment.proxy.https.port";
    public static final String KEY_PROXY_FTP_HOST = "deployment.proxy.ftp.host";
    public static final String KEY_PROXY_FTP_PORT = "deployment.proxy.ftp.port";
    public static final String KEY_PROXY_SOCKS4_HOST = "deployment.proxy.socks.host";
    public static final String KEY_PROXY_SOCKS4_PORT = "deployment.proxy.socks.port";
    public static final String KEY_PROXY_OVERRIDE_HOSTS = "deployment.proxy.override.hosts";

    /*
     * Tracing and Logging
     */
    public static final String KEY_ENABLE_TRACING = "deployment.trace";
    public static final String KEY_ENABLE_LOGGING = "deployment.log";

    /*
     * Console
     */
    public static final String KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode";

    /*
     * Desktop Integration
     */

    public static final String KEY_JNLP_ASSOCIATIONS = "deployment.javaws.associations";
    public static final String KEY_CREATE_DESKTOP_SHORTCUT = "deployment.javaws.shortcut";

    public static final String KEY_JRE_INTSTALL_URL = "deployment.javaws.installURL";
    public static final String KEY_AUTO_DOWNLOAD_JRE = "deployment.javaws.autodownload";

    public static final String KEY_BROWSER_PATH = "deployment.browser.path";
    public static final String KEY_UPDATE_TIMEOUT = "deployment.javaws.update.timeout";

    /*
     * JVM arguments for plugin
     */
    public static final String KEY_PLUGIN_JVM_ARGUMENTS= "deployment.plugin.jvm.arguments";
    public static final String KEY_JRE_DIR= "deployment.jre.dir";

    public enum ConfigType {
        System, User
    }

    /** is it mandatory to load the system properties? */
    private boolean systemPropertiesMandatory = false;

    /** The system's deployment.config file */
    private File systemPropertiesFile = null;
    /** The user's deployment.config file */
    private File userPropertiesFile = null;
    
    /*default user file*/
    public static final File USER_DEPLOYMENT_PROPERTIES_FILE = new File(Defaults.USER_CONFIG_HOME + File.separator + DEPLOYMENT_PROPERTIES);

    /** the current deployment properties */
    private Map<String, Setting<String>> currentConfiguration;

    /** the deployment properties that cannot be changed */
    private Map<String, Setting<String>> unchangeableConfiguration;

    public DeploymentConfiguration() {
        currentConfiguration = new HashMap<String, Setting<String>>();
        unchangeableConfiguration = new HashMap<String, Setting<String>>();
    }

    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @throws DeploymentException if it encounters a fatal error.
     */
    public void load() throws ConfigurationException {
        load(true);
    }

    public static File getAppletTrustUserSettingsPath() {
        return new File(Defaults.USER_CONFIG_HOME + File.separator + APPLET_TRUST_SETTINGS);
    }

     public static File getAppletTrustGlobalSettingsPath() {
       return new File(File.separator + "etc" + File.separator + ".java" + File.separator
                + "deployment" + File.separator + APPLET_TRUST_SETTINGS);
        
    }

    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @param fixIssues If true, fix issues that are discovered when reading configuration by
     * resorting to the default values
     * @throws DeploymentException if it encounters a fatal error.
     */
    public void load(boolean fixIssues) throws ConfigurationException {
        // make sure no state leaks if security check fails later on
        File userFile = new File(USER_DEPLOYMENT_PROPERTIES_FILE.getAbsolutePath());

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(userFile.toString());
        }

        File systemConfigFile = findSystemConfigFile();

        load(systemConfigFile, userFile, fixIssues);
    }

    void load(File systemConfigFile, File userFile, boolean fixIssues) throws ConfigurationException {
        Map<String, Setting<String>> initialProperties = Defaults.getDefaults();

        Map<String, Setting<String>> systemProperties = null;

        /*
         * First, try to read the system's deployment.config file to find if
         * there is a system-level deployment.poperties file
         */

        if (systemConfigFile != null) {
            if (loadSystemConfiguration(systemConfigFile)) {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("System level " + DEPLOYMENT_CONFIG_FILE + " is mandatory: " + systemPropertiesMandatory);
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
        unchangeableConfiguration = new HashMap<String, Setting<String>>();
        Set<String> keys = initialProperties.keySet();
        for (String key : keys) {
            unchangeableConfiguration.put(key, new Setting<String>(initialProperties.get(key)));
        }

        /*
         * Third, read the user's deployment.properties file
         */
        userPropertiesFile = userFile;
        Map<String, Setting<String>> userProperties = loadProperties(ConfigType.User, userPropertiesFile, false);
        if (userProperties != null) {
            mergeMaps(initialProperties, userProperties);
        }

        if (fixIssues) {
            checkAndFixConfiguration(initialProperties);
        }

        currentConfiguration = initialProperties;
    }

    /**
     * Copies the current configuration into the target
     */
    public void copyTo(Properties target) {
        Set<String> names = getAllPropertyNames();

        for (String name : names) {
            String value = getProperty(name);
            // for Properties, missing and null are identical
            if (value != null) {
                target.setProperty(name, value);
            }
        }
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

        String value = null;
        if (currentConfiguration.get(key) != null) {
            value = currentConfiguration.get(key).getValue();
        }
        return value;
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
     * @return a map containing property names and the corresponding settings
     */
    public Map<String, Setting<String>> getRaw() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkRead(userPropertiesFile.toString());
            }
        }

        return currentConfiguration;
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

        Setting<String> currentValue = currentConfiguration.get(key);
        if (currentValue != null) {
            if (!currentValue.isLocked()) {
                currentValue.setValue(value);
            }
        } else {
            currentValue = new Setting<String>(key, R("Unknown"), false, null, null, value, R("Unknown"));
            currentConfiguration.put(key, currentValue);
        }
    }

    /**
     * Check that the configuration is valid. If there are invalid values,set
     * those values to the default values. This is done by using check()
     * method of the ValueCheker for each setting on the actual value. Fixes
     * are made in-place.
     *
     * @param initial a map representing the initial configuration
     */
    public void checkAndFixConfiguration(Map<String, Setting<String>> initial) {

        Map<String, Setting<String>> defaults = Defaults.getDefaults();

        for (String key : initial.keySet()) {
            Setting<String> s = initial.get(key);
            if (!(s.getName().equals(key))) {
                System.out.println(R("DCInternal", "key " + key + " does not match setting name " + s.getName()));
            } else if (!defaults.containsKey(key)) {
                System.out.println(R("DCUnknownSettingWithName", key));
            } else {
                ValueValidator checker = defaults.get(key).getValidator();
                if (checker == null) {
                    continue;
                }

                try {
                    checker.validate(s.getValue());
                } catch (IllegalArgumentException e) {
                    System.out.println(R("DCIncorrectValue", key, s.getValue(), checker.getPossibleValues()));
                    s.setValue(s.getDefaultValue());
                }
            }
        }
    }

    /**
     * @return the location of system-level deployment.config file, or null if none can be found
     */
    private File findSystemConfigFile() {
        File etcFile = new File(File.separator + "etc" + File.separator + ".java" + File.separator
                + "deployment" + File.separator + DEPLOYMENT_CONFIG_FILE);
        if (etcFile.isFile()) {
            return etcFile;
        }

        String jrePath = null;
        try {
            Map<String, Setting<String>> tmpProperties = parsePropertiesFile(USER_DEPLOYMENT_PROPERTIES_FILE);
            Setting<String> jreSetting = tmpProperties.get(KEY_JRE_DIR);
            if (jreSetting != null) {
                jrePath = jreSetting.getValue();
            }
        } catch (Exception ex) {
            if (JNLPRuntime.isDebug()){
                ex.printStackTrace();
            }
        }

        File jreFile;
        if (jrePath != null) {
            jreFile = new File(jrePath + File.separator + "lib"
                    + File.separator + DEPLOYMENT_CONFIG_FILE);
        } else {
            jreFile = new File(System.getProperty("java.home") + File.separator + "lib"
                    + File.separator + DEPLOYMENT_CONFIG_FILE);
        }
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

        Map<String, Setting<String>> systemConfiguration = new HashMap<String, Setting<String>>();
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
            String urlString = systemConfiguration.get("deployment.system.config").getValue();
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
                Setting<String> mandatory = systemConfiguration.get("deployment.system.config.mandatory");
                systemPropertiesMandatory = Boolean.valueOf(mandatory == null ? null : (String) mandatory.getValue());
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
    private Map<String, Setting<String>> loadProperties(ConfigType type, File file, boolean mandatory)
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
            if (JNLPRuntime.isDebug()){
                e.printStackTrace();
            }
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
                    : (String) unchangeableConfiguration.get(key).getValue();
            String newValue = currentConfiguration.get(key) == null ? null : (String) currentConfiguration
                    .get(key).getValue();
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

        FileUtils.createParentDir(userPropertiesFile);
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
    private Map<String, Setting<String>> parsePropertiesFile(File propertiesFile) throws IOException {
        Map<String, Setting<String>> result = new HashMap<String, Setting<String>>();

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
                Setting<String> configValue = result.get(realKey);
                if (configValue == null) {
                    configValue = new Setting<String>(realKey, R("Unknown"), true, null, null, null, propertiesFile.toString());
                    result.put(realKey, configValue);
                } else {
                    configValue.setLocked(true);
                }
            } else {
                /* when parsing a properties we set value without checking if it is locked or not */
                String newValue = properties.getProperty(key);
                Setting<String> configValue = result.get(key);
                if (configValue == null) {
                    configValue = new Setting<String>(key, R("Unknown"), false, null, null, newValue, propertiesFile.toString());
                    result.put(key, configValue);
                } else {
                    configValue.setValue(newValue);
                    configValue.setSource(propertiesFile.toString());
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
    private void mergeMaps(Map<String, Setting<String>> finalMap, Map<String, Setting<String>> srcMap) {
        for (String key : srcMap.keySet()) {
            Setting<String> destValue = finalMap.get(key);
            Setting<String> srcValue = srcMap.get(key);
            if (destValue == null) {
                finalMap.put(key, srcValue);
            } else {
                if (!destValue.isLocked()) {
                    destValue.setSource(srcValue.getSource());
                    destValue.setValue(srcValue.getValue());
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
    private static void dumpConfiguration(Map<String, Setting<String>> config, PrintStream out) {
        System.out.println("KEY: VALUE [Locked]");

        for (String key : config.keySet()) {
            Setting<String> value = config.get(key);
            out.println("'" + key + "': '" + value.getValue() + "'"
                    + (value.isLocked() ? " [LOCKED]" : ""));
        }
    }

    public static void move14AndOlderFilesTo15StructureCatched() {
        try {
            move14AndOlderFilesTo15Structure();
        } catch (Throwable t) {
            System.err.println("Critical error during converting old files to new. Continuing");
            t.printStackTrace();
        }

    }

    private static void move14AndOlderFilesTo15Structure() {
        int errors = 0;
        String PRE_15_DEPLOYMENT_DIR = ".icedtea";
        String LEGACY_USER_HOME = System.getProperty("user.home") + File.separator + PRE_15_DEPLOYMENT_DIR;
        File legacyUserDir = new File(LEGACY_USER_HOME);
        if (legacyUserDir.exists()) {
            System.out.println("Legacy configuration and cache found. Those will be now transported to new locations");
            System.out.println(Defaults.USER_CONFIG_HOME + " and " + Defaults.USER_CACHE_HOME);
            System.out.println("You should not see this message next time you run icedtea-web!");
            System.out.println("Your custom dirs will not be touched and will work");
            System.out.println("-----------------------------------------------");

            System.out.println("Preparing new directories:");
            System.out.println(" " + Defaults.USER_CONFIG_HOME);
            File f1 = new File(Defaults.USER_CONFIG_HOME);
            errors += resultToStd(f1.mkdirs());
            System.out.println(" " + Defaults.USER_CACHE_HOME);
            File f2 = new File(Defaults.USER_CACHE_HOME);
            errors += resultToStd(f2.mkdirs());

            String legacySecurity = LEGACY_USER_HOME + File.separator + "security";
            String currentSecurity = Defaults.USER_SECURITY;
            errors += moveLegacyToCurrent(legacySecurity, currentSecurity);

            String legacyCache = LEGACY_USER_HOME + File.separator + "cache";
            String currentCache = Defaults.getDefaults().get(DeploymentConfiguration.KEY_USER_CACHE_DIR).getDefaultValue();
            errors += moveLegacyToCurrent(legacyCache, currentCache);
            System.out.println("Adapting " + CacheLRUWrapper.CACHE_INDEX_FILE_NAME + " to new destination");
            //replace all legacyCache by currentCache in new recently_used
            try {
                File f = new File(currentCache, CacheLRUWrapper.CACHE_INDEX_FILE_NAME);
                String s = FileUtils.loadFileAsString(f);
                s = s.replace(legacyCache, currentCache);
                FileUtils.saveFile(s, f);
            } catch (IOException ex) {
                ex.printStackTrace();
                errors++;
            }

            String legacyPcahceDir = LEGACY_USER_HOME + File.separator + "pcache";
            String currentPcacheDir = Defaults.getDefaults().get(DeploymentConfiguration.KEY_USER_PERSISTENCE_CACHE_DIR).getDefaultValue();
            errors += moveLegacyToCurrent(legacyPcahceDir, currentPcacheDir);

            String legacyLogDir = LEGACY_USER_HOME + File.separator + "log";
            String currentLogDir = Defaults.getDefaults().get(DeploymentConfiguration.KEY_USER_LOG_DIR).getDefaultValue();
            errors += moveLegacyToCurrent(legacyLogDir, currentLogDir);

            String legacyProperties = LEGACY_USER_HOME + File.separator + DEPLOYMENT_PROPERTIES;
            String currentProperties = Defaults.USER_CONFIG_HOME + File.separator + DEPLOYMENT_PROPERTIES;
            errors += moveLegacyToCurrent(legacyProperties, currentProperties);

            String legacyPropertiesOld = LEGACY_USER_HOME + File.separator + DEPLOYMENT_PROPERTIES + ".old";
            String currentPropertiesOld = Defaults.USER_CONFIG_HOME + File.separator + DEPLOYMENT_PROPERTIES + ".old";
            errors += moveLegacyToCurrent(legacyPropertiesOld, currentPropertiesOld);


            String legacyAppletTrust = LEGACY_USER_HOME + File.separator + APPLET_TRUST_SETTINGS;
            String currentAppletTrust = getAppletTrustUserSettingsPath().getAbsolutePath();
            errors += moveLegacyToCurrent(legacyAppletTrust, currentAppletTrust);

            String legacyTmp = LEGACY_USER_HOME + File.separator + "tmp";
            String currentTmp = Defaults.getDefaults().get(DeploymentConfiguration.KEY_USER_TMP_DIR).getDefaultValue();
            errors += moveLegacyToCurrent(legacyTmp, currentTmp);

            System.out.println("Removing now empty " + LEGACY_USER_HOME);
            errors += resultToStd(legacyUserDir.delete());

            if (errors != 0) {
                System.out.println("There occureed " + errors + " errors");
                System.out.println("Please double check content of old data in " + LEGACY_USER_HOME + " with ");
                System.out.println("new " + Defaults.USER_CONFIG_HOME + " and " + Defaults.USER_CACHE_HOME);
                System.out.println("To disable this check again, please remove " + LEGACY_USER_HOME);
            }

        } else {
            if (JNLPRuntime.isDebug()) {
                System.out.println("System is already following XDG .cache and .config specifications");
                try {
                    System.out.println("config: " + Defaults.USER_CONFIG_HOME + " file exists: " + new File(Defaults.USER_CONFIG_HOME).exists());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    System.out.println("cache: " + Defaults.USER_CACHE_HOME + " file exists:" + new File(Defaults.USER_CACHE_HOME));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    private static int moveLegacyToCurrent(String legacy, String current) {
        System.out.println("Moving " + legacy + " to " + current);
        File cf = new File(current);
        File old = new File(legacy);
        if (cf.exists()) {
            System.out.println("Warning! Destination " + current + " exists!");
        }
        if (old.exists()) {
            boolean moved = old.renameTo(cf);
            return resultToStd(moved);
        } else {
            System.out.println("Source " + legacy + " do not exists, nothing to do");
            return 0;
        }

    }

    private static int resultToStd(boolean securityMove) {
        if (securityMove) {
            System.out.println("OK");
            return 0;
        } else {
            System.out.println("ERROR");
            return 1;
        }
    }
}
