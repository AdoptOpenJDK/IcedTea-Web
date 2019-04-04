/* SplashPanel.java
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
package net.sourceforge.jnlp.splashscreen;

import java.awt.Graphics;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import net.sourceforge.jnlp.splashscreen.SplashUtils.SplashReason;
import net.sourceforge.jnlp.splashscreen.parts.InformationElement;

public interface SplashPanel {

    /**
     * The plugin splashscreens must be placed into another containers,
     * So must return themselves as JComponent.
     * Mostly your SplashScreen will extend some JComponent, so this method will
     * just return "this"
     */
    public JComponent getSplashComponent();

  
    public void setInformationElement(InformationElement content);

    public InformationElement getInformationElement();

    /** Width of the plugin window */
    public void setSplashWidth(int pluginWidth);

    /** Height of the plugin window */
    public void setSplashHeight(int pluginHeight);

    /** Width of the plugin window */
    public int getSplashWidth();

    /** Height of the plugin window */
    public int getSplashHeight();

    public void adjustForSize();

    // Add a new listener for resizes
    public void addComponentListener(ComponentListener cl);

    public boolean isAnimationRunning();

    /**
     * Methods to start the animation in the splash panel.
     *
     * This method exits after starting a new thread to do the animation. It
     * is synchronized to prevent multiple startAnimation threads from being created.
     */
    public void startAnimation();

    public void stopAnimation();

    void paintTo(Graphics g);

    public void setSplashReason(SplashReason splashReason);

    public SplashReason getSplashReason();

    /**
     * Version can be printed in splash window
     * @param version
     */
    public void setVersion(String version);

    String getVersion();

    /**
     * how mny percentage loaded  is shown in progress bar (if any)
     * @param done - should be in 0-100 inclusinve
     */
    public void setPercentage(int done);

    /**
     * returns state of loading progress bar
     * @return percentage showed in possible progress bar  - should be in 0-100
     */
    int getPercentage();
}
