package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import java.util.stream.Stream;

public class JnlpApplicationClassLoader extends URLClassLoader {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    private final Map<String, Part> parts = new HashMap<>();

    private final Lock partsLock = new ReentrantLock();

    private final Function<JARDesc, URL> localJarUrlProvider;

    public JnlpApplicationClassLoader(final JNLPFile jnlpFile, final Function<JARDesc, URL> localJarUrlProvider) {
        super(new URL[0], JnlpApplicationClassLoader.class.getClassLoader());
        this.localJarUrlProvider = localJarUrlProvider;
        addJnlpFile(jnlpFile);
    }

    private void addJnlpFile(final JNLPFile jnlpFile) {
        Arrays.asList(jnlpFile.getResources()).stream()
                .filter(this::matchCurrentSystem)
                .flatMap(resourcesDesc -> {
                    final List<Future<Void>> partFutures = Arrays.asList(resourcesDesc.getPackages())
                            .stream()
                            .map(partPackage -> addPartPackage(partPackage))
                            .collect(Collectors.toList());

                    final List<Future<Void>> extensionFutures = Arrays.asList(resourcesDesc.getExtensions())
                            .stream()
                            .map(extension -> addExtension(jnlpFile, extension))
                            .collect(Collectors.toList());

                    final List<Future<Void>> jarFutures = Arrays.asList(resourcesDesc.getJARs())
                            .stream()
                            .map(jar -> addJar(jar))
                            .collect(Collectors.toList());

                    return Stream.of(partFutures, extensionFutures, jarFutures).flatMap(list -> list.stream());
                }).forEach(f -> {
            try {
                f.get();
            } catch (final Exception e) {
                throw new RuntimeException("Error while creating classloader!", e);
            }
        });
    }

    private boolean matchCurrentSystem(final ResourcesDesc resourcesDesc) {
        //TODO: check arch / os / JRE ... in desc against current system
        return true;
    }

    private Future<Void> addPartPackage(final PackageDesc packageDesc) {
        final PartPackage partPackage = new PartPackage(packageDesc.getName(), packageDesc.isRecursive());
        partsLock.lock();
        try {
            parts.computeIfAbsent(packageDesc.getPart(), name -> new Part(name)).addPackage(partPackage);
        } finally {
            partsLock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Future<Void> addExtension(final JNLPFile parent, final ExtensionDesc extension) {
        final CompletableFuture<Void> result = new CompletableFuture<>();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                final JNLPFile jnlpFile = new JNLPFile(extension.getLocation(), extension.getVersion(), parent.getParserSettings(), JNLPRuntime.getDefaultUpdatePolicy());
                addJnlpFile(jnlpFile);
                result.complete(null);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private Future<Void> addJar(final JARDesc jarDescription) {
        if (jarDescription.isEager()) {
            return downloadAndAdd(jarDescription);
        } else if (jarDescription.isLazy()) {
            final String partName = jarDescription.getPart();
            if (partName == null) {
                return downloadAndAdd(jarDescription);
            } else {
                partsLock.lock();
                try {
                    parts.computeIfAbsent(partName, name -> new Part(name)).addJar(jarDescription);
                } finally {
                    partsLock.unlock();
                }
                return CompletableFuture.completedFuture(null);
            }
        } else {
            //TODO: NATIVE?
            return CompletableFuture.completedFuture(null);
        }
    }

    private void checkParts(final String name) {
        partsLock.lock();
        try {
            parts.values().stream()
                    .filter(part -> part.supports(name))
                    .findFirst()
                    .ifPresent(part -> {
                        downloadAndAddPart(part);
                        parts.remove(part.getName());
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
        futures.forEach(f -> {
            try {
                f.get();
            } catch (final Exception e) {
                throw new RuntimeException("Error while creating classloader!", e);
            }
        });
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
        //TODO: this is overwritten in JNLPClassLoader
        return super.findLibrary(libname);
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
        //This is more tricky than just calling checkParts here...
        return super.findResources(name);
    }

    private class PartPackage {

        private final String value;

        private final boolean recursive;

        public PartPackage(final String value, final boolean recursive) {
            this.value = value;
            this.recursive = recursive;
        }

        public String getValue() {
            return value;
        }

        public boolean isRecursive() {
            return recursive;
        }

        public boolean supports(final String ressourceName) {

            //Copied from PackageDesc

            // form 1: exact class
            if (Objects.equals(value, ressourceName)) {
                return true;
            }
            // form 2: package.*
            Objects.requireNonNull(ressourceName);
            if (value.endsWith("*")) {
                final String pkName = value.substring(0, value.length() - 1);
                if (ressourceName.startsWith(pkName)) {
                    String postfix = ressourceName.substring(pkName.length() + 1);
                    return recursive || !postfix.contains(".");
                }
            }
            return false;
        }
    }

    private class Part {

        private final String name;

        private final List<PartPackage> packages = new CopyOnWriteArrayList<>();

        private final List<JARDesc> jars = new CopyOnWriteArrayList<>();

        public Part(final String name) {
            this.name = name;
        }

        public void addJar(final JARDesc jarDescription) {
            jars.add(jarDescription);
        }

        public void addPackage(final PartPackage packageDef) {
            packages.add(packageDef);
        }

        public List<JARDesc> getJars() {
            return Collections.unmodifiableList(jars);
        }

        public boolean supports(final String ressourceName) {
            return packages.stream().filter(partPackage -> partPackage.supports(ressourceName)).findAny().isPresent();
        }

        public String getName() {
            return name;
        }
    }
}
