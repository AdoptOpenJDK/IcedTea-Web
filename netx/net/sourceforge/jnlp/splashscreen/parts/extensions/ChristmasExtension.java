/*
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
package net.sourceforge.jnlp.splashscreen.parts.extensions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.BasePainter;
import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.ErrorPainter;

public class ChristmasExtension implements SplashExtension {

    @Override
    public Color getBackground() {
        return Color.black;
    }

    @Override
    public Color getTextColor() {
        return Color.DARK_GRAY;
    }

    @Override
    public Color getPluginTextColor() {
        return new Color(30, 30, 30);
    }

    ChristmasExtension() {
        this(0, 0);
    }
    private static final Random seed = new Random();
    private static final int avarege_star_width = 10; //stars will be 5-15
    private final int avarege_fall_speed = 4; //2-6
    private final int avarege_rotation_speed = 2; //1-3

    private class Star {

        private int radiusX;
        private int radiusY;
        private int maxRadiusX;
        private int maxRadiusY;
        private int centerX;
        private int centerY;
        private final int fallSpeed;
        private final boolean orientation;
        private final int[] originalColor = new int[3];
        private final int[] color = new int[originalColor.length];
        private int direction;
        private final boolean haveEight;

        public Star() {
            createRadiuses();
            haveEight = seed.nextBoolean();
            this.centerX = seed.nextInt(w + 1);
            this.centerY = seed.nextInt(h + 1);
            this.fallSpeed = avarege_fall_speed / 2 + seed.nextInt(avarege_fall_speed / 2);
            this.orientation = seed.nextBoolean();
            this.direction = -(avarege_rotation_speed / 2 + seed.nextInt(avarege_rotation_speed / 2));
            if (seed.nextInt(4) == 0) {
                originalColor[0] = Color.yellow.getRed();
                originalColor[1] = Color.yellow.getGreen();
                originalColor[2] = Color.yellow.getBlue();
            } else {
                originalColor[0] = BasePainter.WATER_LIVE_COLOR.getRed();
                originalColor[1] = BasePainter.WATER_LIVE_COLOR.getGreen();
                originalColor[2] = BasePainter.WATER_LIVE_COLOR.getBlue();
            }
        }

        public void paint(Graphics g, Color forceColor1, Color forceColor2) {
            Color c = g.getColor();
            if (forceColor1 == null || forceColor2 == null) {
                g.setColor(new Color(color[0], color[1], color[2]));
            } else {
                g.setColor(ErrorPainter.interpolateColor(h, centerY, forceColor1, forceColor2));
            }
            Polygon p = createPolygon();
            if (haveEight) {
                int min1 = Math.min(radiusX, radiusY);
                int min2 = min1 / 2;
                g.fillRect(centerX - min2, centerY - min2, min1, min1);
            }
            g.fillPolygon(p);
            g.setColor(c);
        }

        private void animate() {
            centerY += fallSpeed;
            if (orientation) {
                radiusX += direction;
                if (radiusX <= -direction) {
                    direction = -direction;
                    radiusX = direction;
                }
                if (radiusX >= maxRadiusX) {
                    direction = -direction;
                    radiusX = maxRadiusX;
                }
                interpolateColors(radiusX, maxRadiusX);
            } else {
                radiusY += direction;
                if (radiusY <= -direction) {
                    direction = -direction;
                    radiusY = direction;
                }
                if (radiusY >= maxRadiusY) {
                    direction = -direction;
                    radiusY = maxRadiusY;
                }
                interpolateColors(radiusY, maxRadiusY);
            }
            if (centerY > h + radiusX * 2 || centerY > h + radiusY * 2) {
                createRadiuses();
                this.centerX = seed.nextInt(w + 1);
                this.centerY = -radiusY * 2;
            }
        }

        private int createRadius() {
            return avarege_star_width / 2 + seed.nextInt(avarege_star_width);
        }

        private Polygon createPolygon() {
            int min = Math.min(radiusX, radiusY) / 3;
            Polygon p = new Polygon();
            p.addPoint(centerX - radiusX, centerY);
            p.addPoint(centerX - min, centerY - min);
            p.addPoint(centerX, centerY - radiusY);
            p.addPoint(centerX + min, centerY - min);
            p.addPoint(centerX + radiusX, centerY);
            p.addPoint(centerX + min, centerY + min);
            p.addPoint(centerX, centerY + radiusY);
            p.addPoint(centerX - min, centerY + min);
            return p;
        }

        private void interpolateColors(int is, int max) {
            for (int i = 0; i < originalColor.length; i++) {
                int fadeMin;
                if (centerY < 0) {
                    fadeMin = 0;
                } else if (centerY > h) {
                    fadeMin = 255;
                } else {
                    fadeMin = (int) ErrorPainter.interpol(h, centerY, 255, 0); //from white  to black
                }
                int fadeMax;
                if (centerY < 0) {
                    fadeMax = 0;
                } else if (centerY > h) {
                    fadeMax = originalColor[i];
                } else {
                    fadeMax = (int) ErrorPainter.interpol(h, centerY, originalColor[i], 0); //from color tho black
                }
                color[i] = (int) ErrorPainter.interpol(max, is, fadeMin, fadeMax);
            }
        }

        private void createRadiuses() {
            this.radiusX = createRadius();
            this.radiusY = radiusX;
            switch (seed.nextInt(3)) {
                case (0):
                    radiusX = radiusX + (2 * radiusX) / 3;
                    break;
                case (1):
                    radiusY = radiusY + (2 * radiusY) / 3;
                    break;
                case (2):
                    //noop
                    break;
            }
            maxRadiusX = radiusX;
            maxRadiusY = radiusY;
        }
    }
    private int w;
    private int h;
    private List<Star> stars = new ArrayList<Star>(50);

    ChristmasExtension(int w, int h) {
        adjustForSize(w, h);
    }

    @Override
    public void paint(Graphics g, BasePainter b) {
        for (ChristmasExtension.Star star : stars) {
            Color forceColor1 = null;
            Color forceColor2 = null;
            if (b instanceof ErrorPainter){
                forceColor1 = b.getBackgroundColor();
                forceColor2 = b.getWaterColor();
            }
            star.paint(g, forceColor1, forceColor2);
        }
    }

    @Override
    public void animate() {
        for (ChristmasExtension.Star star : stars) {
            star.animate();

        }
    }

    @Override
    public final void adjustForSize(int w, int h) {
        this.w = w;
        this.h = h;
        int count = w / (2 * (avarege_star_width + 1));
        while (stars.size() > count) {
            stars.remove(stars.size() - 1);
        }
        while (stars.size() < count) {
            stars.add(new Star());

        }

    }
}
