/* GuiLaunchHandler.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.client;

import net.adoptopenjdk.icedteaweb.client.parts.splashscreen.JNLPSplashScreen;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.AbstractLaunchHandler;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.LaunchHandler;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

import java.net.URL;

/**
 * A {@link LaunchHandler} that gives feedback to the user using GUI elements
 * including splash screens and exception dialogs.
 */
public class GuiLaunchHandler extends AbstractLaunchHandler {

    private volatile JNLPSplashScreen splashScreen = null;
    private final Object mutex = new Object();

    public GuiLaunchHandler(OutputController outputStream) {
        super(outputStream);
    }

    @Override
    public void launchCompleted(ApplicationInstance application) {
        // do nothing
    }

    @Override
    public void handleLaunchError(final LaunchException exception) {
        BasicExceptionDialog.willBeShown();
        SwingUtils.invokeLater(() -> {
            closeSplashScreen();
            BasicExceptionDialog.show(exception);
        });
        printMessage(exception);
    }

    private void closeSplashScreen() {
        synchronized (mutex) {
            if (splashScreen != null) {
                if (splashScreen.isSplashScreenValid()) {
                    splashScreen.setVisible(false);
                    splashScreen.stopAnimation();
                }
                splashScreen.dispose();
            }
        }
    }

    @Override
    public void launchStarting(ApplicationInstance application) {
        SwingUtils.invokeLater(this::closeSplashScreen);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void launchInitialized(final JNLPFile file) {

    	// if show splash is disabled skip create the splash screen
    	if (!JNLPRuntime.isShowWebSplash()) {
    		return;
    	}

        int preferredWidth = 500;
        int preferredHeight = 400;

        final URL splashImageURL = file.getInformation().getIconLocation(
                IconKind.SPLASH, preferredWidth, preferredHeight);

        final ResourceTracker resourceTracker = new ResourceTracker(true);
        if (splashImageURL != null) {
            resourceTracker.addResource(splashImageURL, null);
        }
        synchronized (mutex) {
            SwingUtils.invokeAndWait(() -> {
                splashScreen = new JNLPSplashScreen(resourceTracker, file);
                splashScreen.setSplashImageURL(splashImageURL);
            });
        }

        SwingUtils.invokeLater(() -> {
            if (splashScreen != null) {
                synchronized (mutex) {
                    if (splashScreen.isSplashScreenValid()) {
                        splashScreen.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    public void handleLaunchWarning(LaunchException warning) {
        printMessage(warning);
    }

    @Override
    public void validationError(LaunchException error) {
        closeSplashScreen();
        printMessage(error);
    }

}
