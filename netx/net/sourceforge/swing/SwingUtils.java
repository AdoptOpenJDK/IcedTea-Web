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
package net.sourceforge.swing;

import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Swing / AWT utility class
 */
public final class SwingUtils {

    // debugging flags:
    private static final boolean INFO_DIALOG = false;
    private static final boolean TRACE_INVOKE_EDT = false;
    private static final boolean TRACE_TG = false;

    /** main thread group (initialized at startup) */
    static final ThreadGroup MAIN_GROUP = Thread.currentThread().getThreadGroup();

    private SwingUtils() {
        // forbidden
    }

    static void trace(final String msg) {
        // Use System.err directly for debuging EDT without any conflict with console / logging system
        System.err.println(msg);
    }

    static void traceWithStack(final String msg) {
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

    public static void checkEDT() {
        if (!isEventDispatchThread()) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, new Exception("EDT violation"));
        }
    }

    // --- SwingUtilities wrapper ---
    public static boolean isEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread();
    }

    public static void invokeLater(final Runnable doRun) {
        if (isMainThreadGroup()) {
            if (isEventDispatchThread()) {
                if (TRACE_INVOKE_EDT) {
                    traceWithStack("invokeLater() from EDT: MAY be fixed ?");
                }
            }
            SwingUtilities.invokeLater(doRun);
        } else {
            EDT_DAEMON_THREAD_POOL.submit(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(doRun);
                }
            });
        }
    }

    public static void callOnAppContext(final Runnable doRun) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(doRun);
    }

    public static void invokeAndWait(final Runnable doRun) {
        if (isMainThreadGroup()) {
            if (isEventDispatchThread()) {
                if (TRACE_INVOKE_EDT) {
                    traceWithStack("invokeAndWait() from EDT: MAY be fixed ?");
                }
                doRun.run();
            } else {
                try {
                    callOnAppContext(doRun);
                } catch (InterruptedException ie) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ie);
                } catch (InvocationTargetException ite) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ite);
                }
            }
        } else {
            final Future<?> future = EDT_DAEMON_THREAD_POOL.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    callOnAppContext(doRun);
                    return null;
                }
            });
            try {
                // Wait on Future:
                future.get();
            } catch (InterruptedException ie) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ie);
            } catch (ExecutionException ee) {
                if (ee.getCause() != null) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ee.getCause());
                } else {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ee);
                }
            }
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

    private static final class MainAppContextDaemonThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "itw-edt-thread-";

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(MAIN_GROUP, r,
                    namePrefix + threadNumber.getAndIncrement()
            );
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /** single thread pool with max 1 live daemon thread */
    private static final ExecutorService EDT_DAEMON_THREAD_POOL = new ThreadPoolExecutor(0, 1,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new MainAppContextDaemonThreadFactory()
    );

    /* shared Window owner */
    private static Window window = null;

    public static synchronized Window getOrCreateWindowOwner() {
        if (window == null) {
            invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        window = new JWindow().getOwner();
                        window.setName("getOrCreateWindowOwner");
                    } catch (Exception ex) {
                        OutputController.getLogger().log(ex);
                        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("HEADLESS_MISSCONFIGURED"));
                    }
                }
            });
        }
        return window;
    }
}
