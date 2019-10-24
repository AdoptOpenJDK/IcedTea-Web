package net.adoptopenjdk.icedteaweb.resources;

import net.sourceforge.jnlp.cache.UrlRequestResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class InitializationResult {

    private final List<URL> urls;
    private final Optional<UrlRequestResult> requestResult;

    InitializationResult(List<URL> urls) {
        this.urls = Collections.unmodifiableList(new ArrayList<>(urls));
        this.requestResult = Optional.empty();
    }

    InitializationResult(URL... urls) {
        this.urls = Collections.unmodifiableList(Arrays.asList(urls));
        this.requestResult = Optional.empty();
    }

    InitializationResult(UrlRequestResult requestResult) {
        this.urls = Collections.unmodifiableList(Arrays.asList(requestResult.getLocation()));
        this.requestResult = Optional.of(requestResult);
    }

    boolean needsDownload() {
        return !urls.isEmpty();
    }

    List<URL> getDownloadUrls() {
        return urls;
    }
}
