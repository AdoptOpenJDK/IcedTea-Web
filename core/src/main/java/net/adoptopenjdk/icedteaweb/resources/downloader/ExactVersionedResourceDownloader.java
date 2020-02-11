package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.net.URL;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_PREFIX;

/**
 * ...
 */
class ExactVersionedResourceDownloader extends BaseResourceDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(ExactVersionedResourceDownloader.class);

    private final VersionId versionId;

    ExactVersionedResourceDownloader(Resource resource, List<URL> downloadUrls) {
        super(resource, downloadUrls);
        versionId = VersionString.fromString(resource.getRequestVersion().toString()).getExactVersion();
    }

    @Override
    protected VersionId getVersion(URL downloadFrom, String versionHeaderValue) {
        if (versionHeaderValue != null && versionId.equals(VersionId.fromString(versionHeaderValue))) {
            return versionId;
        }

        if (downloadFrom.getPath().contains(VERSION_PREFIX + versionId.toString())) {
            return versionId;
        }

        LOG.warn("could not determine the version-id from the response of {} -> {}", downloadFrom, versionHeaderValue);
        return null;
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
