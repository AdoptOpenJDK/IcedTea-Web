package net.adoptopenjdk.icedteaweb.classloader;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClassLoaderUtils {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    public static Executor getClassloaderBackgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static <V> V waitForCompletion(Future<V> f, String message) {
        try {
            return f.get();
        } catch (final Exception e) {
            throw new RuntimeException(message, e);
        }
    }
}
