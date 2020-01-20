package net.adoptopenjdk.icedteaweb.resources;

import net.sourceforge.jnlp.DownloadOptions;

public interface ResourceTrackerFactory {
    ResourceTracker create(boolean prefetch, DownloadOptions downloadOptions, UpdatePolicy updatePolicy);
}
