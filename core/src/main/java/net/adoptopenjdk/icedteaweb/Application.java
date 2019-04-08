package net.adoptopenjdk.icedteaweb;

import java.util.concurrent.CompletableFuture;

public interface Application {

    CompletableFuture<Process> start();

}
