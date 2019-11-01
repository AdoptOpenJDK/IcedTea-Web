package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.resources.downloader.ResourceDownloader;
import net.adoptopenjdk.icedteaweb.resources.initializer.InitializationResult;
import net.adoptopenjdk.icedteaweb.resources.initializer.ResourceInitializer;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.Resource;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.sourceforge.jnlp.cache.Resource.Status.DOWNLOADED;

public class ResourceHandler {

    private static final Executor localExecutor = Executors.newCachedThreadPool();

    private final Resource resource;

    public ResourceHandler(Resource resource) {
        this.resource = Assert.requireNonNull(resource, "resource");
    }

    public Future<Resource> putIntoCache() {
        final CompletableFuture<Resource> result = new CompletableFuture<>();

        initNoneCacheableResources();

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

    private void initNoneCacheableResources() {
        if (!CacheUtil.isCacheable(resource.getLocation())) {
            resource.changeStatus(null, EnumSet.of(DOWNLOADED));

            synchronized (resource) {
                if (resource.isComplete()) {
                    return;
                }

                if (resource.getLocation().getProtocol().equals("file")) {
                    final File file = new File(resource.getLocation().getPath());
                    resource.setSize(file.length());
                    resource.setLocalFile(file);
                    resource.setTransferred(file.length());
                }
                resource.changeStatus(null, EnumSet.of(DOWNLOADED));
                resource.notifyAll();
            }
        }
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
