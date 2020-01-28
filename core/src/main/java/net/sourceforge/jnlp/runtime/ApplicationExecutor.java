package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Runs the JNLP application in a child thread group of the {@code mainGroup}.
 */
public class ApplicationExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExecutor.class);

    private static final ThreadGroup mainGroup = new ThreadGroup("Application-Threads-Group");

    public CompletableFuture<ApplicationInstance> execute(final String applicationTitle, final Supplier<ApplicationInstance> launchApplication) {
        Assert.requireNonNull(launchApplication, "launchApplication");

        final ThreadFactory applicationThreadFactory = createThreadFactory(applicationTitle);
        final ExecutorService applicationExecutor = Executors.newCachedThreadPool(applicationThreadFactory);
        return CompletableFuture.supplyAsync(launchApplication, applicationExecutor);

    }

    private ThreadFactory createThreadFactory(final String applicationTitle) {
        return new ThreadFactory() {

            private final AtomicLong counter = new AtomicLong(0);

            private final ThreadGroup group = new ThreadGroup(mainGroup, applicationTitle);

            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(group, "Application-" + applicationTitle + "-thread-" + counter.incrementAndGet());
                thread.setUncaughtExceptionHandler((t, e) -> LOG.error("Error in application thread for app '" + applicationTitle + "'", e));
                return thread;
            }
        };
    }
}
