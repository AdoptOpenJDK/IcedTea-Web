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

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Icons {

    private final IcoHeader header;
    private final int countOfIcons;
    private final List<BufferedImage> images; //size 16*countOfIcons bytes

    Icons(final ImageInputStream src) throws IOException, IcoException {
        this.header = new IcoHeader(src);
        this.countOfIcons = header.getCountOfIcons();

        final List<BufferedImage> imgs = new ArrayList<>(countOfIcons);
        for (final IcoHeaderEntry e : header.getEntries()) {
            final BufferedImage image = readImage(e, src);
            imgs.add(image);
        }

        this.images = Collections.unmodifiableList(imgs);
    }

    BufferedImage getImage(final int imageIndex) {
        checkIndex(imageIndex);
        return images.get(imageIndex);
    }

    List<BufferedImage> getImages() {
        return images;
    }

    int getNumImages() {
        return countOfIcons;
    }

    private void checkIndex(final int imageIndex) {
        if (imageIndex < 0 || imageIndex >= countOfIcons) {
            throw new IndexOutOfBoundsException("bad index " + imageIndex + ". Should be >=0 and < " + countOfIcons);
        }
    }

    int getWidth(final int imageIndex) {
        checkIndex(imageIndex);
        return header.getEntries().get(imageIndex).getWidth();
    }

    int getHeight(final int imageIndex) {
        checkIndex(imageIndex);
        return header.getEntries().get(imageIndex).getHeight();
    }

    private BufferedImage readImage(final IcoHeaderEntry e, final ImageInputStream src1) throws IOException {
        BufferedImage image;
        final byte[] img = new byte[e.getSizeInBytes()];
        src1.readFully(img);
        try {
            image = parse(img, e);
            //readMask(e, src1);
        } catch (final EOFException ex) {
            //some icons do not honour that 0 is 256. Retrying
            if (e.getColorCount() != 0) {
                e.resetColorCount();
                image = parse(img, e);
                //readMask(e, src1);
            } else {
                throw ex;
            }
        }
        return image;
    }

    private BufferedImage parse(final byte[] img, final IcoHeaderEntry e) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(img);
        BufferedImage image = null;
        try {
            image = ImageIO.read(bis);
        } catch (final Exception ex) {
            //not png
        }
        if (image != null) {
            fixSizesInHeader(e, image);
            return image;
        }
        //bmp
        final byte[] imgPrefixed = prefixByFakeHeader(img, e);
        bis = new ByteArrayInputStream(imgPrefixed);
        //don't try catch this one. you will break it
        image = ImageIO.read(bis);

        return image;
    }

    private void fixSizesInHeader(final IcoHeaderEntry e, final BufferedImage image) {
        //may happen for png
        if (e.getWidth() == 0) {
            e.setWidth(image.getWidth());
        }
        if (e.getHeight() == 0) {
            e.setHeight(image.getHeight());
        }
    }

    private byte[] prefixByFakeHeader(final byte[] origArray, final IcoHeaderEntry e) {
        final int fakingArray = 14;
        final byte[] img = new byte[fakingArray + e.getSizeInBytes()];
        System.arraycopy(origArray, 0, img, 14, origArray.length);
        //fake header
        //http://www.daubnet.com/en/file-format-bmp
        final int size = e.getSizeInBytes() + fakingArray;
        final int offset = fakingArray + 40 + 4 * e.getColorCount();
        final int tmpHeight = e.getHeight();

        img[0] = 'B';
        img[1] = 'M';
        img[2] = (byte) (size & 0xFF);
        img[3] = (byte) ((size >> 8) & 0xFF);
        img[4] = (byte) ((size >> 16) & 0xFF);
        img[5] = (byte) ((size >> 24) & 0xFF);
        img[6] = 0;
        img[7] = 0;
        img[8] = 0;
        img[9] = 0;
        img[10] = (byte) (offset & 0xFF);
        img[11] = (byte) ((offset >> 8) & 0xFF);
        img[12] = (byte) ((offset >> 16) & 0xFF);
        img[13] = (byte) ((offset >> 24) & 0xFF);
        //ico is storing height as height of XOR + height of AND bitmaps
        //that is 2 x height. Bitmap expects only height of single image
        img[fakingArray + 4/*size*/ + 4/*width*/] = (byte) (tmpHeight & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 1] = (byte) ((tmpHeight >> 8) & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 2] = (byte) ((tmpHeight >> 16) & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 3] = (byte) ((tmpHeight >> 24) & 0xFF);
        return img;
    }

}
