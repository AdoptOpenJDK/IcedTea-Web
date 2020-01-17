package net.sourceforge.jnlp.runtime;

import java.util.WeakHashMap;

public class ApplicationManager {

    private static final WeakHashMap<ClassLoader, ApplicationInstance> applicationHolder = new WeakHashMap<>();

    public static ApplicationInstance getApplication() {
        return getApplication(Thread.currentThread().getContextClassLoader());
    }

    public static ApplicationInstance getApplication(final ClassLoader classLoader) {
        final ApplicationInstance instance = applicationHolder.get(classLoader);
        if(instance == null) {
            final ClassLoader parentClassloader = classLoader.getParent();
            if(parentClassloader != null) {
                return getApplication(parentClassloader);
            } else {
                return null;
            }
        }
        return instance;
    }

    public static void addApplication(final ApplicationInstance applicationInstance) {
        applicationHolder.put(applicationInstance.getClassLoader(), applicationInstance);
    }
}
