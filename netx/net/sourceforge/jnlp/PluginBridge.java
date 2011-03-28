/*
 * Copyright 2007 Red Hat, Inc.
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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

public class PluginBridge extends JNLPFile {

    String name;
    String[] jars = new String[0];
    String[] cacheJars = new String[0];
    String[] cacheExJars = new String[0];
    Hashtable<String, String> atts;
    private boolean usePack;
    private boolean useVersion;
    private boolean codeBaseLookup;

    public PluginBridge(URL codebase, URL documentBase, String jar, String main,
                        int width, int height, Hashtable<String, String> atts)
            throws Exception {
        specVersion = new Version("1.0");
        fileVersion = new Version("1.1");
        this.codeBase = codebase;
        this.sourceLocation = documentBase;

        if (atts.containsKey("jnlp_href")) {
            try {
                URL jnlp = new URL(codeBase.toExternalForm() + atts.get("jnlp_href"));
                JNLPFile jnlpFile = new JNLPFile(jnlp);
                Map<String, String> jnlpParams = jnlpFile.getApplet().getParameters();

                // Change the parameter name to lowercase to follow conventions.
                for (Map.Entry<String, String> entry : jnlpParams.entrySet()) {
                    atts.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            } catch (MalformedURLException e) {
                // Don't fail because we cannot get the jnlp file. Parameters are optional not required.
                // it is the site developer who should ensure that file exist.
                System.err.println("Unable to get JNLP file at: " + codeBase.toExternalForm()
                        + atts.get("jnlp_href"));
            }
        }

        // also, see if cache_archive is specified
        String cacheArchive = atts.get("cache_archive");
        if (cacheArchive != null && cacheArchive.length() > 0) {

            String[] versions = new String[0];

            // are there accompanying versions?
            String cacheVersion = atts.get("cache_version");
            if (cacheVersion != null) {
                versions = cacheVersion.split(",");
            }

            String[] jars = cacheArchive.split(",");
            cacheJars = new String[jars.length];

            for (int i = 0; i < jars.length; i++) {

                cacheJars[i] = jars[i].trim();

                if (versions.length > 0) {
                    cacheJars[i] += ";" + versions[i].trim();
                }
            }
        }

        String cacheArchiveEx = atts.get("cache_archive_ex");
        if (cacheArchiveEx != null && cacheArchiveEx.length() > 0) {
            cacheExJars = cacheArchiveEx.split(",");
        }

        if (jar != null && jar.length() > 0) {
            this.jars = jar.split(",");

            // trim white spaces
            for (int i = 0; i < this.jars.length; i++) {
                this.jars[i] = this.jars[i].trim();
            }

            if (JNLPRuntime.isDebug()) {
                System.err.println("Jar string: " + jar);
                System.err.println("jars length: " + jars.length);
            }
        }
        this.atts = atts;

        name = atts.get("name");
        if (name == null)
            name = "Applet";
        else
            name = name + " applet";

        if (main.endsWith(".class"))
            main = main.substring(0, main.length() - 6);

        launchType = new AppletDesc(name, main, documentBase, width,
                                    height, atts);

        if (main.endsWith(".class")) //single class file only
            security = new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS,
                                        codebase.getHost());
        else
            security = null;

        /* According to http://download.oracle.com/javase/6/docs/technotes/guides/deployment/deployment-guide/applet-compatibility.html, 
         * classloaders are shared iff these properties match:
         * codebase, cache_archive, java_archive, archive
         * 
         * To achieve this, we create the uniquekey based on those 4 values,
         * always in the same order. The initial "<NAME>=" parts ensure a 
         * bad tag cannot trick the loader into getting shared with another.
         */

        // Firefox sometimes skips the codebase if it is default  -- ".", 
        // so set it that way if absent
        String codebaseAttr =      atts.get("codebase") != null ?
                                   atts.get("codebase") : ".";

        String cache_archiveAttr = atts.get("cache_archive") != null ? 
                                   atts.get("cache_archive") : "";

        String java_archiveAttr =  atts.get("java_archive") != null ? 
                                   atts.get("java_archive") : "";

        String archiveAttr =       atts.get("archive") != null ? 
                                   atts.get("archive") : "";

        this.uniqueKey = "codebase=" + codebaseAttr +
                         "cache_archive=" + cache_archiveAttr + 
                         "java_archive=" + java_archiveAttr + 
                         "archive=" +  archiveAttr;

        usePack = false;
        useVersion = false;
        String jargs = atts.get("java_arguments");
        if (jargs != null) {
            for (String s : jargs.split(" ")) {
                String[] parts = s.trim().split("=");
                if (parts.length == 2 && Boolean.valueOf(parts[1])) {
                    if ("-Djnlp.packEnabled".equals(parts[0])) {
                        usePack = true;
                    } else if ("-Djnlp.versionEnabled".equals(parts[0])) {
                        useVersion = true;
                    }
                }
            }
        }
        String cbl = atts.get("codebase_lookup");
        codeBaseLookup = cbl == null || (Boolean.valueOf(cbl));
    }

    public boolean codeBaseLookup() {
    	return codeBaseLookup;
    }

    /**
     * {@inheritdoc }
     */
    @Override
    public DownloadOptions getDownloadOptionsForJar(JARDesc jar) {
        return new DownloadOptions(usePack, useVersion);
    }

    public String getTitle() {
        return name;
    }

    public InformationDesc getInformation(final Locale locale) {
        return new InformationDesc(this, new Locale[] { locale }) {
            protected List<Object> getItems(Object key) {
                // Should we populate this list with applet attribute tags?
                return new ArrayList<Object>();
            }
        };
    }

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
                        List<JARDesc> jarDescs = new ArrayList<JARDesc>();
                        jarDescs.addAll(sharedResources.getResources(JARDesc.class));

                        for (int i = 0; i < jars.length; i++)
                            if (jars[i].length() > 0)
                                jarDescs.add(new JARDesc(new URL(codeBase, jars[i]),
                                        null, null, false, true, false, true));

                        boolean cacheable = true;

                        String cacheOption = atts.get("cache_option");
                        if (cacheOption != null && cacheOption.equalsIgnoreCase("no"))
                            cacheable = false;

                        for (int i = 0; i < cacheJars.length; i++) {

                            String[] jarAndVer = cacheJars[i].split(";");

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

                        for (int i = 0; i < cacheExJars.length; i++) {

                            if (cacheExJars[i].length() == 0)
                                continue;

                            String[] jarInfo = cacheExJars[i].split(";");

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
                }
                return sharedResources.getResources(launchType);
            }

            @Override
            public JARDesc[] getJARs() {
                List<JARDesc> jarDescs = getResources(JARDesc.class);
                return jarDescs.toArray(new JARDesc[jarDescs.size()]);
            }

            public void addResource(Object resource) {
                // todo: honor the current locale, os, arch values
                sharedResources.addResource(resource);
            }

        };
    }

    /**
     * Returns the resources section of the JNLP file for the
     * specified locale, os, and arch.
     */
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc[] { getResources(locale, os, arch) };
    }

    public boolean isApplet() {
        return true;
    }

    public boolean isApplication() {
        return false;
    }

    public boolean isComponent() {
        return false;
    }

    public boolean isInstaller() {
        return false;
    }
}
