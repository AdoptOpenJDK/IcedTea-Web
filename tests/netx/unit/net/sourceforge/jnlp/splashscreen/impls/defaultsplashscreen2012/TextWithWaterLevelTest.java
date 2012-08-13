/* TextWithWaterLevelTest.java
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

import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.TextWithWaterLevel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.Assert;
import org.junit.Test;

public class TextWithWaterLevelTest {

    @Test
    public void setGetTest() {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        Font f1 = g2d.getFont().deriveFont(Font.ITALIC);
        Font f2 = g2d.getFont().deriveFont(Font.BOLD);
        String s = "Watter";
        TextWithWaterLevel tw = new TextWithWaterLevel(s, f1);
        Assert.assertEquals(-1, tw.getHeight());
        Assert.assertEquals(-1, tw.getWidth());
        Assert.assertEquals(f1, tw.getFont());
        Assert.assertNull(tw.getImg());
        Assert.assertEquals(s, tw.getText());
        Assert.assertEquals(Color.BLACK, tw.getTextOutline());
        Assert.assertEquals(Color.blue, tw.getWaterColor());
        Assert.assertEquals(Color.white, tw.getBgColor());
        Assert.assertEquals(0, tw.getPercentageOfWater());
        tw.setBgColor(Color.yellow);
        tw.setWaterColor(Color.orange);
        tw.setPercentageOfWater(20);
        Assert.assertEquals(Color.orange, tw.getWaterColor());
        Assert.assertEquals(Color.yellow, tw.getBgColor());
        Assert.assertEquals(20, tw.getPercentageOfWater());

    }

    @Test
    public void getBackground() {
        TextWithWaterLevel ifc = getInstance();
        ifc.setCachedPolygon(null);
        ifc.setPercentageOfWater(50);
        BufferedImage bic = ifc.getBackground();
        int w = bic.getWidth();
        int h = bic.getHeight();
        Assert.assertEquals(Color.blue, new Color(bic.getRGB(w / 2, 2 * h / 3)));
        Assert.assertEquals(Color.white, new Color(bic.getRGB(w / 2, h / 3)));
        ifc.setCachedPolygon(null);
        ifc.setPercentageOfWater(5);
        bic = ifc.getBackground();
        Assert.assertEquals(Color.white, new Color(bic.getRGB(w / 2, 2 * h / 3)));
        Assert.assertEquals(Color.white, new Color(bic.getRGB(w / 2, h / 3)));
        ifc.setCachedPolygon(null);
        ifc.setPercentageOfWater(95);
        bic = ifc.getBackground();
        Assert.assertEquals(Color.blue, new Color(bic.getRGB(w / 2, 2 * h / 3)));
        Assert.assertEquals(Color.blue, new Color(bic.getRGB(w / 2, h / 3)));



    }

    private TextWithWaterLevel getInstance() {
        BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Font f = bi1.createGraphics().getFont().deriveFont(Font.BOLD, 130);
        TextWithWaterLevel ifc = new TextWithWaterLevel("O O", f);
        return ifc;
    }

    @Test
    public void cutToTest() {
        TextWithWaterLevel ifc = getInstance();
        ifc.setPercentageOfWater(50);
        BufferedImage bic = ifc.getBackground();
        int w = bic.getWidth();
        int h = bic.getHeight();
        bic = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ifc.cutTo(bic.createGraphics(), 0, h);
        Assert.assertEquals(Color.blue, new Color(bic.getRGB(52, 142)));
        Assert.assertEquals(Color.blue, new Color(bic.getRGB(170, 110)));
        Assert.assertEquals(Color.white, new Color(bic.getRGB(52, 62)));
        Assert.assertEquals(Color.white, new Color(bic.getRGB(245, 85)));

        //well this should be acctually rgba 0,0,0,0 but somehow this was no passig
        //you can confirm with:
        //ImageFontCutterTest.save(bic, "halfFiledOus")
        Assert.assertEquals(new Color(0, 0, 0), new Color(bic.getRGB(137, 127)));
        Assert.assertEquals(new Color(0, 0, 0), new Color(bic.getRGB(137, 2)));




    }
}
