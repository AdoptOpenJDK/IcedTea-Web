/* VoidPluginCallRequest -- represent Java-to-JavaScript requests
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

package sun.applet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import javax.swing.SwingUtilities;

public class PluginStreamHandler {

    private BufferedReader pluginInputReader;
    private BufferedWriter pluginOutputWriter;

    private RequestQueue queue = new RequestQueue();

    private JavaConsole console = new JavaConsole();

    private PluginMessageConsumer consumer;
    private volatile boolean shuttingDown = false;


    public PluginStreamHandler(InputStream inputstream, OutputStream outputstream)
            throws MalformedURLException, IOException {

        PluginDebug.debug("Current context CL=", Thread.currentThread().getContextClassLoader());

        PluginDebug.debug("Creating consumer...");
        consumer = new PluginMessageConsumer(this);

        // Set up input and output pipes.  Use UTF-8 encoding.
        pluginInputReader =
                new BufferedReader(new InputStreamReader(inputstream,
                        Charset.forName("UTF-8")));
        pluginOutputWriter =
                new BufferedWriter(new OutputStreamWriter
                        (outputstream, Charset.forName("UTF-8")));
    }

    public void startProcessing() {

        Thread listenerThread = new Thread() {

            public void run() {

                while (true) {

                    PluginDebug.debug("Waiting for data...");

                    String s = read();

                    if (s != null) {
                        consumer.queue(s);
                    } else {
                        try {
                            // Close input/output channels to plugin.
                            pluginInputReader.close();
                            pluginOutputWriter.close();
                        } catch (IOException exception) {
                            // Deliberately ignore IOException caused by broken
                            // pipe since plugin may have already detached.
                        }
                        AppletSecurityContextManager.dumpStore(0);
                        PluginDebug.debug("APPLETVIEWER: exiting appletviewer");
                        System.exit(0);
                    }
                }
            }
        };

        listenerThread.start();
    }

    /**
     * Given a string, reads the first two (space separated) tokens.
     *
     * @param message The string to read
     * @param start The position to start reading at
     * @param array The array into which the first two tokens are placed
     * @return Position where the next token starts
     */
    private int readPair(String message, int start, String[] array) {
        
        int end = start;
        array[0] = null;
        array[1] = null;

        if (message.length() > start) {
            int firstSpace = message.indexOf(' ', start);
            if (firstSpace == -1) {
                array[0] = message.substring(start);
                end = message.length();
            } else {
                array[0] = message.substring(start, firstSpace);
                if (message.length() > firstSpace + 1) {
                    int secondSpace = message.indexOf(' ', firstSpace + 1);
                    if (secondSpace == -1) {
                        array[1] = message.substring(firstSpace + 1);
                        end = message.length();
                    } else {
                        array[1] = message.substring(firstSpace + 1, secondSpace);
                        end = secondSpace + 1;
                    }
                }
            }
        }

        PluginDebug.debug("readPair: '", array[0], "' - '", array[1], "' ", end);
        return end;
    }

    public void handleMessage(String message) throws PluginException {

        int reference = -1;
        String src = null;
        String[] privileges = null;
        String rest = "";
        String[] msgComponents = new String[2];
        int pos = 0;
        int oldPos = 0;

        pos = readPair(message, oldPos, msgComponents);
        if (msgComponents[0] == null || msgComponents[1] == null) {
            return;
        }

        if (msgComponents[0].startsWith("plugin")) {
            handlePluginMessage(message);
            return;
        }

        // type and identifier are guaranteed to be there
        String type = msgComponents[0];
        final int identifier = Integer.parseInt(msgComponents[1]);

        // reference, src and privileges are optional components, 
        // and are guaranteed to be in that order, if they occur
        oldPos = pos;
        pos = readPair(message, oldPos, msgComponents);

        // is there a reference ?
        if ("reference".equals(msgComponents[0])) {
            reference = Integer.parseInt(msgComponents[1]);
            oldPos = pos;
            pos = readPair(message, oldPos, msgComponents);
        }

        // is there a src?
        if ("src".equals(msgComponents[0])) {
            src = msgComponents[1];
            oldPos = pos;
            pos = readPair(message, oldPos, msgComponents);
        }

        // is there a privileges?
        if ("privileges".equals(msgComponents[0])) {
            String privs = msgComponents[1];
            privileges = privs.split(",");
            oldPos = pos;
        }

        // rest
        if (message.length() > oldPos) {
            rest = message.substring(oldPos);
        }

        try {

            PluginDebug.debug("Breakdown -- type: ", type, " identifier: ", identifier, " reference: ", reference, " src: ", src, " privileges: ", privileges, " rest: \"", rest, "\"");

            if (rest.contains("JavaScriptGetWindow")
                    || rest.contains("JavaScriptGetMember")
                    || rest.contains("JavaScriptSetMember")
                    || rest.contains("JavaScriptGetSlot")
                    || rest.contains("JavaScriptSetSlot")
                    || rest.contains("JavaScriptEval")
                    || rest.contains("JavaScriptRemoveMember")
                    || rest.contains("JavaScriptCall")
                    || rest.contains("JavaScriptFinalize")
                    || rest.contains("JavaScriptToString")) {

                finishCallRequest("reference " + reference + " " + rest);
                return;
            }

            final int freference = reference;
            final String frest = rest;

            if (type.equals("instance")) {
                PluginAppletViewer.handleMessage(identifier, freference, frest);
            } else if (type.equals("context")) {
                PluginDebug.debug("Sending to PASC: ", identifier, "/", reference, " and ", rest);
                AppletSecurityContextManager.handleMessage(identifier, reference, src, privileges, rest);
            }
        } catch (Exception e) {
            throw new PluginException(this, identifier, reference, e);
        }
    }

    private void handlePluginMessage(String message) {
        if (message.equals("plugin showconsole")) {
            showConsole();
        } else if (message.equals("plugin hideconsole")) {
            hideConsole();
        } else {
            // else this is something that was specifically requested
            finishCallRequest(message);
        }
    }

    public void postCallRequest(PluginCallRequest request) {
        synchronized (queue) {
            queue.post(request);
        }
    }

    private void finishCallRequest(String message) {
        PluginDebug.debug("DISPATCHCALLREQUESTS 1");
        synchronized (queue) {
            PluginDebug.debug("DISPATCHCALLREQUESTS 2");
            PluginCallRequest request = queue.pop();

            // make sure we give the message to the right request 
            // in the queue.. for the love of God, MAKE SURE!

            // first let's be efficient.. if there was only one 
            // request in queue, we're already set
            if (queue.size() != 0) {

                int size = queue.size();
                int count = 0;

                while (!request.serviceable(message)) {

                    PluginDebug.debug(request, " cannot service ", message);

                    // something is very wrong.. we have a message to 
                    // process, but no one to service it
                    if (count >= size) {
                        throw new RuntimeException("Unable to find processor for message " + message);
                    }

                    // post request at the end of the queue
                    queue.post(request);

                    // Look at the next request
                    request = queue.pop();

                    count++;
                }

            }

            PluginDebug.debug("DISPATCHCALLREQUESTS 3");
            if (request != null) {
                PluginDebug.debug("DISPATCHCALLREQUESTS 5");
                synchronized (request) {
                    request.parseReturn(message);
                    request.notifyAll();
                }
                PluginDebug.debug("DISPATCHCALLREQUESTS 6");
                PluginDebug.debug("DISPATCHCALLREQUESTS 7");
            }
        }
        PluginDebug.debug("DISPATCHCALLREQUESTS 8");
    }

    /**
     * Read string from plugin.
     *
     * @return the read string
     *
     * @exception IOException if an error occurs
     */
    private String read() {
        String message = null;

        try {
            message = pluginInputReader.readLine();
            PluginDebug.debug("  PIPE: appletviewer read: ", message);

            if (message == null || message.equals("shutdown")) {
                shuttingDown = true;
                try {
                    // Close input/output channels to plugin.
                    pluginInputReader.close();
                    pluginOutputWriter.close();
                } catch (IOException exception) {
                    // Deliberately ignore IOException caused by broken
                    // pipe since plugin may have already detached.
                }
                AppletSecurityContextManager.dumpStore(0);
                PluginDebug.debug("APPLETVIEWER: exiting appletviewer");
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    /**
     * Write string to plugin.
     * 
     * @param message the message to write
     *
     * @exception IOException if an error occurs
     */
    public void write(String message) {

        PluginDebug.debug("  PIPE: appletviewer wrote: ", message);
        synchronized (pluginOutputWriter) {
            try {
                pluginOutputWriter.write(message + "\n", 0, message.length());
                pluginOutputWriter.write(0);
                pluginOutputWriter.flush();
            } catch (IOException e) {
                // if we are shutting down, ignore write failures as 
                // pipe may have closed
                if (!shuttingDown) {
                    e.printStackTrace();
                }

                // either ways, if the pipe is broken, there is nothing 
                // we can do anymore. Don't hang around.
                PluginDebug.debug("Unable to write to PIPE. APPLETVIEWER exiting");
                System.exit(1);
            }
        }

        return;
    }

    private void showConsole() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.showConsole();
            }
        });
    }

    private void hideConsole() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.hideConsole();
            }
        });
    }
}
