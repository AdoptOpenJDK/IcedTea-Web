package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;

public class PlainTextFormatter extends ReplacingTextFormatter {

    private static final String LINE_SEPARATOR = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));

    private final Map<String, Integer> LongestKeys = new TreeMap<>();
    private String currentSection = "none";
    public  static final String SUFFIX = "txt";
    
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    private final String pargraohIndentation;
    private final int maxLineLength;
    public static final String DEFAULT_INDENT = "    ";
    

    public PlainTextFormatter(String pargraohIndentation, int maxLineLength) {
        this.pargraohIndentation = pargraohIndentation;
        this.maxLineLength = maxLineLength;
    }

    public PlainTextFormatter(String pargraohIndentation) {
        this(pargraohIndentation, Integer.MAX_VALUE);
    }
    
    public PlainTextFormatter(int maxLineLength) {
        this(DEFAULT_INDENT, maxLineLength);
    }
    
    public PlainTextFormatter() {
        this.pargraohIndentation = DEFAULT_INDENT;
        this.maxLineLength = Integer.MAX_VALUE;
    }

    @Override
    public String getBoldOpening() {
        return "";
    }

    @Override
    public String getBoldClosing() {
        return "";
    }

    @Override
    public String getTitle(String s) {
        currentSection  = s;
        return localizeTitle(s) + getNewLine();
    }

    @Override
    public String getNewLine() {
        return LINE_SEPARATOR;
    }

    @Override
    public String getHeaders(String id, String encoding) {
        return "  ***  " + TextsProvider.ITW + " " + getVersion() + "  ***  ";
    }

    @Override
    public String getTail() {
        return "";
    }

    @Override
    public String getSeeAlso(String s) {
        return "  * " + s + getNewLine();
    }

    @Override
    public String getFileSuffix() {
        return "."+SUFFIX;
    }

    @Override
    public String wrapParagraph(String s) {
        return wrapParagraph(s, maxLineLength, pargraohIndentation);

    }

    static String wrapParagraph(String input, int maxWidth, String indent) {
        List<String> l = new ArrayList(Arrays.asList(input.split(getLineSeparator())));
        for (int i = 0; i < l.size(); i++) {
            String s = indent + l.get(i).trim();
            if (s.length() > maxWidth) {
                //no splitting in middle of words
                String split1 = s.substring(0, maxWidth);
                int split2 = split1.lastIndexOf(" ");
                int splitTest = split1.trim().lastIndexOf(" ");
                if (split2 < 1 || splitTest < 1) {
                    //avoid words longer then maxWidth
                    split2 = maxWidth;
                }
                l.set(i, s.substring(0, split2));
                l.add(i + 1, s.substring(split2));
            } else {
                l.set(i, s);
            }

        }
        return listToString(l);
    }

    static String listToString(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (String string : l) {
            sb.append(string).append(getLineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String getBreakAndBold() {
        return getNewLine();
    }

    @Override
    public String getCloseBoldAndBreak() {
        return getNewLine();
    }

    @Override
    public String getBoldCloseNwlineBoldOpen() {
        return getNewLine();
    }

    @Override
    public String getOption(String key, String value) {
        Integer longestKey = LongestKeys.get(currentSection);
        if (longestKey == null){
            longestKey = 0;
        }
        if (key.length() > longestKey) {
            longestKey = key.length();
            LongestKeys.put(currentSection, longestKey);
        }
        return key + getSpaces(longestKey + 3, key.length()) + process(value) + getNewLine();
    }

    private String getSpaces(int l, int k) {
        if (l - k <= 3) {
            return " - ";
        }
        String s = "";
        while (s.length() < l - k - 2) {
            s += " ";
        }
        return s + "- ";
    }

}
