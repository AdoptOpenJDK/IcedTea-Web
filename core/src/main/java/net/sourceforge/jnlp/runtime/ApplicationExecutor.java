package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class ApplicationExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExecutor.class);

    private static final ThreadGroup mainGroup = new ThreadGroup("Application-Threads-Group");

    public CompletableFuture<ApplicationInstance> execute(final JNLPFile file, final Function<JNLPFile, ApplicationInstance> appFactoryFunction) {
        Assert.requireNonNull(file, "file");
        Assert.requireNonNull(appFactoryFunction, "appFactoryFunction");

        final ThreadFactory applicationThreadFactory = createThreadFactory(file);
        final ExecutorService applicationExecutor = Executors.newCachedThreadPool(applicationThreadFactory);
        return CompletableFuture.supplyAsync(() -> appFactoryFunction.apply(file), applicationExecutor);

    }

    private ThreadFactory createThreadFactory(final JNLPFile file) {
        return new ThreadFactory() {

            private final AtomicLong counter = new AtomicLong(0);

            private final ThreadGroup group = new ThreadGroup(mainGroup, file.getTitle());

            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(group, "Application-" + file.getTitle() + "-thread-" + counter.incrementAndGet());
                thread.setUncaughtExceptionHandler((t, e) -> LOG.error("Error in application thread for app '" + file.getTitle() + "'", e));
                return thread;
            }
        };
    }
}
