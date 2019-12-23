package net.sourceforge.jnlp.runtime.classloader2;

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
