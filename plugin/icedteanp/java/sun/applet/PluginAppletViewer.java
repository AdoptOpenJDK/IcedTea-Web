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
 
 import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.NetxPanel;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.X11.XEmbeddedFrame;
import sun.misc.Ref;

import com.sun.jndi.toolkit.url.UrlUtil;
 
 /**
  * Lets us construct one using unix-style one shot behaviors
  */
 
 class PluginAppletPanelFactory
 {

     public AppletPanel createPanel(PluginStreamHandler streamhandler, 
                                    int identifier,
                                    long handle, int x, int y,
                                    final URL doc, final Hashtable atts) {

         AppletViewerPanel panel = (AppletViewerPanel) AccessController.doPrivileged(new PrivilegedAction() {
             public Object run() {
                    try {
                        AppletPanel panel = new NetxPanel(doc, atts, false);
                        AppletViewerPanel.debug("Using NetX panel");
                        PluginDebug.debug(atts.toString());
                        return panel;
                    } catch (Exception ex) {
                        AppletViewerPanel.debug("Unable to start NetX applet - defaulting to Sun applet", ex);
                        return new AppletViewerPanel(doc, atts);
                    }
             }
         });

         

         // create the frame.
         PluginAppletViewer.reFrame(null, identifier, System.out, handle, panel);
         
         panel.init();

         // Start the applet
         initEventQueue(panel);

         // Applet initialized. Find out it's classloader and add it to the list
         String portComponent = doc.getPort() != -1 ? ":" + doc.getPort() : "";
         String codeBase = doc.getProtocol() + "://" + doc.getHost() + portComponent;

         if (atts.get("codebase") != null) {
             try {
                 URL appletSrcURL = new URL(codeBase + (String) atts.get("codebase"));
                 codeBase = appletSrcURL.getProtocol() + "://" + appletSrcURL.getHost();
             } catch (MalformedURLException mfue) {
                 // do nothing
             }
         }


         // Wait for the panel to initialize
         // (happens in a separate thread)
         Applet a;
         
         // Wait for panel to come alive
         int maxWait = PluginAppletViewer.APPLET_TIMEOUT; // wait for panel to come alive
         int wait = 0;
         while ((panel == null) || (!((NetxPanel) panel).isAlive() && wait < maxWait)) {
              try {
                  Thread.sleep(50);
                  wait += 50;
              } catch (InterruptedException ie) {
                  // just wait
              }
         }
         
         // Wait for the panel to initialize
         // (happens in a separate thread)
         while (panel.getApplet() == null &&
                ((NetxPanel) panel).isAlive()) {
             try {
                 Thread.sleep(50);
                 PluginDebug.debug("Waiting for applet to initialize...");
             } catch (InterruptedException ie) {
                 // just wait
             }
         }

         a = panel.getApplet();

         // Still null?
         if (panel.getApplet() == null) {
             streamhandler.write("instance " + identifier + " reference " + -1 + " fatalError " + "Initialization failed");
             return null;
         }

         PluginDebug.debug("Applet " + a.getClass() + " initialized");
         streamhandler.write("instance " + identifier + " reference 0 initialized");
         
         AppletSecurityContextManager.getSecurityContext(0).associateSrc(((NetxPanel) panel).getAppletClassLoader(), doc);
         AppletSecurityContextManager.getSecurityContext(0).associateInstance(identifier, ((NetxPanel) panel).getAppletClassLoader());
         
         return panel;
     }
 
     public boolean isStandalone()
     {
         return false;
     }
     
     /**
      * Send the initial set of events to the appletviewer event queue.
      * On start-up the current behaviour is to load the applet and call
      * Applet.init() and Applet.start().
      */
     private void initEventQueue(AppletPanel panel) {
         // appletviewer.send.event is an undocumented and unsupported system
         // property which is used exclusively for testing purposes.
         PrivilegedAction pa = new PrivilegedAction() {
             public Object run() {
                 return System.getProperty("appletviewer.send.event");
             }
         };
         String eventList = (String) AccessController.doPrivileged(pa); 

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
             String [] events = splitSeparator(",", eventList);

             for (int i = 0; i < events.length; i++) {
                 PluginDebug.debug("Adding event to queue: " + events[i]);
                 if (events[i].equals("dispose"))
                     panel.sendEvent(AppletPanel.APPLET_DISPOSE);
                 else if (events[i].equals("load"))
                     panel.sendEvent(AppletPanel.APPLET_LOAD);
                 else if (events[i].equals("init"))
                     panel.sendEvent(AppletPanel.APPLET_INIT);
                 else if (events[i].equals("start"))
                     panel.sendEvent(AppletPanel.APPLET_START);
                 else if (events[i].equals("stop"))
                     panel.sendEvent(AppletPanel.APPLET_STOP);
                 else if (events[i].equals("destroy"))
                     panel.sendEvent(AppletPanel.APPLET_DESTROY);
                 else if (events[i].equals("quit"))
                     panel.sendEvent(AppletPanel.APPLET_QUIT);
                 else if (events[i].equals("error"))
                     panel.sendEvent(AppletPanel.APPLET_ERROR);
                 else
                     // non-fatal error if we get an unrecognized event
                     PluginDebug.debug("Unrecognized event name: " + events[i]);
             }

             while (!panel.emptyEventQueue()) ;
         }
     }
     
     
     /**
      * Split a string based on the presence of a specified separator.  Returns
      * an array of arbitrary length.  The end of each element in the array is
      * indicated by the separator of the end of the string.  If there is a
      * separator immediately before the end of the string, the final element
      * will be empty.  None of the strings will contain the separator.  Useful
      * when separating strings such as "foo/bar/bas" using separator "/".
      *
      * @param sep  The separator.
      * @param s    The string to split.
      * @return     An array of strings.  Each string in the array is determined
      *             by the location of the provided sep in the original string,
      *             s.  Whitespace not stripped.
      */
     private String [] splitSeparator(String sep, String s) {
         Vector v = new Vector();
         int tokenStart = 0;
         int tokenEnd   = 0;

         while ((tokenEnd = s.indexOf(sep, tokenStart)) != -1) {
             v.addElement(s.substring(tokenStart, tokenEnd));
             tokenStart = tokenEnd+1;
         }
         // Add the final element.
         v.addElement(s.substring(tokenStart));

         String [] retVal = new String[v.size()];
         v.copyInto(retVal);
         return retVal;
     }
 }
 
 class PluginParseRequest
 {
     long handle;
     String tag;
     String documentbase;
 }
 
 /*
  */
 // FIXME: declare JSProxy implementation
 public class PluginAppletViewer extends XEmbeddedFrame
     implements AppletContext, Printable {
     /**
      * Some constants...
      */
     private static String defaultSaveFile = "Applet.ser";
     
     private static enum PAV_INIT_STATUS {PRE_INIT, IN_INIT, INIT_COMPLETE, INACTIVE};

     /**
      * The panel in which the applet is being displayed.
      */
     AppletViewerPanel panel;
 
     /**
      * The status line.
      */
     Label label;
 
     /**
      * output status messages to this stream
      */
 
     PrintStream statusMsgStream;
 
     int identifier;
 
     private static HashMap<Integer, PluginParseRequest> requests = 
         new HashMap();
 
     // Instance identifier -> PluginAppletViewer object.
     private static HashMap<Integer, PluginAppletViewer> applets = 
         new HashMap();
     
     private static PluginStreamHandler streamhandler;
     
     private static PluginCallRequestFactory requestFactory;

     private static HashMap<Integer, PAV_INIT_STATUS> status = 
         new HashMap<Integer,PAV_INIT_STATUS>();

     
     private long handle = 0;
     private WindowListener windowEventListener = null;
     private AppletEventListener appletEventListener = null;
     
     public static final int APPLET_TIMEOUT = 180000;

     private static Long requestIdentityCounter = 0L;
     
     /**
      * Null constructor to allow instantiation via newInstance()
      */
     public PluginAppletViewer() {
     }

     public static void reFrame(PluginAppletViewer oldFrame, 
                         int identifier, PrintStream statusMsgStream, 
                         long handle, AppletViewerPanel panel) {

         PluginDebug.debug("Reframing " + panel);
         
         // SecurityManager MUST be set, and only privileged code may call reFrame()
         System.getSecurityManager().checkPermission(new AllPermission());

         // Same handle => nothing to do
         if (oldFrame != null && handle == oldFrame.handle)
             return;

         PluginAppletViewer newFrame = new PluginAppletViewer(handle, identifier, statusMsgStream, panel);

         if (oldFrame != null) {
             applets.remove(oldFrame.identifier);
             oldFrame.removeWindowListener(oldFrame.windowEventListener);
             panel.removeAppletListener(oldFrame.appletEventListener);
             oldFrame.remove(panel);
             oldFrame.dispose();
         }

         newFrame.add("Center", panel);
         newFrame.pack();

         newFrame.appletEventListener = new AppletEventListener(newFrame, newFrame);
         panel.addAppletListener(newFrame.appletEventListener);

         applets.put(identifier, newFrame);

         // dispose oldframe if necessary
         if (oldFrame != null) {
             oldFrame.dispose();
         }
         
         PluginDebug.debug(panel + " reframed");
     }

     /**
      * Create new plugin appletviewer frame
      */
     private PluginAppletViewer(long handle, final int identifier, 
                         PrintStream statusMsgStream, 
                         AppletViewerPanel appletPanel) {
         
         super(handle, true);
         this.statusMsgStream = statusMsgStream;
         this.identifier = identifier;
         this.panel = appletPanel;

         if (!appletPanels.contains(panel))
             appletPanels.addElement(panel);

         windowEventListener = new WindowAdapter() {

             public void windowClosing(WindowEvent evt) {
                 appletClose();
             }

             public void windowIconified(WindowEvent evt) {
                 appletStop();
             }

             public void windowDeiconified(WindowEvent evt) {
                 appletStart();
             }
         };

         addWindowListener(windowEventListener);

     }

     private static class AppletEventListener implements AppletListener  
     {
         final Frame frame;
         final PluginAppletViewer appletViewer;
  
         public AppletEventListener(Frame frame, PluginAppletViewer appletViewer)
         {
         this.frame = frame;
         this.appletViewer = appletViewer;
         }
  
         public void appletStateChanged(AppletEvent evt) 
         {
         AppletPanel src = (AppletPanel)evt.getSource();
  
         switch (evt.getID()) {
                      case AppletPanel.APPLET_RESIZE: {
             if(src != null) {
                 appletViewer.resize(appletViewer.preferredSize());
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
             if (a != null)
                 AppletPanel.changeFrameAppContext(frame, SunToolkit.targetToAppContext(a));
             else
                 AppletPanel.changeFrameAppContext(frame, AppContext.getAppContext());
  
             status.put(appletViewer.identifier, PAV_INIT_STATUS.INIT_COMPLETE);
             
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

     /**
      * Handle an incoming message from the plugin.
      */
     public static void handleMessage(int identifier, int reference, String message)
     {

         PluginDebug.debug("PAV handling: " + message);
         
         try {
             if (message.startsWith("handle")) {
                 
            	 // Extract the information from the message
            	 String[] msgParts = new String[4];
            	 for (int i=0; i < 3; i++) {
            		 int spaceLocation = message.indexOf(' ');
            		 int nextSpaceLocation = message.indexOf(' ', spaceLocation+1);
            		 msgParts[i] = message.substring(spaceLocation + 1, nextSpaceLocation);
            		 message = message.substring(nextSpaceLocation + 1);
            	 }
                 
            	 long handle = Long.parseLong(msgParts[0]);
            	 String width = msgParts[1];
            	 String height = msgParts[2];

            	 int spaceLocation = message.indexOf(' ', "tag".length()+1); 
            	 String documentBase = 
                     UrlUtil.decode(message.substring("tag".length() + 1, spaceLocation));
            	 String tag = message.substring(spaceLocation+1); 

            	 PluginDebug.debug ("Handle = " + handle + "\n" +
            	                    "Width = " + width + "\n" +
            	                    "Height = " + height + "\n" +
            	                    "DocumentBase = " + documentBase + "\n" +
            	                    "Tag = " + tag);

                     status.put(identifier, PAV_INIT_STATUS.PRE_INIT);
            	 PluginAppletViewer.parse
                 			(identifier, handle, width, height,
                 				new StringReader(tag),
                 				new URL(documentBase));
                     

                 int maxWait = APPLET_TIMEOUT; // wait for applet to fully load
                 int wait = 0;
                 while (!status.get(identifier).equals(PAV_INIT_STATUS.INIT_COMPLETE) &&
                         (wait < maxWait)) {

                      try {
                          Thread.sleep(50);
                          wait += 50;
                      } catch (InterruptedException ie) {
                          // just wait
                      }
                 }

                 if (!status.get(identifier).equals(PAV_INIT_STATUS.INIT_COMPLETE))
                     throw new Exception("Applet initialization timeout");
                 
             } else {
                 PluginDebug.debug ("Handling message: " + message + " instance " + identifier + " " + Thread.currentThread());

                 // Wait till initialization finishes
                 while (!applets.containsKey(identifier) && 
                         (
                           !status.containsKey(identifier) || 
                            status.get(identifier).equals(PAV_INIT_STATUS.PRE_INIT)
                         )
                        );
                 
                 // don't bother processing further for inactive applets
                 if (status.get(identifier).equals(PAV_INIT_STATUS.INACTIVE))
                     return;

                 applets.get(identifier).handleMessage(reference, message);
             }
         } catch (Exception e) {

             e.printStackTrace();
             
             // If an exception happened during pre-init, we need to update status
             if (status.get(identifier).equals(PAV_INIT_STATUS.PRE_INIT))
                 status.put(identifier, PAV_INIT_STATUS.INACTIVE);

             throw new RuntimeException("Failed to handle message: " + 
                     message + " for instance " + identifier, e);
         }
     }
 
     public void handleMessage(int reference, String message)
     {
         if (message.startsWith("width")) {

             // Wait for panel to come alive
             int maxWait = APPLET_TIMEOUT; // wait for panel to come alive
                     int wait = 0;
             while (!status.get(identifier).equals(PAV_INIT_STATUS.INIT_COMPLETE) && wait < maxWait) {

                          try {
                              Thread.sleep(50);
                              wait += 50;
                          } catch (InterruptedException ie) {
                              // just wait
                          }
                     }

             
             // 0 => width, 1=> width_value, 2 => height, 3=> height_value
             String[] dimMsg = message.split(" ");
             
             final int height = (int) (Integer.parseInt(dimMsg[3]));
             final int width = (int) (Integer.parseInt(dimMsg[1]));

             if (panel instanceof NetxPanel)
                 ((NetxPanel) panel).updateSizeInAtts(height, width);

             try {
                SwingUtilities.invokeAndWait(new Runnable() {
                     public void run() {

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

                         panel.setSize(1,1);
                         panel.validate();

                         panel.setSize(width, height);
                         panel.validate();

                         panel.applet.resize(width, height);
                         panel.applet.validate();
                     }
                 });
            } catch (InterruptedException e) {
                // do nothing
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // do nothing
                e.printStackTrace();
            }

         } else if (message.startsWith("destroy")) {
             dispose();
             status.put(identifier, PAV_INIT_STATUS.INACTIVE);
         } else if (message.startsWith("GetJavaObject")) {

             // FIXME: how do we determine what security context this
             // object should belong to?
             Object o;

             // Wait for panel to come alive
             int maxWait = APPLET_TIMEOUT; // wait for panel to come alive
             int wait = 0;
             while ((panel == null) || (!((NetxPanel) panel).isAlive() && wait < maxWait)) {
                  try {
                      Thread.sleep(50);
                      wait += 50;
                  } catch (InterruptedException ie) {
                      // just wait
                  }
             }
             
             // Wait for the panel to initialize
             // (happens in a separate thread)
             while (panel.getApplet() == null &&
                    ((NetxPanel) panel).isAlive()) {
                 try {
                     Thread.sleep(50);
                     PluginDebug.debug("Waiting for applet to initialize...");
                 } catch (InterruptedException ie) {
                     // just wait
                 }
             }

             PluginDebug.debug(panel + " -- " + panel.getApplet() + " -- " + ((NetxPanel) panel).isAlive());

             // Still null?
             if (panel.getApplet() == null) {
                 this.streamhandler.write("instance " + identifier + " reference " + -1 + " fatalError " + "Initialization failed");
                 return;
             }

             o = panel.getApplet();
             PluginDebug.debug ("Looking for object " + o + " panel is " + panel);
             AppletSecurityContextManager.getSecurityContext(0).store(o);
             PluginDebug.debug ("WRITING 1: " + "context 0 reference " + reference + " GetJavaObject "
                                 + AppletSecurityContextManager.getSecurityContext(0).getIdentifier(o));
             streamhandler.write("context 0 reference " + reference + " GetJavaObject "
                              + AppletSecurityContextManager.getSecurityContext(0).getIdentifier(o));
             PluginDebug.debug ("WRITING 1 DONE");
         }
     }

     // FIXME: Kind of hackish way to ensure synchronized re-drawing 
     private synchronized void forceredraw() {
         doLayout();
     }

     /*
      * Methods for java.applet.AppletContext
      */
 
     private static Map audioClips = new HashMap();
 
     /**
      * Get an audio clip.
      */
     public AudioClip getAudioClip(URL url) {
    checkConnect(url);
    synchronized (audioClips) {
        AudioClip clip = (AudioClip)audioClips.get(url);
        if (clip == null) {
        audioClips.put(url, clip = new AppletAudioClip(url));
        }
        return clip;
    }
     }
 
     private static Map imageRefs = new HashMap();
 
     /**
      * Get an image.
      */
     public Image getImage(URL url) {
    return getCachedImage(url);
     }
 
     private Image getCachedImage(URL url) {
    // System.getSecurityManager().checkConnection(url.getHost(), url.getPort());
    return (Image)getCachedImageRef(url).get();
     }
 
     /**
      * Get an image ref.
      */
     private synchronized Ref getCachedImageRef(URL url) {
         PluginDebug.debug("getCachedImageRef() searching for " + url);
         
         try {

             String originalURL = url.toString();
             String codeBase = panel.getCodeBase().toString();

             if (originalURL.startsWith(codeBase)) {

                 PluginDebug.debug("getCachedImageRef() got URL = " + url);
                 PluginDebug.debug("getCachedImageRef() plugin codebase = " + codeBase);

                 // try to fetch it locally
                 if (panel instanceof NetxPanel) {

                     URL localURL = null;
                     
                     String resourceName = originalURL.substring(codeBase.length()); 
                     JNLPClassLoader loader = (JNLPClassLoader) ((NetxPanel) panel).getAppletClassLoader(); 

                     if (loader.resourceAvailableLocally(resourceName))
                         localURL = loader.getResource(resourceName);

                     url = localURL != null ? localURL : url;
                 }
             }

             PluginDebug.debug("getCachedImageRef() getting img from URL = " + url);

             synchronized (imageRefs) {
                 AppletImageRef ref = (AppletImageRef)imageRefs.get(url);
                 if (ref == null) {
                     ref = new AppletImageRef(url);
                     imageRefs.put(url, ref);
                 }
                 return ref;
             }
         } catch (Exception e) {
             System.err.println("Error occurred when trying to fetch image:");
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Flush the image cache.
      */
     static void flushImageCache() {
    imageRefs.clear();
     }
 
     static Vector appletPanels = new Vector();
 
     /**
      * Get an applet by name.
      */
     public Applet getApplet(String name) {
    name = name.toLowerCase();
    SocketPermission panelSp =
        new SocketPermission(panel.getCodeBase().getHost(), "connect");
    for (Enumeration e = appletPanels.elements() ; e.hasMoreElements() ;) {
        AppletPanel p = (AppletPanel)e.nextElement();
        String param = p.getParameter("name");
        if (param != null) {
        param = param.toLowerCase();
        }
        if (name.equals(param) &&
        p.getDocumentBase().equals(panel.getDocumentBase())) {
 
        SocketPermission sp =
            new SocketPermission(p.getCodeBase().getHost(), "connect");
 
        if (panelSp.implies(sp)) {
            return p.applet;
        }
        }
    }
    return null;
     }
 
     /**
      * Return an enumeration of all the accessible
      * applets on this page.
      */
     public Enumeration getApplets() {
    Vector v = new Vector();
    SocketPermission panelSp =
        new SocketPermission(panel.getCodeBase().getHost(), "connect");
 
    for (Enumeration e = appletPanels.elements() ; e.hasMoreElements() ;) {
        AppletPanel p = (AppletPanel)e.nextElement();
        if (p.getDocumentBase().equals(panel.getDocumentBase())) {
 
        SocketPermission sp =
            new SocketPermission(p.getCodeBase().getHost(), "connect");
        if (panelSp.implies(sp)) {
            v.addElement(p.applet);
        }
        }
    }
    return v.elements();
     }
 
     /**
      * Ignore.
      */
     public void showDocument(URL url) {
         PluginDebug.debug("Showing document...");
    showDocument(url, "_self");
     }
 
     /**
      * Ignore.
      */
     public void showDocument(URL url, String target) {
    try {
             // FIXME: change to postCallRequest
        write("url " + UrlUtil.encode(url.toString(), "UTF-8") + " " + target);
    } catch (IOException exception) {
        // Deliberately ignore IOException.  showDocument may be
        // called from threads other than the main thread after
        // streamhandler.pluginOutputStream has been closed.
    }
     }
 
     /**
      * Show status.
      */
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
     private static Long getRequestIdentifier() {
         synchronized (requestIdentityCounter) {

             if (requestIdentityCounter == Long.MAX_VALUE)
                 requestIdentityCounter = 0L;
             
             return requestIdentityCounter++;
        }
     }

     public long getWindow() {
         PluginDebug.debug ("STARTING getWindow");
         Long reference = getRequestIdentifier();

         PluginCallRequest request = requestFactory.getPluginCallRequest("window",
             "instance " + identifier + " reference " +  
             + reference + " " + "GetWindow", reference);

         PluginDebug.debug ("STARTING postCallRequest");
         streamhandler.postCallRequest(request);
         PluginDebug.debug ("STARTING postCallRequest done");
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait request 2");
                 while ((Long) request.getObject() == 0)
                     request.wait();
                 PluginDebug.debug ("wait request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                     e);
         }

         PluginDebug.debug ("STARTING getWindow DONE");
         return (Long) request.getObject();
     }
 
     // FIXME: make private, access via reflection.
     public static Object getMember(long internal, String name)
     {
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
             PluginDebug.debug ("wait getMEM request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait getMEM request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait getMEM request 3 GOT: " + request.getObject().getClass());
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" getMember DONE");
         return request.getObject();
     }
 
     public static void setMember(long internal, String name, Object value) {
         System.err.println("Setting to class " + value.getClass() + ":" + value.getClass().isPrimitive());
         AppletSecurityContextManager.getSecurityContext(0).store(name);
         int nameID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(name);
         Long reference = getRequestIdentifier();

         // work on a copy of value, as we don't want to be manipulating 
         // complex objects
         String valueToSetTo;
         if (value instanceof java.lang.Byte ||
             value instanceof java.lang.Character ||
             value instanceof java.lang.Short ||
             value instanceof java.lang.Integer ||
             value instanceof java.lang.Long ||
             value instanceof java.lang.Float ||
             value instanceof java.lang.Double ||
             value instanceof java.lang.Boolean) {

             valueToSetTo = "literalreturn " + value.toString();

             // Character -> Str results in str value.. we need int value as 
             // per specs.
             if (value instanceof java.lang.Character) {
                 valueToSetTo = "literalreturn " + (int) ((java.lang.Character) value).charValue();                 
             } else if (value instanceof Float ||
                        value instanceof Double) {
                 valueToSetTo = "literalreturn " + String.format("%308.308e", value);
             } 
             
         } else {
             AppletSecurityContextManager.getSecurityContext(0).store(value);
             valueToSetTo = Integer.toString(AppletSecurityContextManager.getSecurityContext(0).getIdentifier(value));
         }
 
         // Prefix with dummy instance for convenience.
         PluginCallRequest request = requestFactory.getPluginCallRequest("void",
             "instance " + 0 + " reference " + reference + " SetMember " + 
             internal + " " + nameID + " " + valueToSetTo, reference);

         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait setMem request: " + request.getMessage());
             PluginDebug.debug ("wait setMem request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait setMem request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait setMem request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" setMember DONE");
     }
 
     // FIXME: handle long index as well.
     public static void setSlot(long internal, int index, Object value) {
         AppletSecurityContextManager.getSecurityContext(0).store(value);
         Long reference = getRequestIdentifier();
         
         // work on a copy of value, as we don't want to be manipulating 
         // complex objects
         String valueToSetTo;
         if (value instanceof java.lang.Byte ||
             value instanceof java.lang.Character ||
             value instanceof java.lang.Short ||
             value instanceof java.lang.Integer ||
             value instanceof java.lang.Long ||
             value instanceof java.lang.Float ||
             value instanceof java.lang.Double ||
             value instanceof java.lang.Boolean) {

             valueToSetTo = "literalreturn " + value.toString();

             // Character -> Str results in str value.. we need int value as 
             // per specs.
             if (value instanceof java.lang.Character) {
                 valueToSetTo = "literalreturn " + (int) ((java.lang.Character) value).charValue();                 
             } else if (value instanceof Float ||
                        value instanceof Double) {
                 valueToSetTo = "literalreturn " + String.format("%308.308e", value);
             } 
             
         } else {
             AppletSecurityContextManager.getSecurityContext(0).store(value);
             valueToSetTo = Integer.toString(AppletSecurityContextManager.getSecurityContext(0).getIdentifier(value));
         }
 
         // Prefix with dummy instance for convenience.
         PluginCallRequest request = requestFactory.getPluginCallRequest("void",
             "instance " + 0 + " reference " + reference + " SetSlot " + 
             internal + " " + index + " " + valueToSetTo, reference);

         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait setSlot request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait setSlot request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait setSlot request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" setSlot DONE");
     }
 
     public static Object getSlot(long internal, int index)
     {
         Long reference = getRequestIdentifier();

         // Prefix with dummy instance for convenience.
         PluginCallRequest request = requestFactory.getPluginCallRequest("member", 
             "instance " + 0 + " reference " + reference + " GetSlot " + 
             internal + " " + index, reference);
         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait getSlot request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait getSlot request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait getSlot request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" getSlot DONE");
         return request.getObject();
     }
 
     public static Object eval(long internal, String s)
     {
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
             PluginDebug.debug ("wait eval request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait eval request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait eval request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" getSlot DONE");
         return request.getObject();
     }
 
     public static void removeMember (long internal, String name) {
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
             PluginDebug.debug ("wait removeMember request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait removeMember request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait removeMember request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" RemoveMember DONE");
     }
 
     public static Object call(long internal, String name, Object args[])
     {
         // FIXME: when is this removed from the object store?
         // FIXME: reference should return the ID.
         // FIXME: convenience method for this long line.
         AppletSecurityContextManager.getSecurityContext(0).store(name);
         int nameID = AppletSecurityContextManager.getSecurityContext(0).getIdentifier(name);
         Long reference = getRequestIdentifier();
         
         String argIDs = "";
         for (Object arg : args)
         {
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
             PluginDebug.debug ("wait call request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait call request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait call request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" Call DONE");
         return request.getObject();
     }
 
     public static Object requestPluginCookieInfo(URI uri) {

         PluginCallRequest request;
         Long reference = getRequestIdentifier();

         try
         {
             String encodedURI = UrlUtil.encode(uri.toString(), "UTF-8"); 
             request = requestFactory.getPluginCallRequest("cookieinfo",
                 "plugin PluginCookieInfo " + "reference " + reference + 
                 " " + encodedURI, reference);

         } catch (UnsupportedEncodingException e)
         {
             e.printStackTrace();
             return null;
         }

         PluginMessageConsumer.registerPriorityWait(reference);
         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait cookieinfo request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait cookieinfo request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait cookieinfo request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for cookieinfo request.",
                                        e);
         }
         PluginDebug.debug (" Cookieinfo DONE");
         return request.getObject();
     }

     public static Object requestPluginProxyInfo(URI uri) {

         String requestURI = null;
         Long reference = getRequestIdentifier();

         try {

             // there is no easy way to get SOCKS proxy info. So, we tell mozilla that we want proxy for 
             // an HTTP uri in case of non http/ftp protocols. If we get back a SOCKS proxy, we can 
             // use that, if we get back an http proxy, we fallback to DIRECT connect

             String scheme = uri.getScheme();
             String port = uri.getPort() != -1 ? ":" + uri.getPort() : ""; 
             if (!uri.getScheme().startsWith("http") && !uri.getScheme().equals("ftp"))
                 scheme = "http";

             requestURI = UrlUtil.encode(scheme + "://" + uri.getHost() + port + "/" + uri.getPath(), "UTF-8");
         } catch (Exception e) {
             PluginDebug.debug("Cannot construct URL from " + uri.toString() + " ... falling back to DIRECT proxy");
             e.printStackTrace();
             return null;
         }

         PluginCallRequest request = requestFactory.getPluginCallRequest("proxyinfo",
             "plugin PluginProxyInfo reference " + reference + " " + 
             requestURI, reference);

         PluginMessageConsumer.registerPriorityWait(reference);
         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait call request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait call request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait call request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" Call DONE");
         return request.getObject();
     }
     
     public static void JavaScriptFinalize(long internal)
     {
         Long reference = getRequestIdentifier();

         // Prefix with dummy instance for convenience.
         PluginCallRequest request = requestFactory.getPluginCallRequest("void",
             "instance " + 0 + " reference " + reference + " Finalize " + 
             internal, reference);

         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait finalize request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait finalize request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait finalize request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" finalize DONE");
     }
 
     public static String javascriptToString(long internal)
     {
         Long reference = getRequestIdentifier();

         // Prefix with dummy instance for convenience.
         PluginCallRequest request = requestFactory.getPluginCallRequest("member",
             "instance " + 0 + " reference " + reference + " ToString " + 
             internal, reference);

         streamhandler.postCallRequest(request);
         streamhandler.write(request.getMessage());
         try {
             PluginDebug.debug ("wait ToString request 1");
             synchronized(request) {
                 PluginDebug.debug ("wait ToString request 2");
                 while (request.isDone() == false)
                     request.wait();
                 PluginDebug.debug ("wait ToString request 3");
             }
         } catch (InterruptedException e) {
             throw new RuntimeException("Interrupted waiting for call request.",
                                        e);
         }
         PluginDebug.debug (" ToString DONE");
         return (String) request.getObject();
     }
 
     // FIXME: make this private and access it from JSObject using
     // reflection.
     private void write(String message) throws IOException {
         PluginDebug.debug ("WRITING 2: " + "instance " + identifier + " " + message);
         streamhandler.write("instance " + identifier + " " + message);
         PluginDebug.debug ("WRITING 2 DONE");
     }

     public void setStream(String key, InputStream stream)throws IOException{
    // We do nothing.
     }
 
     public InputStream getStream(String key){
    // We do nothing.
    return null;
     }
 
     public Iterator getStreamKeys(){
    // We do nothing.
    return null;
     }
 
     /**
      * System parameters.
      */
     static Hashtable systemParam = new Hashtable();
 
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
      * Print the HTML tag.
      */
     public static void printTag(PrintStream out, Hashtable atts) {
    out.print("<applet");
 
    String v = (String)atts.get("codebase");
    if (v != null) {
        out.print(" codebase=\"" + v + "\"");
    }
 
    v = (String)atts.get("code");
    if (v == null) {
        v = "applet.class";
    }
    out.print(" code=\"" + v + "\"");
    v = (String)atts.get("width");
    if (v == null) {
        v = "150";
    }
    out.print(" width=" + v);
 
    v = (String)atts.get("height");
    if (v == null) {
        v = "100";
    }
    out.print(" height=" + v);
 
    v = (String)atts.get("name");
    if (v != null) {
        out.print(" name=\"" + v + "\"");
    }
    out.println(">");
 
    // A very slow sorting algorithm
    int len = atts.size();
    String params[] = new String[len];
    len = 0;
    for (Enumeration e = atts.keys() ; e.hasMoreElements() ;) {
        String param = (String)e.nextElement();
        int i = 0;
        for (; i < len ; i++) {
        if (params[i].compareTo(param) >= 0) {
            break;
        }
        }
        System.arraycopy(params, i, params, i + 1, len - i);
        params[i] = param;
        len++;
    }
 
    for (int i = 0 ; i < len ; i++) {
        String param = params[i];
        if (systemParam.get(param) == null) {
        out.println("<param name=" + param +
                " value=\"" + atts.get(param) + "\">");
        }
    }
    out.println("</applet>");
     }
 
     /**
      * Make sure the atrributes are uptodate.
      */
     public void updateAtts() {
    Dimension d = panel.size();
    Insets in = panel.insets();
    panel.atts.put("width",
               new Integer(d.width - (in.left + in.right)).toString());
    panel.atts.put("height",
               new Integer(d.height - (in.top + in.bottom)).toString());
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
     * Fixed #4501142: Classlaoder sharing policy doesn't 
     * take "archive" into account. This will be overridden
     * by Java Plug-in.         [stanleyh]
     */
    AppletPanel.flushClassLoader(panel.getClassLoaderCacheKey());
 
         /*
          * Make sure we don't have two threads running through the event queue
          * at the same time.
          */
         try {
             panel.joinAppletThread();
        panel.release();
         } catch (InterruptedException e) {
             return;   // abort the reload
         }
 
         AccessController.doPrivileged(new PrivilegedAction() {
             public Object run() {
                 panel.createAppletThread();
                 return null;
             }
         });     
    
    panel.sendEvent(AppletPanel.APPLET_LOAD);
    panel.sendEvent(AppletPanel.APPLET_INIT);
    panel.sendEvent(AppletPanel.APPLET_START);
     }
 
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

         new Thread(new Runnable()
         {
             public void run()
             {
                 ThreadGroup tg = ((JNLPClassLoader) p.applet.getClass().getClassLoader()).getApplication().getThreadGroup();
                
                 appletShutdown(p);
                 appletPanels.removeElement(p);
                 dispose();

                 if (tg.activeCount() > 0)
                 tg.stop();

                 if (countApplets() == 0) {
                     appletSystemExit();
                 }
             }
         }).start();

         status.put(identifier, PAV_INIT_STATUS.INACTIVE);
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
      */
 
     public static int countApplets() {
    return appletPanels.size();
     }
 
 
     /**
      * Scan spaces.
      */
     public static void skipSpace(int[] c, Reader in) throws IOException {
         while ((c[0] >= 0) &&
           ((c[0] == ' ') || (c[0] == '\t') || (c[0] == '\n') || (c[0] == '\r'))) {
        	 c[0] = in.read();
    }
     }
 
     /**
      * Scan identifier
      */
     public static String scanIdentifier(int[] c, Reader in) throws IOException {
    StringBuffer buf = new StringBuffer();
    
    if (c[0] == '!') {
        // Technically, we should be scanning for '!--' but we are reading 
        // from a stream, and there is no way to peek ahead. That said, 
        // a ! at this point can only mean comment here afaik, so we 
        // should be okay
        skipComment(c, in);
        return "";
    }
    
    while (true) {
        if (((c[0] >= 'a') && (c[0] <= 'z')) ||
        ((c[0] >= 'A') && (c[0] <= 'Z')) ||
        ((c[0] >= '0') && (c[0] <= '9')) || (c[0] == '_')) {
        buf.append((char)c[0]);
        c[0] = in.read();
        } else {
        return buf.toString();
        }
    }
     }

     public static void skipComment(int[] c, Reader in) throws IOException {
         StringBuffer buf = new StringBuffer();
         boolean commentHeaderPassed = false;
         c[0] = in.read();
         buf.append((char)c[0]);

         while (true) {
             if (c[0] == '-' && (c[0] = in.read()) == '-') {
                 buf.append((char)c[0]);
                 if (commentHeaderPassed) {
                     // -- encountered ... is > next?
                     if ((c[0] = in.read()) == '>') {
                         buf.append((char)c[0]);

                         PluginDebug.debug("Comment skipped: " + buf.toString());

                         // comment skipped.
                         return;
                     }
                 } else {
                     // first -- is part of <!-- ... , just mark that we have passed it
                     commentHeaderPassed = true;
                 }

             } else if (commentHeaderPassed == false) {
                 buf.append((char)c[0]);
                 PluginDebug.debug("Warning: Attempted to skip comment, but this tag does not appear to be a comment: " + buf.toString());
                 return;
             }

             c[0] = in.read();
             buf.append((char)c[0]);
         }
     }
 
     /**
      * Scan tag
      */
     public static Hashtable scanTag(int[] c, Reader in) throws IOException {
    Hashtable atts = new Hashtable();
    skipSpace(c, in);
         while (c[0] >= 0 && c[0] != '>') {
        String att = scanIdentifier(c, in);
        String val = "";
        skipSpace(c, in);
        if (c[0] == '=') {
        int quote = -1;
        c[0] = in.read();
        skipSpace(c, in);
        if ((c[0] == '\'') || (c[0] == '\"')) {
            quote = c[0];
            c[0] = in.read();
        }
        StringBuffer buf = new StringBuffer();
                 while ((c[0] > 0) &&
               (((quote < 0) && (c[0] != ' ') && (c[0] != '\t') &&
                          (c[0] != '\n') && (c[0] != '\r') && (c[0] != '>'))
            || ((quote >= 0) && (c[0] != quote)))) {
            buf.append((char)c[0]);
            c[0] = in.read();
        }
        if (c[0] == quote) {
            c[0] = in.read();
        }
        skipSpace(c, in);
        val = buf.toString();
        }

        att = att.replace("&gt;", ">");
        att = att.replace("&lt;", "<");
        att = att.replace("&amp;", "&");
        att = att.replace("&#10;", "\n");
        att = att.replace("&#13;", "\r");
        
        val = val.replace("&gt;", ">");
        val = val.replace("&lt;", "<");
        val = val.replace("&amp;", "&");
        val = val.replace("&#10;", "\n");
        val = val.replace("&#13;", "\r");

        PluginDebug.debug("PUT " + att + " = '" + val + "'");
        atts.put(att.toLowerCase(java.util.Locale.ENGLISH), val);

             while (true) {
                 if ((c[0] == '>') || (c[0] < 0) ||
                     ((c[0] >= 'a') && (c[0] <= 'z')) ||
                     ((c[0] >= 'A') && (c[0] <= 'Z')) ||
                     ((c[0] >= '0') && (c[0] <= '9')) || (c[0] == '_'))
                     break;
                 c[0] = in.read();
             }
             //skipSpace(in);
    }
    return atts;
     }
     
     // private static final == inline
     private static final boolean isInt(Object o) {
         boolean isInt = false;

         try {
             Integer.parseInt((String) o);
             isInt = true;
         } catch (Exception e) {
             // don't care
         }

         return isInt;
     }
 
     /* values used for placement of AppletViewer's frames */
     private static int x = 0;
     private static int y = 0;
     private static final int XDELTA = 30;
     private static final int YDELTA = XDELTA;
 
     static String encoding = null;
 
     static private Reader makeReader(InputStream is) {
    if (encoding != null) {
        try {
        return new BufferedReader(new InputStreamReader(is, encoding));
        } catch (IOException x) { }
    }
    InputStreamReader r = new InputStreamReader(is);
    encoding = r.getEncoding();
    return new BufferedReader(r);
     }
 
     /**
      * Scan an html file for <applet> tags
      */
     public static void parse(int identifier, long handle, String width, String height, Reader in, URL url, String enc)
         throws IOException {
         encoding = enc;
         parse(identifier, handle, width, height, in, url, System.out, new PluginAppletPanelFactory());
     }
 
     public static void parse(int identifier, long handle, String width, String height, Reader in, URL url)
         throws IOException {
         
         final int fIdentifier = identifier;
         final long fHandle = handle;
         final String fWidth = width;
         final String fHeight = height;
         final Reader fIn = in;
         final URL fUrl = url;
         PrivilegedAction pa = new PrivilegedAction() {
             public Object run() {
                 try {
                     parse(fIdentifier, fHandle, fWidth, fHeight, fIn, fUrl, System.out, new PluginAppletPanelFactory());
                 } catch (IOException ioe) {
                     return ioe;
                 }
                 
                 return null;
             }
         };

         Object ret = AccessController.doPrivileged(pa);
         if (ret instanceof IOException) {
             throw (IOException) ret;
         }
     }
 
     public static void parse(int identifier, long handle, String width, 
    		 String height, Reader in, URL url,
                              PrintStream statusMsgStream,
                              PluginAppletPanelFactory factory)
         throws IOException
     {
         // <OBJECT> <EMBED> tag flags
         boolean isAppletTag = false;
         boolean isObjectTag = false;
         boolean isEmbedTag = false;
         boolean objectTagAlreadyParsed = false;
         // The current character
         // FIXME: This is an evil hack to force pass-by-reference.. the 
         // parsing code needs to be rewritten from scratch to prevent such 
         //a need
         int[] c = new int[1];

         // warning messages
         String requiresNameWarning = amh.getMessage("parse.warning.requiresname");
         String paramOutsideWarning = amh.getMessage("parse.warning.paramoutside");
         String appletRequiresCodeWarning = amh.getMessage("parse.warning.applet.requirescode");
         String appletRequiresHeightWarning = amh.getMessage("parse.warning.applet.requiresheight");
         String appletRequiresWidthWarning = amh.getMessage("parse.warning.applet.requireswidth");
         String objectRequiresCodeWarning = amh.getMessage("parse.warning.object.requirescode");
         String objectRequiresHeightWarning = amh.getMessage("parse.warning.object.requiresheight");
         String objectRequiresWidthWarning = amh.getMessage("parse.warning.object.requireswidth");
         String embedRequiresCodeWarning = amh.getMessage("parse.warning.embed.requirescode");
         String embedRequiresHeightWarning = amh.getMessage("parse.warning.embed.requiresheight");
         String embedRequiresWidthWarning = amh.getMessage("parse.warning.embed.requireswidth");
         String appNotLongerSupportedWarning = amh.getMessage("parse.warning.appnotLongersupported");

         java.net.URLConnection conn = url.openConnection();
         /* The original URL may have been redirected - this
          * sets it to whatever URL/codebase we ended up getting
          */
         url = conn.getURL();

         int ydisp = 1;
         Hashtable atts = null;

         while(true) {
             c[0] = in.read();
             if (c[0] == -1)
                 break;

             if (c[0] == '<') {
                 c[0] = in.read();
                 if (c[0] == '/') {
                     c[0] = in.read();
                     String nm = scanIdentifier(c, in);
                     if (nm.equalsIgnoreCase("applet") ||
                             nm.equalsIgnoreCase("object") ||
                             nm.equalsIgnoreCase("embed")) {

                         // We can't test for a code tag until </OBJECT>
                         // because it is a parameter, not an attribute.
                         if(isObjectTag) {
                             if (atts.get("code") == null && atts.get("object") == null) {
                                 statusMsgStream.println(objectRequiresCodeWarning);
                                 atts = null;
                             }
                         }

                         if (atts != null) {
                             // XXX 5/18 In general this code just simply
                             // shouldn't be part of parsing.  It's presence
                             // causes things to be a little too much of a
                             // hack.
                             
                             // Let user know we are starting up
                             streamhandler.write("instance " + identifier + " status " + amh.getMessage("status.start"));
                             factory.createPanel(streamhandler, identifier, handle, x, y, url, atts);
                             
                             x += XDELTA;
                             y += YDELTA;
                             // make sure we don't go too far!
                             Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                             if ((x > d.width - 300) || (y > d.height - 300)) {
                                 x = 0;
                                 y = 2 * ydisp * YDELTA;
                                 ydisp++;
                             }
                         }
                         atts = null;
                         isAppletTag = false;
                         isObjectTag = false;
                         isEmbedTag = false;
                     }
                 }
                 else {
                     String nm = scanIdentifier(c, in);
                     if (nm.equalsIgnoreCase("param")) {
                         Hashtable t = scanTag(c, in);
                         String att = (String)t.get("name");

                         if (atts.containsKey(att))
                             continue;

                         if (att == null) {
                             statusMsgStream.println(requiresNameWarning);
                         } else {
                             String val = (String)t.get("value");
                             if (val == null) {
                                 statusMsgStream.println(requiresNameWarning);
                             } else if (atts != null) {
                                 att = att.replace("&gt;", ">");
                                 att = att.replace("&lt;", "<");
                                 att = att.replace("&amp;", "&");
                                 att = att.replace("&#10;", "\n");
                                 att = att.replace("&#13;", "\r");
                                 att = att.replace("&quot;", "\"");

                                 val = val.replace("&gt;", ">");
                                 val = val.replace("&lt;", "<");
                                 val = val.replace("&amp;", "&");
                                 val = val.replace("&#10;", "\n");
                                 val = val.replace("&#13;", "\r");
                                 val = val.replace("&quot;", "\"");
                                 PluginDebug.debug("PUT " + att + " = " + val);
                                 atts.put(att.toLowerCase(), val);
                             } else {
                                 statusMsgStream.println(paramOutsideWarning);
                             }
                         }
                     }
                     else if (nm.equalsIgnoreCase("applet")) {
                         isAppletTag = true;
                         atts = scanTag(c, in);

                         // If there is a classid and no code tag present, transform it to code tag
                         if (atts.get("code") == null && atts.get("classid") != null && !((String) atts.get("classid")).startsWith("clsid:")) {
                             atts.put("code", atts.get("classid"));
                         }
                         
                         // remove java: from code tag
                         if (atts.get("code") != null && ((String) atts.get("code")).startsWith("java:")) {
                             atts.put("code", ((String) atts.get("code")).substring(5));
                         }

                         if (atts.get("code") == null && atts.get("object") == null) {
                             statusMsgStream.println(appletRequiresCodeWarning);
                             atts = null;
                         }

                         if (atts.get("width") == null || !isInt(atts.get("width"))) {
                             atts.put("width", width);
                          }

                         if (atts.get("height") == null || !isInt(atts.get("height"))) {
                             atts.put("height", height);
                         }
                     }
                     else if (nm.equalsIgnoreCase("object")) {
                         isObjectTag = true;

                         // Once code is set, additional nested objects are ignored
                         if (!objectTagAlreadyParsed) {
                             objectTagAlreadyParsed = true;
                         atts = scanTag(c, in);
                         }

                         // If there is a classid and no code tag present, transform it to code tag
                         if (atts.get("code") == null && atts.get("classid") != null && !((String) atts.get("classid")).startsWith("clsid:")) {
                             atts.put("code", atts.get("classid"));
                         }
                         
                         // remove java: from code tag
                         if (atts.get("code") != null && ((String) atts.get("code")).startsWith("java:")) {
                             atts.put("code", ((String) atts.get("code")).substring(5));
                         }

                         // java_* aliases override older names:
                         // http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/using_tags.html#in-ie
                         if (atts.get("java_code") != null) {
                             atts.put("code", ((String) atts.get("java_code")));
                         }

                         if (atts.containsKey("code")) {
                             objectTagAlreadyParsed = true;
                         }

                         if (atts.get("java_codebase") != null) {
                             atts.put("codebase", ((String) atts.get("java_codebase")));
                         }

                         if (atts.get("java_archive") != null) {
                             atts.put("archive", ((String) atts.get("java_archive")));
                         }

                         if (atts.get("java_object") != null) {
                             atts.put("object", ((String) atts.get("java_object")));
                         }

                         if (atts.get("java_type") != null) {
                             atts.put("type", ((String) atts.get("java_type")));
                         }

                         if (atts.get("width") == null || !isInt(atts.get("width"))) {
                             atts.put("width", width);
                         }

                         if (atts.get("height") == null || !isInt(atts.get("height"))) {
                             atts.put("height", height);
                         }
                     }
                     else if (nm.equalsIgnoreCase("embed")) {
                         isEmbedTag = true;
                         atts = scanTag(c, in);

                         // If there is a classid and no code tag present, transform it to code tag
                         if (atts.get("code") == null && atts.get("classid") != null && !((String) atts.get("classid")).startsWith("clsid:")) {
                             atts.put("code", atts.get("classid"));
                         }

                         // remove java: from code tag
                         if (atts.get("code") != null && ((String) atts.get("code")).startsWith("java:")) {
                             atts.put("code", ((String) atts.get("code")).substring(5));
                         }
                         
                         // java_* aliases override older names:
                         // http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/using_tags.html#in-nav
                         if (atts.get("java_code") != null) {
                             atts.put("code", ((String) atts.get("java_code")));
                         }
                         
                         if (atts.get("java_codebase") != null) {
                             atts.put("codebase", ((String) atts.get("java_codebase")));
                         }
                         
                         if (atts.get("java_archive") != null) {
                             atts.put("archive", ((String) atts.get("java_archive")));
                         }
                         
                         if (atts.get("java_object") != null) {
                             atts.put("object", ((String) atts.get("java_object")));
                         }
                         
                         if (atts.get("java_type") != null) {
                             atts.put("type", ((String) atts.get("java_type")));
                         }

                         if (atts.get("code") == null && atts.get("object") == null) {
                             statusMsgStream.println(embedRequiresCodeWarning);
                             atts = null;
                         }
                         
                         if (atts.get("width") == null || !isInt(atts.get("width"))) {
                             atts.put("width", width);
                         }

                         if (atts.get("height") == null || !isInt(atts.get("height"))) {
                             atts.put("height", height);
                         }
                     }
                 }
             }
         }
         in.close();
     }
 

     private static AppletMessageHandler amh = new AppletMessageHandler("appletviewer");
 
     private static void checkConnect(URL url)
     {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        try {
        java.security.Permission perm =
            url.openConnection().getPermission();
        if (perm != null)
            security.checkPermission(perm);
        else
            security.checkConnect(url.getHost(), url.getPort());
        } catch (java.io.IOException ioe) {
            security.checkConnect(url.getHost(), url.getPort());
        }
    }
     }
 }
