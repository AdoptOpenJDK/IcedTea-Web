package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;
import net.sourceforge.jnlp.cache.CacheUtil;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.DOWNLOADED;
import static net.adoptopenjdk.icedteaweb.resources.Resource.Status.ERROR;

class ResourceHandler {

    private static final Executor localExecutor = Executors.newCachedThreadPool();

    private final Resource resource;

    ResourceHandler(Resource resource) {
        this.resource = Assert.requireNonNull(resource, "resource");
    }

    Future<Resource> putIntoCache() {
        final CompletableFuture<Resource> result = new CompletableFuture<>();

        // the thread which is processing this resource will set its future onto the resource all other
        // threads will return this future and ensure a resource is only processed by a single thread
        synchronized (resource) {
            final Future<Resource> futureResource = resource.getFutureThis();
            if (futureResource != null) {
                return futureResource;
            }
            resource.startProcessing(result);
        }

        if (resource.isComplete()) {
            result.complete(resource);
        } else if (isNotCacheable()) {
            result.complete(initNoneCacheableResources());
        } else {
            localExecutor.execute(() -> {
                try {
                    result.complete(download());
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
        }

        return result;
    }

    private boolean isNotCacheable() {
        return !CacheUtil.isCacheable(resource.getLocation());
    }

    private Resource initNoneCacheableResources() {
        resource.setStatus(DOWNLOADED);
        if (resource.getLocation().getProtocol().equals("file")) {
            final File file = new File(resource.getLocation().getPath());
            resource.setSize(file.length());
            resource.setLocalFile(file);
            resource.setTransferred(file.length());
        }
        return resource;
    }

    private Resource download() {
        int triesLeft = 2;
        while (true) {
            try {
                return downloadResource();
            } catch (Exception e) {
                if (--triesLeft <= 0) {
                    resource.setStatus(ERROR);
                    throw e;
                }
            }
        }
    }

    private Resource downloadResource() {
        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }
}
