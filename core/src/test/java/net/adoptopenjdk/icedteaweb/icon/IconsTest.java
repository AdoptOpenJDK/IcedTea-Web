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

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class IconsTest {

    private Icons loadIco(String id) throws IOException, IcoException {
        try (InputStream is = this.getClass().getResourceAsStream("resources/" + id)) {
            Assert.assertNotNull(is);
            Icons i = new Icons(ImageIO.createImageInputStream(is));
            Assert.assertNotNull(i);
            return i;
        }
    }

    private void checkColors(Color middle, Color corners, BufferedImage x) {
        checkMiddle(middle, x);
        checkUpLeft(corners, x);
        checkBottomRight(corners, x);
        checkBottomLeft(corners, x);
        checkTopRight(corners, x);
    }

    private void checkTopRight(Color corners, BufferedImage x) {
        Assert.assertEquals(corners, new Color(x.getRGB(x.getWidth() - 1, 0)));
    }

    private void checkBottomLeft(Color corners, BufferedImage x) {
        Assert.assertEquals(corners, new Color(x.getRGB(0, x.getHeight() - 1)));
    }

    private void checkBottomRight(Color corners, BufferedImage x) {
        Assert.assertEquals(corners, new Color(x.getRGB(x.getWidth() - 1, x.getHeight() - 1)));
    }

    private void checkUpLeft(Color corners, BufferedImage x) {
        Assert.assertEquals(corners, new Color(x.getRGB(0, 0)));
    }

    private void checkMiddle(Color middle, BufferedImage x) {
        Assert.assertEquals(middle, new Color(x.getRGB(x.getWidth() / 2, x.getHeight() / 2)));
    }

    @Test
    public void twoPlanesDifferentSizesDifferentTransparencies() throws IOException, IcoException {
        Icons i = loadIco("favicon1.ico");
        Assert.assertEquals(2, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        Color white = new Color(252, 253, 252);
        checkColors(new Color(0, 128, 0), white, i.getImage(0));
        checkColors(new Color(0, 131, 0), Color.white, i.getImage(1));

    }

    @Test
    public void strangeStrips() throws IOException, IcoException {
        Icons i = loadIco("favicon2.ico");
        Assert.assertEquals(2, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        BufferedImage x = i.getImage(0);
        checkMiddle(new Color(67, 75, 130), x);
        checkUpLeft(Color.black, x);
        checkBottomRight(new Color(208, 66, 66), x);
        checkBottomLeft(Color.black, x);
        checkTopRight(Color.black, x);

        x = i.getImage(1);
        checkMiddle(new Color(64, 79, 142), x);
        checkUpLeft(Color.black, x);
        checkBottomRight(new Color(254, 212, 212), x);
        checkBottomLeft(Color.black, x);
        checkTopRight(Color.black, x);
    }

    @Test
    public void strangePng() throws IOException, IcoException {
        Icons i = loadIco("favicon3.ico");
        Assert.assertEquals(1, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        checkColors(new Color(151, 246, 24), Color.white, i.getImage(0));
    }

    @Test
    public void bmpsMixedWithPngAndTransparencyAndLAyersFromBiggestToSmallest() throws IOException, IcoException {
        Icons i = loadIco("favicon4.ico");
        Assert.assertEquals(4, i.getImages().size());
        int index = 0;
        //1 256
        //2 128
        //3 64
        //4 32
        for (BufferedImage x : i.getImages()) {
            index++;
            Assert.assertEquals((int) Math.pow(2, 9 - index), x.getWidth());
            Assert.assertEquals((int) Math.pow(2, 9 - index), x.getHeight());
        }
        checkColors(new Color(0, 0, 0, 255), new Color(0, 0, 0, 255), i.getImage(0));

        checkMiddle(new Color(0, 0, 0, 255), i.getImage(1));
        checkUpLeft(new Color(255, 255, 255, 255), i.getImage(1));
        checkBottomRight(new Color(0, 0, 0, 255), i.getImage(1));
        checkBottomLeft(new Color(0, 0, 0, 255), i.getImage(1));
        checkTopRight(new Color(254, 63, 0, 255), i.getImage(1));

        checkColors(new Color(0, 0, 0, 255), new Color(0, 0, 0, 255), i.getImage(2));
        checkColors(new Color(0, 0, 0, 255), new Color(0, 0, 0, 255), i.getImage(3));

        Assert.assertEquals(new Color(5, 100, 168), new Color(i.getImage(3).getRGB(21, 21)));
        Assert.assertEquals(new Color(5, 100, 168), new Color(i.getImage(2).getRGB(42, 42)));
        Assert.assertEquals(new Color(5, 100, 168), new Color(i.getImage(1).getRGB(94, 94)));
        Assert.assertEquals(new Color(5, 100, 168), new Color(i.getImage(0).getRGB(188, 188)));

    }

    @Test
    public void notHonoredThat256is0() throws IOException, IcoException {
        Icons i = loadIco("favicon5.ico");
        Assert.assertEquals(1, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        checkColors(new Color(151, 246, 24), Color.white, i.getImage(0));

    }

    @Test
    public void corruptedHeader() throws IOException, IcoException {
        Icons i = loadIco("favicon5.ico");
        Assert.assertEquals(1, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        checkColors(new Color(151, 246, 24), Color.white, i.getImage(0));

    }

    @Test
    public void corruptedHeader2() throws IOException, IcoException {
        Icons i = loadIco("favicon6.ico");
        Assert.assertEquals(1, i.getImages().size());
        int ii = 0;
        for (BufferedImage x : i.getImages()) {
            ii++;
            Assert.assertEquals(ii * 16, x.getWidth());
            Assert.assertEquals(ii * 16, x.getHeight());
        }
        checkColors(new Color(151, 246, 24), Color.white, i.getImage(0));

    }

    @Test
    public void allPalettesFormats() throws IOException, IcoException {
        String[] bitsPalettes = new String[]{"1", "4", "8", "24", "32"};
        String[] compressions = new String[]{"bmp", "png"};
        String[] trans = new String[]{"noTrans", "trans"};
        for (String palette : bitsPalettes) {
            for (String comp : compressions) {
                for (String tran : trans) {
                    String name = "ico" + palette + "-" + comp + "-" + tran + ".ico";
                    Icons i = loadIco(name);
                    Assert.assertEquals(1, i.getImages().size());
                    Assert.assertEquals(16, i.getImage(0).getWidth());
                    Assert.assertEquals(16, i.getImage(0).getHeight());
                    if (tran.equals(trans[0])) {
                        checkColors(Color.black, Color.white, i.getImage(0));
                    } else if (tran.equals(trans[1])) {
                        if (comp.equals(compressions[0])) {
                            if (palette.equals("24") || palette.equals("32")) {
                                checkColors(Color.black, new Color(255, 255, 255, 255), i.getImage(0));
                            } else {
                                checkColors(Color.black, new Color(0, 0, 0, 255), i.getImage(0));
                            }
                        } else if (comp.equals(compressions[1])) {
                            if (palette.equals("24") || palette.equals("32")) {
                                checkColors(Color.black, new Color(255, 255, 255, 255), i.getImage(0));
                            } else {
                                checkColors(Color.black, new Color(0, 0, 0, 255), i.getImage(0));
                            }
                        } else {
                            throw new RuntimeException("Invalid compression: " + comp);
                        }
                    } else {
                        throw new RuntimeException("Invalid transparency: " + tran);
                    }
                }
            }
        }

    }

}
