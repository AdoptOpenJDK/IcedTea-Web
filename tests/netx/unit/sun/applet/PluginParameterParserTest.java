/*
Copyright (C) 2012 Red Hat, Inc.

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

package sun.applet;

import static org.junit.Assert.*;

import java.util.Map;

import net.sourceforge.jnlp.PluginParameters;

import org.junit.Test;

public class PluginParameterParserTest {

    @Test
    public void testIsInt() {
        assertFalse(PluginParameterParser.isInt("1.0"));
        assertFalse(PluginParameterParser.isInt("abc"));
        assertTrue(PluginParameterParser.isInt("1"));

        /* Numbers that overflow or underflow can cause problems if we 
         * consider them valid, and pass them to parseInt: */
        assertFalse(PluginParameterParser.isInt("4294967295"));
    }

    @Test
    public void testUnescapeString() {
        assertEquals("", PluginParameterParser.unescapeString(""));
        assertEquals("\n", PluginParameterParser.unescapeString("\n"));
        assertEquals("\\", PluginParameterParser.unescapeString("\\\\"));
        assertEquals(";", PluginParameterParser.unescapeString("\\:"));

        assertEquals("test\n\\;",
                PluginParameterParser.unescapeString("test" + "\\n" + "\\\\" + "\\:"));

        assertEquals("start\n;end\\;",
                PluginParameterParser.unescapeString("start\\n\\:end\\\\;"));
    }

    @Test
    public void testParseEscapedKeyValuePairs() {
        Map<String, String> params;

        params = PluginParameterParser.parseEscapedKeyValuePairs("key1;value1;KEY2\\:;value2\\\\;");
        assertEquals(params.size(), 2);
        assertEquals(params.get("key1"), "value1");
        assertEquals(params.get("key2;"), "value2\\"); // ensure key is lowercased

        params = PluginParameterParser.parseEscapedKeyValuePairs("");
        assertEquals(params.size(), 0);

        params = PluginParameterParser.parseEscapedKeyValuePairs("key;;");
        assertEquals(params.size(), 1);
        assertEquals(params.get("key"), "");

        params = PluginParameterParser.parseEscapedKeyValuePairs(";value;");
        assertEquals(params.size(), 1);
        assertEquals(params.get(""), "value");
    }

    @Test
    public void testAttributeParseWidthHeightAttributes() {
        final String width = "1", height = "1";
        final String codeKeyVal = "code;codeValue;";

        PluginParameterParser parser = new PluginParameterParser();
        PluginParameters params;

        params = parser.parse(width, height, codeKeyVal);
        assertEquals("1", params.get("width"));
        assertEquals("1", params.get("height"));

        //Test that width height are defaulted to in case of not-a-number attributes:
        params = parser.parse(width, height, codeKeyVal + " width;NAN;height;NAN;");
        assertEquals("1", params.get("width"));
        assertEquals("1", params.get("height"));
    }

}
