package net.adoptopenjdk.icedteaweb.jdk89access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing jar index access using sun.misc.JarIndex for both jdk8 and jdk9.
 *
 * @author jvanek
 */
public class JarIndexAccess {

    private final static Logger LOG = LoggerFactory.getLogger(JarIndexAccess.class);

    private static Class<?> jarIndexClass;
    /*JarIndex*/
    private final Object parent;

    static {
        try {
            jarIndexClass = Class.forName("sun.misc.JarIndex");
        } catch (ClassNotFoundException ex) {
            try {
                LOG.error("Running jdk9+ ?", ex);
                jarIndexClass = Class.forName("jdk.internal.util.jar.JarIndex");
            } catch (ClassNotFoundException exx) {
                LOG.error("ERROR", exx);
                throw new RuntimeException("JarIndex not found!");
            }
        }
    }

    private JarIndexAccess(final Object parent) {
        if (parent == null) {
            throw new RuntimeException("JarFile can not be null!");
        }
        this.parent = parent;
    }

    public static JarIndexAccess getJarIndex(final JarFile jarFile) throws IOException {
        try {
            return getJarIndexImpl(jarFile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static JarIndexAccess getJarIndexImpl(final JarFile jarFile) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method method = jarIndexClass.getMethod("getJarIndex", JarFile.class);
        final Object o = method.invoke(null, jarFile);
        if (o == null) {
            return null;
        }
        return new JarIndexAccess(o);
    }

    public LinkedList<String> get(final String replace) {
        try {
            return getImpl(replace);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<String> getImpl(final String replace) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Method method = jarIndexClass.getMethod("get", String.class);
        final Object o = method.invoke(parent, replace);
        return (LinkedList<String>) o;
    }
}
