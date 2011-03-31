/* ParserCornerCases.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Test various corner cases of the parser */
public class ParserCornerCases {
    @Test
    public void testUnsupportedSpecNumber() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='11.11'></jnlp>";
        Node root = Parser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser parser = new Parser(null, null, root, false, false);
        Assert.assertEquals("11.11", parser.getSpecVersion().toString());
    }

    @Test
    public void testApplicationAndComponent() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp><application-desc/><component-desc/></jnlp>";
        Node root = Parser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser parser = new Parser(null, null, root, false, false);
        Assert.assertNotNull(parser.getLauncher(root));
    }

    @Test
    public void testCommentInElements() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='1.0' <!-- comment -->> </jnlp>";
        Node root = Parser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, false, false);
        Assert.assertEquals("1.0", p.getSpecVersion().toString());
    }

    @Test
    public void testCommentInAttributes() throws ParseException {
        String malformedJnlp = "<?xml?><jnlp spec='<!-- something -->'></jnlp>";
        Node root = Parser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, false, false);
        Assert.assertEquals("<!-- something -->", p.getSpecVersion().toString());
    }

    @Test
    public void testNestedComments() throws ParseException {
        String malformedJnlp = "<?xml?>" +
                "<jnlp><information><description>" +
                "<!-- outer <!-- inner --> -->" +
                "</description></information></jnlp>";
        Node root = Parser.getRootNode(new ByteArrayInputStream(malformedJnlp.getBytes()));
        Parser p = new Parser(null, null, root, false, false);
        Assert.assertEquals(" -->", p.getInfo(root).get(0).getDescription());
    }
}
