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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * Provides methods to access the information in a Java Network
 * Launching Protocol (JNLP) file.  The Java Network Launching
 * Protocol specifies in an XML file the information needed to
 * load, cache, and run Java code over the network and in a secure
 * environment.<p>
 *
 * This class represents the overall information about a JNLP file
 * from the jnlp element.  Other information is accessed through
 * objects that represent the elements of a JNLP file
 * (information, resources, application-desc, etc).  References to
 * these objects are obtained by calling the getInformation,
 * getResources, getSecurity, etc methods.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.21 $
 */
public class JNLPFile {

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
    private String[] generalProperties = SecurityDesc.getJnlpRIAPermissions();

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
        this(location, false); // not strict
    }

    /**
     * Create a JNLPFile from a URL checking for updates using the
     * default policy.
     *
     * @param location the location of the JNLP file
     * @param strict whether to enforce the spec when
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, boolean strict) throws IOException, ParseException {
        this(location, (Version) null, strict);
    }

    /**
     * Create a JNLPFile from a URL and a Version checking for updates using
     * the default policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param strict whether to enforce the spec when
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, Version version, boolean strict) throws IOException, ParseException {
        this(location, version, strict, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param strict whether to enforce the spec when
     * @param policy the update policy
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, Version version, boolean strict, UpdatePolicy policy) throws IOException, ParseException {
        this(location, version, strict, policy, null);
    }

    /**
     * Create a JNLPFile from a URL and a version, checking for updates
     * using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param version the version of the JNLP file
     * @param strict whether to enforce the spec when
     * @param policy the update policy
     * @param forceCodebase codebase to use if not specified in JNLP file.
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    protected JNLPFile(URL location, Version version, boolean strict, UpdatePolicy policy, URL forceCodebase) throws IOException, ParseException {
        Node root = Parser.getRootNode(openURL(location, version, policy));
        parse(root, strict, location, forceCodebase);

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

        if (JNLPRuntime.isDebug())
            System.err.println("UNIQUEKEY=" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from a URL, parent URLm a version and checking for
     * updates using the specified policy.
     *
     * @param location the location of the JNLP file
     * @param uniqueKey A string that uniquely identifies connected instances
     * @param version the version of the JNLP file
     * @param strict whether to enforce the spec when
     * @param policy the update policy
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(URL location, String uniqueKey, Version version, boolean strict, UpdatePolicy policy) throws IOException, ParseException {
        this(location, version, strict, policy);
        this.uniqueKey = uniqueKey;

        if (JNLPRuntime.isDebug())
            System.err.println("UNIQUEKEY (override) =" + this.uniqueKey);
    }

    /**
     * Create a JNLPFile from an input stream.
     *
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    public JNLPFile(InputStream input, boolean strict) throws ParseException {
        parse(Parser.getRootNode(input), strict, null, null);
    }

    /**
     * Create a JNLPFile from a character stream.
     *
     * @param input the stream
     * @param strict whether to enforce the spec when
     * @throws IOException if an IO exception occurred
     * @throws ParseException if the JNLP file was invalid
     */
    private JNLPFile(Reader input, boolean strict) throws ParseException {
        // todo: now that we are using NanoXML we can use a Reader
        //parse(Parser.getRootNode(input), strict, null);
    }

    /**
     * Open the jnlp file URL from the cache if there, otherwise
     * download to the cache.  Called from constructor.
     */
    private static InputStream openURL(URL location, Version version, UpdatePolicy policy) throws IOException {
        if (location == null || policy == null)
            throw new IllegalArgumentException(R("NullParameter"));

        try {
            ResourceTracker tracker = new ResourceTracker(false); // no prefetch
            tracker.addResource(location, version, null, policy);

            return tracker.getInputStream(location);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Returns the JNLP file's best localized title. This method returns the same
     * value as InformationDesc.getTitle().
     */
    public String getTitle() {
        return getInformation().getTitle();
    }

    /**
     * Returns the JNLP file's best localized vendor. This method returns the same
     * value as InformationDesc.getVendor().
     */
    public String getVendor() {
        return getInformation().getVendor();
    }

    /**
     * Returns the JNLP file's network location as specified in the
     * JNLP file.
     */
    public URL getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Returns the location of the file parsed to create the JNLP
     * file, or null if it was not created from a URL.
     */
    public URL getFileLocation() {
        return fileLocation;
    }

    /**
     * Returns the location of the parent file if it exists, null otherwise
     */
    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * Returns the JNLP file's version.
     */
    public Version getFileVersion() {
        return fileVersion;
    }

    /**
     * Returns the specification version required by the file.
     */
    public Version getSpecVersion() {
        return specVersion;
    }

    /**
     * Returns the codebase URL for the JNLP file.
     */
    public URL getCodeBase() {
        return codeBase;
    }

    /**
     * Returns the information section of the JNLP file as viewed
     * through the default locale.
     */
    public InformationDesc getInformation() {
        return getInformation(defaultLocale);
    }

    /**
     * Returns the information section of the JNLP file as viewed
     * through the specified locale.
     */
    public InformationDesc getInformation(final Locale locale) {
        return new InformationDesc(this, new Locale[] { locale }) {
            @Override
            protected List<Object> getItems(Object key) {
                List<Object> result = new ArrayList<Object>();

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
     * Returns the update section of the JNLP file.
     */
    public UpdateDesc getUpdate() {
        return update;
    }

    /**
     * Returns the security section of the JNLP file.
     */
    public SecurityDesc getSecurity() {
        return security;
    }

    /**
     * Returns the resources section of the JNLP file as viewed
     * through the default locale and the os.name and os.arch
     * properties.
     */
    public ResourcesDesc getResources() {
        return getResources(defaultLocale, defaultOS, defaultArch);
    }

    /**
     * Returns the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc getResources(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc(this, new Locale[] { locale }, new String[] { os }, new String[] { arch }) {

            @Override
            public <T> List<T> getResources(Class<T> launchType) {
                List<T> result = new ArrayList<T>();

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
     * Returns the resources section of the JNLP file as viewed
     * through the default locale and the os.name and os.arch
     * properties.
     * XXX: Before overriding this method or changing its implementation,
     * read the comment in JNLPFile.getDownloadOptionsForJar(JARDesc).
     */
    public ResourcesDesc[] getResourcesDescs() {
        return getResourcesDescs(defaultLocale, defaultOS, defaultArch);
    }

    /**
     * Returns the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        List<ResourcesDesc> matchingResources = new ArrayList<ResourcesDesc>();
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
     * Returns an object of one of the following types: AppletDesc,
     * ApplicationDesc and InstallerDesc
     */
    public LaunchDesc getLaunchInfo() {
        return launchType;
    }

    /**
     * Returns the launch information for an applet.
     *
     * @throws UnsupportedOperationException if there is no applet information
     */
    public AppletDesc getApplet() {
        if (!isApplet())
            throw new UnsupportedOperationException(R("JNotApplet"));

        return (AppletDesc) launchType;
    }

    /**
     * Returns the launch information for an application.
     *
     * @throws UnsupportedOperationException if there is no application information
     */
    public ApplicationDesc getApplication() {
        if (!isApplication())
            throw new UnsupportedOperationException(R("JNotApplication"));

        return (ApplicationDesc) launchType;
    }

    /**
     * Returns the launch information for a component.
     *
     * @throws UnsupportedOperationException if there is no component information
     */
    public ComponentDesc getComponent() {
        if (!isComponent())
            throw new UnsupportedOperationException(R("JNotComponent"));

        return component;
    }

    /**
     * Returns the launch information for an installer.
     *
     * @throws UnsupportedOperationException if there is no installer information
     */
    public InstallerDesc getInstaller() {
        if (!isInstaller())
            throw new UnsupportedOperationException(R("NotInstaller"));

        return (InstallerDesc) launchType;
    }

    /**
     * Returns whether the lauch descriptor describes an Applet.
     */
    public boolean isApplet() {
        return launchType instanceof AppletDesc;
    }

    /**
     * Returns whether the lauch descriptor describes an Application.
     */
    public boolean isApplication() {
        return launchType instanceof ApplicationDesc;
    }

    /**
     * Returns whether the lauch descriptor describes a Component.
     */
    public boolean isComponent() {
        return component != null;
    }

    /**
     * Returns whether the lauch descriptor describes an Installer.
     */
    public boolean isInstaller() {
        return launchType instanceof InstallerDesc;
    }

    /**
     * Sets the default view of the JNLP file returned by
     * getInformation, getResources, etc.  If unset, the defaults
     * are the properties os.name, os.arch, and the locale returned
     * by Locale.getDefault().
     */
    public void setDefaults(String os, String arch, Locale locale) {
        defaultOS = os;
        defaultArch = arch;
        defaultLocale = locale;
    }

    /**
     * Returns whether a locale is matched by one of more other
     * locales.  Only the non-empty language, country, and variant
     * codes are compared; for example, a requested locale of
     * Locale("","","") would always return true.
     *
     * @param requested the local
     * @param available the available locales
     * @param precision the depth with which to match locales. 1 checks only
     * language, 2 checks language and country, 3 checks language, country and
     * variant for matches. Passing 0 will always return true.
     * @return true if requested matches any of available, or if
     * available is empty or null.
     */
    public boolean localeMatches(Locale requested, Locale available[], Match matchLevel) {

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
     * Returns whether the string is a prefix for any of the strings
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

        for (int i = 0; i < available.length; i++)
            if (available[i] != null && available[i].startsWith(prefixStr))
                return true;

        return false;
    }

    /**
     * Initialize the JNLPFile fields. Private because it's called
     * from the constructor.
     *
     * @param root the root node
     * @param strict whether to enforce the spec when
     * @param location the file location or null
     */
    private void parse(Node root, boolean strict, URL location, URL forceCodebase) throws ParseException {
        try {
            //if (location != null)
            //  location = new URL(location, "."); // remove filename

            Parser parser = new Parser(this, location, root, strict, true, forceCodebase); // true == allow extensions

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
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();

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

        if (getNewVMArgs().size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *  @return a list of args to pass to the new
     *  JVM based on this JNLP file
     */
    public List<String> getNewVMArgs() {

        List<String> newVMArgs = new LinkedList<String>();

        JREDesc[] jres = getResources().getJREs();
        for (int jreIndex = 0; jreIndex < jres.length; jreIndex++) {
            String initialHeapSize = jres[jreIndex].getInitialHeapSize();
            if (initialHeapSize != null) {
                newVMArgs.add("-Xms" + initialHeapSize);
            }

            String maxHeapSize = jres[jreIndex].getMaximumHeapSize();
            if (maxHeapSize != null) {
                newVMArgs.add("-Xmx" + maxHeapSize);
            }

            String vmArgsFromJre = jres[jreIndex].getVMArgs();
            if (vmArgsFromJre != null) {
                String[] args = vmArgsFromJre.split(" ");
                newVMArgs.addAll(Arrays.asList(args));
            }
        }

        return newVMArgs;
    }

    /**
     * XXX: this method does a "==" comparison between the input JARDesc and
     * jars it finds through getResourcesDescs(). If ever the implementation
     * of that function should change to return copies of JARDescs objects,
     * then the "jar == aJar" comparison below should change accordingly.
     * @param jar: the jar whose download options to get.
     * @return the download options.
     */
    public DownloadOptions getDownloadOptionsForJar(JARDesc jar) {
        boolean usePack = false;
        boolean useVersion = false;
        ResourcesDesc[] descs = getResourcesDescs();
        for (ResourcesDesc desc: descs) {
            JARDesc[] jars = desc.getJARs();
            for (JARDesc aJar: jars) {
                if (jar == aJar) {
                    if (Boolean.valueOf(desc.getPropertiesMap().get("jnlp.packEnabled"))) {
                        usePack = true;
                    }
                    if (Boolean.valueOf(desc.getPropertiesMap().get("jnlp.versionEnabled"))) {
                        useVersion = true;
                    }
                }
            }
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
}
