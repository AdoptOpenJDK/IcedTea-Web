/* TextOutlineRenderer.java
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public class TextOutlineRenderer {

    private Image img;
    private Font font;
    private Color outlineColor;
    private final String text;

    public TextOutlineRenderer(Font f, String s) {
        this.font = f;
        outlineColor = Color.black;
        this.text = s;

    }

    public TextOutlineRenderer(Font f, String s, Color textOutline) {
        this(f, s);
        this.outlineColor = textOutline;
    }

    public int getWidth() {
        if (img == null) {
            return -1;
        }
        return img.getWidth(null);
    }

    public int getHeight() {
        if (img == null) {
            return -1;
        }
        return img.getHeight(null);
    }

    public void cutTo(Graphics2D g2, int x, int y) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(getText(), getFont(), frc);
        float sw = (float) tl.getBounds().getWidth();
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(x, y);
        Shape shape = tl.getOutline(transform);
        Rectangle r = shape.getBounds();
        g2.setColor(getTextOutline());
        g2.draw(shape);
        g2.setClip(shape);
        g2.drawImage(getImg(), r.x, r.y, r.width, r.height, null);

    }

    /**
     * @return the img
     */
    public Image getImg() {
        return img;
    }

    /**
     * @param img the img to set
     */
    public void setImg(Image img) {
        this.img = img;
    }

    /**
     * @return the font
     */
    public Font getFont() {
        return font;
    }

    /**
     * @param font the font to set
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @return the color of outline
     */
    public Color getTextOutline() {
        return outlineColor;
    }

    /**
     * @param textOutline the color of outline
     */
    public void setTextOutline(Color textOutline) {
        this.outlineColor = textOutline;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
}
