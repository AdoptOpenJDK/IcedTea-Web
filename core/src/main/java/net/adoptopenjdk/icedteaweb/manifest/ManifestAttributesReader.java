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
import net.sourceforge.jnlp.util.ClasspathMatcher;
import net.sourceforge.jnlp.util.JarFile;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * This class allows to read the security and configuration attributes specified in the JAR file manifest.
 *
 * @implSpec See https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/manifest.html for more details
 * on the security and configuration attributes stored in the JAR file manifest.
 */
public class ManifestAttributesReader {
    private final Manifest manifest;

    public ManifestAttributesReader(File jarFile) {
        this.manifest = getManifest(Assert.requireNonNull(jarFile, "jarFile"));
    }

    private Manifest getManifest(File jarFile) {
        try (JarFile mainJar = new JarFile(jarFile)) {
            Manifest manifest = mainJar.getManifest();
            if (manifest != null && manifest.getMainAttributes() != null) {
                return manifest;
            }
        } catch (IOException ignored) {
        }
        return new Manifest();
    }

    /**
     * The raw string representation (fully qualified class names separated by a space) of the
     * Entry-Point manifest attribute value that can be used as entry point for the RIA.
     *
     * @return the Entry-Point manifest attribute value
     * @see #getEntryPoints() for a tokenized representation of the entry points
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
     * Gets the name of the main method as specified in the manifest used for launching applications
     * packaged in JAR files.
     *
     * @return the main class name, null if there isn't one of if there was an error
     */
    public String getMainClass() {
        return getAttribute(Name.MAIN_CLASS);
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
     * @return plain attribute value
     */
    public String getAttribute(final Name name) {
        return manifest.getMainAttributes().getValue(name);
    }

    private ManifestBoolean getBooleanAttribute(final String name) throws IllegalArgumentException {
        String value = getAttribute(name);
        if (value == null) {
            return ManifestBoolean.UNDEFINED;
        } else {
            value = value.toLowerCase().trim();
            switch (value) {
                case "true":
                    return ManifestBoolean.TRUE;
                case "false":
                    return ManifestBoolean.FALSE;
                default:
                    throw new IllegalArgumentException("Unknown value of " + name + " attribute " + value + ". Expected true or false");
            }
        }
    }
}
