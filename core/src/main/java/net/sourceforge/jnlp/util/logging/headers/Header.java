/* 
Copyright (C) 2009, 2013  Red Hat

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
package net.sourceforge.jnlp.util.logging.headers;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.OutputControllerLevel;
import net.sourceforge.jnlp.util.logging.TeeOutputStream;

import java.util.Date;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_NAME;

public class Header {

    private static final Logger LOG = LoggerFactory.getLogger(Header.class);

    static final String default_user = System.getProperty(USER_NAME);
    static final String unknown = "unknown";

    public final String user;
    public final boolean application;
    public final OutputControllerLevel level;
    public final Date timestamp;
    public final String date;
    public final boolean isPlugin;
    public final boolean isClientApp;
    public final String caller;
    public final String thread1;
    public final String thread2;

    public Header(OutputControllerLevel level) {
        this(level, false);
    }

    public Header(OutputControllerLevel level, String caller) {
        this(level, new Date(), false, Thread.currentThread(), caller);
    }

    public Header(OutputControllerLevel level, boolean isClientApp) {
        this(level, new Date(), isClientApp, Thread.currentThread());
    }

    private Header(OutputControllerLevel level, Date timestamp, boolean isClientApp, Thread thread) {
        this(level, timestamp, isClientApp, thread, getCallerClass(thread.getStackTrace()));
    }

    private Header(OutputControllerLevel level, Date timestamp, boolean isClientApp, Thread thread, String caller) {
        this(
                level, // level
                timestamp, // timestamp
                timestamp.toString(), // date
                JNLPRuntime.isWebstartApplication(), // application
                false, // isPlugin
                isClientApp, // isClientApp
                default_user, // user
                caller, // caller
                Integer.toHexString(thread.hashCode()), // thread1
                thread.getName()) // thread2
        ;
    }

    protected Header(OutputControllerLevel level, Date timestamp, String date, boolean application, boolean isPlugin, boolean isClientApp, String user, String caller, String thread1, String thread2) {
        this.user = user;
        this.application = application;
        this.level = level;
        this.timestamp = timestamp;
        this.date = date;
        this.isPlugin = isPlugin;
        this.isClientApp = isClientApp;
        this.caller = caller;
        this.thread1 = thread1;
        this.thread2 = thread2;
    }

    @Override
    public String toString() {
        return toString(true, true, true, true, true, true, true);
    }

    public String toString(boolean userb, boolean originb, boolean levelb, boolean dateb, boolean callerb, boolean thread1b, boolean thread2b) {
        StringBuilder sb = new StringBuilder();
        try {
            if (userb) {
                sb.append("[").append(user).append("]");
            }
            if (originb) {
                sb.append("[").append(getOrigin()).append("]");
            }

            if (levelb && level != null) {
                sb.append('[').append(level.toString()).append(']');
            }
            if (dateb) {
                sb.append('[').append(date).append(']');
            }
            if (callerb && caller != null) {
                sb.append('[').append(caller).append(']');
            }
            if (thread1b && thread2b) {
                sb.append(threadsToString());
            } else if (thread1b) {
                sb.append(thread1ToString());
            } else if (thread2b) {
                sb.append(thread2ToString());
            }
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        return sb.toString();
    }

    public String thread1ToString() {
        return " NETX Thread# " + thread1;
    }

    public String thread2ToString() {
        return "name " + thread2;
    }

    private String threadsToString() {
        return thread1ToString()
                + ", " + thread2ToString();
    }

    private static final String CLIENT = "CLIENT";

    public String getOrigin() {
        String s;
        if (application) {
            s = "ITW-JAVAWS";
        } else {
            if (isPlugin) {
                s = "ITW-C-PLUGIN";
            } else {
                s = "ITW-APPLET";
            }
        }
        if (isClientApp) {
            s = s + "-" + CLIENT;
        }
        return s;

    }

    private static String getCallerClass(StackTraceElement[] stack) {
        try {
            //0 is always thread
            //1..? is OutputController itself
            //pick up first after.
            StackTraceElement result = stack[0];
            int i = 1;
            for (; i < stack.length; i++) {
                result = stack[i];//at least moving up
                if (stack[i].getClassName().contains(OutputController.class.getName())
                        || //PluginDebug.class.getName() not available during netx make
                        stack[i].getClassName().contains("sun.applet.PluginDebug")
                        || stack[i].getClassName().contains(Header.class.getName())
                        || stack[i].getClassName().contains(TeeOutputStream.class.getName())) {
                } else {
                    break;
                }
            }
            return result.toString();
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return "Unknown caller";
        }
    }
}
