package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDownloadDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JNLPResources;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

public class JarExtractor {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    private final Lock partsLock = new ReentrantLock();
    private final Part defaultEagerPart = createAndAddPart(null);
    private final Part defaultLazyPart = createAndAddPart(null);
    private final List<Part> parts = new ArrayList<>();
    private final Map<PartKey, Part> partKeyMap = new HashMap<>();

    public JarExtractor(final JNLPFile jnlpFile) {
        defaultEagerPart.markAsEager();
        addJnlpFile(jnlpFile);
    }

    public List<Part> getParts() {
        return unmodifiableList(parts);
    }

    private void addJnlpFile(final JNLPFile jnlpFile) {
        final JNLPResources resources = jnlpFile.getJnlpResources();

        final List<Future<Void>> extensionTasks = resources.getExtensions().stream()
                .map(extension -> addExtension(jnlpFile, extension))
                .collect(toList());

        final List<Future<Void>> packageTasks = resources.getPackages().stream()
                .map(packageDesc -> addPackage(jnlpFile, packageDesc))
                .collect(toList());

        final List<Future<Void>> jarTasks = resources.getJARs().stream()
                .map(jarDesc -> addJar(jnlpFile, jarDesc))
                .collect(toList());

        extensionTasks.forEach(f -> waitForCompletion(f, "Error while loading extensions!"));
        packageTasks.forEach(f -> waitForCompletion(f, "Error while processing packages!"));
        jarTasks.forEach(f -> waitForCompletion(f, "Error while processing jars!"));
    }

    private Future<Void> addExtension(final JNLPFile parent, final ExtensionDesc extension) {


        final CompletableFuture<Void> result = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final JNLPFile jnlpFile = new JNLPFile(extension.getLocation(), extension.getVersion(), parent.getParserSettings(), JNLPRuntime.getDefaultUpdatePolicy());
                addExtensionParts(parent, jnlpFile, extension.getDownloads());
                addJnlpFile(jnlpFile);
                result.complete(null);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private void addExtensionParts(JNLPFile parentFile, JNLPFile extensionFile, List<ExtensionDownloadDesc> downloads) throws ParseException {
        partsLock.lock();
        try {
            for (ExtensionDownloadDesc download : downloads) {
                final String extPartName = download.getExtPart();
                final PartKey extensionKey = new PartKey(extensionFile, extPartName);
                final String partName = download.getPart();

                if (partKeyMap.containsKey(extensionKey)) {
                    throw new ParseException("found extension part twice: " + extPartName);
                }

                final Part part = isBlank(partName) ? createAndAddPart(extPartName) : fromMap(parentFile, partName);

                if (!download.isLazy()) {
                    part.markAsEager();
                }
                partKeyMap.put(extensionKey, part);
            }
        } finally {
            partsLock.unlock();
        }
    }

    private Future<Void> addPackage(final JNLPFile jnlpFile, final PackageDesc packageDesc) {
        partsLock.lock();
        try {
            final Part part = fromMap(jnlpFile, packageDesc.getPart());
            part.addPackage(packageDesc);
        } finally {
            partsLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Future<Void> addJar(JNLPFile jnlpFile, final JARDesc jarDescription) {
        final String partName = jarDescription.getPart();

        partsLock.lock();
        try {
            final Part part = isBlank(partName) ? getDefaultPart(jarDescription) : fromMap(jnlpFile, partName);
            part.addJar(jarDescription);
        } finally {
            partsLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Part getDefaultPart(JARDesc jarDescription) {
        return jarDescription.isLazy() && !jarDescription.isMain() ? defaultLazyPart : defaultEagerPart;
    }

    private Part fromMap(JNLPFile jnlpFile, String partName) {
        return partKeyMap.computeIfAbsent(new PartKey(jnlpFile, partName), k -> createAndAddPart(partName));
    }

    private Part createAndAddPart(String partName) {
        final Part newPart = new Part(partName);
        parts.add(newPart);
        return newPart;
    }

    private void waitForCompletion(Future<Void> f, String message) {
        try {
            f.get();
        } catch (final Exception e) {
            throw new RuntimeException(message, e);
        }
    }

    private static class PartKey {
        private final JNLPFile file;
        private final String name;

        private PartKey(JNLPFile file, String name) {
            this.file = file;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartKey partKey = (PartKey) o;
            return file.equals(partKey.file) && name.equals(partKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, name);
        }
    }

}
