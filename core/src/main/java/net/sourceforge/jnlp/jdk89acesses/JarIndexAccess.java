package net.sourceforge.jnlp.jdk89acesses;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.jar.JarFile;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Class to access sun.misc.JarINdex for both jdk9 and 8.
 *
 * @author jvanek
 */
public class JarIndexAccess {

    private static Class<?> jarIndexClass;
    /*JarIndex*/
    private final Object parent;

    static {
        try {
            jarIndexClass = Class.forName("sun.misc.JarIndex");
        } catch (ClassNotFoundException ex) {
            try {
                OutputController.getLogger().log(ex);
                OutputController.getLogger().log("Running jdk9+ ?");
                jarIndexClass = Class.forName("jdk.internal.util.jar.JarIndex");
            } catch (ClassNotFoundException exx) {
                OutputController.getLogger().log(exx);
                throw new RuntimeException("JarIndex not found!");
            }
        }
    }

    private JarIndexAccess(Object parent) {
        if (parent == null) {
            throw new RuntimeException("JarFile can notbe null!");
        }
        this.parent = parent;
    }

    public static JarIndexAccess getJarIndex(JarFile jarFile) throws IOException {
        try {
            return getJarIndexImpl(jarFile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static JarIndexAccess getJarIndexImpl(JarFile jarFile) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = jarIndexClass.getMethod("getJarIndex", JarFile.class);
        Object o = method.invoke(null, jarFile);
        if (o == null) {
            return null;
        }
        return new JarIndexAccess(o);
    }

    public LinkedList<String> get(String replace) {
        try {
            return getImpl(replace);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<String> getImpl(String replace) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = jarIndexClass.getMethod("get", String.class);
        Object o = method.invoke(parent, replace);
        return (LinkedList<String>) o;
    }

}
