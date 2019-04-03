/* ControlCurveTest.java
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

/** This class represents a curve defined by a sequence of control points */
/*  Part of NatCubic implementation, inspire by http://www.cse.unsw.edu.au/~lambert/*/
import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.ControlCurve;
import java.awt.*;
import org.junit.Assert;
import org.junit.Test;

public class ControlCurveTest {

    @Test
    public void setGetTests() {
        Polygon p1 = new Polygon();
        Polygon p2 = new Polygon();
        Polygon p3 = new Polygon();
        ControlCurve cc = new ControlCurve();
        Assert.assertNotNull(cc.getSourcePolygon());
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        Assert.assertTrue(cc.isWithPoints());
        cc.setSourcePolygon(p1);
        Assert.assertNotNull(cc.getSourcePolygon());
        Assert.assertEquals(p1, cc.getSourcePolygon());
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        Assert.assertTrue(cc.isWithPoints());
        cc.setWithPoints(false);
        Assert.assertFalse(cc.isWithPoints());

        cc = new ControlCurve(p2);
        Assert.assertNotNull(cc.getSourcePolygon());
        Assert.assertEquals(p2, cc.getSourcePolygon());
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setWithPoints(false);
        Assert.assertFalse(cc.isWithPoints());
        cc.setSourcePolygon(p3);
        Assert.assertNotNull(cc.getSourcePolygon());
        Assert.assertEquals(p3, cc.getSourcePolygon());
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setWithPoints(false);
        Assert.assertFalse(cc.isWithPoints());
        cc.setWithPoints(true);
        Assert.assertTrue(cc.isWithPoints());

    }

    @Test
    public void sqrTest() {
        Assert.assertEquals(25, ControlCurve.sqr(5));
    }
    static int[] xs = {0, 100, 100, 0};
    static int[] ys = {0, 0, 100, 1000};

    static ControlCurve getTestInstance() {
        ControlCurve cc = new ControlCurve();
        cc.addPoint(xs[0], ys[0]);
        cc.addPoint(xs[1], ys[1]);
        cc.addPoint(xs[2], ys[2]);
        cc.addPoint(xs[3], ys[3]);
        return cc;

    }

    @Test
    public void selectPointTest() {
        ControlCurve cc = getTestInstance();
        int i = cc.selectPoint(-50, -50);
        Assert.assertEquals(-1, i);
        i = cc.selectPoint(-3, 3);
        Assert.assertEquals(0, i);
        i = cc.selectPoint(97, 97);
        Assert.assertEquals(2, i);
        i = cc.selectPoint(100, 50);
        Assert.assertEquals(-1, i);
    }

    @Test
    public void addPoint() {
        ControlCurve cc = new ControlCurve();
        Assert.assertEquals(0, cc.getSourcePolygon().npoints);
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.addPoint(10, 10);
        Assert.assertEquals(1, cc.getSourcePolygon().npoints);
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.addPoint(10, 10);
        Assert.assertEquals(2, cc.getSourcePolygon().npoints);
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.addPoint(100, 100);
        Assert.assertEquals(3, cc.getSourcePolygon().npoints);
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
    }

    @Test
    public void setPointTest1() {
        ControlCurve cc = getTestInstance();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setSelection(-1);
        cc.setPoint(10, 10);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.setSelection(4);
        cc.setPoint(10, 10);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.setSelection(3);
        cc.setPoint(10, 20);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        Assert.assertEquals(20, cc.getSourcePolygon().ypoints[3]);
        Assert.assertEquals(10, cc.getSourcePolygon().xpoints[3]);
    }

    @Test
    public void setPointTest2() {
        ControlCurve cc = getTestInstance();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setPoint(-1, 10, 10);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.setPoint(4, 10, 10);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.setPoint(3, 10, 20);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        Assert.assertEquals(20, cc.getSourcePolygon().ypoints[3]);
        Assert.assertEquals(10, cc.getSourcePolygon().xpoints[3]);

    }

    @Test
    public void removePoint2() {
        ControlCurve cc = getTestInstance();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.removePoint(-1);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.removePoint(4);
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.removePoint(3);
        Assert.assertEquals(3, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());

    }

    public void removePoint1() {
        ControlCurve cc = getTestInstance();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setSelection(-1);
        cc.removePoint();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setSelection(4);
        cc.removePoint();
        Assert.assertEquals(4, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ys[i], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());
        cc.setSelection(0);
        cc.removePoint();
        Assert.assertEquals(3, cc.getSourcePolygon().npoints);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(ys[i + 1], cc.getSourcePolygon().ypoints[i]);
            Assert.assertEquals(xs[i + 1], cc.getSourcePolygon().xpoints[i]);
        }
        Assert.assertNull(cc.getResult());
        cc.calcualteAndSaveResult();
        Assert.assertNull(cc.getResult());

    }
}
