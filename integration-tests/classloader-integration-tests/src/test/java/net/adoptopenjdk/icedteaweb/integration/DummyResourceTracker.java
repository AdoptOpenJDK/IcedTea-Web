package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.sourceforge.jnlp.DownloadOptions;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class DummyResourceTracker implements ResourceTracker {

    public static final class Factory implements ResourceTrackerFactory {
        @Override
        public ResourceTracker create(boolean prefetch, DownloadOptions downloadOptions, UpdatePolicy updatePolicy) {
            return new DummyResourceTracker();
        }
    }

    private final Set<URL> trackedUrls = new HashSet<>();

    @Override
    public void addResource(URL location, VersionString version) {
        trackedUrls.add(location);
    }

    @Override
    public void addResource(URL location, VersionString version, UpdatePolicy updatePolicy) {
        trackedUrls.add(location);
    }

    @Override
    public File getCacheFile(URL location) {
        if (!isResourceAdded(location)) {
            throw new IllegalStateException("Resource " + location + " is not known to the tracker");
        }
        return new File(location.getFile());
    }

    @Override
    public boolean isResourceAdded(URL location) {
        return trackedUrls.contains(location);
    }
}
