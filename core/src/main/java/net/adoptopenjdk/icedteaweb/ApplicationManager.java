package net.adoptopenjdk.icedteaweb;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Facade for the application manager.
 */
public interface ApplicationManager {

    CompletableFuture<Application> install(URL url);

    CompletableFuture<Void> remove(Application application);

    CompletableFuture<Void> clear();

    boolean contains(Application application);

    List<Application> getInstalledApplications();
}
