/* 
Copyright (C) 2011-2012 Red Hat, Inc.

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

package net.sourceforge.jnlp.util.logging;

import java.lang.reflect.Method;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * It is crucial that BeforeClass inits logging subsystem.
 * If  logging subsytem of itw is enabled from itw, then junit's classloader do not
 * see it. And so when is junit manipualting with logging, then it creates new (second!)
 * static instance. On opposite, if junit creates the instance, then itw see this one.
 *
 * Explanation is that junit classloader (fresh for each test-class)  is creating
 * special classlaoder for itw (or better itw is creating its own one). The itw 
 * classlaoder is then branch...or leaf of junit classlaoder. So any class loaded
 * by junit classlaoder is visible from itw, but not vice verse.
 */
public class NoStdOutErrTest {

    private static boolean origialStds;
    private static final String setLogToStreams = "setLogToStreams";
    /*
     * "printed" exceptions are otherwise  consumed via junit if thrown :-/
     */

    @BeforeClass
    public static synchronized void disableStds() {
        try {
            //init logger and log and flush message
            //it is crucial for junit to grip it
            OutputController.getLogger().log("initialising");
            //one more times: if TESTED class is the first which creates instance of logger
            //then when junit can not access this class, and creates its own for its purposes
            //when junit creates this class, then also TESTED class have access to it and so it behaves as expected
            OutputController.getLogger().flush();
            origialStds = LogConfig.getLogConfig().isLogToStreams();
            invokeSetLogToStreams(false);
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        }
    }

    @AfterClass
    public static synchronized void restoreStds() {
        try {
            OutputController.getLogger().flush();
            invokeSetLogToStreams(origialStds);
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        }
    }

    private static synchronized void invokeSetLogToStreams(boolean state) {
        try {
            Method lcs = LogConfig.class.getDeclaredMethod(setLogToStreams, boolean.class);
            lcs.setAccessible(true);
            lcs.invoke(LogConfig.getLogConfig(), state);
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        }
    }
}
