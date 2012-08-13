/* ErrorPainterTest.java
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

import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.ErrorPainter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import net.sourceforge.jnlp.splashscreen.SplashUtils.SplashReason;
import net.sourceforge.jnlp.splashscreen.impls.DefaultSplashScreen2012;
import org.junit.Assert;
import org.junit.Test;

public final class ErrorPainterTest {

    @Test
    public void interpolTest() {
        Assert.assertEquals(15, ErrorPainter.interpol(4, 2, 10, 20), 0.1d);
        Assert.assertEquals(-15, ErrorPainter.interpol(4, 2, -20, -10), 0.1d);
        Assert.assertEquals(30, ErrorPainter.interpol(2, 4, 10, 20), 0.1d);
    }

    @Test
    public void interpolColorTest() {
        Color c1 = new Color(0, 0, 0);
        Color c2 = new Color(200, 200, 200);
        Color c3 = new Color(100, 100, 100);
        Color c4 = ErrorPainter.interpolateColor(4, 2, c1, c2);
        Assert.assertEquals(c3, c4);

    }

//    public static void main(String[] a) {
//        Color c1 = new Color(250, 50, 0, 50);
//        Color c2 = new Color(0, 0, 250, 100);
//        for (int i = 0; i < 21; i++) {
//            Color q = ErrorPainter.interpolateColor(20, i, c1, c2);
//            System.out.println(q.toString());
//            System.out.println(q.getAlpha());
//        }
//    }
    @Test
    public void adjustForSizeTest() {
        ErrorPainter bp = new ErrorPainter(new DefaultSplashScreen2012(100, 100, SplashReason.APPLET));
        bp.master.setSplashHeight(100);
        bp.master.setSplashWidth(100);
        bp.adjustForSize(100, 100);
        Assert.assertNotNull(bp.prerenderedStuff);
        BufferedImage i1 = bp.prerenderStill();
        Assert.assertEquals(100, i1.getWidth());
        Assert.assertEquals(100, i1.getHeight());
        bp.adjustForSize(20, 20);
        Assert.assertNotNull(bp.prerenderedStuff);
        Assert.assertEquals(20, bp.prerenderedStuff.getWidth());
        Assert.assertEquals(20, bp.prerenderedStuff.getHeight());
        Assert.assertFalse(i1.getWidth() == bp.prerenderedStuff.getWidth());
        Assert.assertFalse(i1.getHeight() == bp.prerenderedStuff.getHeight());


    }

    @Test
    public void adjustForSizeTest2() {
        ErrorPainter bp = new ErrorPainter(new DefaultSplashScreen2012(0, 0, SplashReason.APPLET), false);
        Assert.assertNull(bp.prerenderedStuff);
        bp.master.setSplashHeight(10);
        bp.master.setSplashWidth(10);
        BufferedImage i1 = bp.prerenderStill();
        Assert.assertEquals(10, i1.getWidth());
        Assert.assertEquals(10, i1.getHeight());
        bp.adjustForSize(20, 20);
        Assert.assertNotNull(bp.prerenderedStuff);
        Assert.assertEquals(20, bp.prerenderedStuff.getWidth());
        Assert.assertEquals(20, bp.prerenderedStuff.getHeight());
        Assert.assertFalse(i1.getWidth() == bp.prerenderedStuff.getWidth());
        Assert.assertFalse(i1.getHeight() == bp.prerenderedStuff.getHeight());


    }
}
