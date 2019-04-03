/*
 * Copyright 2012 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sourceforge.jnlp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sourceforge.jnlp.SecurityDesc.RequestedPermissionLevel;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.StreamUtils;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.replacements.BASE64Decoder;

/**
 * Allows reuse of code that expects a JNLPFile object,
 * while overriding behaviour specific to applets.
 */
public final class PluginBridge extends JNLPFile {

    private final PluginParameters params;
    final private Set<String> jars = new HashSet<>();
    private List<ExtensionDesc> extensionJars = new ArrayList<>();
    //Folders can be added to the code-base through the archive tag
    final private List<String> codeBaseFolders = new ArrayList<>();
    private String[] cacheJars = new String[0];
    private String[] cacheExJars = new String[0];
    private boolean usePack = false;
    private boolean useVersion = false;
    private boolean useJNLPHref;
    private String debugJnlp;

    /**
     * Creates a new PluginBridge using a default JNLPCreator.
     * @param codebase as specified in attribute
     * @param documentBase as specified in attribute
     * @param jar jar attribute value
     * @param main main method attribute value
     * @param width width of appelt as specified in attribute
     * @param height height of applet as specified in attribute
     * @param params parameters as parsed from source html
     * @throws java.lang.Exception general exception as anything can happen
     */
    public PluginBridge(URL codebase, URL documentBase, String jar, String main,
                        int width, int height, PluginParameters params)
            throws Exception {
        this(codebase, documentBase, jar, main, width, height, params, new JNLPCreator());
    }

    /**
     * Handles archive tag entries, which may be folders or jar files
     * @param archives the components of the archive tag
     */
    private void addArchiveEntries(String[] archives) {
        for (String archiveEntry : archives){
            // trim white spaces
            archiveEntry = archiveEntry.trim();

            /*Only '/' on linux, '/' or '\\' on windows*/
            if (archiveEntry.endsWith("/") || archiveEntry.endsWith(File.pathSeparator)) {
                this.codeBaseFolders.add(archiveEntry);
            } else {
                this.jars.add(archiveEntry);
            }
        }
    }

