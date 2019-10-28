package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.cache.Resource;

import java.net.URL;
import java.util.List;

/**
 * ...
 */
class UnversiondResourceDownloader extends BaseResourceDownloader {
    UnversiondResourceDownloader(Resource resource, List<URL> downloadUrls) {
        super(resource, downloadUrls);
    }

    @Override
    protected VersionId getVersion(URL downloadFrom, String versionHeaderValue) {
        return null;
    }

    @Override
    protected boolean isUpToDate(URL resourceHref, VersionId version, long lastModified) {
        return Cache.isUpToDate(resourceHref, null, lastModified);
    }
}
