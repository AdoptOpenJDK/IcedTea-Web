// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
package net.adoptopenjdk.icedteaweb.launch;

import static java.util.Objects.requireNonNull;

/**
 * Holds the JVM Launcher in use by the web start application environment.
 */
public class JvmLauncherHolder {

    private static JvmLauncher launcher;

    public static JvmLauncher getLauncher() {
        return requireNonNull(launcher, "JvmLauncher is not set.");
    }

    /**
     * Sets the JVM Launcher that should be used by the web start application environment.
     *
     * @param launcher the JVM Launcher
     * @throws IllegalStateException if the JVM launcher is already set
     */
    public static void setLauncher(JvmLauncher launcher) {
        if (JvmLauncherHolder.launcher != null) {
            throw new IllegalStateException("JvmLauncher already set.");
        }
        JvmLauncherHolder.launcher = launcher;
    }
}
