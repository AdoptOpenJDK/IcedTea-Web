/* 
 Copyright (C) 2014 Red Hat, Inc.

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
package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import org.junit.Assert;
import org.junit.Test;

public class FormatterTest {

    @Test
    public void testNoNewLinesPlain() {
        PlainTextFormatter f = new PlainTextFormatter();
        String s = f.getNewLine(0);
        Assert.assertEquals("", s);
    }

    @Test
    public void testOneNewLinesPlain() {
        PlainTextFormatter f = new PlainTextFormatter();
        String s = f.getNewLine(1);
        Assert.assertEquals(f.getNewLine(), s);
    }

    @Test
    public void testXNewLinesPlain() {
        PlainTextFormatter f = new PlainTextFormatter();
        final int x = 10;
        String s = f.getNewLine(x);
        String[] a = s.replace(f.getNewLine(), "X" + f.getNewLine()).split("[" + f.getNewLine() + "]{1}");
        Assert.assertEquals(x, a.length);
    }

    @Test
    public void testNoNewLinesHtml() {
        HtmlFormatter f = new HtmlFormatter();
        String s = f.getNewLine(0);
        Assert.assertEquals("", s);
    }

    @Test
    public void testOneNewLinesHtml() {
        HtmlFormatter f = new HtmlFormatter();
        String s = f.getNewLine(1);
        Assert.assertEquals(f.getNewLine(), s);
    }

    @Test
    public void testXNewLinesHtml() {
        HtmlFormatter f = new HtmlFormatter();
        final int x = 10;
        String s = f.getNewLine(x);
        String[] a = s.split("(?mi)br");
        Assert.assertEquals(x + 1, a.length);//br is in middleof element
    }

    @Test
    public void testNoNewLinesMan() {
        ManFormatter f = new ManFormatter();
        String s = f.getNewLine(0);
        Assert.assertEquals("", s);
    }

    @Test
    public void testOneNewLinesMan() {
        ManFormatter f = new ManFormatter();
        String s = f.getNewLine(1);
        Assert.assertEquals(f.getNewLine(), s);
    }

    @Test
    public void testXNewLinesMan() {
        ManFormatter f = new ManFormatter();
        final int x = 10;
        String s = f.getNewLine(x);
        String[] a = s.split("(?m)\\.br");
        Assert.assertEquals(x + 1, a.length);//br is in middleof element
    }

}
