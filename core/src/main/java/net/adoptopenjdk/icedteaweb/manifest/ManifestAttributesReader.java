// Copyright (C) 2019 Karakun AG
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

package net.adoptopenjdk.icedteaweb.manifest;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.util.ClasspathMatcher;
import net.sourceforge.jnlp.util.JarFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * This class allows to read the security and configuration attributes specified in the JAR file manifest.
 *
 * @implSpec See https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/manifest.html for more details
 * on the security and configuration attributes stored in the JAR file manifest.
 */
public class ManifestAttributesReader {
    private final static Logger LOG = LoggerFactory.getLogger(ManifestAttributesReader.class);

    private final JNLPFile jnlpFile;
    private JNLPClassLoader loader;

    public ManifestAttributesReader(final JNLPFile jnlpFile) {
        this.jnlpFile = jnlpFile;
    }

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
            LOG.debug("Jars not ready to provide main class");
            return null;
        }
        return loader.getMainClass();
    }

    /**
     * The raw string representation (fully qualified class names separated by a space) of the
     * Entry-Point manifest attribute value that can be used as entry point for the RIA.
     *
     * @see #getEntryPoints() for a tokenized representation of the entry points
     *
     * @return the Entry-Point manifest attribute value
     */
    public String getEntryPoint() {
        return getAttribute(ManifestAttributes.ENTRY_POINT.toString());
    }

    /**
     * The fully qualified class names that can be used as entry point for the RIA.
     *
     * @return the Entry-Point class names
     */
    public String[] getEntryPoints() {
        return splitEntryPoints(getEntryPoint());
    }

    static String[] splitEntryPoints(final String entryPointString) {
        if (entryPointString == null || entryPointString.trim().isEmpty()) {
            return null;
        }
        final String[] result = entryPointString.trim().split("\\s+");
        if (result.length == 0) {
            return null;
        }
        return result;
    }


    /**
     * The Application-Name attribute is used in security prompts to provide a title for your signed RIA.
     * Use of this attribute is recommended to help users make the decision to trust and run the RIA.
     *
     * @return value of Application-Name manifest attribute
     */
    public String getApplicationName() {
        return getAttribute(ManifestAttributes.APPLICATION_NAME.toString());
    }

    /**
     * The Caller-Allowable-Codebase attribute is used to identify the domains from which JavaScript
     * code can make calls to your RIA without security prompts. Set this attribute to the domain
     * that hosts the JavaScript code.
     *
     * @return values of Caller-Allowable-Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getCallerAllowableCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString(), false);
    }

    /**
     * The Application-Library-Allowable-Codebase attribute identifies the locations where your signed
     * RIA is expected to be found.
     *
     * @return values of Application-Library-Allowable-Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getApplicationLibraryAllowableCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString(), true);
    }

    /**
     * The Codebase attribute is used to restrict the code base of the JAR file to specific domains.
     * Use this attribute to prevent someone from re-deploying your application on another website
     * for malicious purposes.
     *
     * @return values of Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.CODEBASE.toString(), false);
    }

    private ClasspathMatcher.ClasspathMatchers getCodeBaseMatchersAttribute(final String name, final boolean includePath) {
        final String value = getAttribute(name);
        if (value == null) {
            return null;
        }
        return ClasspathMatcher.ClasspathMatchers.compile(value, includePath);
    }

    /**
     * The Trusted-Only attribute is used to prevent untrusted classes or resources from being loaded for
     * an applet or application.
     *
     * @return value of Trusted-Only manifest attribute
     */
    public ManifestBoolean isTrustedOnly() {
        return getBooleanAttribute(ManifestAttributes.TRUSTED_ONLY.toString());
    }

    /**
     * The Trusted-Library attribute is used for applications and applets that are designed to allow
     * untrusted components.
     *
     * @return value of Trusted-Library manifest attribute
     */
    public ManifestBoolean isTrustedLibrary() {
        return getBooleanAttribute(ManifestAttributes.TRUSTED_LIBRARY.toString());
    }

    /**
     * The Permissions attribute is used to verify that the permissions level requested by the RIA when
     * it runs matches the permissions level that was set when the JAR file was created.
     *
     * @return value of Permissions manifest attribute
     */
    public String getPermissions() {
        return getAttribute(ManifestAttributes.PERMISSIONS.toString());
    }

    /**
     * Get the manifest attribute value.
     */
    private String getAttribute(final String name) {
        return getAttribute(new Name(name));
    }

    /**
     * Returns the value of the specified manifest attribute name.
     *
     * @param name name of the manifest attribute to find in application
     * @return  plain attribute value
     */
    public String getAttribute(final Name name) {
        if (loader == null) {
            LOG.debug("Jars not ready to provide attribute {}", name);
            return null;
        }
        return getAttributeFromJars(name, Arrays.asList(jnlpFile.getResources().getJARs()), loader.getTracker());
    }

    private ManifestBoolean getBooleanAttribute(final String name) throws IllegalArgumentException {
        String value = getAttribute(name);
        if (value == null) {
            return ManifestBoolean.UNDEFINED;
        } else {
            value = value.toLowerCase().trim();
            switch (value) {
                case "true":
                    return  ManifestBoolean.TRUE;
                case "false":
                    return ManifestBoolean.FALSE;
                default:
                    throw new IllegalArgumentException("Unknown value of " + name + " attribute " + value + ". Expected true or false");
            }
        }
    }

    /**
     * Returns the value of the specified manifest attribute name. To do so, the given jar files
     * are consulted in the following order: "main" jar in the given list, first jar in the given list,
     * all jars in the given list.
     *
     * @param name attribute to be found
     * @param jars Jars that are checked to see if they contain the main class
     * @param tracker tracker to use for the jar file lookup
     * @return the attribute value, null if no attribute could be found for some reason
     */
    public static String getAttributeFromJars(final Name name, final List<JARDesc> jars, final ResourceTracker tracker) {
        if (jars.isEmpty()) {
            return null;
        }

        // Check main jar
        final JARDesc mainJarDesc = ResourcesDesc.getMainJAR(jars);
        if (mainJarDesc == null) {
            return null;
        }
        String result = getAttributeFromJar(name, mainJarDesc.getLocation(), tracker);
        if (result != null) {
            return result;
        }

        // Check first jar
        JARDesc firstJarDesc = jars.get(0);
        result = getAttributeFromJar(name, firstJarDesc.getLocation(), tracker);

        if (result != null) {
            return result;
        }

        // Still not found? Iterate and set if only 1 was found
        for (JARDesc jarDesc : jars) {
            final String attributeInThisJar = getAttributeFromJar(name, jarDesc.getLocation(), tracker);
            if (attributeInThisJar != null) {
                if (result == null) { // first main class
                    result = attributeInThisJar;
                } else { // There is more than one main class. Set to null and break.
                    result = null;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the value of the specified manifest attribute name, or null if the JAR referenced by the given location URL
     * does not contain a manifest or the attribute could not not be found in the manifest.
     *
     * @param name name of the attribute to find
     * @param location The JAR location
     * @param tracker resource tracker to use for the jar file lookup
     *
     * @return the attribute value, null if no attribute could be found for some reason
     */
    public static String getAttributeFromJar(final Name name, final URL location, final ResourceTracker tracker) {
        Assert.requireNonNull(name, "name");
        Assert.requireNonNull(location, "location");
        Assert.requireNonNull(tracker, "tracker");

        final File file = tracker.getCacheFile(location);

        if (file != null) {
            try (JarFile mainJar = new JarFile(file)) {
                final Manifest manifest = mainJar.getManifest();
                if (manifest == null || manifest.getMainAttributes() == null) {
                    //yes, jars without manifest exists
                    return null;
                }
                return manifest.getMainAttributes().getValue(name);
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the name of the main method as specified in the manifest used for launching applications
     * packaged in JAR files.
     *
     * @param location The JAR location
     * @return the main class name, null if there isn't one of if there was an error
     */
    public String getMainClass(final URL location, final ResourceTracker resourceTracker) {
        return getAttributeFromJar(Name.MAIN_CLASS, location, resourceTracker);
    }

    /**
     * Returns a set of paths that indicate the Class-Path entries in the manifest file.
     * The paths are rooted in the same directory as the originalJarPath.
     *
     * @param manifest the manifest
     * @param originalJarPathLocation the remote/original path of the jar containing the manifest
     * @return a Set of String where each string is a path to the jar on the original jar's classpath.
     */
    public static Set<String> getClassPaths(final Manifest manifest, final URL originalJarPathLocation) {
        final Set<String> result = new HashSet<>();
        if (manifest != null) {
            // extract the Class-Path entries from the manifest and split them
            final String classpath = manifest.getMainAttributes().getValue(Name.CLASS_PATH.toString());
            if (classpath == null || classpath.trim().length() == 0) {
                return result;
            }
            final String[] paths = classpath.split(" +");
            final String originalJarPath = originalJarPathLocation.getPath();
            for (String path : paths) {
                if (path.trim().length() == 0) {
                    continue;
                }
                // we want to search for jars in the same subdir on the server
                // as the original jar that contains the manifest file, so find
                // out its subdirectory and use that as the dir
                String dir = "";
                int lastSlash = originalJarPath.lastIndexOf("/");
                if (lastSlash != -1) {
                    dir = originalJarPath.substring(0, lastSlash + 1);
                }
                final String fullPath = dir + path;
                result.add(fullPath);
            }
        }
        return result;
    }
}
