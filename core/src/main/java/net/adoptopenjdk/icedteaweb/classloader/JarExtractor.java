package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDownloadDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JNLPResources;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.adoptopenjdk.icedteaweb.classloader.ClassLoaderUtils.waitForCompletion;

public class JarExtractor {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    private final JNLPFileFactory jnlpFileFactory;

    private final Lock partsLock = new ReentrantLock();
    private final List<Part> parts = new ArrayList<>();

    private final Part defaultEagerPart;
    private final Part defaultLazyPart;

    public JarExtractor(final JNLPFile jnlpFile, JNLPFileFactory jnlpFileFactory) {
        this.jnlpFileFactory = jnlpFileFactory;

        this.defaultEagerPart = new Part(null);
        parts.add(defaultEagerPart);
        defaultEagerPart.markAsEager();

        this.defaultLazyPart = new Part(null);
        parts.add(defaultLazyPart);

        addJnlpFile(jnlpFile, false);
    }

    public List<Part> getParts() {
        return unmodifiableList(parts);
    }

    private void addJnlpFile(final JNLPFile jnlpFile, boolean isExtension) {
        final JNLPResources resources = jnlpFile.getJnlpResources();

        final List<Future<Void>> extensionTasks = resources.getExtensions().stream()
                .map(extension -> addExtension(jnlpFile, extension))
                .collect(toList());

        final List<Future<Void>> packageTasks = resources.getPackages().stream()
                .map(packageDesc -> addPackage(jnlpFile, packageDesc, isExtension))
                .collect(toList());

        final List<Future<Void>> jarTasks = resources.getJARs().stream()
                .map(jarDesc -> addJar(jnlpFile, jarDesc, isExtension))
                .collect(toList());

        extensionTasks.forEach(f -> waitForCompletion(f, "Error while loading extensions!"));
        packageTasks.forEach(f -> waitForCompletion(f, "Error while processing packages!"));
        jarTasks.forEach(f -> waitForCompletion(f, "Error while processing jars!"));
    }

    private Future<Void> addExtension(final JNLPFile parent, final ExtensionDesc extension) {
        final CompletableFuture<Void> result = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final JNLPFile jnlpFile = jnlpFileFactory.create(extension.getLocation(), extension.getVersion(), parent.getParserSettings(), JNLPRuntime.getDefaultUpdatePolicy());
                addExtensionParts(parent, jnlpFile, extension.getDownloads());
                addJnlpFile(jnlpFile, true);
                result.complete(null);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private void addExtensionParts(final JNLPFile parentFile, final JNLPFile extensionFile, final List<ExtensionDownloadDesc> downloads) throws ParseException {
        partsLock.lock();
        try {
            for (ExtensionDownloadDesc download : downloads) {
                final String extPartName = download.getExtPart();
                final String partName = download.getPart();


                final Part part = isBlank(partName) ? getOrCreatePart(parentFile, extPartName, true) : getOrCreatePart(extensionFile, partName, true);

                if (!download.isLazy()) {
                    part.markAsEager();
                }
            }
        } finally {
            partsLock.unlock();
        }
    }

    private Part getOrCreatePart(final JNLPFile jnlpFile, final String name, final boolean isExtension) {
        final URL location = jnlpFile.getSourceLocation();
        final String version = Optional.ofNullable(jnlpFile.getFileVersion())
                .map(v -> v.toString())
                .orElse(null);
        final Extension extension = isExtension ? new Extension(location, version) : null;

        return parts.stream()
                .filter(p -> Objects.equals(p.getName(), name))
                .filter(p -> Objects.equals(p.getExtension(), extension))
                .findFirst()
                .orElseGet(() -> {
                    final Part part = new Part(extension, name);
                    parts.add(part);
                    return part;
                });
    }

    private Future<Void> addPackage(final JNLPFile jnlpFile, final PackageDesc packageDesc, final boolean isExtension) {
        partsLock.lock();
        try {
            final Part part = getOrCreatePart(jnlpFile, packageDesc.getPart(), isExtension);
            part.addPackage(packageDesc);
        } finally {
            partsLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Future<Void> addJar(final JNLPFile jnlpFile, final JARDesc jarDescription, final boolean isExtension) {
        final String partName = jarDescription.getPart();

        partsLock.lock();
        try {
            final Part part = isBlank(partName) ? getDefaultPart(jarDescription) : getOrCreatePart(jnlpFile, partName, isExtension);
            part.addJar(jarDescription);
        } finally {
            partsLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Part getDefaultPart(final JARDesc jarDescription) {
        return jarDescription.isLazy() && !jarDescription.isMain() ? defaultLazyPart : defaultEagerPart;
    }
}
