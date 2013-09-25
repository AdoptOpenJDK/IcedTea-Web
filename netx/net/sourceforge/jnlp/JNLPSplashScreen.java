/* JNLPSplashScreen.java
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

package net.sourceforge.jnlp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.splashscreen.SplashPanel;
import net.sourceforge.jnlp.splashscreen.SplashUtils;
import net.sourceforge.jnlp.splashscreen.parts.InformationElement;
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.ScreenFinder;

public class JNLPSplashScreen extends JDialog {


    ResourceTracker resourceTracker;

    URL splashImageUrl;
    Image splashImage;
    private final JNLPFile file;
    public static final  int DEF_WIDTH=635;
    public static final  int DEF_HEIGHT=480;
    private SplashPanel componetSplash;
    private boolean splashImageLoaded=false;

    public JNLPSplashScreen(ResourceTracker resourceTracker, final JNLPFile file) {

        setIconImages(ImageResources.INSTANCE.getApplicationImages());

        // If the JNLP file does not contain any icon images, the splash image
        // will consist of the application's title and vendor, as taken from the
        // JNLP file.

        this.resourceTracker = resourceTracker;
        this.file=file;

    }

    public void setSplashImageURL(URL url) {
        splashImageLoaded = false;
        try {
            if (url != null) {
                splashImageUrl = url;
                splashImage = null;
                try {
                    splashImage = ImageIO.read(resourceTracker.getCacheFile(splashImageUrl));
                    if (splashImage == null) {
                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Error loading splash image: " + url);
                    }
                } catch (IOException e) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Error loading splash image: " + url);
                    splashImage = null;
                } catch (IllegalArgumentException argumentException) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Error loading splash image: " + url);
                    splashImage = null;
                }
            }

            if (splashImage == null) {
                this.setLayout(new BorderLayout());
                SplashPanel splash = SplashUtils.getSplashScreen(DEF_WIDTH, DEF_HEIGHT);
                if (splash != null) {
                    splash.startAnimation();
                    splash.setInformationElement(InformationElement.createFromJNLP(file));
                    this.add(splash.getSplashComponent());
                    this.componetSplash = splash;
                }
            }
            correctSize();
        } finally {
            splashImageLoaded = true;
        }
    }

    public boolean isSplashImageLoaded() {
        return splashImageLoaded;
    }


    public boolean isSplashScreenValid() {
        return (splashImage != null) || (componetSplash != null);
    }

    private void correctSize() {
        int minimumWidth = DEF_WIDTH;
        int minimumHeight = DEF_HEIGHT;
        if (splashImage != null) {
            Insets insets = getInsets();
            minimumWidth = splashImage.getWidth(null) + insets.left
                    + insets.right;
            minimumHeight = splashImage.getHeight(null) + insets.top
                    + insets.bottom;
        }
        setMinimumSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        setSize(new Dimension(minimumWidth, minimumHeight));
        setPreferredSize(new Dimension(minimumWidth, minimumHeight));
        ScreenFinder.centerWindowsToCurrentScreen(this);
    }

    @Override
    public void paint(Graphics g) {
        if (splashImage == null) {
            super.paint(g);
            return;
        }

        correctSize();
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(splashImage, getInsets().left, getInsets().top, null);

    }

    public boolean isCustomSplashscreen() {
       return (componetSplash!=null);
    }

    public void stopAnimation() {
        if (isCustomSplashscreen()) componetSplash.stopAnimation();
    }
}
