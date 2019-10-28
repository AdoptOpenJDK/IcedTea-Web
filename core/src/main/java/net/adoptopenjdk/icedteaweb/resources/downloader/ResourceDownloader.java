package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.resources.DownloadResult;
import net.sourceforge.jnlp.cache.Resource;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ResourceDownloader {

    static ResourceDownloader of(Resource resource, List<URL> downloadUrls) {
        throw new RuntimeException("Not implemented yet!");
    }

    CompletableFuture<DownloadResult> download();
}
