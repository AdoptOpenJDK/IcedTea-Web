/* NatCubicClosed.java
Copyright (C) 2012 Tim Lambert, Red Hat, Inc.,

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

public class NatCubicClosed extends NatCubic {

    /*
     * This class is part of the NatCubic implementation (http://www.cse.unsw.edu.au/~lambert/)
     * which does not have a license. The author (Tim Lambert) has agreed to
     * license this under GPL+Classpath by email
     *
     */
    /*
    NatCubic calcualtion
    calculates the closed natural cubic spline that interpolates
    x[0], x[1], ... x[n]
    The first segment is returned as
    C[0].a + C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0<=u <1
    the other segments are in C[1], C[2], ...  C[n] */
    @Override
    Cubic[] calcNaturalCubic(int n, int[] x) {
        float[] w = new float[n + 1];
        float[] v = new float[n + 1];
        float[] y = new float[n + 1];
        float[] D = new float[n + 1];
        float z, F, G, H;
        int k;
        /* We solve the equation
        [4 1      1] [D[0]]   [3(x[1] - x[n])  ]
        |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
        |  1 4 1   | | .  | = |      .         |
        |    ..... | | .  |   |      .         |
        |     1 4 1| | .  |   |3(x[n] - x[n-2])|
        [1      1 4] [D[n]]   [3(x[0] - x[n-1])]

        by decomposing the matrix into upper triangular and lower matrices
        and then back sustitution.  See Spath "Spline Algorithms for Curves
        and Surfaces" pp 19--21. The D[i] are the derivatives at the knots.
         */
        w[1] = v[1] = z = 1.0f / 4.0f;
        y[0] = z * 3 * (x[1] - x[n]);
        H = 4;
        F = 3 * (x[0] - x[n - 1]);
        G = 1;
        for (k = 1; k < n; k++) {
            v[k + 1] = z = 1 / (4 - v[k]);
            w[k + 1] = -z * w[k];
            y[k] = z * (3 * (x[k + 1] - x[k - 1]) - y[k - 1]);
            H = H - G * w[k];
            F = F - G * y[k - 1];
            G = -v[k] * G;
        }
        H = H - (G + 1) * (v[n] + w[n]);
        y[n] = F - (G + 1) * y[n - 1];

        D[n] = y[n] / H;
        D[n - 1] = y[n - 1] - (v[n] + w[n]) * D[n]; /* This equation is WRONG! in my copy of Spath */
        for (k = n - 2; k >= 0; k--) {
            D[k] = y[k] - v[k + 1] * D[k + 1] - w[k + 1] * D[n];
        }


        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n + 1];
        for (k = 0; k < n; k++) {
            C[k] = new Cubic((float) x[k], D[k], 3 * (x[k + 1] - x[k]) - 2 * D[k] - D[k + 1],
                    2 * (x[k] - x[k + 1]) + D[k] + D[k + 1]);
        }
        C[n] = new Cubic((float) x[n], D[n], 3 * (x[0] - x[n]) - 2 * D[n] - D[0],
                2 * (x[n] - x[0]) + D[n] + D[0]);
        return C;
    }
}
