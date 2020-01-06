package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.adoptopenjdk.icedteaweb.StringUtils.hasPrefixMatch;
import static net.sourceforge.jnlp.util.LocaleUtils.localeMatches;

/**
 * ...
 */
public class JNLPResources {
    private final List<ResourcesDesc> resources;

    public JNLPResources(List<ResourcesDesc> resources) {
        final ArrayList<ResourcesDesc> copy = new ArrayList<>();
        if (resources != null) {
            copy.addAll(resources);
        }
        this.resources = Collections.unmodifiableList(copy);
    }

    public JNLPResources filterResources(Locale locale, String os, String arch) {
        final List<ResourcesDesc> list = resources.stream()
                .filter(rescDesc -> hasPrefixMatch(os, rescDesc.getOS()))
                .filter(rescDesc -> hasPrefixMatch(arch, rescDesc.getArch()))
                .filter(rescDesc -> localeMatches(locale, rescDesc.getLocales()))
                .collect(toList());
        return new JNLPResources(list);
    }

    public List<ResourcesDesc> all() {
        return resources;
    }

    public Stream<ResourcesDesc> stream() {
        return resources.stream();
    }

    /**
     * @return the JVMs.
     */
    public List<JREDesc> getJREs() {
        return getResources(JREDesc.class);
    }


    /**
     * @return all of the JARs.
     */
    public List<JARDesc> getJARs() {
        return getResources(JARDesc.class);
    }

    /**
     * @param partName the part name, null and "" equivalent
     * @return the JARs with the specified part name.
     */
    public List<JARDesc> getJARs(final String partName) {
        Assert.requireNonBlank(partName, "partName");
        return getJARs().stream()
                .filter(jarDesc -> partName.equals(jarDesc.getPart()))
                .collect(toList());
    }

    /**
     * @return the Extensions.
     */
    public List<ExtensionDesc> getExtensions() {
        return getResources(ExtensionDesc.class);
    }

    /**
     * @return the Packages.
     */
    public List<PackageDesc> getPackages() {
        return getResources(PackageDesc.class);
    }

    /**
     * Returns the Packages that match the specified class name.
     *
     * @param className the fully qualified class name
     * @return the PackageDesc objects matching the class name
     */
    public List<PackageDesc> getPackages(final String className) {
        return getPackages().stream()
                .filter(pk -> pk.matches(className))
                .collect(toList());
    }

    /**
     * @return the Properties as a list.
     */
    public List<PropertyDesc> getProperties() {
        return getResources(PropertyDesc.class);
    }

    /**
     * @return the properties as a map.
     */
    public Map<String, String> getPropertiesMap() {
        final Map<String, String> result = new HashMap<>();
        for (PropertyDesc property : getProperties()) {
            result.put(property.getKey(), property.getValue());
        }
        return result;
    }

    /**
     * @param <T>  type of resource to be found
     * @param type resource to be found
     * @return all resources of the specified type.
     */
    private <T> List<T> getResources(final Class<T> type) {
        return resources.stream()
                .flatMap(resourcesDesc -> resourcesDesc.getResources(type, false).stream())
                .collect(toList());
    }
}
