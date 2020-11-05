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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.spi.IIORegistry;

import javax.naming.ConfigurationException;
import javax.swing.JOptionPane;

import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.tools.ico.IcoSpi;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Manages the various properties and configuration related to deployment.
 *
 * See:
 * http://download.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/properties.html
 */
public final class DeploymentConfiguration {

    public static final String DEPLOYMENT_CONFIG_FILE = "deployment.config";
    public static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    public static final String APPLET_TRUST_SETTINGS = ".appletTrustSettings";

    public static final String DEPLOYMENT_COMMENT = "Netx deployment configuration";
    public String userComments;
    public String systemComments;

    public static final int JNLP_ASSOCIATION_NEVER = 0;
    public static final int JNLP_ASSOCIATION_NEW_ONLY = 1;
    public static final int JNLP_ASSOCIATION_ASK_USER = 2;
    public static final int JNLP_ASSOCIATION_REPLACE_ASK = 3;

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console is not visible by default, but may be shown
     */
    public static final String CONSOLE_HIDE = "HIDE";
    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for both javaws and plugin
     */
    public static final String CONSOLE_SHOW = "SHOW";
    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console is not visible by default, nop data are passed to it (save memory and cpu) but can not be shown
     */
    public static final String CONSOLE_DISABLE = "DISABLE";
    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for  plugin
     */
    public static final String CONSOLE_SHOW_PLUGIN = "SHOW_PLUGIN_ONLY";
    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for javaws
     */
    public static final String CONSOLE_SHOW_JAVAWS = "SHOW_JAVAWS_ONLY";

    public static final String KEY_USER_CACHE_DIR = "deployment.user.cachedir";
    public static final String KEY_USER_PERSISTENCE_CACHE_DIR = "deployment.user.pcachedir";
    public static final String KEY_SYSTEM_CACHE_DIR = "deployment.system.cachedir";

    public static final String  KEY_CACHE_MAX_SIZE = "deployment.cache.max.size";

    public static final String KEY_CACHE_ENABLED = "deployment.javapi.cache.enabled";
    public static final String KEY_CACHE_COMPRESSION_ENABLED = "deployment.cache.jarcompression";

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

    /** Boolean. Only install the custom authenticator if true */
    public static final String KEY_SECURITY_ITW_IGNORECERTISSUES = "deployment.security.itw.ignorecertissues";
    
    public static final String KEY_STRICT_JNLP_CLASSLOADER = "deployment.jnlpclassloader.strict";
    
    /** Boolean. Do not prefere https over http */
    public static final String KEY_HTTPS_DONT_ENFORCE = "deployment.https.noenforce";
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
     * Logging
     */
    public static final String KEY_ENABLE_LOGGING = "deployment.log"; //same as verbose or ICEDTEAPLUGIN_DEBUG=true
    public static final String KEY_ENABLE_LOGGING_HEADERS = "deployment.log.headers"; //will add header OutputContorll.getHeader To all messages
    public static final String KEY_ENABLE_LOGGING_TOFILE = "deployment.log.file";
    public static final String KEY_ENABLE_APPLICATION_LOGGING_TOFILE ="deployment.log.file.clientapp"; //also client app will log to its separate file
    public static final String KEY_ENABLE_LEGACY_LOGBASEDFILELOG = "deployment.log.file.legacylog";
    public static final String KEY_ENABLE_LOGGING_TOSTREAMS = "deployment.log.stdstreams";
    public static final String KEY_ENABLE_LOGGING_TOSYSTEMLOG = "deployment.log.system";
    
    /*
     * manifest check
     */
    public static final String KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK = "deployment.manifest.attributes.check";

