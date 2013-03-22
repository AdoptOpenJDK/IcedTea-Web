package sun.applet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jnlp.PluginParameters;

class PluginParameterParser {
    static private final char DELIMITER_ESCAPE = ':';
    static private final String KEY_VALUE_DELIMITER = ";";

    /**
     * Unescape characters passed from C++.
     * Specifically, "\n" -> new line, "\\" -> "\", "\:" -> ";"
     *
     * @param str The string to unescape
     * @return The unescaped string
     */
    static String unescapeString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char chr = str.charAt(i);
            if (chr != '\\') {
                sb.append(chr);
            } else {
                i++; // Skip ahead one
                chr = str.charAt(i);
                if (chr == 'n') {
                    sb.append('\n');
                } else if (chr == '\\') {
                    sb.append('\\');
                } else if (chr == DELIMITER_ESCAPE) {
                    sb.append(KEY_VALUE_DELIMITER);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parse semi-colon delimited key-value pairs.
     * @param keyvalString the escaped, semicolon-delimited, string
     * @return a map of the keys to the values
     */
    static Map<String, String> parseEscapedKeyValuePairs(String keyvalString) {
        // Split on ';', ensuring empty strings at end are kept
        String[] strs = keyvalString.split(KEY_VALUE_DELIMITER, -1 /* Keep empty strings */);

        Map<String, String> attributes = new HashMap<String, String>();

        /* Note that we will typically have one empty string at end */
        for (int i = 0; i < strs.length - 1; i += 2) {
            String key = unescapeString(strs[i]).toLowerCase();
            String value = unescapeString(strs[i + 1]);
            attributes.put(key, value);
        }

        return attributes;
    }

    static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parsers parameters given a string containing 
     * parameters in quotes.
     * 
     * @param width default applet width
     * @param height default applet height
     * @param parameterString the parameters 
     * @return the attributes in a hash table
     */
    public PluginParameters parse(String width,
            String height, String parameterString) {
        Map<String, String> params = parseEscapedKeyValuePairs(parameterString);

        if (params.get("width") == null || !isInt(params.get("width"))) {
            params.put("width", width);
        }

        if (params.get("height") == null || !isInt(params.get("height"))) {
            params.put("height", height);
        }

        return new PluginParameters(params);
    }
}
