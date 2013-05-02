/*   Copyright (C) 2013 Red Hat

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

/* Must be in sun.applet to access PluginAppletSecurityContext's constructor and PluginObjectStore */
package sun.applet;

import java.util.IdentityHashMap;

import sun.applet.mock.PluginPipeMock;

/*
 * Convenience class for PluginPipeMock.
 * Provides convenient methods for installing a custom pipe mock and cleaning it up.
 * 
 * Provides PipeMessageHandler interface and accompany convenience methods which can
 * be used to define mocked pipes in a simple manner.
 * */
public class PluginPipeMockUtil {

    /**************************************************************************
     *                          Basic setup & teardown                        *
     **************************************************************************/

    /* Maps PluginPipeMock instances to a ThreadGroup, allowing us to stop all the 
     * message handling threads that we started when setting up the mock pipes. */
    static private IdentityHashMap<PluginPipeMock, ThreadGroup> pipeToThreadGroup = new IdentityHashMap<PluginPipeMock, ThreadGroup>();

    /* By providing custom implementations of the input stream & output stream used by PluginStreamHandler,
     * we are able to mock the C++-side of the plugin. We do this by sending the messages the Java-side expects
     * to receive. Additionally, we are able to test that the Java-side sends the correct requests.
     * See PluginPipeMock for more details.
     */
    static private PluginPipeMock installPipeMock() {
        AppletSecurityContextManager.addContext(0, new PluginAppletSecurityContext(0, false /* no security manager */));

        PluginPipeMock pipeMock = new PluginPipeMock();

        PluginStreamHandler streamHandler = new PluginStreamHandler(pipeMock.getResponseInputStream(), pipeMock.getRequestOutputStream());
        PluginAppletViewer.setStreamhandler(streamHandler);
        PluginAppletViewer.setPluginCallRequestFactory(new PluginCallRequestFactory());

        streamHandler.startProcessing();

        return pipeMock;
    }


    /* Set up the mocked plugin pipe environment. See installPipeMock for details. */
    static public PluginPipeMock setupMockedMessageHandling() throws Exception {
        ThreadGroup pipeThreadGroup = new ThreadGroup("PluginAppletViewerTestThreadGroup") {
            public void uncaughtException(Thread t, Throwable e) {
                // Silent death for plugin message handler threads
            }
        };

        final PluginPipeMock[] pipeMock = {null};
        // Do set-up in a thread so we can pass along our thread-group, used for clean-up.
        Thread initThread = new Thread(pipeThreadGroup, "InstallPipeMockThread") {
            @Override
            public void run() {
                pipeMock[0] = installPipeMock();
            }
        };

        initThread.start();
        initThread.join();

        pipeToThreadGroup.put(pipeMock[0], pipeThreadGroup);
        return pipeMock[0];
    }
    
    /* Kill any message handling threads started when setting up the mocked pipes */
    @SuppressWarnings("deprecation")
    static public void cleanUpMockedMessageHandling(PluginPipeMock pipeMock) throws Exception {
        ThreadGroup pipeThreadGroup = pipeToThreadGroup.get(pipeMock);
        if (pipeThreadGroup != null) { 
            pipeThreadGroup.stop();
        }
        pipeToThreadGroup.remove(pipeMock);
    }

    /**************************************************************************
     *                          Object store utilities                        *
     **************************************************************************/
    /*
     * Helpers for manipulating the object mapping using to refer to objects in
     * the plugin
     */
    public static Object getPluginStoreObject(int id) {
        return PluginObjectStore.getInstance().getObject(id);
    }

    /* Stores the object if it is not yet stored */
    public static int getPluginStoreId(Object obj) {
        PluginObjectStore.getInstance().reference(obj);
        return PluginObjectStore.getInstance().getIdentifier(obj);
    }
}
