/* NatCubic.java
Copyright (C) 2012 Tim Lambert, Red Hat, Inc.

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

import java.awt.*;

public class NatCubic extends ControlCurve {

    /*
     * This class is part of the NatCubic implementation (http://www.cse.unsw.edu.au/~lambert/)
     * which does not have a license. The author (Tim Lambert) has agreed to
     * license this under GPL+Classpath by email
     *
     */
    /*
    NatCubic calcualtion
    calculates the natural cubic spline that interpolates
    y[0], y[1], ... y[n]
    The first segment is returned as
    C[0].a + C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0<=u <1
    the other segments are in C[1], C[2], ...  C[n-1] */
    Cubic[] calcNaturalCubic(int n, int[] x) {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /* We solve the equation
        [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
        |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
        |  1 4 1   | | .  | = |      .         |
        |    ..... | | .  |   |      .         |
        |     1 4 1| | .  |   |3(x[n] - x[n-2])|
        [       1 2] [D[n]]   [3(x[n] - x[n-1])]

        by using row operations to convert the matrix to upper triangular
        and then back sustitution.  The D[i] are the derivatives at the knots.
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x[1] - x[0]) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n];
        for (i = 0; i < n; i++) {
            C[i] = new Cubic((float) x[i], D[i], 3 * (x[i + 1] - x[i]) - 2 * D[i] - D[i + 1],
                    2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
        }
        return C;
    }
    final int STEPS = 12;

    /* draw a cubic spline */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (pts.npoints >= 2) {
            if (getResult() == null) {
                calcualteAndSaveResult();
            }
            g.drawPolyline(result.xpoints, result.ypoints, result.npoints);
        }
    }

    @Override
    public Polygon calcualteResult() {
        Cubic[] X = calcNaturalCubic(pts.npoints - 1, pts.xpoints);
        Cubic[] Y = calcNaturalCubic(pts.npoints - 1, pts.ypoints);
        /* very crude technique - just break each segment up into steps lines */
        Polygon p = new Polygon();
        p.addPoint(Math.round(X[0].eval(0)), Math.round(Y[0].eval(0)));
        for (int i = 0; i < X.length; i++) {
            for (int j = 1; j <= STEPS; j++) {
                float u = j / (float) STEPS;
                p.addPoint(Math.round(X[i].eval(u)), Math.round(Y[i].eval(u)));
            }
        }
        return p;
    }
}
