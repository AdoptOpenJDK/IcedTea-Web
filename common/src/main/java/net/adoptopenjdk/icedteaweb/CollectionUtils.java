package net.adoptopenjdk.icedteaweb;

import java.util.Collection;

/**
 * Helper methods around collections and arrays.
 */
public class CollectionUtils {

    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNullOrEmpty(Object[] a) {
        return a == null || a.length == 0;
    }
}
