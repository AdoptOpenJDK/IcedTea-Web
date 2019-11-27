package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.Resource;

import java.net.URL;
import java.util.List;

public interface ResourceDownloader {

    static ResourceDownloader of(Resource resource, List<URL> downloadUrls) {
        final VersionString version = resource.getRequestVersion();
        if (version == null) {
            return new UnversionedResourceDownloader(resource, downloadUrls);
        }
        if (version.isExactVersion()) {
            return new ExactVersionedResourceDownloader(resource, downloadUrls);
        } else {
            return new RangeVersionedResourceDownloader(resource, downloadUrls);
        }
    }

    Resource download();
}
