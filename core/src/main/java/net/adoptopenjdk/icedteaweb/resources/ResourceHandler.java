package net.adoptopenjdk.icedteaweb.resources;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.cache.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);

    private static final Executor localExecutor = Executors.newCachedThreadPool();
    private static final Executor remoteExecutor = Executors.newFixedThreadPool(6);

    private final ResourceCache cache;

    public ResourceHandler(final ResourceCache cache) {
        this.cache = Assert.requireNonNull(cache, "cache");
    }

    public Future<Resource> getResource(final Resource resource) {
        Assert.requireNonNull(resource, "resource");

        final CompletableFuture<Resource> result = new CompletableFuture<>();

        if (isAlreadyCompleted(resource)) {
            result.complete(resource);
        } else {
            localExecutor.execute(() -> result.complete(downloadResource(resource)));
        }

        return result;
    }

    private boolean isAlreadyCompleted(Resource resource) {
        synchronized (resource) {
            return resource.isComplete();
        }
    }

    private Resource downloadResource(final Resource resource) {
        final boolean alreadyBeingProcessed;
        synchronized (resource) {
            alreadyBeingProcessed = resource.isBeingProcessed();
            resource.startProcessing();
        }

        if (alreadyBeingProcessed) {
            return waitForResource(resource);
        }

        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult initResult = initializer.init();
        if (initResult.needsDownload()) {
            final ResourceDownloader downloader = ResourceDownloader.of(resource, initResult.getDownloadUrls());
            downloader.download();
        }
        return resource;
    }

    private Resource waitForResource(Resource resource) {
        throw new RuntimeException("Not implemented yet!");
    }

    private Object oldCode(final Resource resource) {

        final ResourceInitializer initializer = ResourceInitializer.of(resource);
        final InitializationResult result = initializer.init();
        return result.handle((r, e) -> {
            if (e != null) {
                throw new RuntimeException("Error in initialization of resource", e);
            }
            if (r.needsDownload()) {
                final ResourceDownloader downloader = ResourceDownloader.of(resource, r.getDownloadUrls());
                try {
                    downloader.download().get();
                } catch (Exception ex) {
                    throw new RuntimeException("Error in download of resource", ex);
                }
            }
            return null;
        });


        final CompletableFuture<HResource> completableFuture = new CompletableFuture<>();
        localExecutor.execute(() -> {
            try {
                LOG.info("Searching for resource based on definition '{}' in cache", definition);
                final Optional<HResource> resourceFromCache = cache.getResource(definition);
                if (resourceFromCache.isPresent() && isPerfectMatch(definition, resourceFromCache.get())) {
                    LOG.info("Found useable version of resource for definition '{}' in cache", definition);
                    completableFuture.complete(resourceFromCache.get());
                } else {
                    try {
                        final List<ResourceRemoteEndpoint> possibleEndpoints = getPossibleEndpoints(definition);
                        final Optional<ResourceRemoteEndpointHeadResult> headResult = getBestResult(possibleEndpoints);

                        if (headResult.isPresent()) {
                            LOG.info("Will try to updated resource for definition '{}' from endpoint '{}'", definition, headResult.get());
                            final HResource downloadedResource = download(headResult.get().getEndpoint()).get();
                            LOG.info("Will store updated resource '{}' for definition '{}' from endpoint '{}' in cache", downloadedResource, definition, headResult.get());
                            final HResource storedResource = cache.store(downloadedResource);
                            completableFuture.complete(storedResource);
                        }

                    } catch (final Exception e) {
                        LOG.error("Error while downloading updated resource", e);
                    }
                    if (!completableFuture.isDone()) {
                        if (resourceFromCache.isPresent()) {
                            LOG.info("Will use resource from cache for definition '{}'", definition);
                            completableFuture.complete(resourceFromCache.get());
                        } else {
                            LOG.warn("No resource found for definition '{}'", definition);
                            completableFuture.completeExceptionally(new RuntimeException("Can not provide resource!"));
                        }
                    }
                }
            } catch (final Exception e) {
                LOG.error("Error while trying to get resource", e);
                completableFuture.completeExceptionally(new RuntimeException("Can not provide resource!", e));
            }
        });
        return completableFuture;
    }

    private Optional<ResourceRemoteEndpointHeadResult> getBestResult(final List<ResourceRemoteEndpoint> possibleEndpoints) {
        Assert.requireNonNull(possibleEndpoints, "possibleEndpoints");
        return possibleEndpoints.stream()
                .map(e -> callHead(e))
                .map(f -> {
                    try {
                        return f.get(5, TimeUnit.SECONDS);
                    } catch (final Exception e) {
                        return ResourceRemoteEndpointHeadResult.fail(e);
                    }
                }).filter(r -> r.isSucessfull())
                // .sorted()
                .findFirst();
    }

    private Future<ResourceRemoteEndpointHeadResult> callHead(final ResourceRemoteEndpoint endpoint) {
        final CompletableFuture<ResourceRemoteEndpointHeadResult> result = new CompletableFuture<>();

        remoteExecutor.execute(() -> {
            //TODO
        });

        return result;
    }

    private Future<HResource> download(final ResourceRemoteEndpoint endpoint) {
        final CompletableFuture<HResource> result = new CompletableFuture<>();

        remoteExecutor.execute(() -> {
            //TODO
        });

        return result;
    }

    private List<ResourceRemoteEndpoint> getPossibleEndpoints(final ResourceDefinition definition) {
        return Collections.emptyList();
    }

    public boolean isPerfectMatch(final ResourceDefinition definition, final HResource resource) {
        return false;
    }
}
