/* TextOutlineRendererTest.java
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
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.Assert;
import org.junit.Test;

public class TextOutlineRendererTest {

    @Test
    public void getSetTest() {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        Font f1 = g2d.getFont().deriveFont(Font.ITALIC);
        Font f2 = g2d.getFont().deriveFont(Font.BOLD);
        String s = "Hello";
        TextOutlineRenderer ifc = new TextOutlineRenderer(f1, s);
        Assert.assertEquals(-1, ifc.getHeight());
        Assert.assertEquals(-1, ifc.getWidth());
        Assert.assertEquals(f1, ifc.getFont());
        Assert.assertNull(ifc.getImg());
        Assert.assertEquals(s, ifc.getText());
        Assert.assertEquals(Color.BLACK, ifc.getTextOutline());
        ifc.setImg(bi);
        Assert.assertEquals(100, ifc.getHeight());
        Assert.assertEquals(100, ifc.getWidth());
        Assert.assertEquals(f1, ifc.getFont());
        Assert.assertEquals(bi, ifc.getImg());
        Assert.assertEquals(s, ifc.getText());
        Assert.assertEquals(Color.BLACK, ifc.getTextOutline());

        TextOutlineRenderer xfc = new TextOutlineRenderer(f1, s, Color.red);
        xfc.setImg(bi);
        xfc.setFont(f2);
        String ss = "HelloHello";
        Assert.assertEquals(100, xfc.getHeight());
        Assert.assertEquals(100, xfc.getWidth());
        Assert.assertEquals(f2, xfc.getFont());
        Assert.assertEquals(bi, xfc.getImg());
        Assert.assertEquals(s, xfc.getText());
        Assert.assertEquals(Color.red, xfc.getTextOutline());
        xfc.setTextOutline(Color.white);
        Assert.assertEquals(Color.white, xfc.getTextOutline());

    }

    @Test
    public void cutToTest() {
        BufferedImage bi1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d1 = bi1.createGraphics();
        g2d1.setColor(Color.red);
        g2d1.fillRect(0, 0, 100, 100);


        BufferedImage bi2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d2 = bi2.createGraphics();
        g2d2.setColor(Color.blue);
        g2d2.fillRect(0, 0, 100, 100);
        TextOutlineRenderer ifc = new TextOutlineRenderer(g2d1.getFont().deriveFont(Font.BOLD, 130), "O");
        ifc.setImg(bi1);
        ifc.cutTo(g2d2, -5, 100);
        Color c1 = new Color(bi2.getRGB(1, 1));
        Assert.assertEquals(Color.blue, c1);
        Color c2 = new Color(bi2.getRGB(50, 50));
        Assert.assertEquals(Color.blue, c2);
        Color c3 = new Color(bi2.getRGB(30, 30));
        Assert.assertEquals(Color.red, c3);
        Color c4 = new Color(bi2.getRGB(70, 70));
        Assert.assertEquals(Color.red, c4);
        Color c5 = new Color(bi2.getRGB(26, 52));
        Assert.assertEquals(Color.black, c5);


    }

    public static void save(BufferedImage bi1, String string) {
        try {
            String name = string;
            if (name == null || name.trim().length() <= 0) {
                name = "testImage";
            }
            ImageIO.write(bi1, "png", new File(System.getProperty("user.home") + "/Desktop/" + name + ".png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
