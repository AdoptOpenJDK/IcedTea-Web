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

public class ReplacingTextFormatterTest {

    Formatter tr = new ReplacingTextFormatter() {

            @Override
            public String wrapParagraph(String s) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getHeaders(String id, String encoding) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getNewLine() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getBoldOpening() {
                return "OPEN";
            }

            @Override
            public String getBoldClosing() {
                return "CLOSE";
            }

            @Override
            public String getBreakAndBold() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getCloseBoldAndBreak() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getBoldCloseNwlineBoldOpen() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getTitle(String name) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getUrl(String url, String appearence) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getOption(String key, String value) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getSeeAlso(String s) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getTail() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public String getFileSuffix() {
                throw new UnsupportedOperationException("Not supported yet."); 
            }
        };
     

    @Test
    public void upperCaseNoSpaces() {
        String s = tr.process("aaa <B> bbb </B> ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }
    @Test
    public void lowercaseNoSpaces() {
        String s = tr.process("aaa <b> bbb </b> ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }
    
    
    @Test
    public void lowercaseSpaces() {
        String s = tr.process("aaa <  b  > bbb <  /     b > ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }
    
     @Test
    public void uppercaseSpaces() {
        String s = tr.process("aaa <   B  > bbb <  /     B > ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }

    
    @Test
    public void mixedCases1() {
        String s = tr.process("aaa <B> bbb </b> ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }
    @Test
    public void mixedSpace2() {
        String s = tr.process("aaa <b> bbb </B> ccc");
        Assert.assertEquals("aaa OPEN bbb CLOSE ccc", s);
    }

  @Test
    public void illegal1() {
        String s = tr.process("aaa <b style=\"blah\"> bbb </B> ccc");
        Assert.assertFalse(s.contains("OPEN"));
        Assert.assertTrue(s.contains("CLOSE"));
    }   
    
    @Test
    public void illegal2() {
        String s = tr.process("</B abc> ccc <b>");
        Assert.assertFalse(s.contains("CLOSE"));
        Assert.assertTrue(s.contains("OPEN"));
    }   

}