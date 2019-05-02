/*
 Copyright (C) 2015 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.icon;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

class IcoHeaderEntry {

    private int width;
    private int height;
    private int colorCount;
    private final int sizeInBytes; // InfoHeader + ANDbitmap + XORbitmap
    private final int fileOffset; //FilePos, where InfoHeader starts

    IcoHeaderEntry(final ImageInputStream src) throws IOException, IcoException {
        width = src.read();
        height = src.read();
        colorCount = src.read();
        // sentence "Number of Colors (2,16, 0=256) " is form doubnet
        //unluckily, both  0==0 and 0==256 does exists
        //going with doubnet by default
        if (colorCount == 0) {
            colorCount = 256;
        }
        int reserved = src.read();
        int planes = src.readUnsignedShort(); //should be 1 but I met quite a lot of  0

        if (reserved != 0 || !(planes == 1 || planes == 0)) {
            throw new IcoException("Invalid header. Expected 0 and 1(0?), got " + reserved + " and " + planes);
        }

        src.readUnsignedShort(); //bitCount
        sizeInBytes = src.readInt();
        fileOffset = src.readInt();
    }

    /**
     * @return the colorCount
     */
    int getColorCount() {
        return colorCount;
    }

    /**
     */
    void resetColorCount() {
        this.colorCount = 0;
    }

    /**
     * @return the width
     */
    int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    void setWidth(final int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    void setHeight(final int height) {
        this.height = height;
    }

    int getSizeInBytes() {
        return sizeInBytes;
    }

    int getFileOffset() {
        return fileOffset;
    }

}
