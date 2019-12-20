package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

class DummyJarProvider implements Function<JARDesc, URL> {

    private final List<JARDesc> downloaded = new CopyOnWriteArrayList<>();

    @Override
    public URL apply(final JARDesc jarDesc) {
        Assert.requireNonNull(jarDesc, "jarDesc");
        if(downloaded.contains(jarDesc)) {
            throw new IllegalStateException("Already downloaded " + jarDesc);
        }
        System.out.println("Should load " + jarDesc.getLocation());
        downloaded.add(jarDesc);
        return jarDesc.getLocation();
    }

    public boolean hasTriedToDownload(final String name) {
        return downloaded.stream()
                .anyMatch(jar -> jar.getLocation().toString().endsWith(name));
    }

    public List<JARDesc> getDownloaded() {
        return Collections.unmodifiableList(downloaded);
    }
}
