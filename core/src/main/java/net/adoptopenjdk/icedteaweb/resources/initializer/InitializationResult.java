package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.resources.UrlRequestResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InitializationResult {

    private final List<URL> urls;

    InitializationResult(List<URL> urls) {
        this.urls = Collections.unmodifiableList(new ArrayList<>(urls));
    }

    InitializationResult(URL... urls) {
        this.urls = Collections.unmodifiableList(Arrays.asList(urls));
    }

    InitializationResult(UrlRequestResult requestResult) {
        this.urls = Collections.unmodifiableList(Arrays.asList(requestResult.getLocation()));
    }

    public boolean needsDownload() {
        return !urls.isEmpty();
    }

    public List<URL> getDownloadUrls() {
        return urls;
    }
}
