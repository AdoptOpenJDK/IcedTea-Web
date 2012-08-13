/* BasicComponentSplashScreen.java
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
package net.sourceforge.jnlp.splashscreen.parts;

import javax.swing.JComponent;
import net.sourceforge.jnlp.splashscreen.SplashPanel;
import net.sourceforge.jnlp.splashscreen.SplashUtils.SplashReason;

public abstract class BasicComponentSplashScreen extends JComponent implements SplashPanel {
    //scaling 100%
    public static final double ORIGINAL_W = 635;
    public static final double ORIGINAL_H = 480;
       /** Width of the plugin window */
    protected int pluginWidth;
    /** Height of the plugin window */
    protected int pluginHeight;
    /** The project name to display */
    private SplashReason splashReason;
    private boolean animationRunning = false;
    private InformationElement content;
    private String version;

    @Override
    public JComponent getSplashComponent() {
        return this;
    }

    @Override
    public boolean isAnimationRunning() {
        return animationRunning;
    }

    public void setAnimationRunning(boolean b){
        animationRunning=b;
    }

    @Override
    public void setInformationElement(InformationElement content) {
        this.content = content;
    }

    @Override
    public InformationElement getInformationElement() {
        return content;
    }

   
    /**
     * @return the pluginWidth
     */
    @Override
    public int getSplashWidth() {
        return pluginWidth;
    }

    /**
     * @param pluginWidth the pluginWidth to set
     */
    @Override
    public void setSplashWidth(int pluginWidth) {
        this.pluginWidth = pluginWidth;
    }

    /**
     * @return the pluginHeight
     */
    @Override
    public int getSplashHeight() {
        return pluginHeight;
    }

    /**
     * @param pluginHeight the pluginHeight to set
     */
    @Override
    public void setSplashHeight(int pluginHeight) {
        this.pluginHeight = pluginHeight;
    }

    /**
     * @return the splashReason
     */
    @Override
    public SplashReason getSplashReason() {
        return splashReason;
    }

    /**
     * @param splashReason the splashReason to set
     */
    @Override
    public void setSplashReason(SplashReason splashReason) {
        this.splashReason = splashReason;
    }

    /**
     * @return the version
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    @Override
    public void setVersion(String version) {
        this.version = version;
    }

   
    protected String createAditionalInfo() {
        if (getVersion() != null) {
            return getSplashReason().toString() + " version: " + getVersion();
        } else {
            return null;
        }
    }

   
}
