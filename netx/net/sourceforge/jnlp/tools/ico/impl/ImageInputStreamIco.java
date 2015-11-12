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
package net.sourceforge.jnlp.tools.ico.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamIco {

    private final IcoHeader header;
    private final List<BufferedImage> images; //size 16*countOfIcons bytes

    public IcoHeader getHeader() {
        return header;
    }

    public BufferedImage getImage(int i) {
        return images.get(i);
    }

    public ImageInputStreamIco(ImageInputStream src) throws IOException, IcoException {
        this.header = new IcoHeader(src);
        images = new ArrayList<>(header.countOfIcons);
        for (IcoHeaderEntry e : header.entries) {
            BufferedImage image = readImage(e, src);
            images.add(image);
        }
    }

    private static void readMask(IcoHeaderEntry e, ImageInputStream src1) throws IOException {
        //acording to spec, behind img bytes, should be another bytes, with AND bitmap.Hoewer, I had not found them.
        //however, that means, that transaprency is lost... But bit offsets mathces.. so...
        //IcoHeader.IcoHeaderEntry q = e.provideMonochromeHeader();
        //src1.getStreamPosition();
        //byte[] mask = new byte[q.sizeInBytes];
        //src1.readFully(mask);
    }

    private static BufferedImage readImage(IcoHeaderEntry e, ImageInputStream src1) throws IOException {
        BufferedImage image;
        byte[] img = new byte[e.getSizeInBytes()];
        if (src1.getStreamPosition() != e.getFileOffset()) {
            //I had never seen this thrown, Still, is it worthy to tempt it, or rather read and die later?
            //throw new IOException("Stream position do nto match expected position. Bmp(or png) will read wrongly");
        }
        src1.readFully(img);
        try {
            image = parse(img, e);
            //readMask(e, src1);
        } catch (EOFException ex) {
            //some icons do not honour that 0 is 256. Retrying
            if (e.getColorCount() != 0) {
                e.setColorCount(0);
                image = parse(img, e);
                //readMask(e, src1);
            } else {
                throw ex;
            }
        }
        return image;
    }

    private static BufferedImage parse(byte[] img, IcoHeaderEntry e) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(img);
        BufferedImage image = null;
        try {
            image = ImageIO.read(bis);
        } catch (Exception ex) {
            //not png
        }
        if (image != null) {
            fixSizesInHeader(e, image);
            return image;
        }
        //bmp
        img = prefixByFakeHeader(img, e);
        bis = new ByteArrayInputStream(img);
        //dont try catch this one. you will break it
        image = ImageIO.read(bis);

        return image;
    }

    private static void fixSizesInHeader(IcoHeaderEntry e, BufferedImage image) {
        //may happen for png
        if (e.getWidth() == 0) {
            e.setWidth(image.getWidth());
        }
        if (e.getHeight() == 0) {
            e.setHeight(image.getHeight());
        }
    }

    private static byte[] prefixByFakeHeader(final byte[] origArray, IcoHeaderEntry e) {
        int fakingArray = 14;
        byte[] img = new byte[fakingArray + e.getSizeInBytes()];
        for (int i = 0; i < origArray.length; i++) {
            byte p = origArray[i];
            img[i + 14] = p;

        }
        //fake header
        //http://www.daubnet.com/en/file-format-bmp
        int size = e.getSizeInBytes() + fakingArray;
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
        int ofset = fakingArray + 40 + 4 * e.getColorCount();
        img[10] = (byte) (ofset & 0xFF);
        img[11] = (byte) ((ofset >> 8) & 0xFF);
        img[12] = (byte) ((ofset >> 16) & 0xFF);
        img[13] = (byte) ((ofset >> 24) & 0xFF);
        //ico is storing height as height of XOR + height of AND bitmaps
        //that is 2 x hight. Bitmap expects only height of single image
        int tmpHeight = e.getHeight();
        img[fakingArray + 4/*size*/ + 4/*width*/] = (byte) (tmpHeight & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 1] = (byte) ((tmpHeight >> 8) & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 2] = (byte) ((tmpHeight >> 16) & 0xFF);
        img[fakingArray + 4/*size*/ + 4/*width*/ + 3] = (byte) ((tmpHeight >> 24) & 0xFF);
        return img;
    }

}
