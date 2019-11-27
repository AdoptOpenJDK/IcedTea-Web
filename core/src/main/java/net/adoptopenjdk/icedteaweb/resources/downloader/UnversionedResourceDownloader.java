package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.net.URL;
import java.util.List;

/**
 * ...
 */
class UnversionedResourceDownloader extends BaseResourceDownloader {
    UnversionedResourceDownloader(Resource resource, List<URL> downloadUrls) {
        super(resource, downloadUrls);
    }

    @Override
    protected VersionId getVersion(URL downloadFrom, String versionHeaderValue) {
        return null;
    }

    @Override
    protected boolean isUpToDate(URL resourceHref, VersionId version, long lastModified) {
        final boolean upToDate = Cache.isUpToDate(resourceHref, null, lastModified);
        if (upToDate && resource.forceUpdateRequested()) {
            invalidateExistingEntryInCache(null);
            return false;
        }
        return upToDate;
    }
}
