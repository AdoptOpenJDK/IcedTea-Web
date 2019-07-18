// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
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

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.ComponentDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.InstallerDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader;
import net.adoptopenjdk.icedteaweb.xmlparser.Node;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.LocaleUtils;
import net.sourceforge.jnlp.util.LocaleUtils.Match;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;
import static net.adoptopenjdk.icedteaweb.StringUtils.hasPrefixMatch;
import static net.sourceforge.jnlp.util.LocaleUtils.localMatches;

/**
 * <p>
 * Provides methods to access the information in a Java Network
 * Launching Protocol (JNLP) file.  The Java Network Launching
 * Protocol specifies in an XML file the information needed to
 * load, cache, and run Java code over the network and in a secure
 * environment.
 * </p>
 * <p>
 * This class represents the overall information about a JNLP file
 * from the jnlp element.  Other information is accessed through
 * objects that represent the elements of a JNLP file
 * (information, resources, application-desc, etc).  References to
 * these objects are obtained by calling the getInformation,
 * getResources, getSecurity, etc methods.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.21 $
 */
public class JNLPFile {
    private final static Logger LOG = LoggerFactory.getLogger(JNLPFile.class);

    public static final String JNLP_ROOT_ELEMENT = "jnlp";

    public static final String SPEC_ATTRIBUTE = "spec";
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String CODEBASE_ATTRIBUTE = "codebase";

    public static final String SPEC_VERSION_DEFAULT = "1.0+";

    // todo: save the update policy, then if file was not updated
    // then do not check resources for being updated.
    //
    // todo: currently does not filter resources by jvm version.
    //

    /**
     * the location this JNLP file was created from
     */
    protected URL sourceLocation = null;

    /**
     * the network location of this JNLP file
     */
    protected URL fileLocation;

    /**
     * the ParserSettings which were used to parse this file
     */
    protected ParserSettings parserSettings = null;

    /**
     * A key that uniquely identifies connected instances (main jnlp+ext)
     */
    protected String uniqueKey = null;

    /**
     * the URL used to resolve relative URLs in the file
     */
    protected URL codeBase;

    /**
     * The version attribute of the jnlp element specifies the version of the application being launched,
     * as well as the version of the JNLP file itself.
     */
    protected VersionString fileVersion;

    /**
     * Specifies the versions of the specification that this JNLP file requires.
     * The value of the attribute is specified as a version string, see JSR-56, section 3.1
     */
    protected VersionString specVersion;

    /**
     * information
     */
    protected List<InformationDesc> infos;

    protected UpdateDesc update;

    /**
     * resources
     */
    protected List<ResourcesDesc> resources;

    /**
     * additional resources not in JNLP file (from command line)
     */
    protected final ResourcesDesc sharedResources = new ResourcesDesc(this, null, null, null);

    /**
     * the application entry point
     */
    protected EntryPoint entryPointDesc;

    /**
     * the component description
     */
    protected ComponentDesc component;

    /**
     * the security descriptor
     */
    protected SecurityDesc security;

    /**
     * the default JVM locale
     */
    protected Locale defaultLocale = null;

    /**
     * the default OS
     */
    protected String defaultOS = null;

    /**
     * the default arch
     */
    protected String defaultArch = null;

    /**
     * A signed JNLP file is missing from the main jar
     */
    private boolean missingSignedJNLP = false;

    /**
     * JNLP file contains special properties
     */
    private boolean containsSpecialProperties = false;

    /**
     * List of acceptable properties (not-special)
     */
    final private String[] generalProperties = SecurityDesc.getJnlpRIAPermissions();

    /**
     * important manifests' attributes
     */
    private final ManifestAttributesReader manifestAttributesReader = new ManifestAttributesReader(this);

    public static final String TITLE_NOT_FOUND = "Application title was not found in manifest. Check with application vendor";


    { // initialize defaults if security allows
        try {
            defaultLocale = Locale.getDefault();
            defaultOS = System.getProperty(OS_NAME);
            defaultArch = System.getProperty(OS_ARCH);
        }
        catch (SecurityException ex) {
            // null values will still work, and app can set defaults later
        }
    }

    /**
     * Empty stub, allowing child classes to override the constructor
     */
    protected JNLPFile() {
    }

