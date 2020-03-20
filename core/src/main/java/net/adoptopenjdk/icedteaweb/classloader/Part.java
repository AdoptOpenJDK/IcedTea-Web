package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * ...
 */
public class Part {

    private final String name;
    private boolean lazy = true;

    private final Extension extension;

    private final List<JARDesc> jars = new ArrayList<>();
    private final List<PackageDesc> packages = new ArrayList<>();

    Part() {
        this(UUID.randomUUID().toString());
    }

    Part(final String name) {
        this(null, name);
    }

    Part(final Extension extension, final String name) {
        this.extension = extension;
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

    public Extension getExtension() {
        return extension;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Part part = (Part) o;
        return Objects.equals(name, part.name) &&
                Objects.equals(extension, part.extension) &&
                lazy == part.lazy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, extension, lazy);
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

    public boolean containsJar(final URL ref, final VersionString version) {
        return jars.stream().anyMatch(jarDesc ->
                Objects.equals(jarDesc.getLocation(), ref) && Objects.equals(jarDesc.getVersion(), version)
        );
    }
}
