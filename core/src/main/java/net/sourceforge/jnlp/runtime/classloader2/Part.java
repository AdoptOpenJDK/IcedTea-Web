package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * ...
 */
public class Part {

    private final String name;
    private boolean lazy = true;

    private final List<JARDesc> jars = new ArrayList<>();
    private final List<PackageDesc> packages = new ArrayList<>();

    Part(final String name) {
        this.name = name;
    }

    void addJar(final JARDesc jarDescription) {
        if (!jarDescription.isLazy()) {
            markAsEager();
        }

        jars.add(jarDescription);
    }

    void addPackage(final PackageDesc packageDef) {
        packages.add(packageDef);
    }

    void markAsEager() {
        lazy = false;
    }

    public List<JARDesc> getJars() {
        return Collections.unmodifiableList(jars);
    }

    public List<PackageDesc> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public boolean supports(final String resourceName) {
        return packages.stream().anyMatch(partPackage -> partPackage.matches(resourceName));
    }

    public String getName() {
        return name;
    }

    public boolean isLazy() {
        return lazy;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Part.class.getSimpleName() + "{", "}")
                .add("name='" + name + "'")
                .add("lazy=" + lazy)
                .add("jars=" + jars)
                .add("packages=" + packages)
                .toString();
    }
}
