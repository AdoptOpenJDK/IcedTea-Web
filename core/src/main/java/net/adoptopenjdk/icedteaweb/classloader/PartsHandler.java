package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.JarProvider;
import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.DefaultResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.services.PartsCache;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.classloader.ClassLoaderUtils.getClassloaderBackgroundExecutor;
import static net.adoptopenjdk.icedteaweb.classloader.ClassLoaderUtils.waitForCompletion;

public class PartsHandler implements JarProvider, PartsCache {

    private static final Logger LOG = LoggerFactory.getLogger(PartsHandler.class);

    private final List<Part> parts;
    private final Lock partsLock = new ReentrantLock();
    private final Set<Part> downloadedParts = new HashSet<>();
    private final Set<Part> loadedByClassloaderParts = new HashSet<>();

    private final Map<URL, Lock> resourceDownloadLocks = new HashMap<>();

    private final ResourceTracker tracker;
    private final ApplicationTrustValidator trustValidator;

    /* Only used in tests. */
    protected PartsHandler(final List<Part> parts, final ApplicationTrustValidator trustValidator) {
        this(parts, new DefaultResourceTrackerFactory().create(true, DownloadOptions.NONE, JNLPRuntime.getDefaultUpdatePolicy()), trustValidator);
    }

    public PartsHandler(final List<Part> parts, final JNLPFile file, final ResourceTracker tracker) {
        this(parts, tracker, new ApplicationTrustValidatorImpl(file));
    }

    private PartsHandler(final List<Part> parts, final ResourceTracker tracker, final ApplicationTrustValidator trustValidator) {
        this.tracker = tracker;
        this.parts = new CopyOnWriteArrayList<>(parts);
        this.trustValidator = trustValidator;
    }

    @Override
    public List<LoadableJar> loadEagerJars() {
        partsLock.lock();
        try {
            return parts.stream()
                    .filter(part -> !part.isLazy())
                    .filter(part -> !loadedByClassloaderParts.contains(part))
                    .map(this::loadEagerPart)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } finally {
            partsLock.unlock();
        }
    }

    private List<LoadableJar> loadEagerPart(final Part part) {
        final List<LoadableJar> result = downloadAllOfPart(part);
        trustValidator.validateEagerJars(result);
        loadedByClassloaderParts.add(part);
        return result;
    }

    @Override
    public List<LoadableJar> loadMoreJars(String resourceName) {
        partsLock.lock();
        try {
            final List<Part> notLoaded = parts.stream()
                    .filter(part -> !loadedByClassloaderParts.contains(part))
                    .filter(part -> !part.getJars().isEmpty())
                    .collect(Collectors.toList());

            if (notLoaded.isEmpty()) {
                return Collections.emptyList();
            }

            final Part next = notLoaded.stream()
                    .filter(part -> part.supports(resourceName))
                    .findFirst()
                    .orElse(notLoaded.get(0));

            return loadLazyPart(next);
        } finally {
            partsLock.unlock();
        }
    }

    private List<LoadableJar> loadLazyPart(final Part part) {
        final List<LoadableJar> result = downloadAllOfPart(part);
        trustValidator.validateLazyJars(result);
        loadedByClassloaderParts.add(part);
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

    private Future<LoadableJar> downloadJar(final JARDesc jarDesc) {
        return CompletableFuture.supplyAsync(() -> {
            final URL localCacheUrl = getLocalUrlForJar(jarDesc);
            return new LoadableJar(localCacheUrl, jarDesc);
        }, getClassloaderBackgroundExecutor());
    }

    protected URL getLocalUrlForJar(final JARDesc jarDesc) {
        LOG.debug("Trying to get local URL of JAR '{}'", jarDesc.getLocation());
        print("Trying to get local URL of JAR '" + jarDesc.getLocation() + "'");
        final Lock jarLock = getOrCreateLock(jarDesc.getLocation());
        jarLock.lock();
        try {
            if (!tracker.isResourceAdded(jarDesc.getLocation())) {
                tracker.addResource(jarDesc.getLocation(), jarDesc.getVersion());
            }
            final URL url = tracker.getCacheFile(jarDesc.getLocation()).toURI().toURL();
            LOG.debug("Local URL of JAR '{}' is '{}'", jarDesc.getLocation(), url);
            print("Local URL of JAR '" + jarDesc.getLocation() + "' is '" + url + "'");
            return url;
        } catch (final Exception e) {
            print("Unable to provide local URL for JAR '" + jarDesc.getLocation() + "'. Error: " + e.getMessage());
            throw new RuntimeException("Unable to provide local URL for JAR '" + jarDesc.getLocation() + "'", e);
        } finally {
            jarLock.unlock();
        }
    }

    private synchronized Lock getOrCreateLock(final URL resourceUrl) {
        return resourceDownloadLocks.computeIfAbsent(resourceUrl, url -> new ReentrantLock());
    }

    //Methods that are needed for JNLP DownloadService interface

    @Override
    public void downloadPart(final String partName) {
        downloadPart(partName, null);
    }

    @Override
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

    @Override
    public void downloadPartContainingJar(final URL ref, final VersionString version) {
        parts.stream()
                .filter(part -> part.containsJar(ref, version))
                .findFirst()
                .ifPresent(this::downloadAllOfPart);
    }

    @Override
    public boolean isPartDownloaded(final String partName) {
        return isPartDownloaded(partName, null);
    }

    @Override
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

    @Override
    public boolean isPartContainingJar(final URL ref, final VersionString version) {
        return parts.stream().anyMatch(part -> part.containsJar(ref, version));
    }

    @Override
    public void removePart(final String partName) {
        removePart(partName, null);
    }

    @Override
    public void removePart(final String partName, final Extension extension) {
        partsLock.lock();
        try {
            parts.stream()
                    .filter(part -> Objects.equals(extension, part.getExtension()))
                    .filter(part -> Objects.equals(partName, part.getName()))
                    .findFirst()
                    .ifPresent(this::removeAllOfPart);
        } finally {
            partsLock.unlock();
        }
    }

    @Override
    public void removePartContainingJar(final URL ref, final VersionString version) {
        partsLock.lock();
        try {
            parts.stream()
                    .filter(part -> part.containsJar(ref, version))
                    .findFirst()
                    .ifPresent(this::removeAllOfPart);
        } finally {
            partsLock.unlock();
        }
    }

    private void removeAllOfPart(final Part part) {
        downloadedParts.remove(part);
        final List<JARDesc> jars = part.getJars();
        jars.forEach(jarDesc -> Cache.deleteFromCache(jarDesc.getLocation(), jarDesc.getVersion()));
    }

    //JUST FOR CURRENT TESTS!
    @Deprecated
    private void print(final String message) {
        try (FileOutputStream out = new FileOutputStream(new File(System.getProperty("user.home") + "/Desktop/itw-log.txt"), true)) {
            out.write((message + System.lineSeparator()).getBytes());
        } catch (final Exception e) {
            throw new RuntimeException("Can not write message to file!", e);
        } finally {
            System.out.println(message);
        }
    }
}
