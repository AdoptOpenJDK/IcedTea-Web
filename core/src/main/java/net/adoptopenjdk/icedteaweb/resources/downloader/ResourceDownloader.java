package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.sourceforge.jnlp.cache.Resource;

import java.net.URL;
import java.util.List;

public interface ResourceDownloader {

    static ResourceDownloader of(Resource resource, List<URL> downloadUrls) {
        if (resource.getRequestVersion() == null) {
            return new UnversiondResourceDownloader(resource, downloadUrls);
        }
        throw new RuntimeException("Not implemented yet!");
    }

    Resource download();
}
