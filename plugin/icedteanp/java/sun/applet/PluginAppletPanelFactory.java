/* Copyright (C) 2012 Red Hat

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

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.NetxPanel;
import net.sourceforge.jnlp.PluginParameters;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Lets us construct one using unix-style one shot behaviors
 */

class PluginAppletPanelFactory {

    public AppletPanel createPanel(PluginStreamHandler streamhandler,
                                   final int identifier,
                                   final long handle,
                                   final URL doc,
                                   final PluginParameters params) {
        final NetxPanel panel = AccessController.doPrivileged(new PrivilegedAction<NetxPanel>() {
            public NetxPanel run() {
                NetxPanel panel = new NetxPanel(doc, params);
                OutputController.getLogger().log("Using NetX panel");
                PluginDebug.debug(params.toString());
                return panel;
            }
        });

        // Framing the panel needs to happen in a thread whose thread group
        // is the same as the threadgroup of the applet thread. If this
        // isn't the case, the awt eventqueue thread's context classloader
        // won't be set to a JNLPClassLoader, and when an applet class needs
        // to be loaded from the awt eventqueue, it won't be found.
        Thread panelInit = new Thread(panel.getThreadGroup(), new Runnable() {
            @Override public void run() {
                panel.createNewAppContext();
                // create the frame.
                PluginDebug.debug("X and Y are: " + params.getWidth() + " " + params.getHeight());
                panel.setAppletViewerFrame(PluginAppletViewer.framePanel(identifier, handle,
                        params.getWidth(), params.getHeight(), panel));

                panel.init();
                // Start the applet
                initEventQueue(panel);
            }
        }, "NetXPanel initializer");

        panelInit.start();
        try {
            panelInit.join();
        } catch (InterruptedException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
        }

        setAppletViewerSize(panel, params.getWidth(), params.getHeight());

        // Wait for the panel to initialize
        PluginAppletViewer.waitForAppletInit(panel);

        Applet a = panel.getApplet();

        // Still null?
        if (a == null) {
            streamhandler.write("instance " + identifier + " reference " + -1 + " fatalError: " + "Initialization timed out");
            return null;
        }

        PluginDebug.debug("Applet ", a.getClass(), " initialized");
        streamhandler.write("instance " + identifier + " reference 0 initialized");

        panel.removeSplash();

        AppletSecurityContextManager.getSecurityContext(0).associateSrc(panel.getAppletClassLoader(), doc);
        AppletSecurityContextManager.getSecurityContext(0).associateInstance(identifier, panel.getAppletClassLoader());

        return panel;
    }

    /* AppletViewerPanel sometimes doesn't set size right initially. This 
     * causes the parent frame to be the default (10x10) size.
     *  
     * Normally it goes unnoticed since browsers like Firefox make a resize 
     * call after init. However some browsers (e.g. Midori) don't.
     * 
     * We therefore manually set the parent to the right size.
     */
    static private void setAppletViewerSize(final AppletPanel panel,
            final int width, final int height) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    panel.getParent().setSize(width, height);
                }
            });
        } catch (InvocationTargetException e) {
            // Not being able to resize is non-fatal
            PluginDebug.debug("Unable to resize panel: ");
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
        } catch (InterruptedException e) {
            // Not being able to resize is non-fatal
            PluginDebug.debug("Unable to resize panel: ");
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL,e);
        }
    }
    /**
     * Send the initial set of events to the appletviewer event queue.
     * On start-up the current behaviour is to load the applet and call
     * Applet.init() and Applet.start().
     */
    private void initEventQueue(AppletPanel panel) {
        // appletviewer.send.event is an undocumented and unsupported system
        // property which is used exclusively for testing purposes.
        PrivilegedAction<String> pa = new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty("appletviewer.send.event");
            }
        };
        String eventList = AccessController.doPrivileged(pa);

        if (eventList == null) {
            // Add the standard events onto the event queue.
            panel.sendEvent(AppletPanel.APPLET_LOAD);
            panel.sendEvent(AppletPanel.APPLET_INIT);
            panel.sendEvent(AppletPanel.APPLET_START);
        } else {
            // We're testing AppletViewer.  Force the specified set of events
            // onto the event queue, wait for the events to be processed, and
            // exit.

            // The list of events that will be executed is provided as a
            // ","-separated list.  No error-checking will be done on the list.
            String[] events = eventList.split(",");

            for (String event : events) {
                PluginDebug.debug("Adding event to queue: ", event);
                if ("dispose".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_DISPOSE);
                else if ("load".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_LOAD);
                else if ("init".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_INIT);
                else if ("start".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_START);
                else if ("stop".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_STOP);
                else if ("destroy".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_DESTROY);
                else if ("quit".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_QUIT);
                else if ("error".equals(event))
                    panel.sendEvent(AppletPanel.APPLET_ERROR);
                else
                    // non-fatal error if we get an unrecognized event
                    PluginDebug.debug("Unrecognized event name: ", event);
            }

            while (!panel.emptyEventQueue())
                ;
        }
    }
}