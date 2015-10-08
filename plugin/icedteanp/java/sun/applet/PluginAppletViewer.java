/* PluginAppletViewer -- Handles embedding of the applet panel
   Copyright (C) 2008  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

/*
  * Copyright 1995-2004 Sun Microsystems, Inc.  All Rights Reserved.
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
  *
  * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
  * CA 95054 USA or visit www.sun.com if you need additional information or
  * have any questions.
  */

package sun.applet;

import com.sun.jndi.toolkit.url.UrlUtil;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.NetxPanel;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.PluginParameters;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import static net.sourceforge.jnlp.runtime.Translator.R;

import net.sourceforge.jnlp.security.ConnectionFactory;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.sourceforge.jnlp.splashscreen.SplashController;
import net.sourceforge.jnlp.splashscreen.SplashPanel;
import net.sourceforge.jnlp.splashscreen.SplashUtils;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.X11.XEmbeddedFrame;

/*
 */
// FIXME: declare JSProxy implementation
@SuppressWarnings("serial")
public class PluginAppletViewer extends XEmbeddedFrame
        implements AppletContext, Printable, SplashController {

    /**
     *  Enumerates the current status of an applet
     *
     *  PRE_INIT -> Parsing and initialization phase
     *  INIT_COMPLETE -> Initialization complete, reframe pending
     *  REFRAME_COMPLETE -> Reframe complete, applet is initialized and usable by the user
     *  INACTIVE -> Browser has directed that the applet be destroyed (this state is non-overridable except by DESTROYED)
     *  DESTROYED -> Applet has been destroyed
     */
    private static enum PAV_INIT_STATUS {
        PRE_INIT, INIT_COMPLETE, REFRAME_COMPLETE, INACTIVE, DESTROYED
    };

    /**
     * The panel in which the applet is being displayed.
     */
    private NetxPanel panel;
    static final ReentrantLock panelLock = new ReentrantLock();
    // CONDITION PREDICATE: panel.isAlive()
    static final Condition panelLive = panelLock.newCondition();
    private int identifier;

    // Instance identifier -> PluginAppletViewer object.
    private static final ConcurrentMap<Integer, PluginAppletViewer> applets =
            new ConcurrentHashMap<>();
    private static final ReentrantLock appletsLock = new ReentrantLock();
    // CONDITION PREDICATE: !applets.containsKey(identifier)
    private static final Condition appletAdded = appletsLock.newCondition();

    private static PluginStreamHandler streamhandler;

    private static PluginCallRequestFactory requestFactory;

    private static final ConcurrentMap<Integer, PAV_INIT_STATUS> status =
            new ConcurrentHashMap<>();
    private static final ReentrantLock statusLock = new ReentrantLock();
    // CONDITION PREDICATE: !status.get(identifier).equals(PAV_INIT_STATUS.INIT_COMPLETE)
    private static final Condition initComplete = statusLock.newCondition();

    private WindowListener windowEventListener = null;
    private AppletEventListener appletEventListener = null;

    public static final long APPLET_TIMEOUT = 180000000000L; // 180s in ns

    private static final Object requestMutex = new Object();
    private static long requestIdentityCounter = 0L;

    private Image bufFrameImg;
    private Graphics bufFrameImgGraphics;


    private SplashPanel splashPanel;
    
    private static long REQUEST_TIMEOUT=60000;//60s

    private static void waitForRequestCompletion(PluginCallRequest request) {
        try {
            if (!request.isDone()) {
                request.wait(REQUEST_TIMEOUT);
            }
            if (!request.isDone()) {
                // Do not wait indefinitely to avoid the potential of deadlock
                throw new RuntimeException("Possible deadlock, releasing");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interrupted waiting for call request.", ex);
        }
    }

    /**
     * Null constructor to allow instantiation via newInstance()
     */
    public PluginAppletViewer() {
    }

    public static PluginAppletViewer framePanel(int identifier, long handle, int width, int height, NetxPanel panel) {

        PluginDebug.debug("Framing ", panel);
 
        // SecurityManager MUST be set, and only privileged code may call framePanel()
        System.getSecurityManager().checkPermission(new AllPermission());

        PluginAppletViewer appletFrame = new PluginAppletViewer(handle, identifier, panel);
        appletFrame.setSize(width, height);
        
        appletFrame.appletEventListener = new AppletEventListener(appletFrame, appletFrame);
        panel.addAppletListener(appletFrame.appletEventListener);
         // Clear references, if any
        if (applets.containsKey(identifier)) {
            PluginAppletViewer oldFrame = applets.get(identifier);            
            oldFrame.remove(panel);
            panel.removeAppletListener(oldFrame.appletEventListener);
        }

        appletsLock.lock();
        try {
            applets.put(identifier, appletFrame);
            appletAdded.signalAll();
        } finally {
            appletsLock.unlock();
        }

        PluginDebug.debug(panel, " framed");
        return appletFrame;
    }

    /**
     * Create new plugin appletviewer frame
     */
    private PluginAppletViewer(long handle, final int identifier,
                               NetxPanel appletPanel) {

        super(handle, true);
        this.identifier = identifier;
        this.panel = appletPanel;

        synchronized(appletPanels) {
            if (!appletPanels.contains(panel))
                appletPanels.addElement(panel);
        }

        windowEventListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                destroyApplet(identifier);
            }

            @Override
            public void windowIconified(WindowEvent evt) {
                appletStop();
            }

            @Override
            public void windowDeiconified(WindowEvent evt) {
                appletStart();
            }
        };

        addWindowListener(windowEventListener);
        final AppletPanel fPanel = panel;
        try {
            SwingUtilities.invokeAndWait(new SplashCreator(fPanel));
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e); // Not much we can do other than  print
        }

    }

    @Override
    public void replaceSplash(final SplashPanel newSplash) {
        if (splashPanel == null) {
            return;
        }
        if (newSplash == null) {
            removeSplash();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    splashPanel.getSplashComponent().setVisible(false);
                    splashPanel.stopAnimation();
                    remove(splashPanel.getSplashComponent());
                    newSplash.setPercentage(splashPanel.getPercentage());
                    newSplash.setSplashWidth(splashPanel.getSplashWidth());
                    newSplash.setSplashHeight(splashPanel.getSplashHeight());
                    newSplash.adjustForSize();
                    splashPanel = newSplash;
                    add("Center", splashPanel.getSplashComponent());
                    pack();
                }
            });
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e); // Not much we can do other than print
        }
    }

    @Override
    public void removeSplash() {
        if (splashPanel == null) {
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    splashPanel.getSplashComponent().setVisible(false);
                    splashPanel.stopAnimation();
                    removeAll();
                    setLayout(new BorderLayout());
                    //remove(splashPanel.getSplashComponent());
                    splashPanel = null;
                    //remove(panel);
                    // Re-add the applet to notify container
                    add(panel);
                    panel.setVisible(true);
                    pack();
                }
            });
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e); // Not much we can do other than print
        }
    }

    @Override
    public int getSplashWidth() {
        if (splashPanel != null) {
            return splashPanel.getSplashWidth();
        } else {
            return -1;
        }
    }

    @Override
    public int getSplashHeigth() {
        if (splashPanel != null) {
            return splashPanel.getSplashHeight();
        } else {
            return -1;
        }
    }

    private static class AppletEventListener implements AppletListener {
        final Frame frame;
        final PluginAppletViewer appletViewer;

        public AppletEventListener(Frame frame, PluginAppletViewer appletViewer) {
            this.frame = frame;
            this.appletViewer = appletViewer;
        }

        @Override
        public void appletStateChanged(AppletEvent evt) {
            AppletPanel src = (AppletPanel) evt.getSource();

            panelLock.lock();
            try {
                panelLive.signalAll();
            } finally {
                panelLock.unlock();
            }
            switch (evt.getID()) {
                case AppletPanel.APPLET_RESIZE: {
                    if (src != null) {
                        appletViewer.setSize(appletViewer.getPreferredSize());
                        appletViewer.validate();
                    }
                    break;
                }
                case AppletPanel.APPLET_LOADING_COMPLETED: {
                    Applet a = src.getApplet(); // sun.applet.AppletPanel

                    // Fixed #4754451: Applet can have methods running on main
                    // thread event queue.
                    //
                    // The cause of this bug is that the frame of the applet
                    // is created in main thread group. Thus, when certain
                    // AWT/Swing events are generated, the events will be
                    // dispatched through the wrong event dispatch thread.
                    //
                    // To fix this, we rearrange the AppContext with the frame,
                    // so the proper event queue will be looked up.
                    //
                    // Swing also maintains a Frame list for the AppContext,
                    // so we will have to rearrange it as well.
                    //
                    if (a != null) {
                        AppletPanel.changeFrameAppContext(frame, SunToolkit.targetToAppContext(a));
                    }
                    else {
                        AppletPanel.changeFrameAppContext(frame, AppContext.getAppContext());
                    }

                    updateStatus(appletViewer.identifier, PAV_INIT_STATUS.INIT_COMPLETE);

                    break;
                }
                case AppletPanel.APPLET_START: {
                    if (src.status != AppletPanel.APPLET_INIT && src.status != AppletPanel.APPLET_STOP) {
                        String s="Applet started, but but reached invalid state";
                        PluginDebug.debug(s);
                        SplashPanel sp=SplashUtils.getErrorSplashScreen(appletViewer.panel.getWidth(), appletViewer.panel.getHeight(), new Exception(s));
                        appletViewer.replaceSplash(sp);
                    }

                    break;
                }
                case AppletPanel.APPLET_ERROR: {
                    String s="Undefined error causing applet not to staart appeared";
                    PluginDebug.debug(s);
                        SplashPanel sp=SplashUtils.getErrorSplashScreen(appletViewer.panel.getWidth(), appletViewer.panel.getHeight(), new Exception(s));
                        appletViewer.replaceSplash(sp);
                    break;
                }
            }
        }
    }

    public static void setStreamhandler(PluginStreamHandler sh) {
        streamhandler = sh;
    }

    public static void setPluginCallRequestFactory(PluginCallRequestFactory rf) {
        requestFactory = rf;
    }

    private static void handleInitializationMessage(int identifier, String message) throws IOException, LaunchException {

        /* The user has specified via a global setting that applets should not be run.*/
        if (AppletStartupSecuritySettings.getInstance().getSecurityLevel() == AppletSecurityLevel.DENY_ALL) {
            throw new LaunchException(null, null, R("LSFatal"), R("LCClient"), R("LUnsignedApplet"), R("LUnsignedAppletPolicyDenied"));
        }

        // If there is a key for this status, it means it
        // was either initialized before, or destroy has been
        // processed. Stop moving further.
        if (updateStatus(identifier, PAV_INIT_STATUS.PRE_INIT) != null) {
            return;
        }

        // Extract the information from the message
        String[] msgParts = new String[4];
        for (int i = 0; i < 3; i++) {
            int spaceLocation = message.indexOf(' ');
            int nextSpaceLocation = message.indexOf(' ', spaceLocation + 1);
            msgParts[i] = message.substring(spaceLocation + 1, nextSpaceLocation);
            message = message.substring(nextSpaceLocation + 1);
        }

        long handle = Long.parseLong(msgParts[0]);
        String width = msgParts[1];
        String height = msgParts[2];

        int spaceLocation = message.indexOf(' ', "tag".length() + 1);
        String documentBase = message.substring("tag".length() + 1, spaceLocation);
        String paramString = message.substring(spaceLocation + 1);

        PluginDebug.debug("Handle = ", handle, "\n",
                            "Width = ", width, "\n",
                            "Height = ", height, "\n",
                            "DocumentBase = ", documentBase, "\n",
                            "Params = ", paramString);
        JNLPRuntime.saveHistory(documentBase);

        AppletMessageHandler amh = new AppletMessageHandler("appletviewer");
        URL url = new URL(documentBase);
        URLConnection conn = ConnectionFactory.getConnectionFactory().openConnection(url);
        /* The original URL may have been redirected - this
         * sets it to whatever URL/codebase we ended up getting
         */
        url = conn.getURL();
        ConnectionFactory.getConnectionFactory().disconnect(conn);

        PluginParameters params = new PluginParameterParser().parse(width, height, paramString);

       // Let user know we are starting up
       streamhandler.write("instance " + identifier + " status " + amh.getMessage("status.start"));
       initialize(params, handle, url, identifier, null);
    }
    
    public static AppletPanel initialize(PluginParameters params, long handle, URL url, int identifier, PluginBridge pb) {
        PluginAppletPanelFactory factory = new PluginAppletPanelFactory();
        AppletPanel p = factory.createPanel(streamhandler, identifier, handle, url, params, pb);

        long maxTimeToSleep = APPLET_TIMEOUT;
        appletsLock.lock();
        try {
            while (!applets.containsKey(identifier)
                    && maxTimeToSleep > 0) { // Map is populated only by reFrame
                maxTimeToSleep -= waitTillTimeout(appletsLock, appletAdded,
                        maxTimeToSleep);
            }
        } finally {
            appletsLock.unlock();
        }

        // If wait exceeded maxWait, we timed out. Throw an exception
        if (maxTimeToSleep <= 0) {
            // Caught in handleMessage
            throw new RuntimeException("Applet initialization timeout");
        }

        // We should not try to destroy an applet during
        // initialization. It may cause an inconsistent state,
        // which would bad if it's a trusted applet that
        // read/writes to files
        waitForAppletInit(applets.get(identifier).panel);

        // Should we proceed with reframing?
        PluginDebug.debug("Init complete");

        if (updateStatus(identifier, PAV_INIT_STATUS.REFRAME_COMPLETE).equals(PAV_INIT_STATUS.INACTIVE)) {
            destroyApplet(identifier);
            return null;
        }
        return p;
    }

    /**
     * Handle an incoming message from the plugin.
     * @param identifier id of plugin
     * @param reference reference id of message
     * @param message text itself
     */
    public static void handleMessage(int identifier, int reference, String message) {

        PluginDebug.debug("PAV handling: ", message);

        try {
            if (message.startsWith("handle")) {
                handleInitializationMessage(identifier, message);
            } else if (message.startsWith("destroy")) {

                // Set it inactive, and try to do cleanup is applicable
                PAV_INIT_STATUS previousStatus = updateStatus(identifier, PAV_INIT_STATUS.INACTIVE);
                PluginDebug.debug("Destroy status set for ", identifier);

                if (previousStatus != null &&
                         previousStatus.equals(PAV_INIT_STATUS.REFRAME_COMPLETE)) {
                    destroyApplet(identifier);
                }

            } else {
                PluginDebug.debug("Handling message: ", message, " instance ", identifier, " ", Thread.currentThread());

                // Wait till initialization finishes
                while (!applets.containsKey(identifier) &&
                         (
                           !status.containsKey(identifier) ||
                            status.get(identifier).equals(PAV_INIT_STATUS.PRE_INIT)
                         ))
                    ;

                // don't bother processing further for inactive applets
                if (status.get(identifier).equals(PAV_INIT_STATUS.INACTIVE)) {
                    return;
                }

                applets.get(identifier).handleMessage(reference, message);
            }
        } catch (Exception e) {

            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);

            // If an exception happened during pre-init, we need to update status
            updateStatus(identifier, PAV_INIT_STATUS.INACTIVE);

            throw new RuntimeException("Failed to handle message: " +
                     message + " for instance " + identifier, e);
        }
    }

    /**
     * Sets the status unless an overriding status is set (e.g. if
     * status is DESTROYED, it may not be overridden).
     *
     * @param identifier The identifier for which the status is to be set
     * @param status The status to switch to
     * @return The previous status
     */
    private static synchronized PAV_INIT_STATUS updateStatus(int identifier, PAV_INIT_STATUS newStatus) {

        PAV_INIT_STATUS prev = status.get(identifier);

        // If the status is set
        if (status.containsKey(identifier)) {

            // Nothing may override destroyed status
            if (status.get(identifier).equals(PAV_INIT_STATUS.DESTROYED)) {
                return prev;
            }

            // If status is inactive, only DESTROYED may override it
            if (status.get(identifier).equals(PAV_INIT_STATUS.INACTIVE)) {
                if (!newStatus.equals(PAV_INIT_STATUS.DESTROYED)) {
                    return prev;
                }
            }
        }


        statusLock.lock();
        try {
            status.put(identifier, newStatus);
            initComplete.signalAll();
        } finally {
            statusLock.unlock();
        }

        return prev;
    }

    /**
     * Destroys the given applet instance.
     *
     * This function may be called multiple times without problems.
     * It does a synchronized check on the status and will only
     * attempt to destroy the applet if not previously destroyed.
     *
     * @param identifier The instance which is to be destroyed
     */

    private static synchronized void destroyApplet(int identifier) {

        // We should not try to destroy an applet during
        // initialization. It may cause an inconsistent state.
        waitForAppletInit( applets.get(identifier).panel );

        PluginDebug.debug("DestroyApplet called for ", identifier);

        PAV_INIT_STATUS prev = updateStatus(identifier, PAV_INIT_STATUS.DESTROYED);

        // If already destroyed, return
        if (prev.equals(PAV_INIT_STATUS.DESTROYED)) {
            PluginDebug.debug(identifier, " already destroyed. Returning.");
            return;
        }

        PluginDebug.debug("Attempting to destroy frame ", identifier);

        // Try to dispose the panel right away
        final PluginAppletViewer pav = applets.get(identifier);
        if (pav != null) {
            pav.dispose();

            // If panel is already disposed, return
            if (pav.panel.getApplet() == null) {
                PluginDebug.debug(identifier, " panel inactive. Returning.");
                return;
            }

            PluginDebug.debug("Attempting to destroy panel ", identifier);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pav.appletClose();
                }
            });
        }

        PluginDebug.debug(identifier, " destroyed");
    }

    /**
     * Function to block until applet initialization is complete.
     *
     * This function will return if the wait is longer than {@link #APPLET_TIMEOUT}
     *
     * @param panel the instance to wait for.
     */
    public static void waitForAppletInit(NetxPanel panel) {

        PluginDebug.debug("Waiting for applet init");

        // Wait till initialization finishes
        long maxTimeToSleep = APPLET_TIMEOUT;

        panelLock.lock();
        try {
            while (!panel.isInitialized() && 
                    maxTimeToSleep > 0) {
                PluginDebug.debug("Waiting for applet panel ", panel, " to initialize...");
                maxTimeToSleep -= waitTillTimeout(panelLock, panelLive, maxTimeToSleep);
            }
        }
        finally {
            panelLock.unlock();
        }

        PluginDebug.debug("Applet panel ", panel, " initialized");
    }

    /* Resizes an applet panel, waiting for the applet to initialze. 
     * Should be done asynchronously to avoid the chance of deadlock. */
    private void resizeAppletPanel(final int width, final int height) {
        // Wait for panel to come alive
        waitForAppletInit(panel);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.updateSizeInAtts(height, width);

                setSize(width, height);

                // There is a rather odd drawing bug whereby resizing
                // the panel makes no difference on initial call
                // because the panel thinks that it is already the
                // right size. Validation has no effect there either.
                // So we work around by setting size to 1, validating,
                // and then setting to the right size and validating
                // again. This is not very efficient, and there is
                // probably a better way -- but resizing happens
                // quite infrequently, so for now this is how we do it

                panel.setSize(1, 1);
                panel.validate();

                panel.setSize(width, height);
                panel.validate();

                panel.getApplet().resize(width, height);
                panel.getApplet().validate();
            }
        });
    }

    public void handleMessage(int reference, String message) {
        if (message.startsWith("width")) {

            // 0 => width, 1=> width_value, 2 => height, 3=> height_value
            String[] dimMsg = message.split(" ");

            final int width = Integer.parseInt(dimMsg[1]);
            final int height = Integer.parseInt(dimMsg[3]);

            /* Resize the applet asynchronously, to avoid the chance of 
             * deadlock while waiting for the applet to initialize. 
             * 
             * In general, worker threads should spawn new threads for any blocking operations. */
            Thread resizeAppletThread = new Thread("resizeAppletThread") {
                @Override
                public void run() {
                    resizeAppletPanel(width, height);
                }
            };

            /* Let it eventually complete */
            resizeAppletThread.start();

        } else if (message.startsWith("GetJavaObject")) {

            // FIXME: how do we determine what security context this
            // object should belong to?
            Object o;

            // Wait for the panel to initialize
            // (happens in a separate thread)
            waitForAppletInit(panel);

            PluginDebug.debug(panel, " -- ", panel.getApplet(), " -- initialized: ", panel.isInitialized());

            // Still null?
            if (panel.getApplet() == null) {
                streamhandler.write("instance " + identifier + " reference " + -1 + " fatalError: " + "Initialization failed");
                streamhandler.write("context 0 reference " + reference + " Error");
                return;
            }

            o = panel.getApplet();
            PluginDebug.debug("Looking for object ", o, " panel is ", panel);
            AppletSecurityContextManager.getSecurityContext(0).store(o);
            PluginDebug.debug("WRITING 1: ", "context 0 reference ", reference, " GetJavaObject "
                                 , AppletSecurityContextManager.getSecurityContext(0).getIdentifier(o));
            streamhandler.write("context 0 reference " + reference + " GetJavaObject "
                              + AppletSecurityContextManager.getSecurityContext(0).getIdentifier(o));
            PluginDebug.debug("WRITING 1 DONE");
        }
    }

    /*
     * Methods for java.applet.AppletContext
     */

    final private static Map<URL, AudioClip> audioClips = new HashMap<>();

    /**
     * Get an audio clip.
     */
    @Override
    public AudioClip getAudioClip(URL url) {
        checkConnect(url);
        synchronized (audioClips) {
            AudioClip clip = audioClips.get(url);
            if (clip == null) {
                audioClips.put(url, clip = new AppletAudioClip(url));
            }
            return clip;
        }
    }

    final private static Map<URL, AppletImageRef> imageRefs = new HashMap<>();

    /**
     * Get an image.
     */
    @Override
    public Image getImage(URL url) {
        return getCachedImage(url);
    }

    private Image getCachedImage(URL url) {
        return (Image) getCachedImageRef(url).get();
    }

    /**
     * Get an image ref.
     */
    private synchronized AppletImageRef getCachedImageRef(URL url) {
        PluginDebug.debug("getCachedImageRef() searching for ", url);

        try {

            String originalURL = url.toString();
            String codeBase = panel.getCodeBase().toString();

            if (originalURL.startsWith(codeBase)) {

                PluginDebug.debug("getCachedImageRef() got URL = ", url);
                PluginDebug.debug("getCachedImageRef() plugin codebase = ", codeBase);

                String resourceName = originalURL.substring(codeBase.length());
                if (panel.getAppletClassLoader() instanceof JNLPClassLoader) {
                    JNLPClassLoader loader = (JNLPClassLoader) panel.getAppletClassLoader();

                    URL localURL = null;
                    if (loader.resourceAvailableLocally(resourceName)) {
                        url = loader.getResource(resourceName);
                    }

                    url = localURL != null ? localURL : url;
                }
            }

            PluginDebug.debug("getCachedImageRef() getting img from URL = ", url);

            synchronized (imageRefs) {
                AppletImageRef ref = imageRefs.get(url);
                if (ref == null) {
                    ref = new AppletImageRef(url);
                    imageRefs.put(url, ref);
                }
                return ref;
            }
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Error occurred when trying to fetch image:");
            OutputController.getLogger().log(e);
            return null;
        }
    }

    /**
     * Flush the image cache.
     */
    static void flushImageCache() {
        imageRefs.clear();
    }

    final private static Vector<NetxPanel> appletPanels = new Vector<>();

    /**
     * Get an applet by name.
     */
    @Override
    public Applet getApplet(String name) {
        name = name.toLowerCase();
        SocketPermission panelSp =
                new SocketPermission(UrlUtils.getHostAndPort(panel.getCodeBase()), "connect");
        synchronized(appletPanels) {
            for (Enumeration<NetxPanel> e = appletPanels.elements(); e.hasMoreElements();) {
                AppletPanel p = e.nextElement();
                String param = p.getParameter("name");
                if (param != null) {
                    param = param.toLowerCase();
                }
                if (name.equals(param) &&
                        p.getDocumentBase().equals(panel.getDocumentBase())) {

                    SocketPermission sp =
                        new SocketPermission(UrlUtils.getHostAndPort(p.getCodeBase()), "connect");

                    if (panelSp.implies(sp)) {
                        return p.applet;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return an enumeration of all the accessible
     * applets on this page.
     */
    @Override
    public Enumeration<Applet> getApplets() {
        Vector<Applet> v = new Vector<Applet>();
        SocketPermission panelSp =
                new SocketPermission(UrlUtils.getHostAndPort(panel.getCodeBase()), "connect");

        synchronized(appletPanels) {
            for (Enumeration<NetxPanel> e = appletPanels.elements(); e.hasMoreElements();) {
                AppletPanel p = e.nextElement();
                if (p.getDocumentBase().equals(panel.getDocumentBase())) {

                    SocketPermission sp =
                        new SocketPermission(UrlUtils.getHostAndPort(p.getCodeBase()), "connect");
                    if (panelSp.implies(sp)) {
                        v.addElement(p.applet);
                    }
                }
            }
        }
        return v.elements();
    }

    @Override
    public void showDocument(URL url) {
        PluginDebug.debug("Showing document...");
        showDocument(url, "_self");
    }

    @Override
    public void showDocument(URL url, String target) {
        // If it is a javascript document, eval on current page.
        if ("javascript".equals(url.getProtocol())) {
            // Snip protocol off string
            String evalString = url.toString().substring("javascript:".length());
            eval(getWindow(), evalString);
            return;
        }
        try {
            Long reference = getRequestIdentifier();
            write("reference " + reference +  " LoadURL " + UrlUtil.encode(url.toString(), "UTF-8") + " " + target);
        } catch (IOException exception) {
            // Deliberately ignore IOException.  showDocument may be
            // called from threads other than the main thread after
            // streamhandler.pluginOutputStream has been closed.
        }
    }

    /**
     * Show status.
     */
    @Override
    public void showStatus(String status) {
        try {
            // FIXME: change to postCallRequest
            // For statuses, we cannot have a newline
            status = status.replace("\n", " ");
            write("status " + status);
        } catch (IOException exception) {
            // Deliberately ignore IOException.  showStatus may be
            // called from threads other than the main thread after
            // streamhandler.pluginOutputStream has been closed.
        }
    }

    /**
     * Returns an incremental number (unique identifier) for a message.
     * If identifier hits Long.MAX_VALUE it loops back starting at 0.
     *
     *  @return A unique Long identifier for the request
     */
    private static long getRequestIdentifier() {
        synchronized(requestMutex) {
            if (requestIdentityCounter == Long.MAX_VALUE) {
                requestIdentityCounter = 0L;
            }

            return requestIdentityCounter++;
        }
    }

    public long getWindow() {
        PluginDebug.debug("STARTING getWindow");
        Long reference = getRequestIdentifier();

        PluginCallRequest request = requestFactory.getPluginCallRequest("window",
                "instance " + identifier + " reference " +
                        +reference + " " + "GetWindow", reference);

        PluginDebug.debug("STARTING postCallRequest");
        streamhandler.postCallRequest(request);
        PluginDebug.debug("STARTING postCallRequest done");
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait request 1");
            synchronized (request) {
                PluginDebug.debug("wait request 2");
                while ((Long) request.getObject() == 0)
                    request.wait();
                PluginDebug.debug("wait request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                     e);
        }

        PluginDebug.debug("STARTING getWindow DONE");
        return (Long) request.getObject();
    }

    // FIXME: make private, access via reflection.
    public static Object getMember(long internal, String name) {
        AppletSecurityContextManager.getSecurityContext(0).store(name);
        int nameID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(name);
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("member",
                "instance " + 0 + " reference " + reference + " GetMember " +
                        internal + " " + nameID, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait getMEM request 1");
            synchronized (request) {
                PluginDebug.debug("wait getMEM request 2");
                while (request.isDone() == false)
                    request.wait();
                PluginDebug.debug("wait getMEM request 3 GOT: ", request.getObject().getClass());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" getMember DONE");
        return request.getObject();
    }

    public static void setMember(long internal, String name, Object value) {
        PluginDebug.debug("Setting to class " + value.getClass() + ":" + value.getClass().isPrimitive());
        PluginAppletSecurityContext securityContext = AppletSecurityContextManager.getSecurityContext(0);
        securityContext.store(name);
        int nameID = securityContext.getIdentifier(name);
        Long reference = getRequestIdentifier();

        // work on a copy of value, as we don't want to be manipulating
        // complex objects
        String objIDStr = securityContext.toObjectIDString(value,
                value.getClass(), true /* unbox primitives */);

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("void",
                "instance " + 0 + " reference " + reference + " SetMember " +
                        internal + " " + nameID + " " + objIDStr, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait setMem request: ", request.getMessage());
            PluginDebug.debug("wait setMem request 1");
            synchronized (request) {
                PluginDebug.debug("wait setMem request 2");
                while (request.isDone() == false)
                    request.wait();
                PluginDebug.debug("wait setMem request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" setMember DONE");
    }

    // FIXME: handle long index as well.
    public static void setSlot(long internal, int index, Object value) {
        PluginAppletSecurityContext securityContext = AppletSecurityContextManager.getSecurityContext(0);
        securityContext.store(value);
        Long reference = getRequestIdentifier();

        String objIDStr = securityContext.toObjectIDString(value,
                value.getClass(), true /* unbox primitives */);

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("void",
                "instance " + 0 + " reference " + reference + " SetSlot " +
                        internal + " " + index + " " + objIDStr, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait setSlot request 1");
            synchronized (request) {
                PluginDebug.debug("wait setSlot request 2");
                while (request.isDone() == false)
                    request.wait();
                PluginDebug.debug("wait setSlot request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" setSlot DONE");
    }

    public static Object getSlot(long internal, int index) {
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("member",
                "instance " + 0 + " reference " + reference + " GetSlot " +
                        internal + " " + index, reference);
        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait getSlot request 1");
            synchronized (request) {
                PluginDebug.debug("wait getSlot request 2");
                while (request.isDone() == false)
                    request.wait();
                PluginDebug.debug("wait getSlot request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" getSlot DONE");
        return request.getObject();
    }

    public static Object eval(long internal, String s) {
        AppletSecurityContextManager.getSecurityContext(0).store(s);
        int stringID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(s);
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        // FIXME: rename GetMemberPluginCallRequest ObjectPluginCallRequest.
        PluginCallRequest request = requestFactory.getPluginCallRequest("member",
                "instance " + 0 + " reference " + reference + " Eval " +
                        internal + " " + stringID, reference);
        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait eval request 1");
            synchronized (request) {
                PluginDebug.debug("wait eval request 2");
                while (request.isDone() == false) {
                    request.wait();
                }
                PluginDebug.debug("wait eval request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" getSlot DONE");
        return request.getObject();
    }

    public static void removeMember(long internal, String name) {
        AppletSecurityContextManager.getSecurityContext(0).store(name);
        int nameID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(name);
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("void",
                "instance " + 0 + " reference " + reference + " RemoveMember " +
                        internal + " " + nameID, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait removeMember request 1");
            synchronized (request) {
                PluginDebug.debug("wait removeMember request 2");
                while (request.isDone() == false)
                    request.wait();
                PluginDebug.debug("wait removeMember request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" RemoveMember DONE");
    }

    public static Object call(long internal, String name, Object args[]) {
        // FIXME: when is this removed from the object store?
        // FIXME: reference should return the ID.
        // FIXME: convenience method for this long line.
        AppletSecurityContextManager.getSecurityContext(0).store(name);
        int nameID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(name);
        Long reference = getRequestIdentifier();

        String argIDs = "";
        for (Object arg : args) {
            AppletSecurityContextManager.getSecurityContext(0).store(arg);
            argIDs += AppletSecurityContextManager.getSecurityContext(0).getIdentifier(arg) + " ";
        }
        argIDs = argIDs.trim();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("member",
                "instance " + 0 + " reference " + reference + " Call " +
                        internal + " " + nameID + " " + argIDs, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait call request 1");
            synchronized (request) {
                PluginDebug.debug("wait call request 2");
                while (request.isDone() == false) {
                    request.wait();
                }
                PluginDebug.debug("wait call request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" Call DONE");
        return request.getObject();
    }

    public static Object requestPluginCookieInfo(URI uri) {

        PluginCallRequest request;
        Long reference = getRequestIdentifier();

        try {
            String encodedURI = UrlUtil.encode(uri.toString(), "UTF-8");
            request = requestFactory.getPluginCallRequest("cookieinfo",
                    "plugin PluginCookieInfo " + "reference " + reference +
                            " " + encodedURI, reference);

        } catch (UnsupportedEncodingException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
            return null;
        }

        PluginMessageConsumer.registerPriorityWait(reference);
        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait cookieinfo request 1");
            synchronized (request) {
                PluginDebug.debug("wait cookieinfo request 2");
                while (request.isDone() == false) {
                    request.wait();
                }
                PluginDebug.debug("wait cookieinfo request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for cookieinfo request.",
                                        e);
        }
        PluginDebug.debug(" Cookieinfo DONE");
        return request.getObject();
    }

    /**
     * Obtain information about the proxy from the browser.
     *
     * @param uri a String in url-encoded form
     * @return a {@link URI} that indicates a proxy.
     */
    public static Object requestPluginProxyInfo(String uri) {
        Long reference = getRequestIdentifier();

        PluginCallRequest request = requestFactory.getPluginCallRequest("proxyinfo",
                "plugin PluginProxyInfo reference " + reference + " " +
                        uri, reference);

        PluginMessageConsumer.registerPriorityWait(reference);
        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait call request 1");
            synchronized (request) {
                PluginDebug.debug("wait call request 2");
                while (request.isDone() == false) {
                    request.wait();
                }
                PluginDebug.debug("wait call request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" Call DONE");
        return request.getObject();
    }

    public static void JavaScriptFinalize(long internal) {
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("void",
                "instance " + 0 + " reference " + reference + " Finalize " +
                        internal, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        try {
            PluginDebug.debug("wait finalize request 1");
            synchronized (request) {
                PluginDebug.debug("wait finalize request 2");
                while (request.isDone() == false) {
                    request.wait();
                }
                PluginDebug.debug("wait finalize request 3");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
        }
        PluginDebug.debug(" finalize DONE");
    }

    public static String javascriptToString(long internal) {
        Long reference = getRequestIdentifier();

        // Prefix with dummy instance for convenience.
        PluginCallRequest request = requestFactory.getPluginCallRequest("member",
                "instance " + 0 + " reference " + reference + " ToString " +
                        internal, reference);

        streamhandler.postCallRequest(request);
        streamhandler.write(request.getMessage());
        PluginDebug.debug("wait ToString request 1");
        synchronized (request) {
            PluginDebug.debug("wait ToString request 2");
            waitForRequestCompletion(request);
            PluginDebug.debug("wait ToString request 3");
        }

        PluginDebug.debug(" ToString DONE");
        return (String) request.getObject();
    }

    // FIXME: make this private and access it from JSObject using
    // reflection.
    private void write(String message) throws IOException {
        PluginDebug.debug("WRITING 2: ", "instance ", identifier, " " + message);
        streamhandler.write("instance " + identifier + " " + message);
        PluginDebug.debug("WRITING 2 DONE");
    }

    @Override
    public void setStream(String key, InputStream stream) throws IOException {
        // We do nothing.
    }

    @Override
    public InputStream getStream(String key) {
        // We do nothing.
        return null;
    }

    @Override
    public Iterator<String> getStreamKeys() {
        // We do nothing.
        return null;
    }

    /**
     * System parameters.
     */
    private final static Map<String, String> systemParam = new HashMap<>();

    static {
        systemParam.put("codebase", "codebase");
        systemParam.put("code", "code");
        systemParam.put("alt", "alt");
        systemParam.put("width", "width");
        systemParam.put("height", "height");
        systemParam.put("align", "align");
        systemParam.put("vspace", "vspace");
        systemParam.put("hspace", "hspace");
    }

    /**
     * Make sure the atrributes are uptodate.
     */
    public void updateAtts() {
        Dimension d = panel.getSize();
        Insets in = panel.getInsets();
        int width = d.width - (in.left + in.right);
        int height = d.height - (in.top + in.bottom);
        panel.updateSizeInAtts(height, width);
    }

    /**
     * Restart the applet.
     */
    void appletRestart() {
        panel.sendEvent(AppletPanel.APPLET_STOP);
        panel.sendEvent(AppletPanel.APPLET_DESTROY);
        panel.sendEvent(AppletPanel.APPLET_INIT);
        panel.sendEvent(AppletPanel.APPLET_START);
    }

    /**
     * Reload the applet.
     */
    void appletReload() {
        panel.sendEvent(AppletPanel.APPLET_STOP);
        panel.sendEvent(AppletPanel.APPLET_DESTROY);
        panel.sendEvent(AppletPanel.APPLET_DISPOSE);

        /**
         * Fixed #4501142: Classloader sharing policy doesn't
         * take "archive" into account. This will be overridden
         * by Java Plug-in.         [stanleyh]
         */
        AppletPanel.flushClassLoader(panel.getClassLoaderCacheKey());

        /*
         * Make sure we don't have two threads running through the event queue
         * at the same time.
         */
        try {
            ((AppletViewerPanelAccess)panel).joinAppletThread();
            ((AppletViewerPanelAccess)panel).release();
        } catch (InterruptedException e) {
            return; // abort the reload
        }

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                ((AppletViewerPanelAccess)panel).createAppletThread();
                return null;
            }
        });

        panel.sendEvent(AppletPanel.APPLET_LOAD);
        panel.sendEvent(AppletPanel.APPLET_INIT);
        panel.sendEvent(AppletPanel.APPLET_START);
    }

    @Override
    public int print(Graphics graphics, PageFormat pf, int pageIndex) {
        return Printable.NO_SUCH_PAGE;
    }

    /**
     * Start the applet.
     */
    void appletStart() {
        panel.sendEvent(AppletPanel.APPLET_START);
    }

    /**
     * Stop the applet.
     */
    void appletStop() {
        panel.sendEvent(AppletPanel.APPLET_STOP);
    }

    /**
     * Shutdown a viewer.
     * Stop, Destroy, Dispose and Quit a viewer
     */
    private void appletShutdown(AppletPanel p) {
        p.sendEvent(AppletPanel.APPLET_STOP);
        p.sendEvent(AppletPanel.APPLET_DESTROY);
        p.sendEvent(AppletPanel.APPLET_DISPOSE);
        p.sendEvent(AppletPanel.APPLET_QUIT);
    }

    /**
     * Close this viewer.
     * Stop, Destroy, Dispose and Quit an AppletView, then
     * reclaim resources and exit the program if this is
     * the last applet.
     */
    void appletClose() {

        // The caller thread is event dispatch thread, so
        // spawn a new thread to avoid blocking the event queue
        // when calling appletShutdown.
        //
        final AppletPanel p = panel;

        new Thread(new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                ClassLoader cl = p.applet.getClass().getClassLoader();

                // Since we want to deal with JNLPClassLoader, extract it if this
                // is a codebase loader
                if (cl instanceof JNLPClassLoader.CodeBaseClassLoader) {
                    cl = ((JNLPClassLoader.CodeBaseClassLoader) cl).getParentJNLPClassLoader();
                }

                appletShutdown(p);
                appletPanels.removeElement(p);
                
                // Mark classloader unusable
                if (cl instanceof JNLPClassLoader) {
                    ((JNLPClassLoader) cl).decrementLoaderUseCount();
                }

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            dispose();
                        }
                    });
                } catch (Exception e) { // ignore, we are just disposing it
                }

                if (countApplets() == 0) {
                    appletSystemExit();
                }

                updateStatus(identifier, PAV_INIT_STATUS.DESTROYED);
            }
        }).start();
    }

    /**
     * Exit the program.
     * Exit from the program (if not stand alone) - do no clean-up
     */
    private void appletSystemExit() {
        // Do nothing. Exit is handled by another
        // block of code, called when _all_ applets are gone
    }

    /**
     * How many applets are running?
     * @return number of applets run in this JVM
     */

    public static int countApplets() {
        return appletPanels.size();
    }


    private static void checkConnect(URL url) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                URLConnection conn = ConnectionFactory.getConnectionFactory().openConnection(url);
                java.security.Permission perm = conn.getPermission();
                ConnectionFactory.getConnectionFactory().disconnect(conn);
                if (perm != null) {
                    security.checkPermission(perm);
                }
                else {
                    security.checkConnect(url.getHost(), url.getPort());
                }
            } catch (java.io.IOException ioe) {
                security.checkConnect(url.getHost(), url.getPort());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * This method calls paint directly, rather than via super.update() since
     * the parent class's update() just does a couple of checks (both of
     * which are accounted for) and then calls paint anyway.
     */
    @Override
    public void paint(Graphics g) {

        // If the image or the graphics don't exist, create new ones
        if (bufFrameImg == null || bufFrameImgGraphics == null) {
            // although invisible applets do not have right to paint
            // we rather paint to 1x1 to be sure all callbacks  will be completed
            bufFrameImg = createImage(Math.max(1, getWidth()), Math.max(1, getHeight()));
            bufFrameImgGraphics = bufFrameImg.getGraphics();
        }

        // Paint off-screen
        for (Component c: this.getComponents()) {
                c.paint(bufFrameImgGraphics);
        }

        // Draw the painted image
        g.drawImage(bufFrameImg, 0, 0, this);
    }
    
    
    @Override
    public void update(Graphics g) {
        paint(g);
    }
  
    /**
     * Waits on a given condition queue until timeout.
     *
     * <b>This function assumes that the monitor lock has already been
     * acquired by the caller.</b>
     *
     * If the given lock is null, this function returns immediately.
     *
     * @param lock the lock that must be held when this method is called.
     * @param cond the condition queue on which to wait for notifications.
     * @param timeout The maximum time to wait (nanoseconds)
     * @return Approximate time spent sleeping (not guaranteed to be perfect)
     */
    public static long waitTillTimeout(ReentrantLock lock, Condition cond,
                                       long timeout) {

        // Can't wait on null. Return 0 indicating no wait happened.
        if (lock == null) {
                                               return 0;
                                           }

        assert lock.isHeldByCurrentThread();

        // Record when we started sleeping
        long sleepStart = 0L;

        try {
            sleepStart = System.nanoTime();
            cond.await(timeout, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ie) {} // Discarded, time to return

        // Return the difference
        return System.nanoTime() - sleepStart;
    }

    private class SplashCreator implements Runnable {

        private final AppletPanel fPanel;

        public SplashCreator(AppletPanel fPanel) {
            this.fPanel = fPanel;
        }

        @Override
        public void run() {
            add("Center", fPanel);
            fPanel.setVisible(false);
            splashPanel = SplashUtils.getSplashScreen(fPanel.getWidth(), fPanel.getHeight());
            if (splashPanel != null) {
                splashPanel.startAnimation();
                PluginDebug.debug("Added splash " + splashPanel);
                add("Center", splashPanel.getSplashComponent());
            }
            pack();
        }
    }
}
