package net.adoptopenjdk.icedteaweb.resources;

import java.util.concurrent.CompletableFuture;

public interface ResourceDownloader {

    CompletableFuture<DownloadResult> download();
}
