/* SecurityDialogMessageHandler.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
*/

package net.sourceforge.jnlp.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import sun.awt.AppContext;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * Handles {@link SecurityDialogMessage}s and shows appropriate security
 * dialogs.
 * <p>
 * In the current architecture, {@link SecurityDialog}s are shown from a
 * different {@link AppContext} than the {@link AppContext} that asks for a
 * security prompt. This ensures that all security prompts are isolated and
 * their Look and Feel is not affected by the Look and Feel of the
 * applet/application.
 * <p>
 * This class contains allows a client application to post a
 * {@link SecurityDialogMessage}. When this class finds a security message in
 * the queue, it shows a security warning to the user, and sets
 * {@link SecurityDialogMessage#userResponse} to the appropriate value.
 */
public final class SecurityDialogMessageHandler implements Runnable {

    /** the queue of incoming messages to show security dialogs */
    private BlockingQueue<SecurityDialogMessage> queue = new LinkedBlockingQueue<SecurityDialogMessage>();

    /**
     * Runs the message handler loop. This waits for incoming security messages
     * and shows a security dialog.
     */
    @Override
    public void run() {
        if (JNLPRuntime.isDebug()) {
            System.out.println("Starting security dialog thread");
        }
        while (true) {
            try {
                SecurityDialogMessage msg = queue.take();
                handleMessage(msg);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Handles a single {@link SecurityDialogMessage} by showing a
     * {@link SecurityDialog}.
     * <p>
     * Once the user has made a choice the
     * {@link SecurityDialogMessage#toDispose} (if not null) is disposed and
     * {@link SecurityDialogMessage#lock} (in not null) is released.
     *
     * @param message the message indicating what type of security dialog to
     * show
     */
    private void handleMessage(SecurityDialogMessage message) {
        final SecurityDialogMessage msg = message;

        final SecurityDialog dialog = new SecurityDialog(message.dialogType,
                message.accessType, message.file, message.certVerifier, message.certificate, message.extras);

        dialog.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                msg.userResponse = dialog.getValue();
                /* Allow the client to continue on the other side */
                if (msg.toDispose != null) {
                    msg.toDispose.dispose();
                }
                if (msg.lock != null) {
                    msg.lock.release();
                }
            }
        });
        dialog.setVisible(true);

    }

    /**
     * Post a message to the security event queue. This message will be picked
     * up by the security thread and used to show the appropriate security
     * dialog.
     * <p>
     * Once the user has made a choice the
     * {@link SecurityDialogMessage#toDispose} (if not null) is disposed and
     * {@link SecurityDialogMessage#lock} (in not null) is released.
     *
     * @param message indicates the type of security dialog to show
     */
    public void postMessage(SecurityDialogMessage message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
