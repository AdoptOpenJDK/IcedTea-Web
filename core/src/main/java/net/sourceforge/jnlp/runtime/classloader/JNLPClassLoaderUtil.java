package net.sourceforge.jnlp.runtime.classloader;

import net.sourceforge.jnlp.runtime.ApplicationInstance;

public class JNLPClassLoaderUtil {

    /**
     * Return the current Application, or null.
     */
    public static ApplicationInstance getApplication(Thread thread, Class<?>[] stack, int maxDepth) {
        ClassLoader cl;
        JNLPClassLoader jnlpCl;

        cl = thread.getContextClassLoader();
        while (cl != null) {
            jnlpCl = getJnlpClassLoader(cl);
            if (jnlpCl != null && jnlpCl.getApplication() != null) {
                return jnlpCl.getApplication();
            }
            cl = cl.getParent();
        }

        if (maxDepth <= 0) {
            maxDepth = stack.length;
        }

        // this needs to be tightened up
        for (int i = 0; i < stack.length && i < maxDepth; i++) {
            cl = stack[i].getClassLoader();
            while (cl != null) {
                jnlpCl = getJnlpClassLoader(cl);
                if (jnlpCl != null && jnlpCl.getApplication() != null) {
                    return jnlpCl.getApplication();
                }
                cl = cl.getParent();
            }
        }
        return null;
    }

    private static JNLPClassLoader getJnlpClassLoader(ClassLoader cl) {
        // Since we want to deal with JNLPClassLoader, extract it if this
        // is a codebase loader
        if (cl instanceof CodeBaseClassLoader) {
            cl = ((CodeBaseClassLoader) cl).getParentJNLPClassLoader();
        }

        if (cl instanceof JNLPClassLoader) {
            JNLPClassLoader loader = (JNLPClassLoader) cl;
            return loader;
        }

        return null;
    }
}
