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

import java.util.Date;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.OutputController.Level;
import net.sourceforge.jnlp.util.logging.TeeOutputStream;

public class Header {
    public static String  default_user = System.getProperty("user.name");
    
    public String user = default_user;
    public boolean application = true;
    public Level level = Level.WARNING_ALL;
    public Date timestamp  = new Date();
    public String date = timestamp.toString();
    public boolean isC = false;//false=> java
    public boolean isClientApp = false;//false=> ITW
    public String caller = "unknown";
    public String thread1 = "unknown";
    public String thread2 = "unknown";

    //to alow simple inheritance
    public Header() {
    }

    public Header(Level level, boolean isC) {
        this(level, Thread.currentThread().getStackTrace(), Thread.currentThread(), isC);   
    }
    
    public Header(Level level, StackTraceElement[] stack, Thread thread, boolean isC) {
        this(level, stack, thread, new Date(), isC);
    }

    public Header(Level level, StackTraceElement[] stack, Thread thread, Date d, boolean isC) {
        this.application = JNLPRuntime.isWebstartApplication();
        this.level = level;
        this.timestamp = d;
        this.date = timestamp.toString();
        this.isC = isC;
        if (stack != null) {
            this.caller = getCallerClass(stack);
        }
        this.thread1 = Integer.toHexString(((Object) thread).hashCode());
        this.thread2 = thread.getName();
    }

    @Override
    public String toString() {
        return toString(true, true, true, true, true, true, true);
    }

    public String toString(boolean userb, boolean originb, boolean levelb, boolean dateb, boolean callerb, boolean thread1b, boolean thread2b) {
        StringBuilder sb = new StringBuilder();
        try {
            if (userb){
                sb.append("[").append(user).append("]");
            }
            if(originb){
                sb.append("[").append(getOrigin()).append("]");
            }

            if (levelb && level != null) {
                sb.append('[').append(level.toString()).append(']');
            }
            if (dateb){
                sb.append('[').append(date.toString()).append(']');
            }
            if (callerb && caller != null) {
                sb.append('[').append(caller).append(']');
            }
            if (thread1b && thread2b){
                sb.append(threadsToString());
            }else if (thread1b) {
                sb.append(thread1ToString());
            }else if (thread2b) {
                sb.append(thread2ToString());
            }
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }
        return sb.toString();
    }

    public String thread1ToString() {
        return " NETX Thread# " + thread1;
    }

    public String thread2ToString() {
        return "name " + thread2;
    }

    public String threadsToString() {
        return thread1ToString()
                + ", " + thread2ToString();
    }

    private static final String CLIENT = "CLIENT";

    public String getOrigin() {
        String s;
        if (application) {
            s = "ITW-JAVAWS";
        } else {
            if (isC) {
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

    static String getCallerClass(StackTraceElement[] stack) {
        try {
            //0 is always thread
            //1..? is OutputController itself
            //pick up first after.
            StackTraceElement result = stack[0];
            int i = 1;
            for (; i < stack.length; i++) {
                result = stack[i];//at least moving up
                if (stack[i].getClassName().contains(OutputController.class.getName())
                        || //PluginDebug.class.getName() not avaiable during netx make
                        stack[i].getClassName().contains("sun.applet.PluginDebug")
                        || stack[i].getClassName().contains(Header.class.getName())
                        || stack[i].getClassName().contains(TeeOutputStream.class.getName())) {
                    continue;
                } else {
                    break;
                }
            }
            return result.toString();
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            return "Unknown caller";
        }
    }
}
