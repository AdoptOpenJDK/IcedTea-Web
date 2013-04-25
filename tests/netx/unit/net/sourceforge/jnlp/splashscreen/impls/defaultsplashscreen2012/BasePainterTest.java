/* BasePainterTest.java
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

import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.BasePainter;
import java.awt.image.BufferedImage;
import net.sourceforge.jnlp.splashscreen.SplashUtils.SplashReason;
import net.sourceforge.jnlp.splashscreen.impls.DefaultSplashScreen2012;
import org.junit.Assert;


import org.junit.Test;

public class BasePainterTest {

    @Test
    public void scaleTest() {
        Assert.assertEquals(10, BasePainter.scale(2, 4, 5), 0.1d);
        Assert.assertEquals(4, BasePainter.scale(4, 2, 8), 0.1d);
    }

    @Test
    public void getRatioTest() {
        Assert.assertEquals(2, BasePainter.getRatio(2, 4), 0.1d);
        Assert.assertEquals(0.5, BasePainter.getRatio(4, 2), 0.1d);
    }

    @Test
    public void incLevel2Test() {
        BasePainter bp = new BasePainter(new DefaultSplashScreen2012(100, 100, SplashReason.APPLET));
        int l1 = bp.getWaterLevel();
        int l2 = bp.getAnimationsPosition();
        bp.increaseAnimationPosition();
        Assert.assertFalse(l2 == bp.getAnimationsPosition());
        Assert.assertTrue(l1 == bp.getWaterLevel());
    }

    @Test
    public void adjustForSizeTest() {
        BasePainter bp = new BasePainter(new DefaultSplashScreen2012(100, 100, SplashReason.APPLET));
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
        BasePainter bp = new BasePainter(new DefaultSplashScreen2012(0, 0, SplashReason.APPLET), false);
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

    @Test
    public void stripCommitFromVersion() {
        Assert.assertEquals("1.4", BasePainter.stripCommitFromVersion("1.4"));
        Assert.assertEquals("1.4.2", BasePainter.stripCommitFromVersion("1.4.2"));
        Assert.assertEquals("1.4pre", BasePainter.stripCommitFromVersion("1.4pre"));
        Assert.assertEquals("1.4", BasePainter.stripCommitFromVersion("1.4+657tgkhyu4iy5"));
        Assert.assertEquals("1.4.2", BasePainter.stripCommitFromVersion("1.4.2+887tgjh07tftvhjj"));
        Assert.assertEquals("1.4pre+0977tyugg", BasePainter.stripCommitFromVersion("1.4pre+0977tyugg"));

        Assert.assertEquals("1.4pre+", BasePainter.stripCommitFromVersion("1.4pre+"));
        Assert.assertEquals("1.4pre+foo+", BasePainter.stripCommitFromVersion("1.4pre+foo+"));
        Assert.assertEquals("1.4pre+foo+bar", BasePainter.stripCommitFromVersion("1.4pre+foo+bar"));
        
        Assert.assertEquals("1.4", BasePainter.stripCommitFromVersion("1.4+"));
        Assert.assertEquals("1.4", BasePainter.stripCommitFromVersion("1.4+foo+"));
        Assert.assertEquals("1.4", BasePainter.stripCommitFromVersion("1.4+foo+bar"));
    }
}
