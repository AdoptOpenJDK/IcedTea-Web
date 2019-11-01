package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;

import java.net.URL;
import java.util.List;

/**
 * ...
 */
class RangeVersionedResourceDownloader extends BaseResourceDownloader {

    RangeVersionedResourceDownloader(Resource resource, List<URL> downloadUrls) {
        super(resource, downloadUrls);
    }

    @Override
    protected VersionId getVersion(URL downloadFrom, String versionHeaderValue) {
        if (versionHeaderValue != null) {
            final VersionId versionId = VersionId.fromString(versionHeaderValue);
            if (resource.getRequestVersion().contains(versionId)) {
                return versionId;
            }
        }

        throw new IllegalStateException("could not determine the version-id from the response of " + downloadFrom + " -> " + versionHeaderValue);
    }

    @Override
    protected boolean isUpToDate(URL resourceHref, VersionId version, long lastModified) {
        final boolean cached = Cache.isCached(resourceHref, version);
        if (cached && resource.forceUpdateRequested()) {
            invalidateExistingEntryInCache(version);
            return false;
        }
        return cached;
    }
}
