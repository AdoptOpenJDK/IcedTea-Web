/* ControlCurve.java
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

/** This class represents a curve defined by a sequence of control points */

/*
 * This class is part of the NatCubic implementation (http://www.cse.unsw.edu.au/~lambert/)
 * which does not have a license. The author (Tim Lambert) has agreed to
 * license this under GPL+Classpath by email
 *
 */
import java.awt.*;

public class ControlCurve {

    protected Polygon pts;
    protected Polygon result;
    protected boolean withPoints = true;
    protected int selection = -1;

    public ControlCurve() {
        pts = new Polygon();
    }

    public ControlCurve(Polygon p) {
        pts = p;
    }

    public Polygon getSourcePolygon() {
        return pts;
    }

    public void setSourcePolygon(Polygon pts) {
        this.pts = pts;
    }
    static Font f = new Font("Courier", Font.PLAIN, 12);

    /**
     * to be overwriten
     */
    public Polygon calcualteResult() {
        return null;
    }

    public void calcualteAndSaveResult() {
        result = calcualteResult();
    }

    /** paint this curve into g.*/
    public void paint(Graphics g) {
        if (isWithPoints()) {
            FontMetrics fm = g.getFontMetrics(f);
            g.setFont(f);
            int h = fm.getAscent() / 2;

            for (int i = 0; i < pts.npoints; i++) {
                String s = Integer.toString(i);
                int w = fm.stringWidth(s) / 2;
                g.drawString(Integer.toString(i), pts.xpoints[i] - w, pts.ypoints[i] + h);
            }
        }
    }
    static final int EPSILON = 36;  /* square of distance for picking */


    /** return index of control point near to (x,y) or -1 if nothing near */
    public int selectPoint(int x, int y) {
        int mind = Integer.MAX_VALUE;
        selection = -1;
        for (int i = 0; i < pts.npoints; i++) {
            int d = sqr(pts.xpoints[i] - x) + sqr(pts.ypoints[i] - y);
            if (d < mind && d < EPSILON) {
                mind = d;
                selection = i;
            }
        }
        return selection;
    }

    // square of an int
    static int sqr(int x) {
        return x * x;
    }

    public Polygon getResult() {
        return result;
    }

    public void resetResult() {
        this.result = null;
    }

    /** add a control point, return index of new control point */
    public int addPoint(int x, int y) {
        pts.addPoint(x, y);
        resetResult();
        return selection = pts.npoints - 1;
    }

    /** set selected control point */
    public void setPoint(int x, int y) {
        setPoint(selection, x, y);
    }

    /** set selected control point */
    public void setPoint(int index, int x, int y) {
        if (index >= 0 && index < pts.npoints) {
            pts.xpoints[index] = x;
            pts.ypoints[index] = y;
            resetResult();
        }
    }

    /** remove selected control point */
    public void removePoint(int index) {
        if (index >= 0 && index < pts.npoints) {
            pts.npoints--;
            for (int i = index; i < pts.npoints; i++) {
                pts.xpoints[i] = pts.xpoints[i + 1];
                pts.ypoints[i] = pts.ypoints[i + 1];
            }
            resetResult();
        }
    }

    /** remove selected control point */
    public void removePoint() {
        removePoint(selection);
    }

    public boolean isWithPoints() {
        return withPoints;
    }

    public void setWithPoints(boolean withPoints) {
        this.withPoints = withPoints;
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < pts.npoints; i++) {
            r.append(" ").append(pts.xpoints[i]).append(" ").append(pts.ypoints[i]);
        }
        return r.toString();
    }

    /**
     * for testing purposes
     * @param selection
     */
    void setSelection(int selection) {
        this.selection = selection;
    }
}
