package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.adoptopenjdk.icedteaweb.classloader.PartsHandler;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DummyPartsHandler extends PartsHandler {

    public DummyPartsHandler(final List<Part> parts) {
        super(parts, (jars) -> {});
    }

    private final List<JARDesc> downloaded = new CopyOnWriteArrayList<>();

    @Override
    protected URL getLocalUrlForJar(final JARDesc jarDesc) {
        Assert.requireNonNull(jarDesc, "jarDesc");
        if (downloaded.contains(jarDesc)) {
            throw new IllegalStateException("Already downloaded " + jarDesc.getLocation());
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
