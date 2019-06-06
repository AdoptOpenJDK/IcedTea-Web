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

package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.awt.AppContext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles {@link SecurityDialogMessage}s and shows appropriate security
 * dialogs.
 * <p>
 * In the current architecture, {@link SecurityDialog}s are shown from a
 * different {@link AppContext} than the {@link AppContext} that asks for a
 * security prompt. This ensures that all security prompts are isolated and
 * their Look and Feel is not affected by the Look and Feel of the
 * applet/application.
 * </p>
 * <p>
 * This class contains allows a client application to post a
 * {@link SecurityDialogMessage}. When this class finds a security message in
 * the queue, it shows a security warning to the user, and sets
 * {@link SecurityDialogMessage#userResponse} to the appropriate value.
 * </p>
 */
public class SecurityDialogMessageHandler implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(SecurityDialogMessageHandler.class);

    /** the queue of incoming messages to show security dialogs */
    private BlockingQueue<SecurityDialogMessage> queue = new LinkedBlockingQueue<>();

    /**
     * Runs the message handler loop. This waits for incoming security messages
     * and shows a security dialog.
     */
    @Override
    public void run() {
        LOG.debug("Starting security dialog thread");
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
     * </p>
     *
     * @param message the message indicating what type of security dialog to
     * show
     */
    protected void handleMessage(final SecurityDialogMessage message) {

        final SecurityDialog dialog = new SecurityDialog(message.dialogType,
                message.accessType, message.file, message.certVerifier, message.certificate, message.extras);

        if (processAutomatedAnswers(message, dialog)){
            return;
        }

        final RememberableDialog found = RememberDialog.getInstance().findRememberablePanel(dialog.getSecurityDialogPanel());
        SavedRememberAction action = null;
        if (found!=null){
            action = RememberDialog.getInstance().getRememberedState(found);
        }
        if (action != null && action.isRemember()) {
            message.userResponse = found.readValue(action.getSavedValue());
            UnsignedAppletTrustConfirmation.updateAppletAction(found.getFile(), action, null, (Class<RememberableDialog>) found.getClass());
            unlockMessagesClient(message);
        } else {

            if (!shouldPromptUser()) {
                message.userResponse =  dialog.getDefaultNegativeAnswer();
                unlockMessagesClient(message);
            } else if (isHeadless()) {
                processMessageInHeadless(dialog, message);
            } else {
                processMessageInGui(dialog, found, message);
            }
        }

    }

    private boolean processAutomatedAnswers(final SecurityDialogMessage message, final SecurityDialog dialog) {
        if (isXtrustNone()) {
            message.userResponse =  dialog.getDefaultNegativeAnswer();
            unlockMessagesClient(message);
            return true;
        }
        if (isXtrustAll()) {
            message.userResponse =  dialog.getDefaultPositiveAnswer();
            unlockMessagesClient(message);
            return true;
        }
        return false;
    }

    private void processMessageInGui(final SecurityDialog dialog, final RememberableDialog found, final SecurityDialogMessage message) {
        dialog.getViwableDialog().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (found == null) {
                    message.userResponse = dialog.getValue();
                } else {
                    message.userResponse = found.getValue();
                    RememberDialog.getInstance().setOrUpdateRememberedState(dialog);
                }
                unlockMessagesClient(message);
            }

        });
        dialog.getViwableDialog().show();
    }

    private void processMessageInHeadless(final SecurityDialog dialog, final SecurityDialogMessage message) {
        try {
            boolean keepGoing = true;
            boolean repeatAll = true;
            do {
                try {
                    if (repeatAll){
                        OutputController.getLogger().printOutLn(dialog.getText());
                    }
                    OutputController.getLogger().printOutLn(Translator.R("HeadlessDialogues"));
                    OutputController.getLogger().printOutLn(dialog.helpToStdIn());
                    String s = OutputController.getLogger().readLine();
                    if (s == null) {
                         throw new IOException("Stream closed");
                    }
                    if (s.trim().toLowerCase().equals("exit")) {
                        JNLPRuntime.exit(0);
                    }
                    boolean codebase = false;
                    boolean remember = false;
                    if (s.startsWith("RC ")){
                        codebase = true;
                        remember = true;
                        s=s.substring(3);
                    }
                    if (s.startsWith("R ")){
                        remember = true;
                        s=s.substring(2);
                    }
                    message.userResponse = dialog.readFromStdIn(s);
                    keepGoing = false;
                    try {
                        String value = "";
                        if (message.userResponse != null) {
                            value = message.userResponse.writeValue();
                        }
                        if (dialog.getSecurityDialogPanel() instanceof CertWarningPane) {
                            CertWarningPane cp = (CertWarningPane) (dialog.getSecurityDialogPanel());
                            if (remember) {
                                cp.saveCert();
                            }
                        }
                        RememberDialog.getInstance().setOrUpdateRememberedState(dialog, codebase, new SavedRememberAction(RememberDialog.createAction(remember, message.userResponse), value));
                    } catch (Exception ex) {
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    }
                } catch (IOException eex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, eex);
                    keepGoing = false;
                } catch (IllegalArgumentException eeex){
                    LOG.error("Probably wrong value?", eeex);
                    repeatAll = false;
                } catch (Exception ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    repeatAll = true;
                }
            } while (keepGoing);
        } finally {
            unlockMessagesClient(message);
        }
    }

    protected void unlockMessagesClient(final SecurityDialogMessage msg) {
        /* Allow the client to continue on the other side */
        if (msg.toDispose != null) {
            msg.toDispose.dispose();
        }
        if (msg.lock != null) {
            msg.lock.release();
        }
    }
        /**
     * Post a message to the security event queue. This message will be picked
     * up by the security thread and used to show the appropriate security
     * dialog.
     * <p>
     * Once the user has made a choice the
     * {@link SecurityDialogMessage#toDispose} (if not null) is disposed and
     * {@link SecurityDialogMessage#lock} (in not null) is released.
     * </p>
     *
     * @param message indicates the type of security dialog to show
     */
    public void postMessage(SecurityDialogMessage message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }


    /**
     * Returns whether the current runtime configuration allows prompting user
     * for security warnings.
     *
     * @return true if security warnings should be shown to the user.
     */
    private static boolean shouldPromptUser() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return Boolean.valueOf(JNLPRuntime.getConfiguration()
                        .getProperty(ConfigurationConstants.KEY_SECURITY_PROMPT_USER));
            }
        });
    }

     /**
     * Returns whether the current runtime configuration is headless
     *
     * @return true X is used
     */
    private static boolean isHeadless() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return JNLPRuntime.isHeadless();
            }
        });
    }

     /**
     * Returns whether the current runtime configuration is trustAll
     *
     * @return true if xtrustall was specified
     */
    private static boolean isXtrustAll() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return JNLPRuntime.isTrustAll();
            }
        });
    }

     /**
     * Returns whether the current runtime configuration is trustNone
     *
     * @return true if xtrustnone was specified
     */
    private static boolean isXtrustNone() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return JNLPRuntime.isTrustNone();
            }
        });
    }

}
