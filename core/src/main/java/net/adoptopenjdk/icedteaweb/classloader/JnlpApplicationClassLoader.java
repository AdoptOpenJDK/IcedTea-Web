package net.adoptopenjdk.icedteaweb.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class JnlpApplicationClassLoader extends URLClassLoader {

    private final JarProvider jarProvider;
    private final NativeLibrarySupport nativeLibrarySupport;

    private final ReentrantLock addJarLock = new ReentrantLock();

    public JnlpApplicationClassLoader(JarProvider jarProvider) {
        this(jarProvider, new NativeLibrarySupport());
    }

    public JnlpApplicationClassLoader(JarProvider jarProvider, NativeLibrarySupport nativeLibrarySupport) {
        super(new URL[0], JnlpApplicationClassLoader.class.getClassLoader());
        this.jarProvider = jarProvider;
        this.nativeLibrarySupport = nativeLibrarySupport;

        jarProvider.loadEagerJars().forEach(this::addJar);
    }

    private boolean loadMoreJars(final String name) {
        return jarProvider.loadMoreJars(name).stream()
                .peek(this::addJar)
                .count() > 0;
    }

    private void addJar(LoadableJar jar) {
        addJarLock.lock();
        try {
            if (!Arrays.asList(getURLs()).contains(jar.getLocation())) {
                if (jar.containsNativeLib()) {
                    nativeLibrarySupport.addSearchJar(jar.getLocation());
                }
                addURL(jar.getLocation());
            }
        } finally {
            addJarLock.unlock();
        }
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        do {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        while (loadMoreJars(name));
        throw new ClassNotFoundException(name);
    }

    @Override
    protected String findLibrary(final String libname) {
        return nativeLibrarySupport.findLibrary(libname)
                .orElseGet(() -> super.findLibrary(libname));
    }

    @Override
    public URL findResource(final String name) {
        do {
            final URL result = super.findResource(name);
            if (result != null) {
                return result;
            }
        }
        while (loadMoreJars(name));
        return null;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        //noinspection StatementWithEmptyBody
        while (loadMoreJars(name)) {
            // continue until finished
        }
        return super.findResources(name);
    }

    public interface JarProvider {
        List<LoadableJar> loadEagerJars();
        List<LoadableJar> loadMoreJars(String name);
    }

    public static class LoadableJar {
        private final URL location;
        private final boolean containsNativeLib;

        LoadableJar(URL location, boolean containsNativeLib) {
            this.location = location;
            this.containsNativeLib = containsNativeLib;
        }

        public URL getLocation() {
            return location;
        }

        public boolean containsNativeLib() {
            return containsNativeLib;
        }
    }
}
