package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;

public class DelegatingClassLoader extends ClassLoader {
    public static final DelegatingClassLoader instance = new DelegatingClassLoader(Thread.currentThread().getContextClassLoader());
    private ClassLoader classLoader;

    public static DelegatingClassLoader getInstance() {
        return instance;
    }

    private DelegatingClassLoader(ClassLoader loader) {
        super(Assert.requireNonNull(loader, "loader"));
        this.classLoader = loader;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = Assert.requireNonNull(loader, "loader");
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }

    @Override
    protected URL findResource(String name) {
        return this.classLoader.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return this.classLoader.getResources(name);
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }

    @Override
    public URL getResource(final String name) {
        return this.classLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return this.classLoader.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        return this.classLoader.getResourceAsStream(name);
    }

    @Override
    public void setDefaultAssertionStatus(final boolean enabled) {
        this.classLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(final String packageName, final boolean enabled) {
        this.classLoader.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(final String className, final boolean enabled) {
        this.classLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        super.clearAssertionStatus();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.classLoader == null ? 0 : this.classLoader.hashCode());
        result = 31 * result + (this.getParent() == null ? 0 : this.getParent().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            DelegatingClassLoader other = (DelegatingClassLoader) obj;
            if (!Objects.equals(this.classLoader, other.classLoader)) {
                return false;
            }
            if (!Objects.equals(this.getParent(), other.getParent())) {
                return false;
            }
            return true;
        }
    }
}
