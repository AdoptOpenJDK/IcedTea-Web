package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.runtime.classloader2.ClassLoaderUtils.waitForCompletion;

public class JnlpApplicationClassLoader extends URLClassLoader {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    private final Lock partsLock = new ReentrantLock();

    private final Function<JARDesc, URL> localJarUrlProvider;

    private List<Part> parts = new CopyOnWriteArrayList<>();

    private final NativeLibrarySupport nativeLibrarySupport;

    public JnlpApplicationClassLoader(List<Part> parts, final Function<JARDesc, URL> localJarUrlProvider) throws Exception {
        super(new URL[0], JnlpApplicationClassLoader.class.getClassLoader());
        this.localJarUrlProvider = localJarUrlProvider;
        this.nativeLibrarySupport = new NativeLibrarySupport();

        final List<Part> lazyParts = parts.stream()
                .filter(part -> part.isLazy())
                .collect(Collectors.toList());
        this.parts.addAll(lazyParts);

        final List<Future<Void>> addJarTasks = parts.stream()
                .filter(part -> !part.isLazy())
                .flatMap(part -> part.getJars().stream())
                .map(jar -> downloadAndAdd(jar))
                .collect(Collectors.toList());
        addJarTasks.forEach(future -> waitForCompletion(future, "Error while creating classloader!"));
    }

    private void checkParts(final String name) {
        partsLock.lock();
        try {
            parts.stream()
                    .filter(part -> part.supports(name))
                    .findFirst()
                    .ifPresent(part -> {
                        downloadAndAddPart(part);
                        parts.remove(part);
                    });
        } finally {
            partsLock.unlock();
        }
    }

    private Future<Void> downloadAndAdd(final JARDesc jarDescription) {
        final CompletableFuture<URL> downloadFuture = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final URL localCacheUrl = localJarUrlProvider.apply(jarDescription);
                try {
                    nativeLibrarySupport.addSearchJar(localCacheUrl);
                } catch (final Exception e) {
                    throw new RuntimeException("Unable to inspect jar for native libraries: " + localCacheUrl, e);
                }
                downloadFuture.complete(localCacheUrl);
            } catch (final Exception e) {
                downloadFuture.completeExceptionally(e);
            }
        });
        return downloadFuture.thenAccept(url -> addURL(url));
    }

    private void downloadAndAddPart(final Part part) {
        final List<Future<Void>> futures = part.getJars().stream()
                .map(jar -> downloadAndAdd(jar))
                .collect(Collectors.toList());
        futures.forEach(future -> waitForCompletion(future, "Error while creating classloader!"));
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (final ClassNotFoundException e) {
            checkParts(name);
            return super.findClass(name);
        }
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (final ClassNotFoundException e) {
            checkParts(name);
            return super.loadClass(name);
        }
    }

    @Override
    protected String findLibrary(final String libname) {
        return nativeLibrarySupport.findLibrary(libname)
                .orElseGet(() -> super.findLibrary(libname));
    }

    @Override
    public URL findResource(final String name) {
        final URL result = super.findResource(name);
        if (result == null) {
            checkParts(name);
            return super.findResource(name);
        }
        return result;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        checkParts(name);
        return super.findResources(name);
    }
}
