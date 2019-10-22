package net.adoptopenjdk.icedteaweb.resources;

import net.sourceforge.jnlp.cache.Resource;

import java.util.concurrent.CompletableFuture;

public interface ResourceInitializer {

    static ResourceInitializer of(final Resource resource) {
        return null;
    }

    CompletableFuture<InitializationResult> init();
}
