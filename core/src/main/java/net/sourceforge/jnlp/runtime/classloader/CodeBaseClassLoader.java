package net.sourceforge.jnlp.runtime.classloader;

import net.sourceforge.jnlp.NullJnlpFileException;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ...
 */ /*
 * Helper class to expose protected URLClassLoader methods.
 * Classes loaded from the codebase are absolutely NOT signed, by definition!
 * If the CodeBaseClassLoader is used to load any classes in JNLPClassLoader,
 * then you *MUST* check if the JNLPClassLoader is set to FULL signing. If so,
 * then it must be set instead to PARTIAL, and the user prompted if it is okay
 * to proceed. If the JNLPClassLoader is already PARTIAL or NONE signing, then
 * nothing must be done. This is required so that we can support partial signing
 * of applets but also ensure that using codebase loading in conjunction with
 * signed JARs still results in the user having to confirm that this is
 * acceptable.
 */
public class CodeBaseClassLoader extends URLClassLoader {

    private final JNLPClassLoader parentJNLPClassLoader;

    /**
     * Classes that are not found, so that findClass can skip them next time
     */
    private final ConcurrentHashMap<String, URL[]> notFoundResources = new ConcurrentHashMap<>();

    CodeBaseClassLoader(URL[] urls, JNLPClassLoader cl) {
        super(urls, cl);
        parentJNLPClassLoader = cl;
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /*
     * Use with care! Check the class-level Javadoc before calling this.
     */
    Class<?> findClassNonRecursive(final String name) throws ClassNotFoundException {
        // If we have searched this path before, don't try again
        if (Arrays.equals(super.getURLs(), notFoundResources.get(name))) {
            throw new ClassNotFoundException(name);
        }

        try {
            return AccessController.doPrivileged(
                    (PrivilegedExceptionAction<Class<?>>) () -> {
                        Class<?> c = CodeBaseClassLoader.super.findClass(name);
                        parentJNLPClassLoader.checkPartialSigningWithUser();
                        return c;
                    }, parentJNLPClassLoader.getAccessControlContextForClassLoading());
        } catch (PrivilegedActionException | NullJnlpFileException pae) {
            notFoundResources.put(name, super.getURLs());
            throw new ClassNotFoundException("Could not find class " + name, pae);
        }
    }

    /*
     * Use with care! Check the class-level Javadoc before calling this.
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // Calls JNLPClassLoader#findClass which may call into this.findClassNonRecursive
        Class<?> c = getParentJNLPClassLoader().findClass(name);
        parentJNLPClassLoader.checkPartialSigningWithUser();
        return c;
    }

    /**
     * Returns the output of super.findLoadedClass().
     * <p>
     * The method is renamed because ClassLoader.findLoadedClass() is final
     *
     * @param name The name of the class to find
     * @return Output of ClassLoader.findLoadedClass() which is the class if
     * found, null otherwise
     * @see ClassLoader#findLoadedClass(String)
     */
    Class<?> findLoadedClassFromParent(String name) {
        return findLoadedClass(name);
    }

    /**
     * Returns JNLPClassLoader that encompasses this loader
     *
     * @return parent JNLPClassLoader
     */
    public JNLPClassLoader getParentJNLPClassLoader() {
        return parentJNLPClassLoader;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {

        // If we have searched this path before, don't try again
        if (Arrays.equals(super.getURLs(), notFoundResources.get(name))) {
            return (new Vector<URL>(0)).elements();
        }

        if (!name.startsWith(ClassLoaderConstants.META_INF)) {
            Enumeration<URL> urls = super.findResources(name);

            if (!urls.hasMoreElements()) {
                notFoundResources.put(name, super.getURLs());
            }

            return urls;
        }

        return (new Vector<URL>(0)).elements();
    }

    @Override
    public URL findResource(String name) {

        // If we have searched this path before, don't try again
        if (Arrays.equals(super.getURLs(), notFoundResources.get(name))) {
            return null;
        }

        URL url = null;
        if (!name.startsWith(ClassLoaderConstants.META_INF)) {
            try {
                final String fName = name;
                url = AccessController.doPrivileged(
                        (PrivilegedExceptionAction<URL>) () -> CodeBaseClassLoader.super.findResource(fName), parentJNLPClassLoader.getAccessControlContextForClassLoading());
            } catch (PrivilegedActionException ignored) {
            }

            if (url == null) {
                notFoundResources.put(name, super.getURLs());
            }

            return url;
        }

        return null;
    }
}
