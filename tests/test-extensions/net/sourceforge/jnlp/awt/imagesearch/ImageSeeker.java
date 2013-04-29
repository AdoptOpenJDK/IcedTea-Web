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


package net.sourceforge.jnlp.awt.imagesearch;

import java.awt.Color;
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
    
    public static Rectangle findBlurredImage(BufferedImage marker, BufferedImage testImage, double minCorrelation){
        return findBlurredImage(marker, testImage, minCorrelation, new Rectangle(0,0,testImage.getWidth(), testImage.getHeight()));
    }
    
    public static Rectangle findBlurredImage(BufferedImage marker, BufferedImage testImage, double minCorrelation, Rectangle actionArea)
    {
        int maxX = actionArea.width - marker.getWidth() - 1;
        int maxY = actionArea.height - marker.getHeight() - 1;
        int markerMaxX = marker.getWidth();
        int markerMaxY = marker.getHeight();

        // it is much faster to work directly with color components stored as float values
        float[][][] testImageArray = createArrayForOneColorComponent(actionArea);
        float[][][] markerImageArray = createArrayForOneColorComponent(marker);

        convertImageToFloatArray(testImage, testImageArray, actionArea);
        convertImageToFloatArray(marker, markerImageArray);

        int bestX = -1;
        int bestY = -1;
        double bestCorrelation = -1;

        for (int yoffset = 0; yoffset < maxY; yoffset++ )
        {
            for (int xoffset = 0; xoffset < maxX; xoffset++)
            {
                double correlation = computeCorrelation(markerMaxX, markerMaxY, testImageArray, markerImageArray, yoffset, xoffset);
                if (correlation > bestCorrelation)
                {
                    bestCorrelation = correlation;
                    bestX = xoffset + actionArea.x;
                    bestY = yoffset + actionArea.y;
                }
            }
        }
        if(bestCorrelation > minCorrelation){
            return new Rectangle(bestX, bestY, marker.getWidth(), marker.getHeight());
        }else{
            return null;
        }
        
    }

    /**
     * Create three-dimensional array with the same size as tested image
     * dimensions (last dimension is used for storing RGB components).
     * 
     * @param testImage tested image
     * @return newly created three-dimensional array
     */
    private static float[][][] createArrayForOneColorComponent(BufferedImage testImage)
    {
        return new float[testImage.getHeight()][testImage.getWidth()][3];
    }
    
    /**
     * Create three-dimensional array with the same size as the given area
     * dimensions (last dimension is used for storing RGB components).
     * 
     * @param actionArea
     * @return newly created three-dimensional array
     */
    private static float[][][] createArrayForOneColorComponent(Rectangle actionArea)
    {
        return new float[actionArea.height][actionArea.width][3];
    }

    /**
     * Conversion from BufferedImage into three dimensional float arrays.
     * It's much faster to work with float arrays even if it's memory ineficient.
     *
     * @param testImage tested image
     * @param array array to fill
     */
    private static void convertImageToFloatArray(BufferedImage testImage, float[][][] array)
    {
        for (int y = 0; y < testImage.getHeight(); y++)
        {
            for (int x = 0; x < testImage.getWidth(); x++)
            {
                int c = testImage.getRGB(x, y);
                // filter out alpha channel
                c = c & 0xffffff;
                array[y][x][0] = ((c >> 16) & 0xff) - 128f;
                array[y][x][1] = ((c >> 8) & 0xff) - 128f;
                array[y][x][2] = (c & 0xff) - 128f;
            }
        }
    }

    /**
     * Conversion from BufferedImage into three dimensional float arrays.
     * It's much faster to work with float arrays even if it's memory ineficient.
     * This method converts only a given part of the image (actionArea)
     * 
     * @param testImage tested image
     * @param array array to fill
     * @param actionArea rectangle part of the image to convert
     */
    private static void convertImageToFloatArray(BufferedImage testImage, float[][][] array, Rectangle actionArea)
    {
        for (int y = actionArea.y; y < (actionArea.height + actionArea.y); y++)
        {
            for (int x = actionArea.x; x < (actionArea.width + actionArea.x); x++)
            {
                int c = testImage.getRGB(x, y);
                // filter out alpha channel
                c = c & 0xffffff;
                array[y - actionArea.y][x - actionArea.x][0] = ((c >> 16) & 0xff) - 128f;
                array[y - actionArea.y][x - actionArea.x][1] = ((c >> 8) & 0xff) - 128f;
                array[y - actionArea.y][x - actionArea.x][2] = (c & 0xff) - 128f;
            }
        }
    }
    
    /**
     * Compute correlation for given two images and 2D offset.
     *
     * @param maxX
     * @param maxY
     * @param testImageArray
     * @param markerImageArray
     * @param yoffset
     * @param xoffset
     * @return
     */
    private static double computeCorrelation(int maxX, int maxY, float[][][] testImageArray,
                    float[][][] markerImageArray, int yoffset, int xoffset)
    {
        double correlation = 0;
        for (int y = 0; y < maxY; y++)
        {
            for (int x = 0; x < maxX; x++)
            {
                for (int rgbIndex = 0; rgbIndex < 3; rgbIndex++)
                {
                    float colorComponent1 = markerImageArray[y][x][rgbIndex];
                    float colorComponent2 = testImageArray[yoffset + y][xoffset + x][rgbIndex];
                    correlation += colorComponent1 * colorComponent2;
                }
            }
        }
        return correlation;
    }

    public static int findHorizontalRule(BufferedImage screen,
            Color ruleColor, Color bgColor, boolean fromTop) {
        final int height = screen.getHeight();
        int gap = 0;

        if (!fromTop) {
            return findHorizontalEdgeGap(screen, ruleColor,
                    bgColor, 1, height - 1, gap);
        } else {
            return findHorizontalEdgeGap(screen, bgColor,
                    ruleColor, 1, height - 1, gap);
        }
    }

    public static int findHorizontalEdgeGap(BufferedImage screen,
            Color area1Color, Color area2Color, int y1, int y2, int gap) {
        final int width = screen.getWidth(); 
        final int area1RGB = area1Color.getRGB();
        final int area2RGB = area2Color.getRGB();
        int edgePosition = Integer.MIN_VALUE;
        int lastFound = Integer.MIN_VALUE;
        
        for (int y = y1+1; y < y2 - gap; y++) {
            int found = 0;
            for (int x = 0; x < width; x++) {
                int c1 = screen.getRGB(x, y - 1);
                int c2 = screen.getRGB(x, y + gap);
                if (c1 == area1RGB && c2 == area2RGB) {
                    found++;
                }
            }
            if (found > lastFound) {
                lastFound = found;
                edgePosition = y;
            }
            
        }
        
        return edgePosition;
    }

    public static int findVerticalEdgeGap(BufferedImage screen,
            Color area1Color, Color area2Color, int y1, int y2, int gap) {
        final int width = screen.getWidth();
        final int area1RGB = area1Color.getRGB();
        final int area2RGB = area2Color.getRGB();
        int edgePosition = Integer.MIN_VALUE;
        int lastFound = Integer.MIN_VALUE;

        for (int x = 1; x < width - 1 - gap; x++) {
            int found = 0;
            for (int y = y1; y < y2; y++) {
                int c1 = screen.getRGB(x - 1, y);
                int c2 = screen.getRGB(x + gap, y);
                if (c1 == area1RGB && c2 == area2RGB) {
                    found++;
                }
            }
            if (found > lastFound) {
                lastFound = found;
                edgePosition = x;
            }
        }
        
        return edgePosition;
    }

    /**
     * method findColoredAreaGap finds a rectangle of given color surrounded by
     * area of the second color with a possible gap at the border
     * 
     * @param screen
     * @param searchForColor
     * @param surroundWithColor
     * @param y1
     * @param y2
     * @param gap
     * @return
     */
    public static Rectangle findColoredAreaGap(BufferedImage screen, Color searchForColor, Color surroundWithColor, int y1, int y2,    int gap) {
        
        int ymin = findHorizontalEdgeGap(screen, surroundWithColor, searchForColor, y1, y2, gap);
        int ymax = findHorizontalEdgeGap(screen, searchForColor, surroundWithColor, y1, y2, gap);
        int xmin = findVerticalEdgeGap(screen, surroundWithColor, searchForColor, ymin, ymax, gap);
        int xmax = findVerticalEdgeGap(screen, searchForColor, surroundWithColor, ymin, ymax, gap);

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }
    
    public static boolean isRectangleValid(Rectangle r){
        
        if (r == null) return false;
        
        return (r.width != 0)&&(r.height != 0)&&(r.x != Integer.MIN_VALUE)&&(r.y != Integer.MIN_VALUE);
    }

    public static BufferedImage getMaskImage(BufferedImage icon) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        boolean[][] b = getMask(icon);
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (b[x][y]) {
                    mask.setRGB(x, y, Color.white.getRGB());
                } else {
                    mask.setRGB(x, y, Color.black.getRGB());
                }
            }
        }
        return mask;
    }

    public static boolean[][] getMask(BufferedImage icon) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        boolean[][] r = new boolean[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = icon.getRGB(x, y);
                int alpha = (i >> 24) & 0xff;
                if (alpha == 255) {
                    r[x][y] = true;
                } else {
                    r[x][y] = false;
                }
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
