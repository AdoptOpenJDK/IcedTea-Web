package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

import static java.security.AccessController.doPrivileged;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class ApplicationManager {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationManager.class);

    private static final WeakHashMap<ClassLoader, ApplicationInstance> applicationHolder = new WeakHashMap<>();

    public static Optional<ApplicationInstance> getApplication() {
        return getApplication(Thread.currentThread().getContextClassLoader());
    }

    public static Optional<ApplicationInstance> getApplication(final ClassLoader classLoader) {
        final ApplicationInstance instance = applicationHolder.get(classLoader);
        if (instance != null) {
            return of(instance);
        }

        return getParentOf(classLoader).flatMap(ApplicationManager::getApplication);
    }

    private static Optional<ClassLoader> getParentOf(ClassLoader classLoader) {
        try {
            return ofNullable(doPrivileged((PrivilegedAction<ClassLoader>) classLoader::getParent));
        }
        catch (Exception e) {
            LOG.warn("Exception while getting parent class loader", e);
            return empty();
        }
    }

    public static Optional<ApplicationInstance> getApplication(final JNLPFile file) {
        return applicationHolder.values().stream()
                .filter(instance -> Objects.equals(instance.getJNLPFile(), file))
                .findFirst();
    }

    public static void addApplication(final ApplicationInstance applicationInstance) {
        applicationHolder.put(applicationInstance.getClassLoader(), applicationInstance);
    }
}
