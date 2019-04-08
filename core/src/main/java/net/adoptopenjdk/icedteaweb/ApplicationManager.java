package net.adoptopenjdk.icedteaweb;

import dev.rico.core.functional.Subscription;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Facade for the application manager.
 */
public interface ApplicationManager {

    Subscription addChangeListener(Consumer<List<Application>> changeListener);

    CompletableFuture<Application> install(URL url);

    CompletableFuture<Void> remove(Application application);

    CompletableFuture<Void> clear();

    boolean contains(Application application);

    List<Application> getInstalledApplications();
}
