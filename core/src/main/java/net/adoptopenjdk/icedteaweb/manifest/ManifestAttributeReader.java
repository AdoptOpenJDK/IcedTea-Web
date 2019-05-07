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

import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.util.ClasspathMatcher;
import net.sourceforge.jnlp.util.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestAttributeReader {
    private final static Logger LOG = LoggerFactory.getLogger(ManifestAttributeReader.class);

    private JNLPFile jnlpFile;
    private JNLPClassLoader loader;

    public ManifestAttributeReader(final JNLPFile jnlpFile) {
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
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#entry_pt
     * @return values of Entry-Points attribute
     */
    public String[] getEntryPoints() {
        return splitEntryPoints(getEntryPointString());
    }

    public String getEntryPointString() {
        return getAttribute(ManifestAttributes.ENTRY_POINT.toString());
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#app_name
     * @return value of Application-Name manifest attribute
     */
    public String getApplicationName() {
        return getAttribute(ManifestAttributes.APPLICATION_NAME.toString());
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#caller_allowable
     * @return values of Caller-Allowable-Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getCallerAllowableCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.CALLER_ALLOWABLE_CODEBASE.toString(), false);
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#app_library
     * @return values of Application-Library-Allowable-Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getApplicationLibraryAllowableCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.APPLICATION_LIBRARY_ALLOWABLE_CODEBASE.toString(), true);
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
     * @return values of Codebase manifest attribute
     */
    public ClasspathMatcher.ClasspathMatchers getCodebase() {
        return getCodeBaseMatchersAttribute(ManifestAttributes.CODEBASE.toString(), false);
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#trusted_only
     * @return value of Trusted-Only manifest attribute
     */
    public ManifestBoolean isTrustedOnly() {
        return processBooleanAttribute(ManifestAttributes.TRUSTED_ONLY.toString());

    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#trusted_library
     * @return value of Trusted-Library manifest attribute
     */
    public ManifestBoolean isTrustedLibrary() {
        return processBooleanAttribute(ManifestAttributes.TRUSTED_LIBRARY.toString());
    }

    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#permissions
     * @return value of Permissions manifest attribute
     */
    public ManifestBoolean isSandboxForced() {
        final String permissionLevel = getManifestPermissionsAttribute();
        if (permissionLevel == null) {
            return ManifestBoolean.UNDEFINED;
        } else if (permissionLevel.trim().equalsIgnoreCase(AppletPermissionLevel.SANDBOX.getValue())) {
            return ManifestBoolean.TRUE;
        } else if (permissionLevel.trim().equalsIgnoreCase(AppletPermissionLevel.ALL.getValue())) {
            return ManifestBoolean.FALSE;
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown value of %s attribute %s. Expected %s or %s",
                            ManifestAttributes.PERMISSIONS.toString(), permissionLevel,
                            AppletPermissionLevel.SANDBOX.getValue(), AppletPermissionLevel.ALL.getValue())
            );
        }
    }
    /**
     * https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#permissions
     * @return plain string values of Permissions manifest attribute
     */
    public String permissionsToString() {
        final String s = getManifestPermissionsAttribute();
        if (s == null) {
            return "Not defined";
        } else if (s.trim().equalsIgnoreCase(AppletPermissionLevel.SANDBOX.getValue())) {
            return s.trim();
        } else if (s.trim().equalsIgnoreCase(AppletPermissionLevel.ALL.getValue())) {
            return s.trim();
        } else {
            return "illegal";
        }
    }

    public String getManifestPermissionsAttribute() {
        return getAttribute(ManifestAttributes.PERMISSIONS.toString());
    }

    /**
     * get custom attribute.
     */
    private String getAttribute(String name) {
        return getAttribute(new Attributes.Name(name));
    }

    /**
     * get standard attribute
     * @param name name of the manifest attribute to find in application
     * @return  plain attribute value
     */
    public String getAttribute(Attributes.Name name) {
        if (loader == null) {
            LOG.debug("Jars not ready to provide attribute {}", name);
            return null;
        }
        return loader.checkForAttributeInJars(Arrays.asList(jnlpFile.getResources().getJARs()), name);
    }

    /**
     * Gets the name of the main method if specified in the manifest
     *
     * @param location The JAR location
     * @param attribute name of the attribute to find
     * @param resourceTracker the resource tracker
     * @return the attribute value, null if there isn't one of if there was an
     * error
     */
    public static String getManifestAttribute(final URL location, final Attributes.Name attribute, final ResourceTracker resourceTracker) {

        String attributeValue = null;
        final File f = resourceTracker.getCacheFile(location);

        if (f != null) {
            JarFile mainJar = null;
            try {
                mainJar = new JarFile(f);
                final Manifest manifest = mainJar.getManifest();
                if (manifest == null || manifest.getMainAttributes() == null) {
                    //yes, jars without manifest exists
                    return null;
                }
                attributeValue = manifest.getMainAttributes().getValue(attribute);
            } catch (IOException ioe) {
                attributeValue = null;
            } finally {
                StreamUtils.closeSilently(mainJar);
            }
        }

        return attributeValue;
    }

    public static String[] splitEntryPoints(final String entryPointString) {
        if (entryPointString == null || entryPointString.trim().isEmpty()) {
            return null;
        }
        final String[] result = entryPointString.trim().split("\\s+");
        if (result.length == 0) {
            return null;
        }
        return result;
    }

    private ClasspathMatcher.ClasspathMatchers getCodeBaseMatchersAttribute(String s, boolean includePath) {
        return getCodeBaseMatchersAttribute(new Attributes.Name(s), includePath);
    }

    private ClasspathMatcher.ClasspathMatchers getCodeBaseMatchersAttribute(Attributes.Name name, boolean includePath) {
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
