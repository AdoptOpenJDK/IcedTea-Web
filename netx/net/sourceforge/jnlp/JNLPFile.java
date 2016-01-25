// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

import java.io.File;
import java.io.FileInputStream;
import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;

import net.sourceforge.jnlp.SecurityDesc.RequestedPermissionLevel;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ClasspathMatcher;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

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

    public static enum ManifestBoolean {
        TRUE, FALSE, UNDEFINED;
    }
   

    // todo: save the update policy, then if file was not updated
    // then do not check resources for being updated.
    //
    // todo: make getLaunchInfo return a superclass that all the
    // launch types implement (can get codebase from it).
    //
    // todo: currently does not filter resources by jvm version.
    //

    /** the location this JNLP file was created from */
    protected URL sourceLocation = null;

    /** the network location of this JNLP file */
    protected URL fileLocation;

    /** the ParserSettings which were used to parse this file */
    protected ParserSettings parserSettings = null;

    /** A key that uniquely identifies connected instances (main jnlp+ext) */
    protected String uniqueKey = null;

    /** the URL used to resolve relative URLs in the file */
    protected URL codeBase;

    /** file version */
    protected Version fileVersion;

    /** spec version */
    protected Version specVersion;

    /** information */
    protected List<InformationDesc> info;

    protected UpdateDesc update;

    /** resources */
    protected List<ResourcesDesc> resources;

    /** additional resources not in JNLP file (from command line) */
    protected ResourcesDesc sharedResources = new ResourcesDesc(this, null, null, null);

    /** the application description */
    protected LaunchDesc launchType;

    /** the component description */
    protected ComponentDesc component;

    /** the security descriptor */
    protected SecurityDesc security;

    /** the default JVM locale */
    protected Locale defaultLocale = null;

    /** the default OS */
    protected String defaultOS = null;

    /** the default arch */
    protected String defaultArch = null;

    /** A signed JNLP file is missing from the main jar */
    private boolean missingSignedJNLP = false;

    /** JNLP file contains special properties */
    private boolean containsSpecialProperties = false;

    /**
     * List of acceptable properties (not-special)
     */
    final private String[] generalProperties = SecurityDesc.getJnlpRIAPermissions();
    
    /** important manifests' attributes */
    private final ManifestsAttributes manifestsAttributes = new ManifestsAttributes();

    public static final String TITLE_NOT_FOUND = "Application title was not found in manifest. Check with application vendor";


    { // initialize defaults if security allows
        try {
            defaultLocale = Locale.getDefault();
            defaultOS = System.getProperty("os.name");
            defaultArch = System.getProperty("os.arch");
        } catch (SecurityException ex) {
            // null values will still work, and app can set defaults later
        }
    }

    static enum Match { LANG_COUNTRY_VARIANT, LANG_COUNTRY, LANG, GENERALIZED }

    /**
     * Empty stub, allowing child classes to override the constructor
     */
    protected JNLPFile() {
    }

    /**
     * Create a JNLPFile from a URL.
     *
     * @param location the location of the JNLP file
     * @throws IOException if an IO exception occurred
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
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, ParserSettings settings) throws IOException, ParseException {
        this(location, (Version) null, settings);
    }

    /**
     * Create a JNLPFile from a URL and a Version checking for updates using
     * the default policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param settings the parser settings to use while parsing the file
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, Version version, ParserSettings settings) throws IOException, ParseException {
        this(location, version, settings, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param settings the {@link ParserSettings} to use when parsing the {@code location}
     * @param policy the update policy
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, Version version, ParserSettings settings, UpdatePolicy policy) throws IOException, ParseException {
	    this(location, version, settings, policy, null);
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param settings the parser settings to use while parsing the file
     * @param policy the update policy
     * @param forceCodebase codebase to use if not specified in JNLP file.
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    protected JNLPFile(URL location, Version version, ParserSettings settings, UpdatePolicy policy, URL forceCodebase) throws IOException, ParseException {
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
                         ((int)(Math.random()*Integer.MAX_VALUE)) + "-" +
                         location;

        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "UNIQUEKEY=" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from a URL, parent URLm a version and checking for
     * updates using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param uniqueKey A string that uniquely identifies connected instances
     * @param version the version of the JNLP file
     * @param settings the parser settings to use while parsing the file
     * @param policy the update policy
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, String uniqueKey, Version version, ParserSettings settings, UpdatePolicy policy) throws IOException, ParseException {
        this(location, version, settings, policy);
        this.uniqueKey = uniqueKey;

        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "UNIQUEKEY (override) =" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from an input stream.
     *
     * @param input input stream from which create jnlp file
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
     * @param input input stream of JNLP file.
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
     * @param location of resource to open
     * @param version of resource
     * @param policy update policy of resource
     * @return  opened streamfrom given url
     * @throws java.io.IOException  if something goes wrong
     */
    public static InputStream openURL(URL location, Version version, UpdatePolicy policy) throws IOException {
        if (location == null || policy == null)
            throw new IllegalArgumentException(R("NullParameter"));

        try {
            ResourceTracker tracker = new ResourceTracker(false); // no prefetch
            tracker.addResource(location, version, null, policy);
            File f = tracker.getCacheFile(location);
            return new FileInputStream(f);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * @return the JNLP file's best localized title. This method returns the same
     * value as InformationDesc.getTitle().
     * 
     * Since jdk7 u45, also manifest title, and mainclass are taken to consideration;
     * See PluginBridge
     */
    public String getTitle() {
        String jnlpTitle = getTitleFromJnlp();
        String manifestTitle = getTitleFromManifest();
        if (jnlpTitle != null && manifestTitle != null) {
            if (jnlpTitle.equals(manifestTitle)) {
                return jnlpTitle;
            }
            return jnlpTitle+" ("+manifestTitle+")";
        }
        if (jnlpTitle != null && manifestTitle == null) {
            return jnlpTitle;
        }
        if (jnlpTitle == null && manifestTitle != null) {
            return manifestTitle;
        }
        String mainClass = getManifestsAttributes().getMainClass();
        return mainClass;        
    }
    
    /**
     * @return the JNLP file's best localized title. This method returns the same
     * value as InformationDesc.getTitle().
     */
    public String getTitleFromJnlp() {
        return getInformation().getTitle();
    }
    
    public String getTitleFromManifest() {
        String inManifestTitle = getManifestsAttributes().getApplicationName();
        if (inManifestTitle == null && getManifestsAttributes().isLoader()){
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, TITLE_NOT_FOUND);
        }
        return inManifestTitle;
    }
    
    

    /**
     * @return the JNLP file's best localized vendor. This method returns the same
     * value as InformationDesc.getVendor().
     */
    public String getVendor() {
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
    public Version getFileVersion() {
        return fileVersion;
    }

    /**
     * @return the specification version required by the file.
     */
    public Version getSpecVersion() {
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
     *
     * If you are not sure, use getCodeBase and chek null as you need. See that this method is used mostly for xtendedAppletSecuriyty dialogs.
     * 
     * @return the codebase URL for the JNLP file  or url of location of calling file (jnlp, hreffed jnlp, or directly html)
     */
    public URL getNotNullProbalbeCodeBase() {
        if (getCodeBase()!=null){
            return getCodeBase();
        }
        try {
            return UrlUtils.removeFileName(getSourceLocation());
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
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
     * @param locale preferred locale of informations
     * @return the information section of the JNLP file as viewed
     * through the specified locale.
     */
    public InformationDesc getInformation(final Locale locale) {
        boolean strict = false;
        if (this.info != null) {
            for (InformationDesc infoDesc : this.info) {
                if (infoDesc.strict) {
                    strict = true;
                    break;
                }
            }
        }
        return new InformationDesc(new Locale[] { locale }, strict) {
            @Override
            protected List<Object> getItems(Object key) {
                List<Object> result = new ArrayList<>();

                for (Match precision : Match.values()) {
                    for (InformationDesc infoDesc : JNLPFile.this.info) {
                        if (localeMatches(locale, infoDesc.getLocales(), precision)) {
                            result.addAll(infoDesc.getItems(key));
                        }
                    }

                    if (result.size() > 0) {
                        return result;
                    }
                }
                return result;
            }

            @Override
            public String getTitle() {
                for (Match precision : Match.values()) {
                    for (InformationDesc infoDesc : JNLPFile.this.info) {
                        String title = infoDesc.getTitle();
                        if (localeMatches(locale, infoDesc.getLocales(), precision)
                                && title != null && !"".equals(title)) {
                            return title;
                        }
                    }
                }

                return null;
            }

            @Override
            public String getVendor() {
                for (Match precision : Match.values()) {
                    for (InformationDesc infoDesc : JNLPFile.this.info) {
                        String vendor = infoDesc.getVendor();
                        if (localeMatches(locale, infoDesc.getLocales(), precision)
                                && vendor != null && !"".equals(vendor)) {
                            return vendor;
                        }
                    }
                }

                return null;
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

    public RequestedPermissionLevel getRequestedPermissionLevel() {
        return this.security.getRequestedPermissionLevel();
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
     * @param os preferred os of resource
     * @param arch preferred arch of resource
     * @return the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc getResources(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc(this, new Locale[] { locale }, new String[] { os }, new String[] { arch }) {

            @Override
            public <T> List<T> getResources(Class<T> launchType) {
                List<T> result = new ArrayList<>();

                for (ResourcesDesc rescDesc : resources) {
                    boolean hasUsableLocale = false;
                    for (Match match : Match.values()) {
                        hasUsableLocale |= localeMatches(locale, rescDesc.getLocales(), match);
                    }
                    if (hasUsableLocale
                            && stringMatches(os, rescDesc.getOS())
                            && stringMatches(arch, rescDesc.getArch()))
                        result.addAll(rescDesc.getResources(launchType));
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
     * @param os preferred os of resource
     * @param arch preferred arch of resource
     * @return the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        List<ResourcesDesc> matchingResources = new ArrayList<>();
        for (ResourcesDesc rescDesc: resources) {
            boolean hasUsableLocale = false;
            for (Match match : Match.values()) {
                hasUsableLocale |= localeMatches(locale, rescDesc.getLocales(), match);
            }
            if (hasUsableLocale
                    && stringMatches(os, rescDesc.getOS())
                    && stringMatches(arch, rescDesc.getArch())) {
                matchingResources.add(rescDesc);
            }
        }
        return matchingResources.toArray(new ResourcesDesc[0]);
    }

    /**
     * @return an object of one of the following types: AppletDesc,
     * ApplicationDesc and InstallerDesc
     */
    public LaunchDesc getLaunchInfo() {
        return launchType;
    }

    /**
     * @return the launch information for an applet.
     *
     * @throws UnsupportedOperationException if there is no applet information
     */
    public AppletDesc getApplet() {
        if (!isApplet())
            throw new UnsupportedOperationException(R("JNotApplet"));

        return (AppletDesc) launchType;
    }

    /**
     * @return the launch information for an application.
     *
     * @throws UnsupportedOperationException if there is no application information
     */
    public ApplicationDesc getApplication() {
        if (!isApplication())
            throw new UnsupportedOperationException(R("JNotApplication"));

        return (ApplicationDesc) launchType;
    }

    /**
     * @return the launch information for a component.
     *
     * @throws UnsupportedOperationException if there is no component information
     */
    public ComponentDesc getComponent() {
        if (!isComponent())
            throw new UnsupportedOperationException(R("JNotComponent"));

        return component;
    }

    /**
     * @return the launch information for an installer.
     *
     * @throws UnsupportedOperationException if there is no installer information
     */
    public InstallerDesc getInstaller() {
        if (!isInstaller())
            throw new UnsupportedOperationException(R("NotInstaller"));

        return (InstallerDesc) launchType;
    }

    /**
     * @return whether the lauch descriptor describes an Applet.
     */
    public boolean isApplet() {
        return launchType instanceof AppletDesc;
    }

    /**
     * @return whether the lauch descriptor describes an Application.
     */
    public boolean isApplication() {
        return launchType instanceof ApplicationDesc;
    }

    /**
     * @return whether the lauch descriptor describes a Component.
     */
    public boolean isComponent() {
        return component != null;
    }

    /**
     * @return whether the lauch descriptor describes an Installer.
     */
    public boolean isInstaller() {
        return launchType instanceof InstallerDesc;
    }

    /**
     * Sets the default view of the JNLP file returned by
     * getInformation, getResources, etc.  If unset, the defaults
     * are the properties os.name, os.arch, and the locale returned
     * by Locale.getDefault().
     * @param os preferred os of resource      
     * @param arch preferred arch of resource
     * @param locale preferred locale of resource
     */
    public void setDefaults(String os, String arch, Locale locale) {
        defaultOS = os;
        defaultArch = arch;
        defaultLocale = locale;
    }

    /**
     * Returns whether a locale is matched by one of more other
     * locales. Only the non-empty language, country, and variant
     * codes are compared; for example, a requested locale of
     * Locale("","","") would always return true.
     *
     * @param requested the requested locale
     * @param available the available locales
     * @param matchLevel the depth with which to match locales.
     * @return {@code true} if {@code requested} matches any of {@code available}, or if
     * {@code available} is empty or {@code null}.
     * @see Locale
     * @see Match
     */
    public boolean localeMatches(Locale requested, Locale[] available, Match matchLevel) {

        if (matchLevel == Match.GENERALIZED)
            return available == null || available.length == 0;

        String language = requested.getLanguage(); // "" but never null
        String country = requested.getCountry();
        String variant = requested.getVariant();

        for (Locale locale : available) {
            switch (matchLevel) {
                case LANG:
                    if (!language.isEmpty()
                            && language.equals(locale.getLanguage())
                            && locale.getCountry().isEmpty()
                            && locale.getVariant().isEmpty())
                        return true;
                    break;
                case LANG_COUNTRY:
                    if (!language.isEmpty()
                            && language.equals(locale.getLanguage())
                            && !country.isEmpty()
                            && country.equals(locale.getCountry())
                            && locale.getVariant().isEmpty())
                        return true;
                    break;
                case LANG_COUNTRY_VARIANT:
                    if (language.equals(locale.getLanguage())
                            && country.equals(locale.getCountry())
                            && variant.equals(locale.getVariant()))
                        return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * @return whether the string is a prefix for any of the strings
     * in the specified array.
     *
     * @param prefixStr the prefix string
     * @param available the strings to test
     * @return true if prefixStr is a prefix of any strings in
     * available, or if available is empty or null.
     */
    private boolean stringMatches(String prefixStr, String available[]) {
        if (available == null || available.length == 0)
            return true;

        for (String available1 : available) {
            if (available1 != null && available1.startsWith(prefixStr)) {
                return true;
            }
        }

        return false;
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

            Node root = Parser.getRootNode(input, parserSettings);
            Parser parser = new Parser(this, location, root, parserSettings, forceCodebase); // true == allow extensions

            // JNLP tag information
            specVersion = parser.getSpecVersion();
            fileVersion = parser.getFileVersion();
            codeBase = parser.getCodeBase();
            sourceLocation = parser.getFileLocation() != null ? parser.getFileLocation() : location;
            info = parser.getInfo(root);
            parser.checkForInformation();
            update = parser.getUpdate(root);
            resources = parser.getResources(root, false); // false == not a j2se/java resources section
            launchType = parser.getLauncher(root);
            component = parser.getComponent(root);
            security = parser.getSecurity(root);

            checkForSpecialProperties();

        } catch (ParseException ex) {
            throw ex;
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
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
                    } else if (!property.equals(generalProperties[i])
                            && i == generalProperties.length - 1) {
                        containsSpecialProperties = true;
                        return;
                    }
                }

            }
        }
    }

    /**
     *
     * @return true if the JNLP file specifies things that can only be
     * applied on a new vm (eg: different max heap memory)
     */
    public boolean needsNewVM() {

        return !getNewVMArgs().isEmpty();
    }

    /**
     *  @return a list of args to pass to the new
     *  JVM based on this JNLP file
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

    public ManifestsAttributes getManifestsAttributes() {
        return manifestsAttributes;
    }
    
    
    public class ManifestsAttributes {

        public static final String APP_NAME = "Application-Name";
        public static final String CALLER_ALLOWABLE = "Caller-Allowable-Codebase";
        public static final String APP_LIBRARY_ALLOWABLE = "Application-Library-Allowable-Codebase";
        public static final String PERMISSIONS = "Permissions";
        public static final String CODEBASE = "Codebase";
        public static final String TRUSTED_ONLY = "Trusted-Only";
        public static final String TRUSTED_LIBRARY = "Trusted-Library";
        public static final String ENTRY_POINT="Entry-Point";
        
        private JNLPClassLoader loader;


        public void setLoader(JNLPClassLoader loader) {
            this.loader = loader;
        }

        public boolean isLoader() {
            return loader != null;
        }
        
        

        /**
         * main class can be defined outside of manifest.
         * This method is mostly for completeness
         * @return main-class as it is specified in application
         */
        public String getMainClass(){
            if (loader == null) {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Jars not ready to provide main class");
                return null;    
            }
            return loader.getMainClass();
        }
        
         /**
         *
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#entry_pt
         * @return values of Entry-Points attribute
         */
        public String[] getEntryPoints() {
            return splitEntryPoints(getEntryPointString());
        }
        
        public String getEntryPointString() {
            return getAttribute(ENTRY_POINT);
        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#app_name
         * @return value of Application-Name manifest attribute
         */
        public String getApplicationName(){
            return getAttribute(APP_NAME);
        }
        
        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#caller_allowable
         * @return values of Caller-Allowable-Codebase manifest attribute
         */
        public ClasspathMatcher.ClasspathMatchers getCallerAllowableCodebase() {
            return getCodeBaseMatchersAttribute(CALLER_ALLOWABLE, false);
        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#app_library
         * @return values of Application-Library-Allowable-Codebase manifest attribute
         */
        public ClasspathMatcher.ClasspathMatchers getApplicationLibraryAllowableCodebase() {
            return getCodeBaseMatchersAttribute(APP_LIBRARY_ALLOWABLE, true);
        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
         * @return values of Codebase manifest attribute
         */
        public ClasspathMatcher.ClasspathMatchers getCodebase() {
            return getCodeBaseMatchersAttribute(CODEBASE, false);
        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#trusted_only
         * @return value of Trusted-Only manifest attribute
         */
        public ManifestBoolean isTrustedOnly() {
            return processBooleanAttribute(TRUSTED_ONLY);

        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#trusted_library
         * @return value of Trusted-Library manifest attribute
         */
        public ManifestBoolean isTrustedLibrary() {
            return processBooleanAttribute(TRUSTED_LIBRARY);

        }

        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#permissions
         * @return value of Permissions manifest attribute
         */
        public ManifestBoolean isSandboxForced() {
            String s = getAttribute(PERMISSIONS);
            if (s == null) {
                return ManifestBoolean.UNDEFINED;
            } else if (s.trim().equalsIgnoreCase(SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString())) {
                return ManifestBoolean.TRUE;
            } else if (s.trim().equalsIgnoreCase(SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString())) {
                return ManifestBoolean.FALSE;
            } else {
                throw new IllegalArgumentException("Unknown value of " + PERMISSIONS + " attribute " + s + ". Expected "+SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString()+" or "+SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString());
            }


        }
        /**
         * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#permissions
         * @return plain string values of Permissions manifest attribute
         */
        public String permissionsToString() {
            String s = getAttribute(PERMISSIONS);
            if (s == null) {
                return "Not defined";
            } else if (s.trim().equalsIgnoreCase(SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString())) {
                return s.trim();
            } else if (s.trim().equalsIgnoreCase(SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString())) {
                return s.trim();
            } else {
                return "illegal";
            }
        }

        /**
         * get custom attribute.
         */
        String getAttribute(String name) {
            return getAttribute(new Attributes.Name(name));
        }

        /**
         * get standard attribute
         * @param name name of the manifest attribute to find in application
         * @return  plain attribute value
         */
        public String getAttribute(Attributes.Name name) {
            if (loader == null) {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Jars not ready to provide attribute " + name);
                return null;
            }
            return loader.checkForAttributeInJars(Arrays.asList(getResources().getJARs()), name);
        }

        public ClasspathMatcher.ClasspathMatchers getCodeBaseMatchersAttribute(String s, boolean includePath) {
            return getCodeBaseMatchersAttribute(new Attributes.Name(s), includePath);
        }

        public ClasspathMatcher.ClasspathMatchers getCodeBaseMatchersAttribute(Attributes.Name name, boolean includePath) {
            String s = getAttribute(name);
            if (s == null) {
                return null;
            }
            return ClasspathMatcher.ClasspathMatchers.compile(s, includePath);
        }

        private ManifestBoolean processBooleanAttribute(String id) throws IllegalArgumentException {
            String s = getAttribute(id);
            if (s == null) {
                return ManifestBoolean.UNDEFINED;
            } else {
                s = s.toLowerCase().trim();
                switch (s) {
                    case "true":
                        return  ManifestBoolean.TRUE;
                    case "false":
                        return ManifestBoolean.FALSE;
                    default:
                        throw new IllegalArgumentException("Unknown value of " + id + " attribute " + s + ". Expected true or false");
                }
            }
        }
    }

    public String createJnlpVendorValue() {
        final String location;
        if (getSourceLocation() != null) {
            location = getSourceLocation().toString();
        } else if (getCodeBase() != null) {
            location = getCodeBase().toString();
        } else {
            location = "unknown";
        }
        return location;
    }

    public String createJnlpVendor() {
        return "Generated from applet from " + createJnlpVendorValue();
    }

    public String createJnlpTitleValue() {
        final String location;
        if (getSourceLocation() != null) {
            location = new File(getSourceLocation().getFile()).getName();
        } else if (getCodeBase() != null) {
            location = new File(getCodeBase().getFile()).getName();
        } else {
            location = "unknown";
        }
        return location;
    }

    public String createJnlpTitle() {
        //case when creating name from already created name
        String shortenedTitle = getTitle();
        int i = shortenedTitle.lastIndexOf("(");
        if (i >= 2) { // not cutting immidiately...
            shortenedTitle = shortenedTitle.substring(0, i - 1);
        }
        if (createJnlpTitleValue().startsWith(shortenedTitle)) {
            return createJnlpTitleValue();
        }
        return getTitle() + " from " + createJnlpTitleValue();
    }
    
    //not private for testing purposes
    static String[] splitEntryPoints(String entryPointString) {
        if (entryPointString == null || entryPointString.trim().isEmpty()) {
            return null;
        }
        String[] result = entryPointString.trim().split("\\s+");
        if (result.length == 0) {
            return null;
        }
        return result;
    }
}