    /**
     * Console initial status.
     * One of CONSOLE_* values
     * See declaration above:
     * CONSOLE_HIDE = "HIDE";
     * CONSOLE_SHOW = "SHOW";
     * CONSOLE_DISABLE = "DISABLE";
     * CONSOLE_SHOW_PLUGIN = "SHOW_PLUGIN_ONLY";
     * CONSOLE_SHOW_JAVAWS = "SHOW_JAVAWS_ONLY";
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
    //for legacy reasons, also $BROWSER variable is supported
    public static final String BROWSER_ENV_VAR = "BROWSER";
    // both browser.path and BROWSER can ave those for-fun keys:
    public static final String ALWAYS_ASK="ALWAYS-ASK";
    public static final String INTERNAL_HTML="INTERNAL-HTML";
    public static final String LEGACY_WIN32_URL__HANDLER="rundll32 url.dll,FileProtocolHandler ";
    
    public static final String KEY_UPDATE_TIMEOUT = "deployment.javaws.update.timeout";
    
    public static final String IGNORE_HEADLESS_CHECK = "deployment.headless.ignore";

    /*
     * JVM arguments for plugin
     */
    public static final String KEY_PLUGIN_JVM_ARGUMENTS= "deployment.plugin.jvm.arguments";
    public static final String KEY_JRE_DIR= "deployment.jre.dir";
    /**
     * remote configuration properties
     */
    public static final String KEY_SYSTEM_CONFIG = "deployment.system.config";
    public static final String KEY_SYSTEM_CONFIG_MANDATORY = "deployment.system.config.mandatory";
    
    /**
     * Possibility to control hack which resizes very small applets
     */
    public static final String KEY_SMALL_SIZE_OVERRIDE_TRESHOLD = "deployment.small.size.treshold";
    public static final String KEY_SMALL_SIZE_OVERRIDE_WIDTH = "deployment.small.size.override.width";
    public static final String KEY_SMALL_SIZE_OVERRIDE_HEIGHT = "deployment.small.size.override.height";
    public static final String KEY_ENABLE_CACHE_FSYNC = "deployment.enable.cache.fsync";
    public static final String KEY_BACKGROUND_THREADS_COUNT = "deployment.background.threads.count";
    public static final String KEY_MAX_URLS_DOWNLOAD_INDICATOR = "deployment.max.urls.download.indicator";

    public static final String TRANSFER_TITLE = "Legacy configuration and cache found. Those will be now transported to new locations";
    
    private ConfigurationException loadingException = null;

    public void setLoadingException(ConfigurationException ex) {
        loadingException = ex;
    }

    public ConfigurationException getLoadingException() {
        return loadingException;
    }

    public void resetToDefaults() {
        currentConfiguration = Defaults.getDefaults();
    }

    static boolean checkUrl(URL file) {
        try (InputStream s = file.openStream()) {
            return true;
        } catch (Throwable ex) {
            // this should be logged, however, logging botle neck may not be initialised here
            return false;
        }
    }

    public enum ConfigType {
        System, User
    }

    /** is it mandatory to load the system properties? */
    private boolean systemPropertiesMandatory = false;

    /** The system's subdirResult deployment.config file */
    private URL systemPropertiesFile = null;
    /** Source of always right and only path to file (even if underlying path changes) */
    private final InfrastructureFileDescriptor userDeploymentFileDescriptor;
    /** The user's subdirResult deployment.config file */
    private File userPropertiesFile = null;
    
    /** the current deployment properties */
    private Map<String, Setting<String>> currentConfiguration;

    /** the deployment properties that cannot be changed */
    private Map<String, Setting<String>> unchangeableConfiguration;

    public DeploymentConfiguration() {
        this(PathsAndFiles.USER_DEPLOYMENT_FILE);
    }
    
