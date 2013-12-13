/* DescriptionInfoItemTest.java
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

import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class DescriptionInfoItemTest {

    private static final DescriptionInfoItem[] d = {new DescriptionInfoItem("Firm 1", null),
        new DescriptionInfoItem("Firm 2", null),
        new DescriptionInfoItem("Firm 3", "k1"),
        new DescriptionInfoItem("Firm 4", "k2"),
        new DescriptionInfoItem("Firm 6", "k1")};

    @Test
    public void setGetTest() {
        DescriptionInfoItem di = new DescriptionInfoItem("a", "b");
        Assert.assertEquals("a", di.getValue());
        Assert.assertEquals("b", di.getKind());
        Assert.assertEquals(InfoItem.description, di.getType());
        di.setKind("q");
        Assert.assertEquals("q", di.getKind());

    }

    @Test
    public void isOfSameKindTests() {
        boolean[] results = new boolean[]{true, true, false, false, false, true, true, false, false, false, false, false, true, false, true, false, false, false, true, false, false, false, true, false, true};
        int x = -1;
        for (int i = 0; i < d.length; i++) {
            DescriptionInfoItem d1 = d[i];
            for (int j = 0; j < d.length; j++) {
                x++;
                DescriptionInfoItem d2 = d[j];
                ServerAccess.logOutputReprint(x + ": " + i + "x" + j + " " + d1.toString() + "x" + d2.toString() + "- same kind - " + d1.isOfSameKind(d2));
                Assert.assertEquals(results[x], d1.isOfSameKind(d2));
            }

        }
    }

    @Test
    public void isSameTest() {
        boolean[] results = new boolean[]{true, true, false, false, false, true, true, false, false, false, false, false, true, false, true, false, false, false, true, false, false, false, true, false, true
        };
        int x = -1;
        for (int i = 0; i < d.length; i++) {
            DescriptionInfoItem d1 = d[i];
            for (int j = 0; j < d.length; j++) {
                x++;
                DescriptionInfoItem d2 = d[j];
                ServerAccess.logOutputReprint(x + ": " + i + "x" + j + " " + d1.toString() + "x" + d2.toString() + "- same  - " + d1.isSame(d2));
                Assert.assertEquals(results[x], d1.isSame(d2));
            }

        }

    }

    @Test
    public void equalsTest() {
        boolean[] results = new boolean[]{true, false, false, false, false, false, true, false, false, false, false, false, true, false, false, false, false, false, true, false, false, false, false, false, true
        };
        int x = -1;
        for (int i = 0; i < d.length; i++) {
            DescriptionInfoItem d1 = d[i];
            for (int j = 0; j < d.length; j++) {
                x++;
                DescriptionInfoItem d2 = d[j];
                ServerAccess.logOutputReprint(x + ": " + i + "x" + j + ", " + d1.toString() + " x " + d2.toString() + "- equals  - " + d1.equals(d2));
                Assert.assertEquals(results[x], d1.equals(d2));
            }

        }


    }

    @Test
    public void toStringTest() {
        DescriptionInfoItem d1 = new DescriptionInfoItem("Firm 3", null);
        Assert.assertTrue(d1.toString().contains(d1.getValue()));
        Assert.assertTrue(d1.toString().contains(d1.getType()));
        Assert.assertTrue(d1.toString().contains("null"));
        DescriptionInfoItem dd = new DescriptionInfoItem("Firm 3", "k1");
        Assert.assertTrue(dd.toString().contains(dd.getValue()));
        Assert.assertTrue(dd.toString().contains(dd.getType()));
        Assert.assertTrue(dd.toString().contains(dd.getKind()));

    }

    @Test
    public void toNiceStringTest() {
        DescriptionInfoItem d1 = new DescriptionInfoItem("Firm 3", null);
        Assert.assertTrue(d1.toNiceString().contains(d1.getValue()));
        Assert.assertTrue(d1.toNiceString().contains(InfoItem.SPLASH + d1.getType()) || !d1.toNiceString().contains(InfoItem.SPLASH));
        DescriptionInfoItem dd = new DescriptionInfoItem("Firm 3", "k1");
        Assert.assertTrue(dd.toNiceString().contains(dd.getValue()));
        Assert.assertTrue(dd.toNiceString().contains(InfoItem.SPLASH + dd.getType()) || !d1.toNiceString().contains(InfoItem.SPLASH));

    }
}
