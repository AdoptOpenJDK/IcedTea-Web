package net.sourceforge.jnlp.runtime.classloader;

import java.net.URL;
import java.util.Objects;

public class DelegatingClassLoader extends ClassLoader {
    public static final DelegatingClassLoader instance = new DelegatingClassLoader(Thread.currentThread().getContextClassLoader());
    private ClassLoader classLoader;

    public static DelegatingClassLoader getInstance() {
        return instance;
    }

    private DelegatingClassLoader(ClassLoader loader) {
        super(loader);
        this.classLoader = loader;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }

    protected URL findResource(String name) {
        return this.classLoader.getResource(name);
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.classLoader == null ? 0 : this.classLoader.hashCode());
        result = 31 * result + (this.getParent() == null ? 0 : this.getParent().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            DelegatingClassLoader other = (DelegatingClassLoader) obj;
            if(!Objects.equals(this.classLoader,other.classLoader)) {
                return false;
            }
            if(!Objects.equals(this.getParent(),other.getParent())) {
                return false;
            }
            return true;
        }
    }
}
