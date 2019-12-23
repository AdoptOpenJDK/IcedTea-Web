package net.sourceforge.jnlp.runtime.classloader2;

import java.util.concurrent.Future;

public class ClassLoaderUtils {

    public static void waitForCompletion(Future<Void> f, String message) {
        try {
            f.get();
        } catch (final Exception e) {
            throw new RuntimeException(message, e);
        }
    }
}
