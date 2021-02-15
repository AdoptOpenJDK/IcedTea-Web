/* 
 Copyright (C) 2014 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */
package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class JREDescTest {

    @Test
    public void testNulls() throws ParseException {
        new JREDesc(null, null, false, null, null, null, null, null);
    }

    @Test
    public void testInitialHeapSize() throws ParseException {
        new JREDesc(null, null, false, null, null, "1", null, null);
        new JREDesc(null, null, false, null, null, "99999999", null, null);
        new JREDesc(null, null, false, null, null, "1k", null, null);
        new JREDesc(null, null, false, null, null, "1000k", null, null);
        new JREDesc(null, null, false, null, null, "1K", null, null);
        new JREDesc(null, null, false, null, null, "1000K", null, null);
        new JREDesc(null, null, false, null, null, "1m", null, null);
        new JREDesc(null, null, false, null, null, "1m", null, null);
        new JREDesc(null, null, false, null, null, "1M", null, null);
        new JREDesc(null, null, false, null, null, "1g", null, null);
        new JREDesc(null, null, false, null, null, "1G", null, null);
        new JREDesc(null, null, false, null, null, "10000G", null, null);
    }

    @Test
    public void testMaximumHeapSize() throws ParseException {
        new JREDesc(null, null, false, null, null, null, "1", null);
        new JREDesc(null, null, false, null, null, null, "99999999", null);
        new JREDesc(null, null, false, null, null, null, "1k", null);
        new JREDesc(null, null, false, null, null, null, "1000k", null);
        new JREDesc(null, null, false, null, null, null, "1K", null);
        new JREDesc(null, null, false, null, null, null, "1000K", null);
        new JREDesc(null, null, false, null, null, null, "1m", null);
        new JREDesc(null, null, false, null, null, null, "1m", null);
        new JREDesc(null, null, false, null, null, null, "1M", null);
        new JREDesc(null, null, false, null, null, null, "1g", null);
        new JREDesc(null, null, false, null, null, null, "1G", null);
        new JREDesc(null, null, false, null, null, null, "10000G", null);
    }

    @Test(expected = ParseException.class)
    public void testInitialHeapSizeBad() throws ParseException {
        new JREDesc(null, null, false, null, null, "blah", null, null);

    }

    @Test(expected = ParseException.class)
    public void testMaximumHeapSizeBad() throws ParseException {
        new JREDesc(null, null, false, null, null, null, "blah", null);

    }

    @Test
    public void checkHeapSize() throws ParseException {
        Assert.assertNull(JREDesc.checkHeapSize(null));
        Assert.assertEquals("0", JREDesc.checkHeapSize("0"));
        Assert.assertEquals("0k", JREDesc.checkHeapSize(" 0k"));
        Assert.assertEquals("1", JREDesc.checkHeapSize("1 "));
        Assert.assertEquals("10", JREDesc.checkHeapSize("10"));
        Assert.assertEquals("1k", JREDesc.checkHeapSize(" 1k"));
        Assert.assertEquals("10m", JREDesc.checkHeapSize("10m "));

        Assert.assertEquals("0", JREDesc.checkHeapSize("0"));
        Assert.assertEquals("0K", JREDesc.checkHeapSize(" 0K"));
        Assert.assertEquals("1", JREDesc.checkHeapSize("1 "));
        Assert.assertEquals("10", JREDesc.checkHeapSize("10"));
        Assert.assertEquals("1M", JREDesc.checkHeapSize(" 1M"));
        Assert.assertEquals("10G", JREDesc.checkHeapSize("10G "));
        Assert.assertEquals("99K", JREDesc.checkHeapSize("99K"));
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

    @Test
    public void parsingNoArguments() throws ParseException {
        final JREDesc desc = new JREDesc(null, null, false, null, null, null, null, null);
        final List<String> result = desc.getAllVmArgs();

        Assert.assertEquals(emptyList(), result);
    }

    @Test
    public void parsingOnlyHeapArguments() throws ParseException {
        final JREDesc desc = new JREDesc(null, null, false, null, null, "1k", "1m", null);
        final List<String> result = desc.getAllVmArgs();

        Assert.assertEquals(Arrays.asList("-Xms1k", "-Xmx1m"), result);
    }

    @Test
    public void parsingUnquotedArguments() throws ParseException {
        final JREDesc desc = new JREDesc(null, null, false, null, "  some arguments \t\n without   quotes  ", null, null, null);
        final List<String> result = desc.getAllVmArgs();

        Assert.assertEquals(Arrays.asList("some", "arguments", "without", "quotes"), result);
    }

    @Test
    public void parsingQuotedArguments() throws ParseException {
        final JREDesc desc = new JREDesc(null, null, false, null, "  some arguments \t\n \"with   Quotes\"  ", null, null, null);
        final List<String> result = desc.getAllVmArgs();

        Assert.assertEquals(Arrays.asList("some", "arguments", "with   Quotes"), result);
    }

    @Test(expected = ParseException.class)
    public void parsingWrongQuotedArguments() throws ParseException {
        new JREDesc(null, null, false, null, "  some arguments \"with   Quotes\"AndMissingSpace after quote  ", null, null, null);
    }
}
