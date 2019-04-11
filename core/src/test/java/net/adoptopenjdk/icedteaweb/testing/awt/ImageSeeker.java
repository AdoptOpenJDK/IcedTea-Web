/* ImageSeeker.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
 */


package net.adoptopenjdk.icedteaweb.testing.awt;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;



public class ImageSeeker
{
    public static Rectangle findExactImage(BufferedImage marker, BufferedImage screen){
        return findExactImage(marker, screen, new Rectangle(0,0,screen.getWidth(), screen.getHeight()));
    }
    
    public static Rectangle findExactImage(BufferedImage marker /*usually small*/, BufferedImage screen, Rectangle actionArea) {
        Rectangle result = new Rectangle(0, 0, 0, 0);
        boolean found = false;
        boolean ok = true;
        //to filter out values with alpha
        boolean[][] mask = getMask(marker);
        //accessing those too often, copying
        int[][] markerPixels = getPixels(marker);
        int mw = marker.getWidth();
        int mh = marker.getHeight();
        for (int y = actionArea.y; (y < (actionArea.y + actionArea.height - marker.getHeight())) && !found; y++) {
            for (int x = actionArea.x; (x < (actionArea.x + actionArea.width - marker.getWidth())) && !found; x++) {


                for (int my = 0; (my < mh) && ok; my++) {
                    for (int mx = 0; (mx < mw) && ok; mx++) {

                        //ignore masked (having alpha) values
                        if (!mask[mx][my]) {
                            continue;
                        }
                        if (markerPixels[mx][my] != screen.getRGB(x + mx, y + my)) {
                            ok = false;
                        }
                    }
                }
                if( ok ){
                    found = true;
                    result.x = x;
                    result.y = y;
                    result.height = marker.getHeight();
                    result.width = marker.getWidth();
                }else{
                    ok = true;
                }                
            }
        }
        
        if(found){
            return result;
        }else{
            return null;
        }
    }



    public static boolean isRectangleValid(Rectangle r){
        
        if (r == null) return false;
        
        return (r.width != 0)&&(r.height != 0)&&(r.x != Integer.MIN_VALUE)&&(r.y != Integer.MIN_VALUE);
    }

    public static boolean[][] getMask(BufferedImage icon) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        boolean[][] r = new boolean[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = icon.getRGB(x, y);
                int alpha = (i >> 24) & 0xff;
                r[x][y] = alpha == 255;
            }
        }
        return r;
    }

    public static int[][] getPixels(BufferedImage icon) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        int[][] r = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int i = icon.getRGB(x, y);
                //remove mask? not yet...
                r[x][y] = i;
            }
        }
        return r;
    }
}
