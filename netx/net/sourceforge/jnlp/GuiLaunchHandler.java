/* GuiLaunchHandler.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.util.BasicExceptionDialog;

/**
 * A {@link LaunchHandler} that gives feedback to the user using GUI elements
 * including splash screens and exception dialogs.
 */
public class GuiLaunchHandler extends AbstractLaunchHandler {

    private JNLPSplashScreen splashScreen = null;
    private final Object mutex = new Object();
    private UpdatePolicy policy = UpdatePolicy.ALWAYS;

    public GuiLaunchHandler(PrintStream outputStream) {
        super(outputStream);
    }

    @Override
    public void launchCompleted(ApplicationInstance application) {
        // do nothing
    }

    @Override
    public void launchError(final LaunchException exception) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                closeSplashScreen();
                BasicExceptionDialog.show(exception);
            }
        });
        printMessage(exception);
    }

    private void closeSplashScreen() {
        synchronized(mutex) {
            if (splashScreen != null) {
                if (splashScreen.isSplashScreenValid()) {
                    splashScreen.setVisible(false);
                }
                splashScreen.dispose();
            }
        }
    }

    @Override
    public void launchStarting(ApplicationInstance application) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                closeSplashScreen();
            }
        });
    }

    @Override
    public void launchInitialized(final JNLPFile file) {
        
        int preferredWidth = 500;
        int preferredHeight = 400;

        final URL splashImageURL = file.getInformation().getIconLocation(
                IconDesc.SPLASH, preferredWidth, preferredHeight);

        if (splashImageURL != null) {
            final ResourceTracker resourceTracker = new ResourceTracker(true);
            resourceTracker.addResource(splashImageURL, file.getFileVersion(), null, policy);
            synchronized(mutex) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            splashScreen = new JNLPSplashScreen(resourceTracker, null, null);
                        }
                    });
                } catch (InterruptedException ie) {
                    // Wait till splash screen is created
                    while (splashScreen == null);
                } catch (InvocationTargetException ite) {
                    ite.printStackTrace();
                }

                splashScreen.setSplashImageURL(splashImageURL);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (splashImageURL != null) {
                    synchronized(mutex) {
                        if (splashScreen.isSplashScreenValid()) {
                            splashScreen.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean launchWarning(LaunchException warning) {
        printMessage(warning);
        return true;
    }

    @Override
    public boolean validationError(LaunchException error) {
        closeSplashScreen();
        printMessage(error);
        return true;
    }

}
