/*
 * Copyright 2012 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sourceforge.jnlp;

import net.sourceforge.jnlp.runtime.AppletInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.sourceforge.jnlp.splashscreen.SplashController;
import net.sourceforge.jnlp.splashscreen.SplashPanel;
import net.sourceforge.jnlp.splashscreen.SplashUtils;

import sun.applet.AppletViewerPanel;
import sun.awt.SunToolkit;

/**
 * This panel calls into netx to run an applet, and pipes the display
 * into a panel from gcjwebplugin.
 *
 * @author      Francis Kung <fkung@redhat.com>
 */
public class NetxPanel extends AppletViewerPanel implements SplashController {
    private final PluginParameters parameters;
    private PluginBridge bridge = null;
    private boolean exitOnFailure = true;
    private AppletInstance appInst = null;
    private SplashController splashController;
    private boolean appletAlive;

    // We use this so that we can create exactly one thread group
    // for all panels with the same uKey.
    private static final Map<String, ThreadGroup> uKeyToTG =
        new HashMap<String, ThreadGroup>();
    private static final Object TGMapMutex = new Object();

    // This map is actually a set (unfortunately there is no ConcurrentSet
    // in java.util.concurrent). If KEY is in this map, then we know that
    // an app context has been created for the panel that has uKey.equals(KEY),
    // so we avoid creating it a second time for panels with the same uKey.
    // Because it's a set, only the keys matter. However, we can't insert
    // null values in because if we did, we couldn't use null checks to see
    // if a key was absent before a putIfAbsent. 
    private static final ConcurrentMap<String, Boolean> appContextCreated =
        new ConcurrentHashMap<String, Boolean>();

    public NetxPanel(URL documentURL, PluginParameters params) {
        super(documentURL, params.getUnderlyingHashtable());

        this.parameters = params;

        String uniqueKey = params.getUniqueKey();
        synchronized(TGMapMutex) {
            if (!uKeyToTG.containsKey(uniqueKey)) {
                ThreadGroup tg = new ThreadGroup(Launcher.mainGroup, this.documentURL.toString());
                uKeyToTG.put(uniqueKey, tg);
            }
        }
    }

    // overloaded constructor, called when initialized via plugin
    public NetxPanel(URL documentURL, PluginParameters params,
                     boolean exitOnFailure) {
        this(documentURL, params);
        this.exitOnFailure = exitOnFailure;
        this.appletAlive = true;
    }

    @Override
    protected void showAppletException(Throwable t) {
        /*
         * Log any exceptions thrown while loading, initializing, starting,
         * and stopping the applet. 
         */
        AppletLog.log(t);
        super.showAppletException(t);
    }

    //Overriding to use Netx classloader. You might need to relax visibility
    //in sun.applet.AppletPanel for runLoader().
    @Override
    protected void runLoader() {

        try {
            bridge = new PluginBridge(baseURL,
                                getDocumentBase(),
                                getJarFiles(),
                                getCode(),
                                getWidth(),
                                getHeight(),
                                parameters);

            doInit = true;
            dispatchAppletEvent(APPLET_LOADING, null);
            status = APPLET_LOAD;

            Launcher l = new Launcher(exitOnFailure);

            try {
                appInst = (AppletInstance) l.launch(bridge, this);
            } catch (LaunchException e) {
                // Assume user has indicated he does not trust the
                // applet.
                if (exitOnFailure)
                    System.exit(1);
            }
            applet = appInst.getApplet();

            //On the other hand, if you create an applet this way, it'll work
            //fine. Note that you might to open visibility in sun.applet.AppletPanel
            //for this to work (the loader field, and getClassLoader).
            //loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
            //applet = createApplet(loader);

            // This shows that when using NetX's JNLPClassLoader, keyboard input
            // won't make it to the applet, whereas using sun.applet.AppletClassLoader
            // works just fine.

            if (applet != null) {
                // Stick it in the frame
                applet.setStub(this);
                applet.setVisible(false);
                add("Center", applet);
                showAppletStatus("loaded");
                validate();
            }
        } catch (Exception e) {
            this.appletAlive = false;
            e.printStackTrace();
            replaceSplash(SplashUtils.getErrorSplashScreen(getWidth(), getHeight(), e));
        } finally {
            // PR1157: This needs to occur even in the case of an exception
            // so that the applet's event listeners are signaled.
            // Once PluginAppletViewer.AppletEventListener is signaled PluginAppletViewer it can properly stop waiting
            // in PluginAppletViewer.waitForAppletInit
            dispatchAppletEvent(APPLET_LOADING_COMPLETED, null);
        }
    }

    /**
     * Creates a new Thread (in a new applet-specific ThreadGroup) for running
     * the applet
     */
    // Reminder: Relax visibility in sun.applet.AppletPanel
    @Override
    protected synchronized void createAppletThread() {
        // initialize JNLPRuntime in the main threadgroup
        synchronized (JNLPRuntime.initMutex) {
            //The custom NetX Policy and SecurityManager are set here.
            if (!JNLPRuntime.isInitialized()) {
                if (JNLPRuntime.isDebug())
                    System.out.println("initializing JNLPRuntime...");

                JNLPRuntime.initialize(false);
            } else {
                if (JNLPRuntime.isDebug())
                    System.out.println("JNLPRuntime already initialized");
            }
        }

        handler = new Thread(getThreadGroup(), this);
        handler.start();
    }

    public void updateSizeInAtts(int height, int width) {
        parameters.updateSize(width, height);
    }

    public ClassLoader getAppletClassLoader() {
        return appInst.getClassLoader();
    }

    public boolean isAlive() {
        return handler != null && handler.isAlive() && this.appletAlive;
    }

    public ThreadGroup getThreadGroup() {
        synchronized(TGMapMutex) {
            return uKeyToTG.get(parameters.getUniqueKey());
        }
    }

    public void createNewAppContext() {
        if (Thread.currentThread().getThreadGroup() != getThreadGroup()) {
            throw new RuntimeException("createNewAppContext called from the wrong thread.");
        }
        // only create a new context if one hasn't already been created for the
        // applets with this unique key.
        if (null == appContextCreated.putIfAbsent(parameters.getUniqueKey(), Boolean.TRUE)) {
            SunToolkit.createNewAppContext();
        }
    }

    
   

    public void setAppletViewerFrame(SplashController framePanel) {
        splashController=framePanel;
    }

    @Override
    public void removeSplash() {
        splashController.removeSplash();
    }

    @Override
    public void replaceSplash(SplashPanel r) {
        splashController.replaceSplash(r);
    }

    @Override
    public int getSplashWidth() {
        return splashController.getSplashWidth();
    }

    @Override
    public int getSplashHeigth() {
        return splashController.getSplashHeigth();
    }

}
