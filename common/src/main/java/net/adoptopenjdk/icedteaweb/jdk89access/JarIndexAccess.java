package net.adoptopenjdk.icedteaweb.jdk89access;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.jar.JarFile;

/**
 * Class providing jar index access using sun.misc.JarIndex for both jdk8 and jdk9.
 *
 * @author jvanek
 */
public class JarIndexAccess {

    private static final Logger LOG = LoggerFactory.getLogger(JarIndexAccess.class);

    private static final String CLASS_SUN_MISC_JAR_INDEX = "sun.misc.JarIndex";
    private static final String CLASS_JDK_INTERNAL_UTIL_JAR_JAR_INDEX = "jdk.internal.util.jar.JarIndex";
    private static final String METHOD_GET_JAR_INDEX = "getJarIndex";
    private static final String METHOD_GET = "get";

    private static Class<?> jarIndexClass;
    /*JarIndex*/
    private final Object parent;


    static {
        try {
            jarIndexClass = Class.forName(CLASS_SUN_MISC_JAR_INDEX);
        } catch (ClassNotFoundException ex) {
            try {
                LOG.debug(CLASS_SUN_MISC_JAR_INDEX + " not found - Running jdk9+ ?");
                jarIndexClass = Class.forName(CLASS_JDK_INTERNAL_UTIL_JAR_JAR_INDEX);
            } catch (ClassNotFoundException exx) {
                LOG.debug(CLASS_JDK_INTERNAL_UTIL_JAR_JAR_INDEX + " not found");
                jarIndexClass = JarIndexDummy.class;
            }
        }
    }

    private JarIndexAccess(final Object parent) {
        if (parent == null) {
            throw new RuntimeException("JarFile can not be null!");
        }
        this.parent = parent;
    }

    public static JarIndexAccess getJarIndex(final JarFile jarFile) {
        try {
            return getJarIndexImpl(jarFile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static JarIndexAccess getJarIndexImpl(final JarFile jarFile) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method method = jarIndexClass.getMethod(METHOD_GET_JAR_INDEX, JarFile.class);
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

    @SuppressWarnings("unchecked")
    private LinkedList<String> getImpl(final String replace) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Method method = jarIndexClass.getMethod(METHOD_GET, String.class);
        final Object o = method.invoke(parent, replace);
        return (LinkedList<String>) o;
    }

    /**
     * Because starting of JAVA 21 the JarIndex is no longer supported we provide a dummy implementation which always returns {@code null}.
     */
    @SuppressWarnings("unused")
    private static class JarIndexDummy {
        public static Object getJarIndex(JarFile jar) {
            return null;
        }

        public Object get(String fileName) {
            return null;
        }
    }
}
