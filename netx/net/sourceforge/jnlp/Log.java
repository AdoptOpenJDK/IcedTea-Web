/* Log.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import java.io.File;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * This file provides the information required to do logging.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
abstract class Log {

    // Directory where the logs are stored.
    protected static String icedteaLogDir;

    protected static boolean enableLogging = false;
    protected static boolean enableTracing = false;

    // Prepare for logging.
    static {
        DeploymentConfiguration config = JNLPRuntime.getConfiguration();

        // Check whether logging and tracing is enabled.
        enableLogging = Boolean.parseBoolean(config.getProperty(DeploymentConfiguration.KEY_ENABLE_LOGGING));
        enableTracing = Boolean.parseBoolean(config.getProperty(DeploymentConfiguration.KEY_ENABLE_TRACING));

        // Get log directory, create it if it doesn't exist. If unable to create and doesn't exist, don't log.
        icedteaLogDir = config.getProperty(DeploymentConfiguration.KEY_USER_LOG_DIR);
        if (icedteaLogDir != null) {
            File f = new File(icedteaLogDir);
            if (f.isDirectory() || f.mkdirs())
                icedteaLogDir += File.separator;
            else {
                enableLogging = false;
                enableTracing = false;
            }
        }
    }
}