    public PluginBridge(URL codebase, URL documentBase, String archive, String main,
                        int width, int height, final PluginParameters params, JNLPCreator jnlpCreator)
            throws Exception {
        specVersion = new Version("1.0");
        fileVersion = new Version("1.1");
        this.codeBase = codebase;
        this.sourceLocation = documentBase;
        this.params = params;
        this.parserSettings = ParserSettings.getGlobalParserSettings();

        if (params.getJNLPHref() != null) {
            useJNLPHref = true;
            try {
                // Use codeBase as the context for the URL. If jnlp_href's
                // value is a complete URL, it will replace codeBase's context.
                final ParserSettings defaultSettings = new ParserSettings();
                final URL jnlp = new URL(codeBase, params.getJNLPHref());
                if (fileLocation == null){
                    fileLocation = jnlp;
                }
                JNLPFile jnlpFile;

                if (params.getJNLPEmbedded() != null) {
                    InputStream jnlpInputStream = new ByteArrayInputStream(decodeBase64String(params.getJNLPEmbedded()));
                    jnlpFile = new JNLPFile(jnlpInputStream, codeBase, defaultSettings);
                    debugJnlp = new StreamProvider() {

                        @Override
                        InputStream getStream() throws Exception {
                            return new ByteArrayInputStream(decodeBase64String(params.getJNLPEmbedded()));
                        }

                    }.readStream();
                } else {
                    // see http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=2746#c3
                    URL codebaseRewriter=UrlUtils.ensureSlashTail(UrlUtils.removeFileName(jnlp));
                    this.codeBase = codebaseRewriter;
                    jnlpFile = jnlpCreator.create(jnlp, null, defaultSettings, JNLPRuntime.getDefaultUpdatePolicy(), codebaseRewriter);
                    debugJnlp = new StreamProvider() {

                        @Override
                        InputStream getStream() throws Exception {
                            return JNLPFile.openURL(jnlp, null, UpdatePolicy.ALWAYS);
                        }
                    }.readStream();
                }
                OutputController.getLogger().log("Loaded JNLPhref:");
                OutputController.getLogger().log((debugJnlp == null) ? "null" : debugJnlp);

                if (jnlpFile.isApplet())
                    main = jnlpFile.getApplet().getMainClass();

                Map<String, String> jnlpParams = jnlpFile.getApplet().getParameters();
                info = jnlpFile.info;

                // Change the parameter name to lowercase to follow conventions.
                for (Map.Entry<String, String> entry : jnlpParams.entrySet()) {
                    this.params.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                JARDesc[] jarDescs = jnlpFile.getResources().getJARs();
                for (JARDesc jarDesc : jarDescs) {
                     String fileName = jarDesc.getLocation().toExternalForm();
                     this.jars.add(fileName);
                 }

                usePack = jnlpFile.getDownloadOptions().useExplicitPack();
                useVersion = jnlpFile.getDownloadOptions().useExplicitVersion();

                // Store any extensions listed in the JNLP file to be returned later on, namely in getResources()
                extensionJars = Arrays.asList(jnlpFile.getResources().getExtensions());
            } catch (MalformedURLException e) {
                // Don't fail because we cannot get the jnlp file. Parameters are optional not required.
                // it is the site developer who should ensure that file exist.
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Unable to get JNLP file at: " + params.getJNLPHref()
                        + " with context of URL as: " + codeBase.toExternalForm());
            }
        } else {
            // Should we populate this list with applet attribute tags?
            info = new ArrayList<>();
            useJNLPHref = false;
        }

        // also, see if cache_archive is specified
        String cacheArchive = params.getCacheArchive();
        if (!cacheArchive.isEmpty()) {

            String[] versions = new String[0];

            // are there accompanying versions?
            String cacheVersion = params.getCacheVersion();
            if (!cacheVersion.isEmpty()) {
                versions = cacheVersion.split(",");
            }

            String[] ljars = cacheArchive.split(",");
            cacheJars = new String[ljars.length];

            for (int i = 0; i < ljars.length; i++) {

                cacheJars[i] = ljars[i].trim();

                if (versions.length > 0) {
                    cacheJars[i] += ";" + versions[i].trim();
                }
            }
        }

        String cacheArchiveEx = params.getCacheArchiveEx();
        if (!cacheArchiveEx.isEmpty()) {
            cacheExJars = cacheArchiveEx.split(",");
        }

        if (archive != null && archive.length() > 0) {
            String[] archives = archive.split(",");

            addArchiveEntries(archives);

            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Jar string: " + archive);
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "jars length: " + archives.length);
        }

        if (main.endsWith(".class"))
            main = main.substring(0, main.length() - 6);

        // the class name should be of the form foo.bar.Baz not foo/bar/Baz
        String mainClass = main.replace('/', '.');
        launchType = new AppletDesc(getTitle(), mainClass, documentBase, width,
                                    height, params.getUnmodifiableMap());

