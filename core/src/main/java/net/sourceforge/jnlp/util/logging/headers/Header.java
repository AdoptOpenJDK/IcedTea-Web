/*
Copyright (C) 2009, 2013  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */
package net.sourceforge.jnlp.util.logging.headers;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.OutputControllerLevel;
import net.sourceforge.jnlp.util.logging.TeeOutputStream;

import java.util.Date;

public class Header {

    private static final String[] LOGGING_INFRASTRUCTURE_CLASSES = {OutputController.class.getName(), Header.class.getName(), TeeOutputStream.class.getName(), "sun.applet.PluginDebug"};
    private static final String DEFAULT_USER = JavaSystemProperties.getUserName();

    public final String osUser = DEFAULT_USER;
    public final OutputControllerLevel level;
    public final Date timestampForSorting;
    public final String timestamp;
    public final boolean isClientApp;
    public final String origin;
    public final String callerClass;
    public final String threadHash;
    public final String threadName;

    public Header(OutputControllerLevel level, String callerClass) {
        this(level, new Date(), false, Thread.currentThread(), callerClass);
    }

    public Header(OutputControllerLevel level, boolean isClientApp) {
        this(level, new Date(), isClientApp, Thread.currentThread(), getCallerClass());
    }

    private Header(OutputControllerLevel level, Date timestamp, boolean isClientApp, Thread thread, String callerClass) {
        this.level = level;
        this.timestampForSorting = timestamp;
        this.timestamp = timestamp.toString();
        this.isClientApp = isClientApp;
        this.origin = isClientApp ? "ITW-APP " : "ITW-CORE";
        this.callerClass = callerClass;
        this.threadHash = Integer.toHexString(thread.hashCode());
        this.threadName = thread.getName();
    }

    @Override
    public String toString() {
        return toString(true, true, true, true, true, true, true);
    }

    public String toShortString() {
        return toString(false, true, true, true, true, false, false);
    }

    public String toString(boolean showOsUser, boolean showOrigin, boolean showTimestamp, boolean showLogLevel, boolean showCallerClass, boolean showThreadHash, boolean showThreadName) {
        StringBuilder sb = new StringBuilder();
        try {
            if (showOsUser) {
                sb.append("[").append(osUser).append("]");
            }
            if (showOrigin) {
                sb.append("[").append(origin).append("]");
            }
            if (showTimestamp) {
                sb.append('[').append(timestamp).append(']');
            }
            if (showLogLevel && level != null) {
                sb.append('[').append(level.display()).append(']');
            }
            if (showCallerClass && callerClass != null) {
                sb.append('[').append(callerClass).append(']');
            }
            if (showThreadName || showThreadHash) {
                sb.append('[');
                if (showThreadName) {
                    sb.append(threadName);
                }
                if (showThreadHash) {
                    sb.append('#').append(threadHash);
                }
                sb.append(']');
            }
        } catch (Exception ignored) {
            // cannot log here as we are creating a log message
        }
        return sb.toString();
    }

    private static String getCallerClass() {
        final StackTraceElement[] stack = (new Exception()).getStackTrace();
        try {
            //0 is always thread
            //1..? is OutputController itself
            //pick up first after.
            StackTraceElement result = stack[0];
            final int numStacks = stack.length;
            for (int i = 1; i < numStacks; i++) {
                result = stack[i];
                if (!isLoggingInfrastructureStackElement(result)) {
                    break;
                }
            }
            return result.toString();
        } catch (Exception ex) {
            // cannot log here as we are in the process of creating a log message
            return "Unknown caller";
        }
    }

    private static boolean isLoggingInfrastructureStackElement(StackTraceElement element) {
        final String classOfCurrentStackElement = element.getClassName();

        for (final String loggingInfrastructureClass : LOGGING_INFRASTRUCTURE_CLASSES) {
            if (classOfCurrentStackElement.contains(loggingInfrastructureClass)) {
                return true;
            }
        }
        return false;
    }
}
