/* InfoItemTest.java
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
package net.sourceforge.jnlp.splashscreen.parts;

import org.junit.Assert;
import org.junit.Test;

/**
 *The optional kind="splash" attribute may be used in an icon element to indicate that the image is to be used as a "splash" screen during the launch of an application. If the JNLP file does not contain an icon element with kind="splash" attribute, Java Web Start will construct a splash screen using other items from the information Element.
 *If the JNLP file does not contain any icon images, the splash image will consist of the application's title and vendor, as taken from the JNLP file.
 *
 * items not used inside
 */
public class InfoItemTest {

    @Test
    public void testGettersSetters() {
        InfoItem ii = new InfoItem("a", "b");
        Assert.assertEquals("a", ii.getType());
        Assert.assertEquals("b", ii.getValue());
        ii.setType("c");
        Assert.assertEquals("c", ii.getType());
        ii.setValue("d");
        Assert.assertEquals("d", ii.getValue());

    }

    @Test
    public void TestIsOfSameType() {
        InfoItem i1 = new InfoItem("a", "b");
        InfoItem i2 = new InfoItem("a", "c");
        InfoItem i3 = new InfoItem("b", "a");
        Assert.assertTrue(i1.isofSameType(i2));
        Assert.assertFalse(i1.isofSameType(i3));
        Assert.assertFalse(i2.isofSameType(i3));

        DescriptionInfoItem d1 = new DescriptionInfoItem("a", InfoItem.descriptionKindToolTip);
        InfoItem id1 = new InfoItem(InfoItem.description, "a");
        Assert.assertTrue(id1.isofSameType(d1));

    }

    @Test
    public void testEquals() {
        InfoItem i1 = new InfoItem("a", "b");
        InfoItem i11 = new InfoItem("a", "b");
        InfoItem i2 = new InfoItem("a", "c");
        InfoItem i3 = new InfoItem("b", "a");
        Assert.assertFalse(i1.equals(i2));
        Assert.assertFalse(i1.equals(i3));
        Assert.assertFalse(i2.equals(i3));
        Assert.assertTrue(i1.equals(i11));
        DescriptionInfoItem d1 = new DescriptionInfoItem("a", InfoItem.descriptionKindToolTip);
        InfoItem id1 = new InfoItem(InfoItem.description, "a");
        Assert.assertTrue(id1.equals(d1));


    }

    @Test
    public void toStringTest() {
        InfoItem i1 = new InfoItem("aa", "bb");
        Assert.assertTrue(i1.toString().contains("aa"));
        Assert.assertTrue(i1.toString().contains("bb"));
        Assert.assertTrue(i1.toString().length() > 4);

    }

    @Test
    public void toNiceString() {
        InfoItem i1 = new InfoItem("aaa", "bbb");
        Assert.assertTrue(i1.toNiceString().contains(InfoItem.SPLASH + "aaa") || !i1.toNiceString().contains(InfoItem.SPLASH));
        Assert.assertTrue(i1.toNiceString().contains("bbb"));
        Assert.assertFalse(i1.toNiceString().equals(i1.toString()));
    }

}