     public DeploymentConfiguration(InfrastructureFileDescriptor configFile) {
        userDeploymentFileDescriptor = configFile;
        currentConfiguration = new HashMap<>();
        unchangeableConfiguration = new HashMap<>();
         try {
            IcoSpi spi = new IcoSpi();
            IIORegistry.getDefaultInstance().registerServiceProvider(spi);
            OutputController.getLogger().log("Ico provider registered correctly.");
        } catch (Exception ex) {
            OutputController.getLogger().log("Exception registering ico provider.");
            OutputController.getLogger().log(ex);
        }
    }

    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @throws ConfigurationException if it encounters a fatal error.
     */
    public void load() throws ConfigurationException {
        try {
            load(true);
        } catch (MalformedURLException ex) {
            throw new ConfigurationException(ex.toString());
        }
    }

    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @param fixIssues If true, fix issues that are discovered when reading configuration by
     * resorting to the default values
     * @throws ConfigurationException if it encounters a fatal error.
     */
    public void load(boolean fixIssues) throws ConfigurationException, MalformedURLException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(userDeploymentFileDescriptor.getFullPath());
        }

        URL systemConfigFile = findSystemConfigFile();

        load(systemConfigFile, userDeploymentFileDescriptor.getFile(), fixIssues);
    }

    void load(URL systemConfigFile, File userFile, boolean fixIssues) throws ConfigurationException, MalformedURLException {
        Map<String, Setting<String>> initialProperties = Defaults.getDefaults();

        Map<String, Setting<String>> systemProperties = null;

        /*
         * First, try to read the system's subdirResult deployment.config file to find if
         * there is a system-level deployment.poperties file
         */

        if (systemConfigFile != null) {
            if (loadSystemConfiguration(systemConfigFile)) {
                OutputController.getLogger().log("System level " + DEPLOYMENT_CONFIG_FILE + " is mandatory: " + systemPropertiesMandatory);
                /* Second, read the System level deployment.properties file */
                systemProperties = loadProperties(ConfigType.System, systemPropertiesFile,
                        systemPropertiesMandatory);
                systemComments=loadComments(systemPropertiesFile);
            }
            if (systemProperties != null) {
                mergeMaps(initialProperties, systemProperties);
            }
        }

        /* need a copy of the original when we have to save */
        unchangeableConfiguration = new HashMap<>();
        Set<String> keys = initialProperties.keySet();
        for (String key : keys) {
            unchangeableConfiguration.put(key, new Setting<>(initialProperties.get(key)));
        }

        /*
         * Third, read the user's subdirResult deployment.properties file
         */
        userPropertiesFile = userFile;
        Map<String, Setting<String>> userProperties = loadProperties(ConfigType.User, userPropertiesFile.toURI().toURL(), false);
        userComments = loadComments(userPropertiesFile.toURI().toURL());
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
     * @param target properties where to copy actual ones
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
            currentValue = new Setting<>(key, R("Unknown"), false, null, null, value, R("Unknown"));
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
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("DCInternal", "key " + key + " does not match setting name " + s.getName()));
            } else if (!defaults.containsKey(key)) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("DCUnknownSettingWithName", key));
            } else {
                ValueValidator checker = defaults.get(key).getValidator();
                if (checker == null) {
                    continue;
                }

                try {
                    checker.validate(s.getValue());
                } catch (IllegalArgumentException e) {
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("DCIncorrectValue", key, s.getValue(), checker.getPossibleValues()));
                    s.setValue(s.getDefaultValue());
                    OutputController.getLogger().log(e);
                }
            }
        }
    }

    /**
     * @return the location of system-level deployment.config file, or null if none can be found
     */
    private URL findSystemConfigFile() throws MalformedURLException {
        if (PathsAndFiles.ETC_DEPLOYMENT_CFG.getFile().isFile()) {
            return PathsAndFiles.ETC_DEPLOYMENT_CFG.getUrl();
        }

        String jrePath = null;
        try {
            Map<String, Setting<String>> tmpProperties = parsePropertiesFile(userDeploymentFileDescriptor.getUrl());
            Setting<String> jreSetting = tmpProperties.get(KEY_JRE_DIR);
            if (jreSetting != null) {
                jrePath = jreSetting.getValue();
            }
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }

        File jreFile;
        if (jrePath != null) {
            //based on property KEY_JRE_DIR
            jreFile = new File(jrePath + File.separator + "lib"
                    + File.separator + DEPLOYMENT_CONFIG_FILE);
        } else {
            jreFile = PathsAndFiles.JAVA_DEPLOYMENT_PROP_FILE.getFile();
        }
        if (jreFile.isFile()) {
            return jreFile.toURI().toURL();
        }

        return null;
    }

    /**
     * Reads the system configuration file and sets the relevant
     * system-properties related variables
     */
    private boolean loadSystemConfiguration(URL configFile) throws ConfigurationException {

        OutputController.getLogger().log("Loading system configuation from: " + configFile);

        Map<String, Setting<String>> systemConfiguration = new HashMap<>();
        try {
            systemConfiguration = parsePropertiesFile(configFile);
        } catch (IOException e) {
            OutputController.getLogger().log("No System level " + DEPLOYMENT_CONFIG_FILE + " found.");
            OutputController.getLogger().log(e);
            return false;
        }

        /*
         * at this point, we have read the system deployment.config file
         * completely
         */
        String urlString = null;
        try {
            Setting<String> urlSettings = systemConfiguration.get(KEY_SYSTEM_CONFIG);
            if (urlSettings == null || urlSettings.getValue() == null) {
                OutputController.getLogger().log("No System level " + DEPLOYMENT_PROPERTIES + " found in "+configFile.toExternalForm());
                return false;
            }
            urlString = urlSettings.getValue();
            Setting<String> mandatory = systemConfiguration.get(KEY_SYSTEM_CONFIG_MANDATORY);
            systemPropertiesMandatory = Boolean.valueOf(mandatory == null ? null : mandatory.getValue()); //never null
            OutputController.getLogger().log("System level settings " + DEPLOYMENT_PROPERTIES + " are mandatory:" + systemPropertiesMandatory);
            URL url = new URL(urlString);
            systemPropertiesFile = url;
            OutputController.getLogger().log("Using System level" + DEPLOYMENT_PROPERTIES + ": " + systemPropertiesFile);
            return true;
        } catch (MalformedURLException e) {
            OutputController.getLogger().log("Invalid url for " + DEPLOYMENT_PROPERTIES+ ": " + urlString + "in " + configFile.toExternalForm());
            OutputController.getLogger().log(e);
            if (systemPropertiesMandatory){
                ConfigurationException ce = new ConfigurationException("Invalid url to system properties, which are mandatory");
                ce.initCause(e);
                throw ce;
            } else {
                return false;
            }
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
    private Map<String, Setting<String>> loadProperties(ConfigType type, URL file, boolean mandatory)
            throws ConfigurationException {
        if (file == null || !checkUrl(file)) {
            OutputController.getLogger().log("No " + type.toString() + " level " + DEPLOYMENT_PROPERTIES + " found.");
            if (!mandatory) {
                return null;
            } else {
                throw new ConfigurationException();
            }
        }

        OutputController.getLogger().log("Loading " + type.toString() + " level properties from: " + file);
        try {
            return parsePropertiesFile(file);
        } catch (IOException e) {
            if (mandatory){
                ConfigurationException ce = new ConfigurationException("Exception during loading of " + file + " which is mandatory to read");
                ce.initCause(e);
                throw ce;
            }
            OutputController.getLogger().log(e);
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

        OutputController.getLogger().log("Saving properties into " + userPropertiesFile.toString());
        Properties toSave = new Properties();

        for (String key : currentConfiguration.keySet()) {
            String oldValue = unchangeableConfiguration.get(key) == null ? null
                    : unchangeableConfiguration.get(key).getValue();
            String newValue = currentConfiguration.get(key) == null ? null : currentConfiguration
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
            if (backupPropertiesFile.exists()){
                boolean result = backupPropertiesFile.delete();
                if(!result){
                    OutputController.getLogger().log("Failed to delete backup properties file " + backupPropertiesFile+ " silently continuing.");
                }
            }
            if (!userPropertiesFile.renameTo(backupPropertiesFile)) {
                throw new IOException("Error saving backup copy of " + userPropertiesFile);
            }
        }

        FileUtils.createParentDir(userPropertiesFile);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(userPropertiesFile))) {
            String comments = DEPLOYMENT_COMMENT;
            if (userComments.length() > 0) {
                comments = comments + System.lineSeparator() + userComments;
            }
            toSave.store(out, comments); ;
        }
    }

    /**
     * Reads a properties file and returns a map representing the properties
     *
     * @param propertiesFile the file to read Properties from
     * @throws IOException if an IO problem occurs
     */
    private Map<String, Setting<String>> parsePropertiesFile(URL propertiesFile) throws IOException {
        Map<String, Setting<String>> result = new HashMap<>();

        Properties properties = new Properties();

        try (Reader reader = new BufferedReader(new InputStreamReader(propertiesFile.openStream(), "UTF-8"))) {
            properties.load(reader);
        }

        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            if (key.endsWith(".locked")) {
                String realKey = key.substring(0, key.length() - ".locked".length());
                Setting<String> configValue = result.get(realKey);
                if (configValue == null) {
                    configValue = new Setting<>(realKey, R("Unknown"), true, null, null, null, propertiesFile.toString());
                    result.put(realKey, configValue);
                } else {
                    configValue.setLocked(true);
                }
            } else {
                /* when parsing a properties we set value without checking if it is locked or not */
                String newValue = properties.getProperty(key);
                Setting<String> configValue = result.get(key);
                if (configValue == null) {
                    configValue = new Setting<>(key, R("Unknown"), false, null, null, newValue, propertiesFile.toString());
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
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "KEY: VALUE [Locked]");

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
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Critical error during converting old files to new. Continuing");
            OutputController.getLogger().log(t);
        }

    }

    private static void move14AndOlderFilesTo15Structure() {
        int errors = 0;
        String PRE_15_DEPLOYMENT_DIR = ".icedtea";
        String LEGACY_USER_HOME = System.getProperty("user.home") + File.separator + PRE_15_DEPLOYMENT_DIR;
        String legacyProperties = LEGACY_USER_HOME + File.separator + DEPLOYMENT_PROPERTIES;
        File configDir = new File(PathsAndFiles.USER_CONFIG_HOME);
        File cacheDir = new File(PathsAndFiles.USER_CACHE_HOME);
        File legacyUserDir = new File(LEGACY_USER_HOME);
        if (legacyUserDir.exists()) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, TRANSFER_TITLE);
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, PathsAndFiles.USER_CONFIG_HOME + " and " + PathsAndFiles.USER_CACHE_HOME);
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "You should not see this message next time you run icedtea-web!");
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Your custom dirs will not be touched and will work");
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "-----------------------------------------------");

            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Preparing new directories:");
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " " + PathsAndFiles.USER_CONFIG_HOME);
            errors += resultToStd(configDir.mkdirs());
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, " " + PathsAndFiles.USER_CACHE_HOME);
            errors += resultToStd(cacheDir.mkdirs());
            //move this first, the creation of config singleton may happen anytime...
            //but must not before USER_DEPLOYMENT_FILE is moved and should not in this block
            String currentProperties = PathsAndFiles.USER_DEPLOYMENT_FILE.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyProperties, currentProperties);

            String legacyPropertiesOld = LEGACY_USER_HOME + File.separator + DEPLOYMENT_PROPERTIES + ".old";
            String currentPropertiesOld = currentProperties + ".old";
            errors += moveLegacyToCurrent(legacyPropertiesOld, currentPropertiesOld);

            String legacySecurity = LEGACY_USER_HOME + File.separator + "security";
            String currentSecurity = PathsAndFiles.USER_DEFAULT_SECURITY_DIR;
            errors += moveLegacyToCurrent(legacySecurity, currentSecurity);
            
            String legacyAppletTrust = LEGACY_USER_HOME + File.separator + APPLET_TRUST_SETTINGS;
            String currentAppletTrust = PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyAppletTrust, currentAppletTrust);

            //note - all here use default path. Any call to getFullPAth will invoke creation of config singleton
            // but: we DO copy only defaults. There is no need to copy nondefaults!
            // nond-efault will be used thanks to config singleton read from copied deployment.properties

            String legacyCache = LEGACY_USER_HOME + File.separator + "cache";
            String currentCache = PathsAndFiles.CACHE_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyCache, currentCache);
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Adapting " + PathsAndFiles.CACHE_INDEX_FILE_NAME + " to new destination");
            //replace all legacyCache by currentCache in new recently_used
            try {
                File f = PathsAndFiles.getRecentlyUsedFile().getDefaultFile();
                String s = FileUtils.loadFileAsString(f);
                s = s.replace(legacyCache, currentCache);
                FileUtils.saveFile(s, f);
            } catch (IOException ex) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, ex);
                errors++;
            }

            String legacyPcahceDir = LEGACY_USER_HOME + File.separator + "pcache";
            String currentPcacheDir = PathsAndFiles.PCACHE_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyPcahceDir, currentPcacheDir);

            String legacyLogDir = LEGACY_USER_HOME + File.separator + "log";
            String currentLogDir = PathsAndFiles.LOG_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyLogDir, currentLogDir);

            String legacyTmp = LEGACY_USER_HOME + File.separator + "tmp";
            String currentTmp = PathsAndFiles.TMP_DIR.getDefaultFullPath();
            errors += moveLegacyToCurrent(legacyTmp, currentTmp);

            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Removing now empty " + LEGACY_USER_HOME);
            errors += resultToStd(legacyUserDir.delete());

            if (errors != 0) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "There occureed " + errors + " errors");
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Please double check content of old data in " + LEGACY_USER_HOME + " with ");
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "new " + PathsAndFiles.USER_CONFIG_HOME + " and " + PathsAndFiles.USER_CACHE_HOME);
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "To disable this check again, please remove " + LEGACY_USER_HOME);
            }

        } else {
            OutputController.getLogger().log("System is already following XDG .cache and .config specifications");
            try {
                OutputController.getLogger().log("config: " + PathsAndFiles.USER_CONFIG_HOME + " file exists: " + configDir.exists());
            } catch (Exception ex) {
                OutputController.getLogger().log(ex);
            }
            try {
                OutputController.getLogger().log("cache: " + PathsAndFiles.USER_CACHE_HOME + " file exists:" + cacheDir.exists());
            } catch (Exception ex) {
                OutputController.getLogger().log(ex);
            }
        }
        //this call should endure even if (ever) will migration code be removed
        DirectoryValidator.DirectoryCheckResults r = new DirectoryValidator().ensureDirs();
        if (r.getFailures() > 0) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, r.getMessage());
            if (!JNLPRuntime.isHeadless()) {
                JOptionPane.showMessageDialog(null, r.getMessage());
            }
        }

    }

    private static int moveLegacyToCurrent(String legacy, String current) {
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Moving " + legacy + " to " + current);
        File cf = new File(current);
        File old = new File(legacy);
        if (cf.exists()) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Warning! Destination " + current + " exists!");
        }
        if (old.exists()) {
            boolean moved = old.renameTo(cf);
            return resultToStd(moved);
        } else {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Source " + legacy + " do not exists, nothing to do");
            return 0;
        }

    }

    private static int resultToStd(boolean securityMove) {
        if (securityMove) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "OK");
            return 0;
        } else {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "ERROR");
            return 1;
        }
    }
    
    //standard date.toString format
    public static final SimpleDateFormat pattern = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    
    private static String loadComments(URL path) {
        StringBuilder r = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(path.openStream(), "UTF-8"))) {
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (s.startsWith("#")) {
                    String decommented = s.substring(1);
                    if (decommented.isEmpty()){
                        continue;
                    }
                    if (decommented.equals(DEPLOYMENT_COMMENT)){
                        continue;
                    }
                    //there is always also date
                    Date dd = null;
                    try {
                        dd = pattern.parse(decommented);
                    } catch (Exception ex) {
                        //we really dont care, failure is our decision point
                    }
                    if (dd == null){
                        r.append(decommented).append("\n");
                    }
                }
            }
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }
        
        return r.toString().trim();
    }
}
