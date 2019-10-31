package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;
import net.sourceforge.jnlp.cache.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ResourceHandler {

    private static final Executor localExecutor = Executors.newCachedThreadPool();

    private final Resource resource;

    public ResourceHandler(Resource resource) {
        this.resource = Assert.requireNonNull(resource, "resource");
    }

    public Future<Resource> putIntoCache() {
        final CompletableFuture<Resource> result = new CompletableFuture<>();

        if (isAlreadyCompleted()) {
            result.complete(resource);
        } else {
            localExecutor.execute(() -> {
                try {
                    result.complete(downloadResource());
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
        }

        return result;
    }

    private boolean isAlreadyCompleted() {
        synchronized (resource) {
            return resource.isComplete();
        }
    }

    private Resource downloadResource() throws InterruptedException {
        final boolean alreadyBeingProcessed;
        synchronized (resource) {
            alreadyBeingProcessed = resource.isBeingProcessed();
            resource.startProcessing();
        }

        if (alreadyBeingProcessed) {
            return waitForResource();
        }

        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }

    private Resource waitForResource() throws InterruptedException {
        while (true) {
            synchronized (resource) {
                if (resource.isComplete()) {
                    return resource;
                }

                resource.wait();
            }
        }
    }
}
