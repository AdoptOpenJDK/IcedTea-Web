/* SplinesDefsTest.java
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

import net.sourceforge.jnlp.splashscreen.impls.defaultsplashscreen2012.SplinesDefs;
import java.awt.Point;
import java.awt.Polygon;
import org.junit.Assert;
import org.junit.Test;

public class SplinesDefsTest {

    private static Point[] testArray = {
        new Point(0, 0),
        new Point(100, 0),
        new Point(100, 100),
        new Point(0, 100)
    };

    @Test
    public void polygonizeControlPointsTest() {

        Polygon p = SplinesDefs.polygonizeControlPoints(testArray, 1d, 1d);
        Assert.assertTrue(p.contains(50, 50));
        Assert.assertFalse(p.contains(150, 150));
        Assert.assertFalse(p.contains(-50, -50));
        p = SplinesDefs.polygonizeControlPoints(testArray, 0.5d, 0.5d);
        Assert.assertTrue(p.contains(20, 20));
        Assert.assertFalse(p.contains(75, 75));
        Assert.assertFalse(p.contains(-25, -25));
        p = SplinesDefs.polygonizeControlPoints(testArray, 2d, 2d);
        Assert.assertTrue(p.contains(150, 150));
        Assert.assertFalse(p.contains(250, 250));
        Assert.assertFalse(p.contains(-50, -50));


    }

    @Test
    public void testApi() {
        double x = 1d;
        Polygon[] p = {SplinesDefs.getMainLeaf(x, x),
            SplinesDefs.getMainLeafCurve(x, x),
            SplinesDefs.getMainLeafStalk(x, x),
            SplinesDefs.getMainLeafStalkCurve(x, x),
            SplinesDefs.getSecondLeaf(x, x),
            SplinesDefs.getSecondLeafCurve(x, x),
            SplinesDefs.getSecondLeafStalk(x, x),
            SplinesDefs.getSecondLeafStalkCurve(x, x)};
        for (Polygon polygon : p) {
            Assert.assertNotNull(polygon);
            Assert.assertTrue(polygon.npoints > 5);

        }

    }
}
