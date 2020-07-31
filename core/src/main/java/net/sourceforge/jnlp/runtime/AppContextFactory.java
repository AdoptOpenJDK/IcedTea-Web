package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.runtime.classloader.DelegatingClassLoader;
import sun.awt.SunToolkit;

public class AppContextFactory {
    public static void createNewAppContext() {
        //set temporary classloader for EventQueue initialization
        //already a call to AppContext.getAppContext(...) initializes the EventQueue.class
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        DelegatingClassLoader delegatingLoader = DelegatingClassLoader.getInstance();
        delegatingLoader.setClassLoader(originalLoader);

        Thread.currentThread().setContextClassLoader(delegatingLoader);

        SunToolkit.createNewAppContext();
        //restore original classloader
        Thread.currentThread().setContextClassLoader(originalLoader);
    }
}
