/* ScreenFinder.java
Copyright (C) 2012 Red Hat, Inc.

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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

public class ScreenFinder {

    private final static Logger LOG = LoggerFactory.getLogger(ScreenFinder.class);

    public static GraphicsDevice getCurrentScreen() {
        final Point p = MouseInfo.getPointerInfo().getLocation();
        return getScreenOnCoords(p);
    }

    public static Rectangle  getCurrentScreenSizeWithoutBounds() {
        try {
            final Point p = MouseInfo.getPointerInfo().getLocation();
            return getScreenOnCoordsWithoutBounds(p);
        } catch (HeadlessException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return new Rectangle(800, 600);
        }
    }

     public static void  centerWindowsToCurrentScreen(final Window w) {
        final Rectangle bounds = getCurrentScreenSizeWithoutBounds();
        w.setLocation(bounds.x + (bounds.width - w.getWidth())/2,
                bounds.y + (bounds.height - w.getHeight())/2);
    }

    private static GraphicsDevice getScreenOnCoords(final Point point) {
        final GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] devices = e.getScreenDevices();
        GraphicsDevice result = null;
        //now get the configuration(s) for each device
        for (GraphicsDevice device : devices) {
            //GraphicsConfiguration[] configurations = device.getConfigurations();
            //or?
            GraphicsConfiguration[] configurations = new GraphicsConfiguration[]{device.getDefaultConfiguration()};
            for (GraphicsConfiguration config : configurations) {
                Rectangle gcBounds = config.getBounds();
                if (gcBounds.contains(point)) {
                    result = device;
                }
            }
        }
        if (result == null) {
            //not found, get the default display
            result = e.getDefaultScreenDevice();
        }
        return result;
    }

    private static Rectangle getScreenOnCoordsWithoutBounds(final Point p) {
        try {
            final GraphicsDevice device = getScreenOnCoords(p);
            final Rectangle screenSize = device.getDefaultConfiguration().getBounds();
            final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
            return new Rectangle((int) screenSize.getX() + insets.left, (int) screenSize.getY() + insets.top, (int) screenSize.getWidth() - insets.left, (int) screenSize.getHeight() - insets.bottom);
        } catch (HeadlessException | IllegalArgumentException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return new Rectangle(800, 600);
        }
    }
}
