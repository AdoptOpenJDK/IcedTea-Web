package net.sourceforge.jnlp.runtime;

import sun.awt.SunToolkit;

public class AppContextFactory {
    public static void createNewAppContext() {
        //set temporary classloader for EventQueue initialization
        //already a call to AppContext.getAppContext(...) initializes the EventQueue.class
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        try {
            SunToolkit.createNewAppContext();
        } finally {
            //restore original classloader
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }
}