    /**
     * Create a JNLPFile from a URL.
     *
     * @param location the location of the JNLP file
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location) throws IOException, ParseException {
        this(location, new ParserSettings());
    }

    /**
     * Create a JNLPFile from a URL checking for updates using the
     * default policy.
     *
     * @param location the location of the JNLP file
     * @param settings the parser settings to use while parsing the file
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, ParserSettings settings) throws IOException, ParseException {
        this(location, (VersionString) null, settings, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version  the version of the JNLP file
     * @param settings the {@link ParserSettings} to use when parsing the {@code location}
     * @param policy   the update policy
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, VersionString version, ParserSettings settings, UpdatePolicy policy) throws IOException, ParseException {
        this(location, version, settings, policy, null);
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location      the location of the JNLP file
     * @param version       the version of the JNLP file
     * @param settings      the parser settings to use while parsing the file
     * @param policy        the update policy
     * @param forceCodebase codebase to use if not specified in JNLP file.
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    protected JNLPFile(URL location, VersionString version, ParserSettings settings, UpdatePolicy policy, URL forceCodebase) throws IOException, ParseException {
        InputStream input = openURL(location, version, policy);
        this.parserSettings = settings;
        parse(input, location, forceCodebase);

        //Downloads the original jnlp file into the cache if possible
        //(i.e. If the jnlp file being launched exist locally, but it
        //originated from a website, then download the one from the website
        //into the cache).
        if (sourceLocation != null && "file".equals(location.getProtocol())) {
            openURL(sourceLocation, version, policy);
        }

        this.fileLocation = location;

        this.uniqueKey = Calendar.getInstance().getTimeInMillis() + "-" +
                ((int) (Math.random() * Integer.MAX_VALUE)) + "-" +
                location;

        LOG.debug("UNIQUEKEY=" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from a URL, parent URLm a version and checking for
     * updates using the specified policy.
     *
     * @param location  the location of the JNLP file
     * @param uniqueKey A string that uniquely identifies connected instances
     * @param version   the version of the JNLP file
     * @param settings  the parser settings to use while parsing the file
     * @param policy    the update policy
     * @throws IOException    if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, String uniqueKey, VersionString version, ParserSettings settings, UpdatePolicy policy) throws IOException, ParseException {
        this(location, version, settings, policy);
        this.uniqueKey = uniqueKey;

        LOG.warn("UNIQUEKEY (override) =" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from an input stream.
     *
     * @param input    input stream from which create jnlp file
     * @param settings settings of parser
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(InputStream input, ParserSettings settings) throws ParseException {
        this.parserSettings = settings;
        parse(input, null, null);
    }

    /**
     * Create a JNLPFile from an input stream.
     *
     * @param input    input stream of JNLP file.
     * @param codebase codebase to use if not specified in JNLP file..
     * @param settings the {@link ParserSettings} to use when parsing
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(InputStream input, URL codebase, ParserSettings settings) throws ParseException {
        this.parserSettings = settings;
        parse(input, null, codebase);
    }


    /**
     * Open the jnlp file URL from the cache if there, otherwise
     * download to the cache.
     * Unless file is find in cache, this method blocks until it is downloaded.
     * This is the best way in itw how to download and cache file
     *
     * @param location of resource to open
     * @param version  of resource
     * @param policy   update policy of resource
     * @return opened stream from given url
     * @throws java.io.IOException if something goes wrong
     */
    public static InputStream openURL(URL location, VersionString version, UpdatePolicy policy) throws IOException {
        if (location == null || policy == null)
            throw new IllegalArgumentException("Null parameter");

        try {
            ResourceTracker tracker = new ResourceTracker(false); // no prefetch
            tracker.addResource(location, version, null, policy);
            File f = tracker.getCacheFile(location);
            return new FileInputStream(f);
        }
        catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * @return the JNLP file's best localized title. This method returns the same
     * value as InformationDesc.getTitle().
     * <p>
     * Since jdk7 u45, also manifest title, and mainclass are taken to consideration;
     * See PluginBridge
     */
    public String getTitle() {
        try {
            return getTitle(false);
        }
        catch (MissingTitleException cause) {
            throw new RuntimeException(cause);
        }
    }

    public String getTitle(boolean kill) throws MissingTitleException {
        String title = getTitleImpl();
        if (title == null) {
            title = "";

        }
        if (title.trim().isEmpty() && kill) {
            throw new MissingTitleException();
        }
        if (title.trim().isEmpty()) {
            LOG.warn("The title section has not been specified for your locale nor does a default value exist in the JNLP file. and Missing Title");
            title = "Corrupted or missing title. Do not trust this application!";
            LOG.warn("However there is to many applications known to suffer this issue, so providing fake:" + ": {}", title);
        }
        else {
            LOG.info("Acceptable title tag found, contains: {}", title);
        }
        return title;
    }

    private String getTitleImpl() {
        String jnlpTitle = getTitleFromJnlp();
        String manifestTitle = getTitleFromManifest();
        if (jnlpTitle != null && manifestTitle != null) {
            if (jnlpTitle.equals(manifestTitle)) {
                return jnlpTitle;
            }
            return jnlpTitle + " (" + manifestTitle + ")";
        }
        if (jnlpTitle != null && manifestTitle == null) {
            return jnlpTitle;
        }
        if (jnlpTitle == null && manifestTitle != null) {
            return manifestTitle;
        }
        String mainClass = getManifestAttributesReader().getMainClass();
        return mainClass;
    }

    /**
     * @return the JNLP file's best localized title. This method returns the
     * same value as InformationDesc.getTitle().
     */
    public String getTitleFromJnlp() {
        return getInformation().getTitle();
    }

    public String getTitleFromManifest() {
        String inManifestTitle = getManifestAttributesReader().getApplicationName();
        if (inManifestTitle == null && getManifestAttributesReader().isLoader()) {
            LOG.warn(TITLE_NOT_FOUND);
        }
        return inManifestTitle;
    }

    /**
     * @return the JNLP file's best localized vendor. This method returns the
     * same value as InformationDesc.getVendor().
     */
    public String getVendor() {
        try {
            return getVendor(false);
        }
        catch (MissingVendorException cause) {
            throw new RuntimeException(cause);
        }
    }

    public String getVendor(boolean kill) throws MissingVendorException {
        String vendor = getVendorImpl();
        if (vendor == null) {
            vendor = "";
        }
        if (vendor.trim().isEmpty() && kill) {
            throw new MissingVendorException();
        }
        if (vendor.trim().isEmpty()) {
            LOG.warn("The vendor section has not been specified for your locale nor does a default value exist in the JNLP file.");
            vendor = "Corrupted or missing vendor. Do not trust this application!";
            LOG.warn("However there is to many applications known to suffer this issue, so providing fake:" + "vendor" + ": " + vendor);
        }
        else {
            LOG.info("Acceptable vendor tag found, contains: {}", vendor);
        }
        return vendor;
    }

    private String getVendorImpl() {
        return getInformation().getVendor();
    }

    /**
     * @return the JNLP file's network location as specified in the
     * JNLP file.
     */
    public URL getSourceLocation() {
        return sourceLocation;
    }

    /**
     * @return the location of the file parsed to create the JNLP
     * file, or null if it was not created from a URL.
     */
    public URL getFileLocation() {
        return fileLocation;
    }

    /**
     * @return the location of the parent file if it exists, null otherwise
     */
    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * @return the ParserSettings that was used to parse this file
     */
    public ParserSettings getParserSettings() {
        return parserSettings;
    }

    /**
     * @return the JNLP file's version.
     */
    public VersionString getFileVersion() {
        return fileVersion;
    }

    /**
     * Returns the versions of the specification that this JNLP file requires.
     * The value of the attribute is specified as a version string, see JSR-56, section 3.1
     *
     * @return the versions of the specification that this JNLP file requires.
     */
    public VersionString getSpecVersion() {
        return specVersion;
    }

    /**
     * @return the codebase URL for the JNLP file.
     */
    public URL getCodeBase() {
        return codeBase;
    }

    /**
     * It is not recommended to use this method for internals of itw - use normal getCodeBase rather, as null is expected always except toString calls.
     * <p>
     * If you are not sure, use getCodeBase and check null as you need. See that this method is used mostly for xtendedAppletSecuriyty dialogs.
     *
     * @return the codebase URL for the JNLP file  or url of location of calling file (jnlp, hreffed jnlp, or directly html)
     */
    public URL getNotNullProbableCodeBase() {
        if (getCodeBase() != null) {
            return getCodeBase();
        }
        try {
            return UrlUtils.removeFileName(getSourceLocation());
        }
        catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        return getSourceLocation();
    }

    /**
     * @return the information section of the JNLP file as viewed
     * through the default locale.
     */
    public InformationDesc getInformation() {
        return getInformation(defaultLocale);
    }

    /**
     * @param locale preferred locale of information element
     * @return the information section of the JNLP file as viewed
     * through the specified locale.
     */
    public InformationDesc getInformation(final Locale locale) {
        return this.getInformation(locale, defaultOS, defaultArch);
    }

    /**
     * @param locale preferred locale of information element
     * @param os     preferred os of information element
     * @param arch   preferred arch of information element
     * @return the information section of the JNLP file as viewed
     * through the specified locale.
     */
    public InformationDesc getInformation(final Locale locale, final String os, final String arch) {
        Objects.requireNonNull(locale, "locale");

        if (infos == null || infos.isEmpty()) {
            return new InformationDesc(new Locale[]{locale}, os, arch);
        }

        final boolean strict = infos.stream().anyMatch(infoDesc -> infoDesc.strict);

        final Map<String, List<Object>> mergedItems = new HashMap<>();

        for (InformationDesc infoDesc : infos) {
            for (Match precision : Match.values()) {
                if (localMatches(locale, precision, infoDesc.getLocales())) {
                    if (StringUtils.isBlank(os) || StringUtils.isBlank(infoDesc.getOs()) ||
                            (Objects.nonNull(infoDesc.getOs()) && infoDesc.getOs().toLowerCase().startsWith(os.toLowerCase()))) {
                        if (StringUtils.isBlank(arch) || StringUtils.isBlank(infoDesc.getArch()) ||
                                (Objects.nonNull(infoDesc.getArch()) && infoDesc.getArch().toLowerCase().startsWith(arch.toLowerCase()))) {

                            final Map<String, List<Object>> items = infoDesc.getItems();

                            for (String key : items.keySet()) {
                                if (mergedItems.containsKey(key)) {
                                    final List<Object> values = mergedItems.get(key);
                                    items.get(key).stream()
                                            .filter(v -> !StringUtils.isBlank(v.toString()))
                                            .forEach(values::add);
                                }
                                else {
                                    mergedItems.put(key, infoDesc.getItems(key));
                                }
                            }
                        }
                    }
                    break; // after first match of the locale break out of the precision loop.
                }
            }
        }

        return new InformationDesc(new Locale[]{locale}, os, arch, strict) {
            @Override
            public List<Object> getItems(Object key) {
                final List<Object> result = mergedItems.get(key);
                return result == null ? Collections.emptyList() : result;
            }

            @Override
            public void addItem(final String key, final Object value) {
                throw new IllegalStateException();
            }
        };
    }

    /**
     * @return the update section of the JNLP file.
     */
    public UpdateDesc getUpdate() {
        return update;
    }

    /**
     * @return the security section of the JNLP file.
     */
    public SecurityDesc getSecurity() {
        return security;
    }

    /**
     * @return the requested security level of the application represented by this JNLP file.
     */
    public ApplicationPermissionLevel getApplicationPermissionLevel() {
        return this.security.getApplicationPermissionLevel();
    }

    /**
     * @return the requested security level of the applet represented by this JNLP file.
     */
    public AppletPermissionLevel getAppletPermissionLevel() {
        return this.security.getAppletPermissionLevel();
    }

    /**
     * @return the resources section of the JNLP file as viewed
     * through the default locale and the os.name and os.arch
     * properties.
     */
    public ResourcesDesc getResources() {
        return getResources(defaultLocale, defaultOS, defaultArch);
    }

    /**
     * @param locale preferred locale of resource
     * @param os     preferred os of resource
     * @param arch   preferred arch of resource
     * @return the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc getResources(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc(this, new Locale[]{locale}, new String[]{os}, new String[]{arch}) {

            @Override
            public <T> List<T> getResources(Class<T> launchType) {
                List<T> result = new ArrayList<>();

                for (ResourcesDesc rescDesc : resources) {
                    boolean hasUsableLocale = false;
                    for (final Match match : Match.values()) {
                        hasUsableLocale |= LocaleUtils.localeMatches(locale, rescDesc.getLocales(), match);
                    }
                    if (hasUsableLocale
                            && hasPrefixMatch(os, rescDesc.getOS())
                            && hasPrefixMatch(arch, rescDesc.getArch())) {
                        final List<T> ll = rescDesc.getResources(launchType);
                        result.addAll(ll);
                    }
                }

                result.addAll(sharedResources.getResources(launchType));

                return result;
            }

            @Override
            public void addResource(Object resource) {
                // todo: honor the current locale, os, arch values
                sharedResources.addResource(resource);
            }
        };
    }

    /**
     * @return the resources section of the JNLP file as viewed
     * through the default locale and the os.name and os.arch
     * properties.
     * XXX: Before overriding this method or changing its implementation,
     * read the comment in JNLPFile.getDownloadOptionsForJar(JARDesc).
     */
    public ResourcesDesc[] getResourcesDescs() {
        return getResourcesDescs(defaultLocale, defaultOS, defaultArch);
    }

    /**
     * @param locale preferred locale of resource
     * @param os     preferred os of resource
     * @param arch   preferred arch of resource
     * @return the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        List<ResourcesDesc> matchingResources = new ArrayList<>();
        for (ResourcesDesc rescDesc : resources) {
            boolean hasUsableLocale = false;
            for (Match match : Match.values()) {
                hasUsableLocale |= LocaleUtils.localeMatches(locale, rescDesc.getLocales(), match);
            }
            if (hasUsableLocale
                    && hasPrefixMatch(os, rescDesc.getOS())
                    && hasPrefixMatch(arch, rescDesc.getArch())) {
                matchingResources.add(rescDesc);
            }
        }
        return matchingResources.toArray(new ResourcesDesc[0]);
    }

    /**
     * @return an object of one of the following types: AppletDesc,
     * ApplicationDesc and InstallerDesc
     */
    public EntryPoint getEntryPointDesc() {
        return entryPointDesc;
    }

    /**
     * @return the launch information for an applet.
     * @throws UnsupportedOperationException if there is no applet information
     */
    public AppletDesc getApplet() {
        if (!isApplet())
            throw new UnsupportedOperationException("File is not an applet.");

        return (AppletDesc) entryPointDesc;
    }

    /**
     * @return the launch information for an application.
     * @throws UnsupportedOperationException if there is no application information
     */
    public ApplicationDesc getApplication() {
        if (!isApplication())
            throw new UnsupportedOperationException("File is not an application.");

        return (ApplicationDesc) entryPointDesc;
    }

    /**
     * @return the launch information for a component.
     * @throws UnsupportedOperationException if there is no component information
     */
    public ComponentDesc getComponent() {
        if (!isComponent())
            throw new UnsupportedOperationException("File is not a component.");

        return component;
    }

    /**
     * @return the launch information for an installer.
     * @throws UnsupportedOperationException if there is no installer information
     */
    public InstallerDesc getInstaller() {
        if (!isInstaller())
            throw new UnsupportedOperationException("File is not an installer.");

        return (InstallerDesc) entryPointDesc;
    }

    /**
     * @return whether the launch descriptor describes an Applet.
     */
    public boolean isApplet() {
        return entryPointDesc instanceof AppletDesc;
    }

    /**
     * @return whether the launch descriptor describes an Application.
     */
    public boolean isApplication() {
        return entryPointDesc instanceof ApplicationDesc;
    }

    /**
     * @return whether the launch descriptor describes a Component.
     */
    public boolean isComponent() {
        return component != null;
    }

    /**
     * @return whether the launch descriptor describes an Installer.
     */
    public boolean isInstaller() {
        return entryPointDesc instanceof InstallerDesc;
    }

    /**
     * Sets the default view of the JNLP file returned by
     * getInformation, getResources, etc.  If unset, the defaults
     * are the properties os.name, os.arch, and the locale returned
     * by Locale.getDefault().
     *
     * @param os     preferred os of resource
     * @param arch   preferred arch of resource
     * @param locale preferred locale of resource
     */
    public void setDefaults(String os, String arch, Locale locale) {
        defaultOS = os;
        defaultArch = arch;
        defaultLocale = locale;
    }

    /**
     * Initialize the JNLPFile fields. Private because it's called
     * from the constructor.
     *
     * @param location the file location or {@code null}
     */
    private void parse(InputStream input, URL location, URL forceCodebase) throws ParseException {
        try {
            //if (location != null)
            //  location = new URL(location, "."); // remove filename

            final XMLParser xmlParser = XmlParserFactory.getParser(parserSettings.getParserType());
            final Node root = xmlParser.getRootNode(input);
            final Parser parser = new Parser(this, location, root, parserSettings, forceCodebase); // true == allow extensions

            // JNLP tag information
            specVersion = parser.getSpecVersion();
            fileVersion = parser.getFileVersion();
            codeBase = parser.getCodeBase();
            sourceLocation = parser.getFileLocation();
            infos = parser.getInformationDescs(root);
            parser.checkForInformation();
            update = parser.getUpdate(root);
            resources = parser.getResources(root, false); // false == not a j2se/java resources section
            entryPointDesc = parser.getEntryPointDesc(root);
            component = parser.getComponent(root);
            security = parser.getSecurity(root);

            checkForSpecialProperties();

        }
        catch (ParseException ex) {
            throw ex;
        }
        catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * Inspects the JNLP file to check if it contains any special properties
     */
    private void checkForSpecialProperties() {

        for (ResourcesDesc res : resources) {
            for (PropertyDesc propertyDesc : res.getProperties()) {

                for (int i = 0; i < generalProperties.length; i++) {
                    String property = propertyDesc.getKey();

                    if (property.equals(generalProperties[i])) {
                        break;
                    }
                    else if (!property.equals(generalProperties[i])
                            && i == generalProperties.length - 1) {
                        containsSpecialProperties = true;
                        return;
                    }
                }

            }
        }
    }

    /**
     * @return true if the JNLP file specifies things that can only be
     * applied on a new vm (eg: different max heap memory)
     */
    public boolean needsNewVM() {

        return !getNewVMArgs().isEmpty();
    }

    /**
     * @return a list of args to pass to the new
     * JVM based on this JNLP file
     */
    public List<String> getNewVMArgs() {

        List<String> newVMArgs = new LinkedList<>();

        JREDesc[] jres = getResources().getJREs();
        for (JREDesc jre : jres) {
            String initialHeapSize = jre.getInitialHeapSize();
            if (initialHeapSize != null) {
                newVMArgs.add("-Xms" + initialHeapSize);
            }
            String maxHeapSize = jre.getMaximumHeapSize();
            if (maxHeapSize != null) {
                newVMArgs.add("-Xmx" + maxHeapSize);
            }
            String vmArgsFromJre = jre.getVMArgs();
            if (vmArgsFromJre != null) {
                String[] args = vmArgsFromJre.split(" ");
                newVMArgs.addAll(Arrays.asList(args));
            }
        }

        return newVMArgs;
    }

    /**
     * @return the download options to use for downloading jars listed in this jnlp file.
     */
    public DownloadOptions getDownloadOptions() {
        boolean usePack = false;
        boolean useVersion = false;
        ResourcesDesc desc = getResources();
        if (Boolean.valueOf(desc.getPropertiesMap().get("jnlp.packEnabled"))) {
            usePack = true;
        }
        if (Boolean.valueOf(desc.getPropertiesMap().get("jnlp.versionEnabled"))) {
            useVersion = true;
        }
        return new DownloadOptions(usePack, useVersion);
    }

    /**
     * Returns a boolean after determining if a signed JNLP warning should be
     * displayed in the 'More Information' panel.
     *
     * @return true if a warning should be displayed; otherwise false
     */
    public boolean requiresSignedJNLPWarning() {
        return (missingSignedJNLP && containsSpecialProperties);
    }

    /**
     * Informs that a signed JNLP file is missing in the main jar
     */
    public void setSignedJNLPAsMissing() {
        missingSignedJNLP = true;
    }

    public ManifestAttributesReader getManifestAttributesReader() {
        return manifestAttributesReader;
    }


    public String createJnlpVendorValue() {
        final String location;
        if (getSourceLocation() != null) {
            location = getSourceLocation().toString();
        }
        else if (getCodeBase() != null) {
            location = getCodeBase().toString();
        }
        else {
            location = "unknown";
        }
        return location;
    }

    public String createJnlpVendor() {
        return "Generated from applet from " + createJnlpVendorValue();
    }

    private String createJnlpTitleValue() {
        final String location;
        if (getSourceLocation() != null) {
            location = new File(getSourceLocation().getFile()).getName();
        }
        else if (getCodeBase() != null) {
            location = new File(getCodeBase().getFile()).getName();
        }
        else {
            location = "unknown";
        }
        return location;
    }

    public String createJnlpTitle() {
        //case when creating name from already created name
        String shortenedTitle = getTitle();
        int i = shortenedTitle.lastIndexOf("(");
        if (i >= 2) { // not cutting immediately...
            shortenedTitle = shortenedTitle.substring(0, i - 1);
        }
        if (createJnlpTitleValue().startsWith(shortenedTitle)) {
            return createJnlpTitleValue();
        }
        return getTitle() + " from " + createJnlpTitleValue();
    }

    public String createNameForDesktopFile() {
        String basicTitle = getTitle();
        if (basicTitle == null || basicTitle.trim().isEmpty()) {
            return createJnlpTitleValue().replaceAll(".jnlp$", "");
        }
        else {
            return basicTitle;
        }
    }
}

