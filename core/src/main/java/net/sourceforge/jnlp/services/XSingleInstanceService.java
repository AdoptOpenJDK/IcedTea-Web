// Copyright (C) 2009 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.services;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jnlp.SingleInstanceListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements SingleInstanceService
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 */
public class XSingleInstanceService implements ExtendedSingleInstanceService {

    private final static Logger LOG = LoggerFactory.getLogger(XSingleInstanceService.class);

    boolean initialized = false;
    List<SingleInstanceListener> listeners = new LinkedList<SingleInstanceListener>();

    /**
     * Implements a server that listens for arguments from new instances of this
     * application
     *
     */
    class SingleInstanceServer implements Runnable {

        SingleInstanceLock lockFile = null;

        public SingleInstanceServer(SingleInstanceLock lockFile) {
            this.lockFile = lockFile;
        }

        public void run() {
            ServerSocket listeningSocket = null;
            try {
                listeningSocket = new ServerSocket(0);
                lockFile.createWithPort(listeningSocket.getLocalPort());

                LOG.debug("Starting SingleInstanceServer on port {}", listeningSocket);

                while (true) {
                    try {
                        Socket communicationSocket = listeningSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(communicationSocket
                                .getInputStream());
                        String[] arguments = (String[]) ois.readObject();
                        notifySingleInstanceListeners(arguments);
                    } catch (Exception exception) {
                        // not much to do here...
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exception);
                    }

                }
            } catch (IOException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } finally {
                if (listeningSocket != null) {
                    try {
                        listeningSocket.close();
                    } catch (IOException e) {
                        // Give up.
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                    }
                }
            }
        }
    }

    /**
     * Create a new XSingleInstanceService
     */
    protected XSingleInstanceService() {
    }

    /**
     * Initialize the new SingleInstanceService
     *
     * @throws InstanceExistsException if the instance already exists
     */
    public void initializeSingleInstance() {
        // this is called after the application has started. so safe to use
        // JNLPRuntime.getApplication()
        JNLPFile jnlpFile = JNLPRuntime.getApplication().getJNLPFile();
        if (!initialized || jnlpFile instanceof PluginBridge) {
            // Either a new process or a new applet being handled by the plugin.
            checkSingleInstanceRunning(jnlpFile);
            initialized = true;
            SingleInstanceLock lockFile;
            lockFile = new SingleInstanceLock(jnlpFile);
            if (!lockFile.isValid()) {
                startListeningServer(lockFile);
            }
        }

    }

    /**
     * Check if another instance of this application is already running
     *
     * @param jnlpFile The {@link JNLPFile} that specifies the application
     *
     * @throws InstanceExistsException if an instance of this application
     *         already exists
     */
    @Override
    public void checkSingleInstanceRunning(JNLPFile jnlpFile) {
        SingleInstanceLock lockFile = new SingleInstanceLock(jnlpFile);
        if (lockFile.isValid()) {
            int port = lockFile.getPort();
            LOG.debug("Lock file is valid (port={}). Exiting.", port);

            String[] args = null;
            if (jnlpFile.isApplet()) {
                throw new IllegalStateException("Applet not supported!");
            } else if (jnlpFile.isInstaller()) {
                // TODO Implement this once installer service is available.
            } else {
                args = jnlpFile.getApplication().getArguments();
            }

            try {
                sendProgramArgumentsToExistingApplication(port, args);
                throw new InstanceExistsException(String.valueOf(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Start the listening server to accept arguments from new instances of
     * applications
     *
     * @param lockFile
     *            the {@link SingleInstanceLock} that the server should use
     */
    private void startListeningServer(SingleInstanceLock lockFile) {
        SingleInstanceServer server = new SingleInstanceServer(lockFile);
        Thread serverThread = new Thread(server);
        /*
         * mark as daemon so the JVM can shutdown if the server is the only
         * thread running
         */
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Send the arguments for this application to the main instance
     *
     * @param port the port at which the SingleInstanceServer is listening at
     * @param arguments the new arguments
     * @throws IOException on any io exception
     */
    private void sendProgramArgumentsToExistingApplication(int port, String[] arguments)
            throws IOException {
        try {
            Socket serverCommunicationSocket = new Socket((String) null, port);
            ObjectOutputStream argumentStream = new ObjectOutputStream(serverCommunicationSocket
                    .getOutputStream());
            argumentStream.writeObject(arguments);
            argumentStream.close();
            serverCommunicationSocket.close();

        } catch (UnknownHostException unknownHost) {
            LOG.error("Unable to find localhost");
            throw new RuntimeException(unknownHost);
        }
    }

    /**
     * Notify any SingleInstanceListener with new arguments
     *
     * @param arguments the new arguments to the application
     */
    private void notifySingleInstanceListeners(String[] arguments) {
        for (SingleInstanceListener listener : listeners) {
            // TODO this proxy is privileged. should i worry about security in
            // methods being called?
            listener.newActivation(arguments);
        }
    }

    /**
     * Add the specified SingleInstanceListener
     *
     * @throws InstanceExistsException which is likely to terminate the
     *         application but not guaranteed to
     */
    public void addSingleInstanceListener(SingleInstanceListener sil) {
        initializeSingleInstance();

        if (sil == null) {
            return;
        }

        listeners.add(sil);
    }

    /**
     * Remove the specified SingleInstanceListener
     *
     * @throws InstanceExistsException if an instance of this single instance
     *         application already exists
     *
     */
    public void removeSingleInstanceListener(SingleInstanceListener sil) {
        initializeSingleInstance();

        if (sil == null) {
            return;
        }

        listeners.remove(sil);
    }

}
