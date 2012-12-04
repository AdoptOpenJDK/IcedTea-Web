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
