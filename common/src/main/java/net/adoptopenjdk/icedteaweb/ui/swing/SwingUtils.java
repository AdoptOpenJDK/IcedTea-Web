/* SwingUtils.java
   Copyright (C) 2018 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.ui.swing;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.JWindow;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.ITW_EDT_DEBUG;

/**
 * Swing / AWT utility class
 */
public final class SwingUtils {

    private final static Logger LOG = LoggerFactory.getLogger(SwingUtils.class);

    private static final boolean DEBUG_EDT = System.getProperty(ITW_EDT_DEBUG, "false").equalsIgnoreCase("true");

    // debugging flags:
    private static final boolean INFO_DIALOG = false;
    private static final boolean TRACE_INVOKE_EDT = false;
    private static final boolean TRACE_TG = false;

    // internals:
    private static boolean DO_SETUP = true;

    /** main thread group (initialized at startup) */
    private static final ThreadGroup MAIN_GROUP = Thread.currentThread().getThreadGroup();

    /* shared Window owner */
    private static Window window = null;

    private SwingUtils() {
        // forbidden
    }

    public static void setup() {
        if (DO_SETUP) {
            DO_SETUP = false; // avoid reentrance

            if (DEBUG_EDT) {
                trace("Using ThreadCheckingRepaintManager");
                RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
            }
        }
    }

    static void trace(final String msg) {
        // Use System.err directly for debugging EDT without any conflict with console / logging system
        System.err.println(msg);
    }

    private static void traceWithStack(final String msg) {
        trace(msg);
        new Throwable().printStackTrace();
    }

    public static void info(final Window dialog) {
        if (INFO_DIALOG) {
            trace("Dialog[" + dialog.getName() + "]"
                    + " in TG [" + Thread.currentThread().getThreadGroup() + "]");
            checkEDT();
        }
    }

    private static void checkEDT() {
        if (!isEventDispatchThread()) {
            LOG.error("EDT VIOLATION", new Exception("EDT violation"));
        }
    }

    private static boolean isMainThreadGroup() {
        final Thread t = Thread.currentThread();
        final ThreadGroup g = t.getThreadGroup();

        if (g != MAIN_GROUP) {
            if (TRACE_TG) {
                traceWithStack("----------\ncheckThreadGroup: " + t);
            }
            return false;
        }
        return true;
    }

    // --- SwingUtilities wrapper ---
    public static Window getWindowAncestor(Component c) {
        return SwingUtilities.getWindowAncestor(c);
    }

    public static boolean isEventDispatchThread() {
        return EventQueue.isDispatchThread();
    }

    public static void invokeLater(final Runnable doRun) {
        if (isMainThreadGroup()) {
            if (TRACE_INVOKE_EDT && isEventDispatchThread()) {
                traceWithStack("invokeLater() from EDT: MAY be fixed (useless) ?");
            }
        }
        EventQueue.invokeLater(doRun);
    }

    public static void invokeRunnableOrEnqueueLater(final Runnable runnable) {
        if (isEventDispatchThread()) {
            runnable.run();
        } else {
            invokeLater(runnable);
        }
    }

    public static void callOnAppContext(final Runnable doRun) throws InterruptedException, InvocationTargetException {
        EventQueue.invokeAndWait(doRun);
    }

    public static void invokeAndWait(final Runnable doRun) {
        if (isEventDispatchThread()) {
            if (TRACE_INVOKE_EDT) {
                traceWithStack("invokeAndWait() from EDT: to be fixed (illegal) ?");
            }
            // Direct invocation:
            doRun.run();
        } else {
            try {
                callOnAppContext(doRun);
            } catch (InterruptedException | InvocationTargetException ie) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ie);
            }
        }
    }

    public static synchronized Window getOrCreateWindowOwner() {
        if (window == null) {
            invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        window = new JWindow().getOwner();
                        window.setName("getOrCreateWindowOwner");
                    } catch (Exception ex) {
                        LOG.error("Headless check failed. You are forced to run without any graphics. IcedTea-Web can run like this, but your app probably not. This is likely bug in your system.", ex);
                    }
                }
            });
        }
        return window;
    }
}
