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

import java.util.*;

/**
 * The resources element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class ResourcesDesc {

    /** the locales of these resources */
    private final Locale locales[];

    /** the OS for these resources */
    private final String os[];

    /** the arch for these resources */
    private final String arch[];

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
    public ResourcesDesc(JNLPFile jnlpFile, Locale locales[], String os[], String arch[]) {
        this.jnlpFile = jnlpFile;
        this.locales = locales;
        this.os = os;
        this.arch = arch;
    }

    /**
     * @return the JVMs.
     */
    public JREDesc[] getJREs() {
        List<JREDesc> lresources = getResources(JREDesc.class);
        return lresources.toArray(new JREDesc[lresources.size()]);
    }

    public static JARDesc getMainJAR(JARDesc jars[] ) {
        return getMainJAR(Arrays.asList(jars));
    }

    public static JARDesc getMainJAR(List<JARDesc> jars) {
        for (JARDesc jar : jars) {
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
        List<JARDesc> lresources = getResources(JARDesc.class);
        return lresources.toArray(new JARDesc[lresources.size()]);
    }

    /**
     * @return the JARs with the specified part name.
     *
     * @param partName the part name, null and "" equivalent
     */
    public JARDesc[] getJARs(String partName) {
        List<JARDesc> lresources = getResources(JARDesc.class);

        for (int i = lresources.size(); i-- > 0;) {
            JARDesc jar = lresources.get(i);

            if (!("" + jar.getPart()).equals("" + partName))
                lresources.remove(i);
        }

        return lresources.toArray(new JARDesc[lresources.size()]);
    }

    /**
     * @return the Extensions.
     */
    public ExtensionDesc[] getExtensions() {
        List<ExtensionDesc> lresources = getResources(ExtensionDesc.class);
        return lresources.toArray(new ExtensionDesc[lresources.size()]);
    }

    /**
     * @return the Packages.
     */
    public PackageDesc[] getPackages() {
        List<PackageDesc> lresources = getResources(PackageDesc.class);
        return lresources.toArray(new PackageDesc[lresources.size()]);
    }

    /**
     * Returns the Packages that match the specified class name.
     *
     * @param className the fully qualified class name
     * @return the PackageDesc objects matching the class name
     */
    public PackageDesc[] getPackages(String className) {
        List<PackageDesc> lresources = getResources(PackageDesc.class);

        for (int i = lresources.size(); i-- > 0;) {
            PackageDesc pk = lresources.get(i);

            if (!pk.matches(className))
                lresources.remove(i);
        }

        return lresources.toArray(new PackageDesc[lresources.size()]);
    }

    /**
     * @return the Properties as a list.
     */
    public PropertyDesc[] getProperties() {
        List<PropertyDesc> lresources = getResources(PropertyDesc.class);
        return lresources.toArray(new PropertyDesc[lresources.size()]);
    }

    /**
     * @return the properties as a map.
     */
    public Map<String, String> getPropertiesMap() {
        Map<String, String> properties = new HashMap<>();
        List<PropertyDesc> lresources = getResources(PropertyDesc.class);
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
    public <T> List<T> getResources(Class<T> type) {
        List<T> result = new ArrayList<>();

        for (Object resource : resources) {
            if (type.isAssignableFrom(resource.getClass()))
                result.add(type.cast(resource));
        }

        return result;
    }

    /**
     * Add a resource.
     * @param resource to be added
     */
    public void addResource(Object resource) {
        // if this is going to stay public it should probably take an
        // interface instead of an Object
        if (resource == null)
            throw new IllegalArgumentException("null resource");

        resources.add(resource);
    }

}
