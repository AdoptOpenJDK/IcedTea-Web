package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.cache.Resource;
import net.sourceforge.jnlp.cache.ResourceUrlCreator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.sourceforge.jnlp.cache.ResourceUrlCreator.getUrl;
import static net.sourceforge.jnlp.cache.ResourceUrlCreator.getVersionedUrl;

/**
 * ...
 */
abstract class VersionedResourceInitializer extends BaseResourceInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(VersionedResourceInitializer.class);

    VersionedResourceInitializer(Resource resource) {
        super(resource);
    }

    List<URL> getUrlCandidates() {
        final List<URL> candidates = new ArrayList<>();

        final DownloadOptions downloadOptions = getDownloadOptions();
        final boolean usePack = downloadOptions.useExplicitPack();
        final boolean useVersion = downloadOptions.useExplicitVersion();

        final URL packAndVersionUrl = getUrl(resource, usePack, useVersion);
        if (packAndVersionUrl != null) {
            candidates.add(packAndVersionUrl);
        }

        final URL versionUrl = getUrl(resource, false, useVersion);
        if (versionUrl != null) {
            candidates.add(versionUrl);
        }

        final URL packUrl = getUrl(resource, usePack, false);
        final URL versionedPackUrl = getVersionedUrl(packUrl, resource.getRequestVersion());
        if (versionedPackUrl != null) {
            candidates.add(versionedPackUrl);
        }

        final URL versionedUrl = getVersionedUrl(resource.getLocation(), resource.getRequestVersion());
        if (versionedUrl != null) {
            candidates.add(versionedUrl);
        }

        if (candidates.isEmpty()) {
            LOG.error("Failed to find a candidate URL for {}", resource);
        }

        return ResourceUrlCreator.prependHttps(candidates);
    }

}
