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

package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.sourceforge.jnlp.JNLPFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * The resources element specifies all the resources that are part of the application, such as
 * Java class files, native libraries, and system properties.
 *
 * @implSpec See <b>JSR-56, Section 4 Application Resources</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class ResourcesDesc {

    public static final String RESOURCES_ELEMENT = "resources";
    public static final String OS_ATTRIBUTE = "os";
    public static final String ARCH_ATTRIBUTE = "arch";

    public static final String NATIVELIB_ELEMENT = "nativelib";
    public static final String JAR_ELEMENT = "jar";
    public static final String J2SE_ELEMENT = "j2se";
    public static final String JAVA_ELEMENT = "java";
    public static final String EXTENSION_ELEMENT = "extension";
    public static final String PROPERTY_ELEMENT = "property";
    public static final String PACKAGE_ELEMENT = "package";
    public static final String HREF_ATTRIBUTE = "href";

    /**
     * the locales of these resources
     */
    private final Locale[] locales;

    /**
     * the OS for these resources
     */
    private final String[] os;

    /**
     * the arch for these resources
     */
    private final String[] arch;

    /** the JNLPFile this information is for */
    private final JNLPFile jnlpFile;

    /** list of jars, packages, properties, and extensions */
    private final List<Object> resources = new ArrayList<>();

    // mixed list makes easier for lookup code

    /**
     * Create a representation of one information section of the
     * JNLP File.
     *
     * @param jnlpFile JNLP file the resources are for
     * @param locales the locales of these resources
     * @param os the os of these resources
     * @param arch the arch of these resources
     */
    public ResourcesDesc(final JNLPFile jnlpFile, final Locale[] locales, final String[] os, final String[] arch) {
        this.jnlpFile = jnlpFile;
        this.locales = locales;
        this.os = os;
        this.arch = arch;
    }

    /**
     * @return the JVMs.
     */
    public JREDesc[] getJREs() {
        final List<JREDesc> lresources = getResources(JREDesc.class);
        return lresources.toArray(new JREDesc[lresources.size()]);
    }

    public static JARDesc getMainJAR(final JARDesc[] jars) {
        return getMainJAR(asList(jars));
    }

    public static JARDesc getMainJAR(List<JARDesc> jars) {
        for (final JARDesc jar : jars) {
            if (jar.isMain()) {
                return jar;
            } 
        }

        if (jars.size() > 0) {
            return jars.get(0);
        } else {
            return null;
        }
    }
    /**
     * @return the main JAR for these resources.  There first JAR
     * is returned if no JARs are specified as the main JAR, and if
     * there are no JARs defined then null is returned.
     */
    public JARDesc getMainJAR() {
        return getMainJAR(getJARs());
    }

    /**
     * @return all of the JARs.
     */
    public JARDesc[] getJARs() {
        final List<JARDesc> lresources = getResources(JARDesc.class);
        return lresources.toArray(new JARDesc[lresources.size()]);
    }

    /**
     * @return the JARs with the specified part name.
     *
     * @param partName the part name, null and "" equivalent
     */
    public JARDesc[] getJARs(final String partName) {
        final List<JARDesc> lresources = getResources(JARDesc.class);

        for (int i = lresources.size(); i-- > 0;) {
            final JARDesc jar = lresources.get(i);

            if (!("" + jar.getPart()).equals("" + partName))
                lresources.remove(i);
        }

        return lresources.toArray(new JARDesc[lresources.size()]);
    }

    /**
     * @return the Extensions.
     */
    public ExtensionDesc[] getExtensions() {
        final List<ExtensionDesc> lresources = getResources(ExtensionDesc.class);
        return lresources.toArray(new ExtensionDesc[lresources.size()]);
    }

    /**
     * @return the Packages.
     */
    public PackageDesc[] getPackages() {
        final List<PackageDesc> lresources = getResources(PackageDesc.class);
        return lresources.toArray(new PackageDesc[lresources.size()]);
    }

    /**
     * Returns the Packages that match the specified class name.
     *
     * @param className the fully qualified class name
     * @return the PackageDesc objects matching the class name
     */
    public PackageDesc[] getPackages(final String className) {
        final List<PackageDesc> lresources = getResources(PackageDesc.class);

        for (int i = lresources.size(); i-- > 0;) {
            final PackageDesc pk = lresources.get(i);

            if (!pk.matches(className))
                lresources.remove(i);
        }

        return lresources.toArray(new PackageDesc[lresources.size()]);
    }

    /**
     * @return the Properties as a list.
     */
    public PropertyDesc[] getProperties() {
        final List<PropertyDesc> lresources = getResources(PropertyDesc.class);
        return lresources.toArray(new PropertyDesc[lresources.size()]);
    }

    /**
     * @return the properties as a map.
     */
    public Map<String, String> getPropertiesMap() {
        final Map<String, String> properties = new HashMap<>();
        final List<PropertyDesc> lresources = getResources(PropertyDesc.class);
        for (PropertyDesc prop : lresources) {
            properties.put(prop.getKey(), prop.getValue());
        }

        return properties;
    }

    /**
     * @return the os required by these resources, or null if no
     * locale was specified in the JNLP file.
     */
    public String[] getOS() {
        return os;
    }

    /**
     * @return the architecture required by these resources, or null
     * if no locale was specified in the JNLP file.
     */
    public String[] getArch() {
        return arch;
    }

    /**
     * @return the locale required by these resources, or null if no
     * locale was specified in the JNLP file.
     */
    public Locale[] getLocales() {
        return locales;
    }

    /**
     * @return the JNLPFile the resources are for.
     */
    public JNLPFile getJNLPFile() {
        return jnlpFile;
    }

    /**
     * @param <T> type of resource to be found
     * @param type resource to be found
     * @return all resources of the specified type.
     */
    public <T> List<T> getResources(final Class<T> type) {
        final List<T> result = new ArrayList<>();
        for (final Object resource : resources) {
            if (resource instanceof JREDesc) {
                final JREDesc jre = (JREDesc) resource;
                final List<ResourcesDesc> descs = jre.getResourcesDesc();
                for (final ResourcesDesc desc : descs) {
                    result.addAll(desc.getResources(type));
                }
            }
            if (isWontedResource(resource, type)) {
                result.add(getWontedResource(resource, type));
            }
        }

        return result;
    }

    private static <T> boolean isWontedResource(final Object resource, final Class<T> type) {
        final T l = getWontedResource(resource, type);
        return l != null;
    }

    private static <T> T getWontedResource(final Object resource, final Class<T> type) {
        if (type.isAssignableFrom(resource.getClass())) {
            return type.cast(resource);
        }
        return null;
    }

    /**
     * Add a resource.
     * @param resource to be added
     */
    public void addResource(final Object resource) {
        // if this is going to stay public it should probably take an
        // interface instead of an Object
        if (resource == null)
            throw new IllegalArgumentException("null resource");

        resources.add(resource);
    }

}
