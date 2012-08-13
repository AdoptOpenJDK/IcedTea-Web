/* TextWithWaterLevel.java
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

import java.awt.Polygon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TextWithWaterLevel extends TextOutlineRenderer {

    private Color waterColor;
    private Color bgColor;
    private int percentageOfWater;
    private Random sea = new Random();
    //set to null befor getBackground if waving is needed
    //or create new TWL ;)
    private Polygon cachedPolygon;

    public TextWithWaterLevel(String s, Font f) {
        super(f, s);
        waterColor = Color.BLUE;
        bgColor = Color.white;

    }

    protected Point getFutureSize() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        FontMetrics fm = bi.createGraphics().getFontMetrics(getFont());
        int w = fm.stringWidth(getText());
        int h = fm.getHeight();
        return new Point(w, h);
    }

    public BufferedImage getBackground() {
        Point p = getFutureSize();
        int w = p.x;
        int h = p.y;
        if (w <= 0 || h <= 0) {
            return null;
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h);
        if (cachedPolygon == null) {
            int level = (h * percentageOfWater) / 100;
            int waveHeight = 10;
            int waveLength = 20;
            if (level > waveHeight / 2 + 1) {
                NatCubic line = new NatCubic();
                int x = 0;
                while (x < w + 2 * waveLength) {
                    line.addPoint(x, h - level - waveHeight / 2 - sea.nextInt(waveHeight));
                    x = x + waveLength;
                }
                cachedPolygon = line.calcualteResult();
                cachedPolygon.addPoint(w, h);
                cachedPolygon.addPoint(0, h);
            }
        }
        g2d.setColor(waterColor);
        if (cachedPolygon != null) {
            g2d.fillPolygon(cachedPolygon);
        }
        //line.paint(g2d);
        //FlodFill.floodFill(bi, waterColor, new Point(1, h - 1));
        return bi;
    }

    public Polygon getCachedPolygon() {
        return cachedPolygon;
    }

    public void setCachedPolygon(Polygon cachedPolygon) {
        this.cachedPolygon = cachedPolygon;
    }

    @Override
    public void cutTo(Graphics2D g2, int x, int y) {
        if (this.getImg() == null) {
            this.setImg(getBackground());
        }
        if (this.getImg() == null) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(getFont());
        g2.setColor(getTextOutline());
        g2.drawString(getText(), x, y - 2);
        g2.drawString(getText(), x, y + 2);
        g2.drawString(getText(), x - 2, y);
        g2.drawString(getText(), x + 2, y);
        //sorry, cuted text have disturbed borders
        super.cutTo(g2, x, y);
    }

    /**
     * @return the waterColor
     */
    public Color getWaterColor() {
        return waterColor;
    }

    /**
     * @param waterColor the waterColor to set
     */
    public void setWaterColor(Color waterColor) {
        this.waterColor = waterColor;
    }

    /**
     * @return the bgColor
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * @param bgColor the bgColor to set
     */
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * @return the percentageOfWater
     */
    public int getPercentageOfWater() {
        return percentageOfWater;
    }

    /**
     * @param percentageOfWater the percentageOfWater to set
     */
    public void setPercentageOfWater(int percentageOfWater) {
        this.percentageOfWater = percentageOfWater;
    }
}
