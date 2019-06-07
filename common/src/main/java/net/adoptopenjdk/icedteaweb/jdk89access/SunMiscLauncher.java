/*
 Copyright (C) 2016 Red Hat, Inc.

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
 statement from your version.*/
package net.adoptopenjdk.icedteaweb.jdk89access;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.ImageIcon;
import java.lang.reflect.Method;

/**
 * Class providing secure image icon using sun.misc.Launcher, which was removed in jdk9.
 *
 * @author jvanek
 */
public class SunMiscLauncher {
    private final static Logger LOG = LoggerFactory.getLogger(SunMiscLauncher.class);

    private static final String CLASS_SUN_MISC_LAUNCHER = "sun.misc.Launcher";
    private static final String METHOD_GET_CLASS_LOADER = "getClassLoader";

    @SuppressWarnings("unchecked")
    public static ImageIcon getSecureImageIcon(final String resource) {
        try {
            final Class clazz = Class.forName(CLASS_SUN_MISC_LAUNCHER);
            final Object obj  = clazz.newInstance();
            final Method m = clazz.getMethod(METHOD_GET_CLASS_LOADER);
            final ClassLoader cl = (ClassLoader) m.invoke(obj);

            return new ImageIcon(cl.getResource(resource));
        } catch (Exception ex) {
            LOG.error("sun.misc.Launcher not found. Running jdk9 or higher? Using insecure BootClassLoader", ex);
            return new ImageIcon(ClassLoader.getSystemClassLoader().getParent().getResource(resource));
        }
    }
}
