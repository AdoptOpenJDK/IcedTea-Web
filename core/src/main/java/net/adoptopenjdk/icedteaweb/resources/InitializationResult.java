package net.adoptopenjdk.icedteaweb.resources;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class InitializationResult {

    public boolean needsDownload() {
        return false;
    }

    public List<URL> getDownloadUrls() {
        return Collections.emptyList();
    }
}
