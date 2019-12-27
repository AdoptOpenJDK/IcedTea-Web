package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
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

    private final Function<JARDesc, URL> localCacheAccess;

    private final List<Part> parts;

    private final NativeLibrarySupport nativeLibrarySupport;

    public JnlpApplicationClassLoader(List<Part> parts, final Function<JARDesc, URL> localCacheAccess) throws Exception {
        super(new URL[0], JnlpApplicationClassLoader.class.getClassLoader());
        this.localCacheAccess = localCacheAccess;
        this.nativeLibrarySupport = new NativeLibrarySupport();

        this.parts = parts.stream()
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));

        parts.stream()
                .filter(part -> !part.isLazy())
                .forEach(this::downloadAndAddPart);
    }

    private void checkParts(final String name) {
        partsLock.lock();
        try {
            parts.stream()
                    .filter(part -> part.supports(name))
                    .filter(part -> !part.isDownloaded())
                    .forEach(part -> downloadAndAddPart(part));
        } finally {
            partsLock.unlock();
        }
    }

    private Future<Void> downloadAndAdd(final JARDesc jarDescription) {
        final CompletableFuture<URL> downloadFuture = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final URL localCacheUrl = localCacheAccess.apply(jarDescription);
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
        return downloadFuture.thenAccept(this::addURL);
    }

    private void downloadAndAddPart(final Part part) {
        part.getJars().stream()
                .map(this::downloadAndAdd)
                .forEach(future -> waitForCompletion(future, "Error while creating classloader!"));
        part.setDownloaded(true);
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




    //Methods that are needed for JNLP DownloadService interface

    public void downloadPart(final String partName) {
        downloadPart(partName, null);
    }

    public void downloadPart(final String partName, final Extension extension) {
        partsLock.lock();
        try {
            parts.stream()
                    .filter(part -> Objects.equals(extension, part.getExtension()))
                    .filter(part -> Objects.equals(partName, part.getName()))
                    .findFirst()
                    .ifPresent(part -> downloadAndAddPart(part));
        } finally {
            partsLock.unlock();
        }
    }

    public boolean isPartDownloaded(final String partName) {
        return isPartDownloaded(partName, null);
    }

    public boolean isPartDownloaded(final String partName, final Extension extension) {
        partsLock.lock();
        try {
            return parts.stream()
                    .filter(part -> Objects.equals(extension, part.getExtension()))
                    .filter(part -> Objects.equals(partName, part.getName()))
                    .anyMatch(part -> part.isDownloaded());
        } finally {
            partsLock.unlock();
        }
    }

    @Deprecated
    public void removePartDownloads(final String partName) {
        removePartDownloads(partName, null);
    }

    @Deprecated
    public void removePartDownloads(final String partName, final Extension extension) {
        // While DownloadService provides the possibility to remove a part we can not really do that since
        // the URLClassLoader do not provide functionallity to remove a URL.
        //Once this ClassLoader is used in ITW the exception should be thrown in the XDownloadService.
        //This is just a reminder that such functionallity can not be implemented.
        throw new RuntimeException("Can not remove part!");
    }
}
