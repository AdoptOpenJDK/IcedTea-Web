package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.JarProvider;
import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.classloader.ClassLoaderUtils.waitForCompletion;

public class PartsHandler implements JarProvider {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    private final Function<JARDesc, URL> localCacheAccess;

    private final List<Part> parts;
    private final Lock partsLock = new ReentrantLock();
    private final Set<Part> downloadedParts = new HashSet<>();
    private final Set<Part> loadedParts = new HashSet<>();

    public PartsHandler(List<Part> parts, final Function<JARDesc, URL> localCacheAccess) {
        this.localCacheAccess = localCacheAccess;
        this.parts = new CopyOnWriteArrayList<>(parts);
    }

    @Override
    public List<LoadableJar> loadEagerJars() {
        partsLock.lock();
        try {
            return parts.stream()
                    .filter(part -> !part.isLazy())
                    .filter(part -> !loadedParts.contains(part))
                    .map(this::loadPart)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } finally {
            partsLock.unlock();
        }
    }

    @Override
    public List<LoadableJar> loadMoreJars(String name) {
        partsLock.lock();
        try {
            final List<Part> notLoaded = parts.stream()
                    .filter(o -> !loadedParts.contains(o))
                    .collect(Collectors.toList());

            if (notLoaded.isEmpty()) {
                return Collections.emptyList();
            }

            final Part next = notLoaded.stream()
                    .filter(part -> part.supports(name))
                    .findFirst()
                    .orElse(notLoaded.get(0));

            return loadPart(next);
        } finally {
            partsLock.unlock();
        }
    }

    private List<LoadableJar> loadPart(final Part part) {
        final List<LoadableJar> result = downloadAllOfPart(part);
        loadedParts.add(part);
        return result;
    }

    private List<LoadableJar> downloadAllOfPart(final Part part) {
        final List<Future<LoadableJar>> tasks = part.getJars().stream()
                .map(this::downloadJar)
                .collect(Collectors.toList());

        final List<LoadableJar> result = tasks.stream()
                .map(future -> waitForCompletion(future, "Error while downloading jar!"))
                .collect(Collectors.toList());

        downloadedParts.add(part);
        return result;
    }

    private Future<LoadableJar> downloadJar(final JARDesc jarDescription) {
        final CompletableFuture<LoadableJar> downloadFuture = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final URL localCacheUrl = localCacheAccess.apply(jarDescription);
                downloadFuture.complete(new LoadableJar(localCacheUrl, jarDescription.isNative()));
            } catch (final Exception e) {
                downloadFuture.completeExceptionally(e);
            }
        });
        return downloadFuture;
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
                    .ifPresent(this::downloadAllOfPart);
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
                    .anyMatch(downloadedParts::contains);
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
        // the URLClassLoader do not provide functionality to remove a URL.
        // Once this ClassLoader is used in ITW the exception should be thrown in the XDownloadService.
        // This is just a reminder that such functionality can not be implemented.
        throw new RuntimeException("Can not remove part!");
    }
}
