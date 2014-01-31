/* ErrorPainter.java
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
package net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Observable;

import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.splashscreen.parts.BasicComponentSplashScreen;
import net.sourceforge.jnlp.splashscreen.parts.InformationElement;
import net.sourceforge.jnlp.splashscreen.parts.extensions.ExtensionManager;
import net.sourceforge.jnlp.util.logging.OutputController;

public final class ErrorPainter extends BasePainter {

    //colors
    private static final Color TEA_DEAD_COLOR = Color.darkGray;
    private static final Color BACKGROUND_DEAD_COLOR = Color.gray;
    private static final Color TEA_LEAFS_STALKS_DEAD_COLOR = new Color(100, 100, 100);
    private static final Color PLUGIN_DEAD_COLOR = Color.darkGray;
    private static final Color WATER_DEAD_COLOR = Color.darkGray;
    private static final Color PLAIN_TEXT_DEAD_COLOR = Color.white;
    private static final String ERROR_MESSAGE_KEY = "SPLASHerror";
    private static final String ERROR_FLY_MESSAGE_KEY = "SPLASH_ERROR";
    private static final Color ERROR_FLY_COLOR = Color.red;
    //for clicking ot error message
    private Point errorCorner = null;
    private boolean errorIsFlying = false;
    private int errorFlyPercentage = 100;

    /**
     * Interpolation is root ratior is r= (currentSize / origSize)
     * then value to-from is interpolaed from to to from accroding to ratio
     *
     * @param origSize
     * @param currentSize
     * @param from
     * @param to
     * @return interpolated value
     */
    public static double interpol(double origSize, double currentSize, double from, double to) {
        return getRatio(origSize, currentSize) * (to - from) + from;
    }

    /**
     * is interpolating one color to another based on ration current/orig
     * Each (r,g,b,a) part of color is interpolated separately
     * resturned is new color composed form new r,g,b,a
     * @param origSize
     * @param currentSize
     * @param from
     * @param to
     * @return interpolated {@link Color}
     */
    public static Color interpolateColor(double origSize, double currentSize, Color from, Color to) {
        double r = interpol(origSize, currentSize, to.getRed(), from.getRed());
        double g = interpol(origSize, currentSize, to.getGreen(), from.getGreen());
        double b = interpol(origSize, currentSize, to.getBlue(), from.getBlue());
        double a = interpol(origSize, currentSize, to.getAlpha(), from.getAlpha());
        return new Color((int) r, (int) g, (int) b, (int) a);
    }
    //scaling end

    public ErrorPainter(BasicComponentSplashScreen master) {
        this(master, false);
    }

    public ErrorPainter(BasicComponentSplashScreen master, boolean startScream) {
        super(master);
        if (startScream) {
            startErrorScream();
        }

    }

    public void startErrorScream() {
        errorIsFlying = true;
        getFlyingRedErrorTextThread().start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        ensurePrerenderedStuff();
        if (errorIsFlying) {
            paintStillTo(g2d, master.getInformationElement(), master.getVersion());
        } else {
            if (prerenderedStuff != null) {
                g2d.drawImage(prerenderedStuff, 0, 0, null);
            }
        }

        if (super.showNiceTexts) {
            ExtensionManager.getExtension().paint(g, this);
            paintNiceTexts(g2d);
        } else {
            paintPlainTexts(g2d);
        }

        if (errorIsFlying) {
            g2d.setClip(0, 0, master.getSplashWidth(), master.getSplashHeight());
            drawBigError(g2d);
        }


    }

    private void drawBigError(Graphics2D g2d) {
        Font f = new Font("Serif", Font.PLAIN, (int) scale(100, errorFlyPercentage, master.getSplashHeight()));
        g2d.setColor(ERROR_FLY_COLOR);
        g2d.setFont(f);
        drawTextAroundCenter(g2d, 0, geFlyingErrorMessage());
    }

    public Point getErrorCorner() {
        return errorCorner;
    }

    private void setColors() {

        teaColor = TEA_DEAD_COLOR;
        backgroundColor = BACKGROUND_DEAD_COLOR;
        teaLeafsStalksColor = TEA_LEAFS_STALKS_DEAD_COLOR;
        pluginColor = PLUGIN_DEAD_COLOR;
        waterColor = WATER_DEAD_COLOR;

    }

    private void interpolateColor(int origSize, int currentSize) {
        teaColor = interpolateColor(origSize, currentSize, TEA_LIVE_COLOR, TEA_DEAD_COLOR);
        backgroundColor = interpolateColor(origSize, currentSize, BACKGROUND_LIVE_COLOR, BACKGROUND_DEAD_COLOR);
        teaLeafsStalksColor = interpolateColor(origSize, currentSize, TEA_LEAFS_STALKS_LIVE_COLOR, TEA_LEAFS_STALKS_DEAD_COLOR);
        pluginColor = interpolateColor(origSize, currentSize, PLUGIN_LIVE_COLOR, PLUGIN_DEAD_COLOR);
        waterColor = interpolateColor(origSize, currentSize, WATER_LIVE_COLOR, WATER_DEAD_COLOR);
        plainTextColor = interpolateColor(origSize, currentSize, PLAIN_TEXT_LIVE_COLOR, PLAIN_TEXT_DEAD_COLOR);
    }

    @Override
    protected void paintStillTo(Graphics2D g2d, InformationElement ic, String version) {
        RenderingHints r = g2d.getRenderingHints();
        FontMetrics fm = drawBase(g2d, ic, version);
        drawError(g2d, ic, version, fm);
        g2d.setRenderingHints(r);
    }

    private String getErrorMessage() {
        String localised = Translator.R(ERROR_MESSAGE_KEY);
        //if (localised==null)return errorMessage;
        return localised;
    }

    private String geFlyingErrorMessage() {
        String localised = Translator.R(ERROR_FLY_MESSAGE_KEY);
        return localised;
    }

    private Thread getFlyingRedErrorTextThread() {
        // Create a new thread to draw big flying error in case of failure
        Thread t = new Thread(new FlyingRedErrorTextRunner(this));
        //t.setDaemon(true);
        return t;
    }

    private final class FlyingRedErrorTextRunner extends Observable implements Runnable {

        private static final int FLYING_ERROR_PERCENTAGE_INCREMENT = -3;
        private static final int FLYING_ERROR_PERCENTAGE_MINIMUM = 5;
        private static final int FLYING_ERROR_PERCENTAGE_LOWER_BOUND = 80;
        private static final int FLYING_ERROR_PERCENTAGE_UPPER_BOUND = 90;
        private static final int FLYING_ERROR_DELAY = 75;

        private FlyingRedErrorTextRunner(ErrorPainter o) {
            this.addObserver(o);
        }

        @Override
        public void run() {
            try {
                while (errorIsFlying) {
                    errorFlyPercentage += FLYING_ERROR_PERCENTAGE_INCREMENT;
                    interpolateColor(100, errorFlyPercentage);
                    if (errorFlyPercentage <= FLYING_ERROR_PERCENTAGE_MINIMUM) {
                        errorIsFlying = false;
                        setColors();
                        prerenderedStuff = null;
                    }
                    this.setChanged();
                    this.notifyObservers();
                    Thread.sleep(FLYING_ERROR_DELAY);
                    if (errorFlyPercentage < FLYING_ERROR_PERCENTAGE_UPPER_BOUND
                            && errorFlyPercentage > FLYING_ERROR_PERCENTAGE_LOWER_BOUND) {
                        clearCachedWaterTextImage();
                        canWave = false;
                    }
                }
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            } finally {
                canWave = true;
                errorIsFlying = false;
                setColors();
                prerenderedStuff = null;
                master.repaint();

            }
        }
    };

    private void drawError(Graphics2D g2d, InformationElement ic, String version, FontMetrics fm) {
        int minh = fm.getHeight();
        int minw = fm.stringWidth(getErrorMessage());
        int space = 5;
        g2d.setColor(backgroundColor);
        errorCorner = new Point(master.getSplashWidth() - space * 4 - minw, master.getSplashHeight() - space * 4 - minh);
        if (errorCorner.x < 0) {
            errorCorner.x = 0;
        }
        g2d.fillRect(errorCorner.x, errorCorner.y, space * 4 + minw, space * 4 + minh);
        g2d.setColor(plainTextColor);
        g2d.drawRect(errorCorner.x + space, errorCorner.y + space, space * 2 + minw, space * 2 + minh);
        g2d.drawString(getErrorMessage(), errorCorner.x + 2 * space, errorCorner.y + 5 * space);
    }
}
