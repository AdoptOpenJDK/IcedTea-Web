package net.adoptopenjdk.icedteaweb.resources;

import net.sourceforge.jnlp.DownloadOptions;

public class DefaultResourceTrackerFactory implements ResourceTrackerFactory{
    public ResourceTracker create(boolean prefetch, DownloadOptions downloadOptions, UpdatePolicy updatePolicy) {
        return new DefaultResourceTracker(prefetch, downloadOptions, updatePolicy);
    }
}
