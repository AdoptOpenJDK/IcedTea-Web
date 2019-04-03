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
import net.sourceforge.jnlp.util.logging.OutputController;

import sun.applet.AppletViewerPanelAccess;
import sun.awt.SunToolkit;

/**
 * This panel calls into netx to run an applet, and pipes the display
 * into a panel from the icedtea-web browser plugin.
 *
 * @author      Francis Kung &lt;fkung@redhat.com&gt;
 */
public class NetxPanel extends AppletViewerPanelAccess implements SplashController {
    private final PluginParameters parameters;
    private PluginBridge bridge = null;
    private AppletInstance appInst = null;
    private SplashController splashController;
    private volatile boolean initialized;

    // We use this so that we can create exactly one thread group
    // for all panels with the same uKey.
    private static final Map<String, ThreadGroup> uKeyToTG =
        new HashMap<>();
    private static final Object TGMapMutex = new Object();

    // This map is actually a set (unfortunately there is no ConcurrentSet
    // in java.util.concurrent). If KEY is in this map, then we know that
    // an app context has been created for the panel that has uKey.equals(KEY),
    // so we avoid creating it a second time for panels with the same uKey.
    // Because it's a set, only the keys matter. However, we can't insert
    // null values in because if we did, we couldn't use null checks to see
    // if a key was absent before a putIfAbsent. 
    private static final ConcurrentMap<String, Boolean> appContextCreated =
        new ConcurrentHashMap<>();

    public NetxPanel(URL documentURL, PluginParameters params, PluginBridge bridge) {
        super(documentURL, params.getUnderlyingMap());
        this.bridge = bridge;
        this.parameters = params;
        this.initialized = false;

        String uniqueKey = params.getUniqueKey(getCodeBase());
        synchronized(TGMapMutex) {
            if (!uKeyToTG.containsKey(uniqueKey)) {
                ThreadGroup tg = new ThreadGroup(Launcher.mainGroup, this.getDocumentURL().toString());
                uKeyToTG.put(uniqueKey, tg);
            }
        }
    }

    public AppletInstance getAppInst() {
        return appInst;
    }

    @Override
    protected void showAppletException(Throwable t) {
        /*
         * Log any exceptions thrown while loading, initializing, starting,
         * and stopping the applet. 
         */
        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, t); //new logger
        super.showAppletException(t);
    }

    //Overriding to use Netx classloader. You might need to relax visibility
    //in sun.applet.AppletPanel for runLoader().
    @Override
    protected void ourRunLoader() {

        try {
            if (bridge == null) {
                bridge = new PluginBridge(getBaseURL(),
                        getDocumentBase(),
                        getJarFiles(),
                        getCode(),
                        getWidth(),
                        getHeight(),
                        parameters);
            }
            init(bridge);

        } catch (Exception e) {
            status = APPLET_ERROR;
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            replaceSplash(SplashUtils.getErrorSplashScreen(getWidth(), getHeight(), e));
        } finally {
            // PR1157: This needs to occur even in the case of an exception
            // so that the applet's event listeners are signaled.
            // Once PluginAppletViewer.AppletEventListener is signaled PluginAppletViewer can properly stop waiting
            // in PluginAppletViewer.waitForAppletInit
            this.initialized = true;
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
                OutputController.getLogger().log("initializing JNLPRuntime...");

                JNLPRuntime.initialize(false);
            } else {
                OutputController.getLogger().log("JNLPRuntime already initialized");
            }
        }

        handler = new Thread(getThreadGroup(), this, "NetxPanelThread@" + this.getDocumentURL());
        handler.start();
    }

    public void updateSizeInAtts(int height, int width) {
        parameters.updateSize(width, height);
    }

    public ClassLoader getAppletClassLoader() {
        return appInst.getClassLoader();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ThreadGroup getThreadGroup() {
        synchronized(TGMapMutex) {
            return uKeyToTG.get(parameters.getUniqueKey(getCodeBase()));
        }
    }

    public void createNewAppContext() {
        if (Thread.currentThread().getThreadGroup() != getThreadGroup()) {
            throw new RuntimeException("createNewAppContext called from the wrong thread.");
        }
        // only create a new context if one hasn't already been created for the
        // applets with this unique key.
        if (null == appContextCreated.putIfAbsent(parameters.getUniqueKey(getCodeBase()), Boolean.TRUE)) {
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

    public void init(PluginBridge bridge) throws LaunchException {
        setDoInitIfExists(true);
        dispatchAppletEvent(APPLET_LOADING, null);
        status = APPLET_LOAD;

        Launcher l = new Launcher(false);

        // May throw LaunchException:
        appInst = (AppletInstance) l.launch(bridge, this);
        setApplet(appInst.getApplet());

        if (getApplet() != null) {
            // Stick it in the frame
            getApplet().setStub(this);
            getApplet().setVisible(false);
            add("Center", getApplet());
            showAppletStatus("loaded");
            validate();
        }

    }        

}
