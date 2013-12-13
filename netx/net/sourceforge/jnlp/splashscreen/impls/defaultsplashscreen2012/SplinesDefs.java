/* SplinesDefs.java
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

import java.awt.Point;
import java.awt.Polygon;

public class SplinesDefs {

    private final static Point[] mainLeafArray = {
        new Point(268, 307),
        new Point(274, 326),
        new Point(289, 337),
        new Point(317, 349),
        new Point(362, 350),
        new Point(413, 334),
        new Point(428, 326),
        new Point(453, 309),
        new Point(469, 292),
        new Point(496, 264),
        new Point(516, 236),
        new Point(531, 215),
        new Point(550, 185),
        new Point(567, 155),
        new Point(580, 130),
        new Point(571, 139),
        new Point(555, 148),
        new Point(540, 157),
        new Point(521, 167),
        new Point(502, 174),
        new Point(477, 183),
        new Point(443, 193),
        new Point(413, 201),
        new Point(392, 209),
        new Point(376, 218),
        new Point(363, 228),
        new Point(356, 250),
        new Point(372, 231),
        new Point(398, 218),
        new Point(420, 209),
        new Point(446, 200),
        new Point(479, 192),
        new Point(505, 182),
        new Point(547, 168),
        new Point(539, 182),
        new Point(526, 204),
        new Point(509, 227),
        new Point(498, 244),
        new Point(486, 257),
        new Point(469, 272),
        new Point(460, 281),
        new Point(449, 293),
        new Point(436, 303),
        new Point(418, 315),
        new Point(400, 323),
        new Point(383, 332),
        new Point(367, 334),
        new Point(343, 338),
        new Point(322, 335),
        new Point(304, 330),
        new Point(288, 322)
    };
    private final static Point[] mainLeafStalkArray = {
        new Point(353, 287),
        new Point(366, 295),
        new Point(376, 291),
        new Point(392, 283),
        new Point(428, 251),
        new Point(441, 233),
        new Point(462, 217),
        new Point(446, 225),
        new Point(434, 236),
        new Point(428, 242),
        new Point(408, 261),
        new Point(392, 275),
        new Point(373, 284),
        new Point(363, 289)
    };
    private final static Point[] smallLeafArray = {
        new Point(342, 207),
        new Point(352, 213),
        new Point(360, 218),
        new Point(374, 217),
        new Point(389, 202),
        new Point(397, 175),
        new Point(396, 143),
        new Point(397, 113),
        new Point(380, 127),
        new Point(350, 145),
        new Point(327, 155),
        new Point(313, 166),
        new Point(297, 182),
        new Point(293, 196),
        new Point(308, 183),
        new Point(332, 167),
        new Point(364, 150),
        new Point(385, 137),
        new Point(384, 158),
        new Point(382, 187),
        new Point(371, 204)
    };
    private final static Point[] smallLeafStalkArray = {
        new Point(320, 203),
        new Point(331, 191),
        new Point(345, 185),
        new Point(356, 183),
        new Point(365, 177),
        new Point(368, 171),
        new Point(368, 165),
        new Point(360, 173),
        new Point(354, 176),
        new Point(341, 180),
        new Point(334, 184),
        new Point(321, 194)
    };

    public static Polygon getMainLeaf(Double scalex, double scaley) {

        return polygonizeControlPoints(mainLeafArray, scalex, scaley);
    }

    static Polygon polygonizeControlPoints(Point[] pp, double scalex, double scaley) {
        Polygon r = new Polygon();
        for (int i = 0; i < pp.length; i++) {
            Point p = pp[i];
            r.addPoint( (int) ((double) p.x * scalex), (int) ((double) p.y * scaley));
        }
        return r;
    }

    public static Polygon getSecondLeaf(double scalex, double scaley) {
        return polygonizeControlPoints(smallLeafArray, scalex, scaley);
    }

    public static Polygon getSecondLeafStalk(double scalex, double scaley) {
        return polygonizeControlPoints(smallLeafStalkArray, scalex, scaley);
    }

    public static Polygon getMainLeafStalk(double scalex, double scaley) {
        return polygonizeControlPoints(mainLeafStalkArray, scalex, scaley);
    }

    public static Polygon getMainLeafCurve(Double scalex, double scaley) {
        return getNatCubicClosed(getMainLeaf(scalex, scaley));
    }

    public static Polygon getMainLeafStalkCurve(Double scalex, double scaley) {
        return getNatCubicClosed(getMainLeafStalk(scalex, scaley));
    }

    public static Polygon getSecondLeafCurve(Double scalex, double scaley) {
        return getNatCubicClosed(getSecondLeaf(scalex, scaley));
    }

    public static Polygon getSecondLeafStalkCurve(Double scalex, double scaley) {
        return getNatCubicClosed(getSecondLeafStalk(scalex, scaley));
    }

    static Polygon getNatCubicClosed(Polygon p) {
        NatCubicClosed c = new NatCubicClosed();
        c.setSourcePolygon(p);
        return c.calcualteResult();
    }
}
