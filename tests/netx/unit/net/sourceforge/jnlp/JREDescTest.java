/* 
 Copyright (C) 2014 Red Hat, Inc.

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
package net.sourceforge.jnlp;

import org.junit.Assert;
import org.junit.Test;

public class JREDescTest {

    @Test
    public void testNulls() throws ParseException {
        JREDesc a = new JREDesc(null, null, null, null, null, null);
    }

    @Test
    public void testInitialHeapSize() throws ParseException {
        JREDesc a = new JREDesc(null, null, null, "1", null, null);
        a = new JREDesc(null, null, null, "99999999", null, null);
        a = new JREDesc(null, null, null, "1k", null, null);
        a = new JREDesc(null, null, null, "1000k", null, null);
        a = new JREDesc(null, null, null, "1K", null, null);
        a = new JREDesc(null, null, null, "1000K", null, null);
        a = new JREDesc(null, null, null, "1m", null, null);
        a = new JREDesc(null, null, null, "1m", null, null);
        a = new JREDesc(null, null, null, "1M", null, null);
        a = new JREDesc(null, null, null, "1g", null, null);
        a = new JREDesc(null, null, null, "1G", null, null);
        a = new JREDesc(null, null, null, "10000G", null, null);
    }

    @Test
    public void testMaximumHeapSize() throws ParseException {
        JREDesc a = new JREDesc(null, null, null, null, "1", null);
        a = new JREDesc(null, null, null, null, "99999999", null);
        a = new JREDesc(null, null, null, null, "1k", null);
        a = new JREDesc(null, null, null, null, "1000k", null);
        a = new JREDesc(null, null, null, null, "1K", null);
        a = new JREDesc(null, null, null, null, "1000K", null);
        a = new JREDesc(null, null, null, null, "1m", null);
        a = new JREDesc(null, null, null, null, "1m", null);
        a = new JREDesc(null, null, null, null, "1M", null);
        a = new JREDesc(null, null, null, null, "1g", null);
        a = new JREDesc(null, null, null, null, "1G", null);
        a = new JREDesc(null, null, null, null, "10000G", null);
    }

    @Test(expected = ParseException.class)
    public void testInitialHeapSizeBad() throws ParseException {
        JREDesc a = new JREDesc(null, null, null, "blah", null, null);

    }

    @Test(expected = ParseException.class)
    public void testMaximumHeapSizeBad() throws ParseException {
        JREDesc a = new JREDesc(null, null, null, null, "blah", null);

    }

    @Test
    public void checkHeapSize() throws ParseException {
        String s = JREDesc.checkHeapSize(null);
        Assert.assertEquals(null, s);
        s = JREDesc.checkHeapSize("0");
        Assert.assertEquals("0", s);
        s = JREDesc.checkHeapSize(" 0k");
        Assert.assertEquals("0k", s);
        s = JREDesc.checkHeapSize("1 ");
        Assert.assertEquals("1", s);
        s = JREDesc.checkHeapSize("10");
        Assert.assertEquals("10", s);
        s = JREDesc.checkHeapSize(" 1k");
        Assert.assertEquals("1k", s);
        s = JREDesc.checkHeapSize("10m ");
        Assert.assertEquals("10m", s);

        s = JREDesc.checkHeapSize("0");
        Assert.assertEquals("0", s);
        s = JREDesc.checkHeapSize(" 0K");
        Assert.assertEquals("0K", s);
        s = JREDesc.checkHeapSize("1 ");
        Assert.assertEquals("1", s);
        s = JREDesc.checkHeapSize("10");
        Assert.assertEquals("10", s);
        s = JREDesc.checkHeapSize(" 1M");
        Assert.assertEquals("1M", s);
        s = JREDesc.checkHeapSize("10G ");
        Assert.assertEquals("10G", s);
        s = JREDesc.checkHeapSize("99K");
        Assert.assertEquals("99K", s);

    }

    @Test(expected = ParseException.class)
    public void checkHeapSizeBad1() throws ParseException {
        JREDesc.checkHeapSize("10k10m");
    }

    @Test(expected = ParseException.class)
    public void checkHeapSizeBad2() throws ParseException {
        JREDesc.checkHeapSize("one gigabyte");
    }

    @Test(expected = ParseException.class)
    public void checkHeapSizeBad3() throws ParseException {
        JREDesc.checkHeapSize("99l");
    }

    @Test(expected = ParseException.class)
    public void checkHeapSizeBad4() throws ParseException {
        JREDesc.checkHeapSize("99KK");
    }
}
