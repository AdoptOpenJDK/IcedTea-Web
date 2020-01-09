package net.adoptopenjdk.icedteaweb.classloader;

import java.util.concurrent.Future;

public class ClassLoaderUtils {

    public static <V> V waitForCompletion(Future<V> f, String message) {
        try {
            return f.get();
        } catch (final Exception e) {
            throw new RuntimeException(message, e);
        }
    }
}