        if (main.endsWith(".class")) //single class file only
            security = new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS,
                                        codebase);
        else
            security = null;

        this.uniqueKey = params.getUniqueKey(codebase);
        String jargs = params.getJavaArguments();
        if (!jargs.isEmpty()) {
            for (String s : jargs.split(" ")) {
                String[] parts = s.trim().split("=");
                if (parts.length == 2 && Boolean.valueOf(parts[1])) {
                    if (null != parts[0]) switch (parts[0]) {
                        case "-Djnlp.packEnabled":
                            usePack = true;
                            break;
                        case "-Djnlp.versionEnabled":
                            useVersion = true;
                            break;
                    }
                }
            }
        }
    }

    public List<String> getArchiveJars() {
        return new ArrayList<>(jars);
    }

    public boolean codeBaseLookup() {
    	return params.useCodebaseLookup();
    }

    public boolean useJNLPHref() {
        return useJNLPHref;
    }

    public PluginParameters getParams() {
        return params;
    }
    
    

    @Override
    public RequestedPermissionLevel getRequestedPermissionLevel() {
        final String level = params.getPermissions();
        if (level == null) {
            return RequestedPermissionLevel.NONE;
        } else if (level.equals(SecurityDesc.RequestedPermissionLevel.DEFAULT.toHtmlString())) {
            return RequestedPermissionLevel.NONE;
        } else if (level.equals(SecurityDesc.RequestedPermissionLevel.SANDBOX.toHtmlString())) {
            return RequestedPermissionLevel.SANDBOX;
        } else if (level.equals(SecurityDesc.RequestedPermissionLevel.ALL.toHtmlString())) {
            return RequestedPermissionLevel.ALL;
        } else {
            return RequestedPermissionLevel.NONE;
        }
    }

    /**
     * {@inheritDoc }
     * @return  options of download
     */
    @Override
    public DownloadOptions getDownloadOptions() {
        return new DownloadOptions(usePack, useVersion);
    }

    @Override
    public String getTitle() {
        String inManifestTitle = super.getTitleFromManifest();
        if (inManifestTitle != null) {
            return inManifestTitle;
        }
        //specification is recommending  main class instead of html parameter
        //http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#app_name
        String mainClass = getManifestsAttributes().getMainClass();
        if (mainClass != null) {
            return mainClass;
        }
        return params.getAppletTitle();
    }

    @Override
    public ResourcesDesc getResources(final Locale locale, final String os,
                                      final String arch) {
        return new ResourcesDesc(this, new Locale[] { locale }, new String[] { os },
                new String[] { arch }) {
            @Override
            public <T> List<T> getResources(Class<T> launchType) {
                // Need to add the JAR manually...
                //should this be done to sharedResources on init?
                if (launchType.equals(JARDesc.class)) {
                    try {
                        List<JARDesc> jarDescs = new ArrayList<>();
                        jarDescs.addAll(sharedResources.getResources(JARDesc.class));

                        for (String name : jars) {
                            if (name.length() > 0)
                                jarDescs.add(new JARDesc(new URL(codeBase, name),
                                        null, null, false, true, false, true));
                        }

                        boolean cacheable = true;
                        if (params.getCacheOption().equalsIgnoreCase("no"))
                            cacheable = false;

                        for (String cacheJar : cacheJars) {

                            String[] jarAndVer = cacheJar.split(";");

                            String jar = jarAndVer[0];
                            Version version = null;

                            if (jar.length() == 0)
                                continue;

                            if (jarAndVer.length > 1) {
                                version = new Version(jarAndVer[1]);
                            }

                            jarDescs.add(new JARDesc(new URL(codeBase, jar),
                                    version, null, false, true, false, cacheable));
                        }

                        for (String cacheExJar : cacheExJars) {

                            if (cacheExJar.length() == 0)
                                continue;

                            String[] jarInfo = cacheExJar.split(";");

                            String jar = jarInfo[0].trim();
                            Version version = null;
                            boolean lazy = true;

                            if (jarInfo.length > 1) {

                                // format is name[[;preload];version]

                                if (jarInfo[1].equals("preload")) {
                                    lazy = false;
                                } else {
                                    version = new Version(jarInfo[1].trim());
                                }

                                if (jarInfo.length > 2) {
                                    lazy = false;
                                    version = new Version(jarInfo[2].trim());
                                }
                            }

                            jarDescs.add(new JARDesc(new URL(codeBase, jar),
                                    version, null, lazy, true, false, false));
                        }
                        // We know this is a safe list of JarDesc objects
                        @SuppressWarnings("unchecked")
                        List<T> result = (List<T>) jarDescs;
                        return result;
                    } catch (MalformedURLException ex) { /* Ignored */
                    }
                } else if (launchType.equals(ExtensionDesc.class)) {
                    // We hope this is a safe list of JarDesc objects
                    @SuppressWarnings("unchecked")
                    List<T> castList = (List<T>) extensionJars; // this list is populated when the PluginBridge is first constructed
                    return castList;
                }
                return sharedResources.getResources(launchType);
            }

            @Override
            public JARDesc[] getJARs() {
                List<JARDesc> jarDescs = getResources(JARDesc.class);
                return jarDescs.toArray(new JARDesc[jarDescs.size()]);
            }

            @Override
            public void addResource(Object resource) {
                // todo: honor the current locale, os, arch values
                sharedResources.addResource(resource);
            }

        };
    }

    /**
     * @return the list of folders to be added to the codebase
     */
    public List<String> getCodeBaseFolders() {
        return new ArrayList<>(codeBaseFolders);
    }

    /**
     * @return the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    @Override
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc[] { getResources(locale, os, arch) };
    }

    @Override
    public boolean isApplet() {
        return true;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isComponent() {
        return false;
    }

    @Override
    public boolean isInstaller() {
        return false;
    }

    /**
     * Returns the decoded BASE64 string
     */
    static byte[] decodeBase64String(String encodedString) throws IOException {
        BASE64Decoder base64 = new BASE64Decoder();
        return base64.decodeBuffer(encodedString);
    }

    public String getDebugJnlp() {
        return debugJnlp;
    }

    public boolean haveDebugJnlp() {
        return debugJnlp != null;
    }
    
    
    public String toJnlp(boolean needSecurity, boolean useHref, boolean fix) {
        if (useJNLPHref && debugJnlp != null && useHref) {
            OutputController.getLogger().log("Using debugjnlp as return value toJnlp");
            if (fix) {
                return fixCommonIsuses(needSecurity, debugJnlp);
            } else {
                return debugJnlp;
            }
        } else {
            StringBuilder s = new StringBuilder();
            s.append("<?xml version='1.0' encoding='UTF-8'?>\n"
                   + "<jnlp codebase='").append(getNotNullProbalbeCodeBase().toString()).append("'>\n")
                    .append("  <information>\n"
                          + "    <title>").append(createJnlpTitle()).append("</title>\n"
                          + "    <vendor>").append(createJnlpVendor()).append("</vendor>\n"
                          + "  </information>\n");
            if (needSecurity) {
                s.append(getSecurityElement());
            }
            s.append("  <resources>\n");
            for (String i : getArchiveJars()) {
                s.append("    <jar href='").append(i).append("' />\n");
            }
            s.append("  </resources>\n"
                   + "  <applet-desc\n")
                    .append("    name='").append(getTitle()).append("'\n"
                          + "    main-class='").append(getStrippedMain()).append("'\n"
                          + "    width='").append(getApplet().getWidth()).append("'\n"
                          + "    height='").append(getApplet().getHeight()).append("'>\n");
            if (!getApplet().getParameters().isEmpty()) {
                Set<Map.Entry<String, String>> prms = getApplet().getParameters().entrySet();
                for (Map.Entry<String, String> entry : prms) {
                    s.append("    <param name='").append(entry.getKey()).append("' value='").append(entry.getValue()).append("'/>\n");
                }
            }
            s.append("  </applet-desc>\n"
                        + "</jnlp>\n");
            OutputController.getLogger().log("toJnlp generated:");
            OutputController.getLogger().log(s.toString());
            return s.toString();
        }

    }

    private String getStrippedMain() {
        return strippClass(getApplet().getMainClass().trim());
    }

    public static String strippClass(String s) {
        if (s.endsWith(".class")) {
            return s.substring(0, s.length() - ".class".length());
        } else {
            return s;
        }
    }

    //Those constants are public, because they are tested in PluginBridgeTest
    static final String SANDBOX_REGEX = toBaseRegex("sandbox", false);
    static final String CLOSE_INFORMATION_REGEX = toBaseRegex("information", true);
    static final String SECURITY_REGEX = toBaseRegex("security", false);
    static final String RESOURCE_REGEX = toBaseRegex("resources", false);
    static final String TITLE_REGEX = toBaseRegex("title", false);
    static final String VENDOR_REGEX = toBaseRegex("vendor", false);
    static final String AP_REGEX = toBaseRegex("all-permissions", false);
    static final String CODEBASE_REGEX1 = "(?i).*\\s+codebase\\s*=\\s*";
    static final String CODEBASE_REGEX2 = "(?i)\\s+codebase\\s*=\\s*.\\.{0,1}.((\\s+)|(\\s*>))";// "." '.' '' ""

    static String toMatcher(String regex) {
        return "(?s).*" + regex + ".*";
    }

    static String toBaseRegex(String tagName, boolean closing) {
        return "(?i)<\\s*" + ((closing) ? "/\\s*" : "") + tagName + "\\s*>";
    }

     private String fixCommonIsuses(boolean needSecurity, String orig) {
        String codebase = getNotNullProbalbeCodeBase().toString();
        return fixCommonIsuses(needSecurity, orig, codebase, createJnlpTitle(), createJnlpVendor());
    }
     
    //testing allowing method
    static String fixCommonIsuses(boolean needSecurity, String orig, String codebase, String title, String vendor) {
        //no information element at all
        if (!orig.matches(toMatcher(CLOSE_INFORMATION_REGEX))) {
            OutputController.getLogger().log("no information element Found. Trying to fix");
            if (orig.matches(toMatcher(SECURITY_REGEX))) {
                orig = orig.replaceAll(SECURITY_REGEX, "\n<information>\n</information>\n<security>\n");
            } else {
                if (orig.matches(toMatcher(RESOURCE_REGEX))) {
                    orig = orig.replaceAll(RESOURCE_REGEX, "\n<information>\n</information>\n<resources>\n");
                }
            }
        }
        //some have missing codebase, thats fatal
        if (!orig.matches(toMatcher(CODEBASE_REGEX1))) {
            OutputController.getLogger().log("jnlphref did not had codebase. Fixing");
            orig = orig.replaceAll("(?i)<\\s*jnlp\\s+", "<jnlp codebase='" + codebase + "' ");
        } else {
            //codebase="."
            if (orig.matches(toMatcher(CODEBASE_REGEX2))) {
                OutputController.getLogger().log("'.' codebase found. fixing");
                orig = orig.replaceAll(CODEBASE_REGEX2, " codebase='" + codebase + "'");
            }
        }
        //surprisingly also title or vendor may be misisng
        if (!orig.matches(toMatcher(TITLE_REGEX))) {
            OutputController.getLogger().log("Missing title. Fixing");
            orig = orig.replaceAll(CLOSE_INFORMATION_REGEX, "\n<title>" + title + "</title>\n</information>\n");
        }
        if (!orig.matches(toMatcher(VENDOR_REGEX))) {
            OutputController.getLogger().log("Missing vendor. Fixing");
            orig = orig.replaceAll(CLOSE_INFORMATION_REGEX, "\n<vendor>" + vendor + "</vendor>\n</information>\n");
        }
        //also all-security is not enforced via jnlpHref
        if (needSecurity && !orig.matches(toMatcher(AP_REGEX))) {
            OutputController.getLogger().log("all-permissions not found and app is signed.");
            if (orig.matches(SANDBOX_REGEX)) {
                OutputController.getLogger().log("Replacing sandbox by all-permissions");
                orig = orig.replaceAll(SANDBOX_REGEX, getAllPermissionsElement());
            } else {
                OutputController.getLogger().log("adding security element");
                orig = orig.replaceAll(CLOSE_INFORMATION_REGEX, "</information>\n" + getSecurityElement());
            }
        }
        return orig;
    }

    private static String getSecurityElement() {
        return "  <security>\n" + getAllPermissionsElement() + "  </security>\n";
    }

    private static String getAllPermissionsElement() {
        return "    <all-permissions/>\n";
    }

    
    private abstract class StreamProvider {

        abstract InputStream getStream() throws Exception;

        String readStream() {
            try {
                return StreamUtils.readStreamAsString(getStream());
            } catch (Exception ex) {
                OutputController.getLogger().log(ex);
            }
            return null;
        }

    }
}
