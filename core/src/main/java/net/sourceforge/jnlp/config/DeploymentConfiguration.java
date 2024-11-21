// Copyright (C) 2010 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.config.validators.ValueValidator;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.icon.IcoReaderSpi;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.config.validators.ValidatorUtils.splitCombination;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.config.ConfigurationConstants.DEPLOYMENT_PROPERTIES;

/**
 * Manages the various properties and configuration related to deployment.
 *
 * See:
 * http://download.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/properties.html
 */
public final class DeploymentConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DeploymentConfiguration.class);
    public static final String LOCKED_POSTFIX = ".locked";
    public static final String LOCAL_DEPLOYMENT_PROPERTIES_FILE_PATH = "localDeploymentPropertiesFilePath";

    public static final String USER_HOME_DIR_TOKEN = "#USER_HOME_DIR#";
    private String userComments;

    private ConfigurationException loadingException = null;


    enum ConfigType {
        SYSTEM, USER
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
    private final Map<String, Setting> currentConfiguration;

    /** the deployment properties that cannot be changed */
    private final Map<String, Setting> unchangeableConfiguration;

    public DeploymentConfiguration() {
        this(PathsAndFiles.USER_DEPLOYMENT_FILE);
    }

    DeploymentConfiguration(final InfrastructureFileDescriptor configFile) {
        userDeploymentFileDescriptor = configFile;
        currentConfiguration = new HashMap<>();
        unchangeableConfiguration = new HashMap<>();
        try {
            final IcoReaderSpi spi = new IcoReaderSpi();
            IIORegistry.getDefaultInstance().registerServiceProvider(spi);
            LOG.info("Ico provider registered correctly.");
        } catch (final Exception ex) {
            LOG.error("Exception registering ico provider.", ex);
        }
    }

    private Optional<Setting> getSetting(final String key) {
        return Optional.ofNullable(getRaw().get(key));
    }

    public boolean isLocked(final String key) {
        return getSetting(key)
                .map(Setting::isLocked)
                .orElse(false);
    }

    public void lock(final String key) {
        getSetting(key).ifPresent(s -> s.setLocked(true));
    }

    public void unlock(final String key) {
        getSetting(key).ifPresent(s -> s.setLocked(false));
    }

    public void setLoadingException(final ConfigurationException ex) {
        loadingException = ex;
    }

    public ConfigurationException getLoadingException() {
        return loadingException;
    }

    public void resetToDefaults() {
        currentConfiguration.clear();
        currentConfiguration.putAll(Defaults.getDefaults());
    }

    static boolean checkUrl(final URL file) {
        try {
            try (InputStream ignored = file.openStream()) {
                return true;
            } catch (ConnectException e) {
                // get some sleep and retry
                Thread.sleep(500);
                try (InputStream ignored = file.openStream()) {
                    return true;
                }
            }
        } catch (Throwable ex) {
            LOG.info("checkUrl failed for " + file.toExternalForm() + " reason: " + ex.getMessage());
            return false;
        }
    }


    /**
     * Initialize this deployment configuration by reading configuration files.
     * Generally, it will try to continue and ignore errors it finds (such as file not found).
     *
     * @throws ConfigurationException if it encounters a fatal error.
     */
    public void load() throws ConfigurationException {
        LOG.debug("Start DeploymentConfiguration.load()");
        try {
            load(true);
        } catch (final MalformedURLException ex) {
            throw new ConfigurationException(ex.toString());
        } finally {
            LOG.debug("End DeploymentConfiguration.load()");
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
    public void load(final boolean fixIssues) throws ConfigurationException, MalformedURLException {
        final String localDeploymentPropertiesFilePath = System.getProperty(LOCAL_DEPLOYMENT_PROPERTIES_FILE_PATH);
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (localDeploymentPropertiesFilePath != null) {
                sm.checkRead(localDeploymentPropertiesFilePath);
            } else {
                sm.checkRead(userDeploymentFileDescriptor.getFullPath());
            }
        }

        final Map<String, Setting> properties = Defaults.getDefaults();
        final Map<String, Setting> systemProperties = loadSystemProperties();

        mergeMaps(properties, systemProperties);

        /* need a copy of the original when we have to save */
        unchangeableConfiguration.clear();
        for (final Map.Entry<String, Setting> entry : properties.entrySet()) {
            unchangeableConfiguration.put(entry.getKey(), entry.getValue().copy());
        }

        /*
         * Third, read the user's subdirResult deployment.properties file or the custom config properties
         */
        userPropertiesFile = localDeploymentPropertiesFilePath != null ? new File(localDeploymentPropertiesFilePath) : userDeploymentFileDescriptor.getFile();
        final URL userPropertiesUrl = userPropertiesFile.toURI().toURL();
        final Map<String, Setting> userProperties = loadProperties(ConfigType.USER, userPropertiesUrl, false);
        userComments = loadComments(userPropertiesUrl);
        mergeMaps(properties, userProperties);

        processPropertiesWithHomeDirToken(properties);

        if (fixIssues) {
            checkAndFixConfiguration(properties);
        }

        currentConfiguration.clear();
        currentConfiguration.putAll(properties);
    }

    /**
     * First loads the {@code deployment.config} file to determine the URL of the system properties.
     * The deployment config file must contain a property with the key {@link ConfigurationConstants#KEY_SYSTEM_CONFIG}.
     * Optionally it can require the system properties to exist with the kye {@link ConfigurationConstants#KEY_SYSTEM_CONFIG_MANDATORY}.
     *
     * @see #findSystemConfigFile()
     * @see #loadSystemConfiguration(URL)
     *
     * @return the system properties
     */
    private Map<String, Setting> loadSystemProperties() throws MalformedURLException, ConfigurationException {
        /*
         * First, try to read the system's deployment.config file to find if
         * there is a system-level deployment.properties file
         */
        final URL systemConfigFile = findSystemConfigFile();
        if (systemConfigFile != null) {
            if (loadSystemConfiguration(systemConfigFile)) {
                LOG.info("System level {} is mandatory: {}", ConfigurationConstants.DEPLOYMENT_CONFIG_FILE, systemPropertiesMandatory);
                /*
                 * Second, read the System level deployment.properties file
                 */
                return loadProperties(ConfigType.SYSTEM, systemPropertiesFile, systemPropertiesMandatory);
            }
        }
        return new HashMap<>();
    }

    /**
     * Copies the current configuration into the target
     * @param target properties where to copy actual ones
     */
    public void copyTo(final Properties target) {
        final Set<String> names = getAllPropertyNames();

        for (final String name : names) {
            final String value = getProperty(name);
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
    public String getProperty(final String key) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkRead(userPropertiesFile.toString());
            }
        }
        if (currentConfiguration.get(key) != null) {
            return currentConfiguration.get(key).getValue();
        }
        return null;
    }

    public List<String> getPropertyAsList(final String key) {
        return splitCombination(getProperty(key));
    }

    /**
     * @return a Set containing all the property names
     */
    public Set<String> getAllPropertyNames() {
        final SecurityManager sm = System.getSecurityManager();
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
    public Map<String, Setting> getRaw() {
        final SecurityManager sm = System.getSecurityManager();
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
    public void setProperty(final String key, final String value) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (userPropertiesFile != null) {
                sm.checkWrite(userPropertiesFile.toString());
            }
        }

        final Setting currentValue = currentConfiguration.get(key);
        if (currentValue != null) {
            if (!currentValue.isLocked()) {
                currentValue.setValue(value);
            }
        } else {
            currentConfiguration.put(key, Setting.createUnknown(key, value));
        }
    }

    /**
     * Check that the configuration is valid. If there are invalid values,set
     * those values to the default values. This is done by using check()
     * method of the ValueChecker for each setting on the actual value. Fixes
     * are made in-place.
     *
     * @param initial a map representing the initial configuration
     */
    private void checkAndFixConfiguration(final Map<String, Setting> initial) {

        final Map<String, Setting> defaults = Defaults.getDefaults();

        for (final Map.Entry<String, Setting> entry : initial.entrySet()) {
            final String key = entry.getKey();
            final Setting setting = entry.getValue();
            if (!(Objects.equals(setting.getName(), key))) {
                LOG.warn("key '{}' does not match setting name '{}'", key, setting.getName());
            } else if (!defaults.containsKey(key)) {
                LOG.warn("Property '{}' is unknown.", key);
            } else {
                final ValueValidator checker = defaults.get(key).getValidator();
                if (checker != null) {
                    try {
                        checker.validate(setting.getValue());
                    } catch (final IllegalArgumentException e) {
                        LOG.error("Property '{}' has incorrect value \"{}\". Possible values {}. Setting default {}", key, setting.getValue(), checker.getPossibleValues(), setting.getDefaultValue(), e);
                        setting.setValue(setting.getDefaultValue());
                    }
                }
            }
        }
    }

    private void processPropertiesWithHomeDirToken(final Map<String, Setting> properties) {
        for (final Map.Entry<String, Setting> entry : properties.entrySet()) {
            final String key = entry.getKey();
            final Setting setting = entry.getValue();
            final String propertyValue = setting.getValue();
            if (propertyValue != null && propertyValue.contains(USER_HOME_DIR_TOKEN)) {
                final String newValue = propertyValue.replace(USER_HOME_DIR_TOKEN, JavaSystemProperties.getUserHome());
                setting.setValue(newValue);
                LOG.debug("Replaced USER_HOME_DIR_TOKEN in key {} value {} default {}", key, setting.getValue(), setting.getDefaultValue());
            }
        }
    }

    /**
     * Looks in the following locations:
     * <pre>
     * Unix:
     *   - /etc/.java/deployment.config
     *   - ${JAVA_HOME}/lib/deployment.config
     *
     * Windows:
     *   - ${WINDIR}\Sun\Java\deployment.config
     *   - ${JAVA_HOME}/lib/deployment.config
     * </pre>
     *
     *
     * @return the location of system-level deployment.config file, or null if none can be found
     */
    private URL findSystemConfigFile() throws MalformedURLException {
        if (PathsAndFiles.ITW_SYSTEM_DEPLOYMENT_CFG.getFile().isFile()) {
            return PathsAndFiles.ITW_SYSTEM_DEPLOYMENT_CFG.getUrl();
        }
        if (PathsAndFiles.ETC_DEPLOYMENT_CFG.getFile().isFile()) {
            return PathsAndFiles.ETC_DEPLOYMENT_CFG.getUrl();
        }

        String jrePath = null;
        try {
            final Map<String, Setting> tmpProperties = parsePropertiesFile(userDeploymentFileDescriptor.getUrl());
            final Setting jreSetting = tmpProperties.get(ConfigurationConstants.KEY_JRE_DIR);
            if (jreSetting != null) {
                jrePath = jreSetting.getValue();
            }
        } catch (final Exception ex) {
            LOG.error("Failed to parse property file " + userDeploymentFileDescriptor.getFile(), ex);
        }

        File jreFile;
        if (jrePath != null) {
            //based on property KEY_JRE_DIR
            jreFile = new File(jrePath + File.separator + "lib"
                    + File.separator + ConfigurationConstants.DEPLOYMENT_CONFIG_FILE);
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
    private boolean loadSystemConfiguration(final URL configFile) throws ConfigurationException {

        LOG.info("Loading system configuration from: {}", configFile);

        final Map<String, Setting> systemConfiguration;
        try {
            systemConfiguration = new HashMap<>(parsePropertiesFile(configFile));
        } catch (final IOException e) {
            LOG.error("No System level " + ConfigurationConstants.DEPLOYMENT_CONFIG_FILE + " found.", e);
            return false;
        }

        /*
         * at this point, we have read the system deployment.config file
         * completely
         */
        String urlString = null;
        try {
            final Setting urlSettings = systemConfiguration.get(ConfigurationConstants.KEY_SYSTEM_CONFIG);
            if (urlSettings == null || urlSettings.getValue() == null) {
                LOG.info("No System level {} found in {}", DEPLOYMENT_PROPERTIES, configFile.toExternalForm());
                return false;
            }
            urlString = urlSettings.getValue();
            final Setting mandatory = systemConfiguration.get(ConfigurationConstants.KEY_SYSTEM_CONFIG_MANDATORY);
            systemPropertiesMandatory = Boolean.parseBoolean(mandatory == null ? null : mandatory.getValue()); //never null
            LOG.info("System level settings {} are mandatory: {}", DEPLOYMENT_PROPERTIES, systemPropertiesMandatory);
            systemPropertiesFile = new URL(urlString);
            LOG.info("Using System level {} : {}", DEPLOYMENT_PROPERTIES, systemPropertiesFile);
            return true;
        } catch (final MalformedURLException e) {
            LOG.error("Invalid url for " + DEPLOYMENT_PROPERTIES + ": " + urlString + "in " + configFile.toExternalForm(), e);
            if (systemPropertiesMandatory){
                final ConfigurationException ce = new ConfigurationException("Invalid url to system properties, which are mandatory");
                ce.initCause(e);
                throw ce;
            } else {
                return false;
            }
        }
    }

    /**
     * Loads the properties from the given file into a map of {@link Setting}s. If the given properties file does not
     * exist or the URL is invalid and not mandatory, an empty Map is returned. If the file is considered to be
     * mandatory but does not exist, a ConfigurationException is thrown.
     *
     * @param type the ConfigType to load
     * @param file the File to load Properties from
     * @param mandatory indicates if reading this file is mandatory
     *
     * @throws ConfigurationException if the file is mandatory but does not exist or cannot be read properly
     */
    static Map<String, Setting> loadProperties(final ConfigType type, final URL file, final boolean mandatory)
            throws ConfigurationException {
        if (file == null || !checkUrl(file)) {
            final String message = String.format("No %s level %s found at %s.", type, DEPLOYMENT_PROPERTIES, file);
            if (mandatory) {
                final ConfigurationException configurationException = new ConfigurationException(message);
                LOG.error(message);
                throw configurationException;
            } else {
                LOG.warn(message);
                return new HashMap<>();
            }
        }

        LOG.info("Loading {} level properties from: {}", type, file);
        try {
            return parsePropertiesFile(file);
        } catch (final IOException e) {
            final String message = String.format("Exception during loading of properties file '%s'.", file);
            if (mandatory) {
                final ConfigurationException configurationException = new ConfigurationException(message);
                configurationException.initCause(e);
                LOG.error(message, e);
                throw configurationException;
            }
            LOG.warn(message);
            return new HashMap<>();
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

        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkWrite(userPropertiesFile.toString());
        }

        LOG.info("Saving properties into {}", userPropertiesFile.toString());
        final Properties toSave = new Properties();

        for (final String key : currentConfiguration.keySet()) {
            final Setting defaultSetting = unchangeableConfiguration.get(key);
            final Setting currentSetting = currentConfiguration.get(key);

            final String defaultValue = defaultSetting == null ? null : defaultSetting.getValue();
            final String newValue = currentSetting == null ? null : currentSetting.getValue();

            if (newValue != null && !newValue.equals(defaultValue)) {
                LOG.debug("Will save property '{}' with value ('{}')", key, newValue);
                toSave.setProperty(key, newValue);
            } else {
                LOG.debug("Won't save property '{}' since its current value is its default value ('{}')", key, defaultValue);
            }
            if (currentSetting != null && currentSetting.isLocked()) {
                LOG.debug("Will lock property '{}'", key);
                toSave.setProperty(key + LOCKED_POSTFIX, "true");
            } else {
                LOG.debug("Will not lock property '{}'", key);
            }
        }

        final File backupPropertiesFile = new File(userPropertiesFile.toString() + ".old");
        if (userPropertiesFile.isFile()) {
            if (backupPropertiesFile.exists()){
                final boolean result = backupPropertiesFile.delete();
                if(!result){
                    LOG.info("Failed to delete backup properties file {} silently continuing.", backupPropertiesFile);
                }
            }
            if (!userPropertiesFile.renameTo(backupPropertiesFile)) {
                throw new IOException("Error saving backup copy of " + userPropertiesFile);
            }
        }

        FileUtils.createParentDir(userPropertiesFile);
        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(userPropertiesFile))) {
            String comments = ConfigurationConstants.DEPLOYMENT_COMMENT;
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
    private static Map<String, Setting> parsePropertiesFile(final URL propertiesFile) throws IOException {
        final Map<String, Setting> result = new HashMap<>();

        final Properties properties = new Properties();

        try (final CloseableConnection con = ConnectionFactory.openConnection(propertiesFile)) {
            try (final InputStream inputStream = con.getInputStream()) {
                properties.load(inputStream);
            }
        }

        final Set<String> keys = properties.stringPropertyNames();
        for (final String key : keys) {
            if (key.endsWith(LOCKED_POSTFIX)) {
                final String realKey = key.substring(0, key.length() - LOCKED_POSTFIX.length());
                final Setting configValue = result.get(realKey);
                final String value = configValue == null ? null : configValue.getValue();
                result.put(realKey, Setting.createFromPropertyFile(realKey, value, true, propertiesFile));
            } else {
                final String newValue = properties.getProperty(key);
                final Setting configValue = result.get(key);
                final boolean locked = configValue != null && configValue.isLocked();
                result.put(key, Setting.createFromPropertyFile(key, newValue, locked, propertiesFile));
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
    private void mergeMaps(final Map<String, Setting> finalMap, final Map<String, Setting> srcMap) {
        for (String key : srcMap.keySet()) {
            final Setting destValue = finalMap.get(key);
            final Setting srcValue = srcMap.get(key);
            if (destValue == null) {
                finalMap.put(key, srcValue);
            } else {
                if (!destValue.isLocked()) {
                    finalMap.put(key, destValue.copyValuesFrom(srcValue));
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
    private static void dumpConfiguration(final Map<String, Setting> config, final PrintStream out) {
        LOG.info("KEY: VALUE [Locked]");

        for (final String key : config.keySet()) {
            Setting value = config.get(key);
            out.println("'" + key + "': '" + value.getValue() + "'"
                    + (value.isLocked() ? " [LOCKED]" : ""));
        }
    }

    //standard date.toString format
    private static final SimpleDateFormat pattern = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    private static String loadComments(final URL path) {
        final StringBuilder result = new StringBuilder();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(path.openStream(), StandardCharsets.UTF_8))) {
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (s.startsWith("#")) {
                    final String decommented = s.substring(1);
                    if (decommented.isEmpty()){
                        continue;
                    }
                    if (decommented.equals(ConfigurationConstants.DEPLOYMENT_COMMENT)){
                        continue;
                    }
                    //there is always also date
                    Date dd = null;
                    try {
                        dd = pattern.parse(decommented);
                    } catch (Exception ex) {
                        //we really don't care, failure is our decision point
                    }
                    if (dd == null){
                        result.append(decommented).append("\n");
                    }
                }
            }
        } catch (final Exception ex) {
            LOG.warn("Exception while loading comment form config file: {}", ex.getMessage());
        }

        return result.toString().trim();
    }

    /**
     * convenient method to show VVPossibleBrowserValues with all four params
     *
     * @return translation of VVPossibleBrowserValues with all params in
     */
    public static String VVPossibleBrowserValues() {
        return R(ConfigurationConstants.VV_POSSIBLE_BROWSER_VALUES, ConfigurationConstants.LEGACY_WIN32_URL__HANDLER,
                ConfigurationConstants.BROWSER_ENV_VAR,
                ConfigurationConstants.INTERNAL_HTML,
                ConfigurationConstants.ALWAYS_ASK,
                ConfigurationConstants.KEY_BROWSER_PATH
        );
    }
}
