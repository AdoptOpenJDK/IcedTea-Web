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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.sourceforge.jnlp.tools.ico.IcoSpi;
import net.sourceforge.jnlp.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.spi.IIORegistry;
import javax.naming.ConfigurationException;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Manages the various properties and configuration related to deployment.
 *
 * See:
 * http://download.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/properties.html
 */
public final class DeploymentConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentConfiguration.class);

    public static final String DEPLOYMENT_CONFIG_FILE = "deployment.config";
    public static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    public static final String APPLET_TRUST_SETTINGS = ".appletTrustSettings";

    public static final String DEPLOYMENT_COMMENT = "Netx deployment configuration";
    private String userComments;

    public static final int JNLP_ASSOCIATION_NEVER = 0;
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
    private static final String LEGACY_WIN32_URL__HANDLER="rundll32 url.dll,FileProtocolHandler ";
    
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


    private static final String VV_POSSIBLE_BROWSER_VALUES = "VVPossibleBrowserValues";

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
            LOG.info("Ico provider registered correctly.");
        } catch (Exception ex) {
            LOG.error("Exception registering ico provider.", ex);
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

    private void load(URL systemConfigFile, File userFile, boolean fixIssues) throws ConfigurationException, MalformedURLException {
        Map<String, Setting<String>> initialProperties = Defaults.getDefaults();

        Map<String, Setting<String>> systemProperties = null;

        /*
         * First, try to read the system's subdirResult deployment.config file to find if
         * there is a system-level deployment.poperties file
         */

        if (systemConfigFile != null) {
            if (loadSystemConfiguration(systemConfigFile)) {
                LOG.info("System level {} is mandatory: {}", DEPLOYMENT_CONFIG_FILE, systemPropertiesMandatory);
                /* Second, read the System level deployment.properties file */
                systemProperties = loadProperties(ConfigType.System, systemPropertiesFile,
                        systemPropertiesMandatory);
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
    private void checkAndFixConfiguration(Map<String, Setting<String>> initial) {

        Map<String, Setting<String>> defaults = Defaults.getDefaults();

        for (String key : initial.keySet()) {
            Setting<String> s = initial.get(key);
            if (!(s.getName().equals(key))) {
                LOG.info(R("DCInternal", "key " + key + " does not match setting name " + s.getName()));
            } else if (!defaults.containsKey(key)) {
                LOG.info(R("DCUnknownSettingWithName", key));
            } else {
                ValueValidator checker = defaults.get(key).getValidator();
                if (checker == null) {
                    continue;
                }

                try {
                    checker.validate(s.getValue());
                } catch (IllegalArgumentException e) {
                    LOG.error(R("DCIncorrectValue", key, s.getValue(), checker.getPossibleValues()), e);
                    s.setValue(s.getDefaultValue());
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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
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

        LOG.info("Loading system configuation from: {}", configFile);

        Map<String, Setting<String>> systemConfiguration = new HashMap<>();
        try {
            systemConfiguration = parsePropertiesFile(configFile);
        } catch (IOException e) {
            LOG.error("No System level " + DEPLOYMENT_CONFIG_FILE + " found.", e);
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
                LOG.info("No System level {} found in {}", DEPLOYMENT_PROPERTIES, configFile.toExternalForm());
                return false;
            }
            urlString = urlSettings.getValue();
            Setting<String> mandatory = systemConfiguration.get(KEY_SYSTEM_CONFIG_MANDATORY);
            systemPropertiesMandatory = Boolean.valueOf(mandatory == null ? null : mandatory.getValue()); //never null
            LOG.info("System level settings {} are mandatory: {}", DEPLOYMENT_PROPERTIES, systemPropertiesMandatory);
            systemPropertiesFile = new URL(urlString);
            LOG.info("Using System level {} : {}", DEPLOYMENT_PROPERTIES, systemPropertiesFile);
            return true;
        } catch (MalformedURLException e) {
            LOG.error("Invalid url for " + DEPLOYMENT_PROPERTIES+ ": " + urlString + "in " + configFile.toExternalForm(), e);
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
            LOG.info("No {} level {} found.", type.toString(), DEPLOYMENT_PROPERTIES);
            if (!mandatory) {
                return null;
            } else {
                throw new ConfigurationException();
            }
        }

        LOG.info("Loading {} level properties from: {}", type.toString(), file);
        try {
            return parsePropertiesFile(file);
        } catch (IOException e) {
            if (mandatory){
                ConfigurationException ce = new ConfigurationException("Exception during loading of " + file + " which is mandatory to read");
                ce.initCause(e);
                throw ce;
            }
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
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

        LOG.info("Saving properties into {}", userPropertiesFile.toString());
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
                    LOG.info("Failed to delete backup properties file {} silently continuing.", backupPropertiesFile);
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
            toSave.store(out, comments);
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

        try (Reader reader = new BufferedReader(new InputStreamReader(propertiesFile.openStream(), StandardCharsets.UTF_8))) {
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
        LOG.info("KEY: VALUE [Locked]");

        for (String key : config.keySet()) {
            Setting<String> value = config.get(key);
            out.println("'" + key + "': '" + value.getValue() + "'"
                    + (value.isLocked() ? " [LOCKED]" : ""));
        }
    }
    
    //standard date.toString format
    private static final SimpleDateFormat pattern = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    
    private static String loadComments(URL path) {
        StringBuilder r = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(path.openStream(), StandardCharsets.UTF_8))) {
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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        
        return r.toString().trim();
    }

    /**
     * convenient method to show VVPossibleBrowserValues with all four params
     *
     * @return translation of VVPossibleBrowserValues with all params in
     */
    public static String VVPossibleBrowserValues() {
        return R(VV_POSSIBLE_BROWSER_VALUES, DeploymentConfiguration.LEGACY_WIN32_URL__HANDLER,
                DeploymentConfiguration.BROWSER_ENV_VAR,
                DeploymentConfiguration.INTERNAL_HTML,
                DeploymentConfiguration.ALWAYS_ASK,
                DeploymentConfiguration.KEY_BROWSER_PATH
        );
    }
}
