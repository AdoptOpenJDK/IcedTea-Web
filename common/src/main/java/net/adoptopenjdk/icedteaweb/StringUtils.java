package net.adoptopenjdk.icedteaweb;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class StringUtils {

    private static final String WHITESPACE_CHARACTER_SEQUENCE = "\\s+";

    private StringUtils() {
        // do not instantiate
    }

    /**
     * <p>Determines whether a given string is <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>false</code>.</p>
     *
     * @param str The string to test.
     * @return <code>true</code> if the string is <code>null</code>, or
     * blank.
     */
    public static boolean isBlank(final String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        for (final char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method for splitting long strings into multiple lines.
     * This is mainly used for pretty printing.
     * Whitespace characters in the input string are not handled specially.
     *
     * All lines will have the length passed as {@code maxCharsPerLine}
     * except the last one which may be shorter.
     *
     * If null is passed instead of a string then an empty list is returned.
     * If zero or a negative maxCharsPerLine is passed then a list containing
     * the input string as the only element is returned.
     *
     * @param s the string to be split into multiple lines
     * @param maxCharsPerLine the maximum characters a line may contain.
     * @return a list of strings with the maximal length as specified.
     */
    public static List<String> splitIntoMultipleLines(final String s, final int maxCharsPerLine) {
        if (s == null) {
            return emptyList();
        }
        if (maxCharsPerLine < 1) {
            return singletonList(s);
        }

        final List<String> lines = new ArrayList<>();
        String tmp = s;
        while (tmp.length() > maxCharsPerLine) {
            lines.add(tmp.substring(0, maxCharsPerLine));
            tmp = tmp.substring(maxCharsPerLine);
        }
        lines.add(tmp);
        return lines;
    }

    /**
     * Checks whether the first part of the given prefixString is a prefix for any of the strings
     * in the specified array. If no array is specified (empty or null) it is considered to be a
     * match.
     *
     * If the {@code prefixString} contains multiple words separated by a space character, the
     * first word is taken as prefix for comparison.
     *
     * @param prefixString the prefixString string
     * @param available the strings to test
     * @return true if the first part of the given prefixString is a prefix for any of the strings
     * in the specified array or the specified array is empty or null, false otherwise
     */
    public static boolean hasPrefixMatch(final String prefixString, final String[] available) {
        Assert.requireNonBlank(prefixString, "prefixString");

        if (available == null || available.length == 0){
            return true;
        }

        final String trimmedPrefix = prefixString.split(WHITESPACE_CHARACTER_SEQUENCE)[0];

        for (final String candidate : available) {
            String trimmedCandidate = null;
            if (candidate != null) {
                trimmedCandidate = candidate.split(WHITESPACE_CHARACTER_SEQUENCE)[0];
            }
            if (trimmedCandidate != null && trimmedCandidate.startsWith(trimmedPrefix)) {
                return true;
            }
        }

        return false;
    }

    public static String substringBeforeLast(final String s, final String separator) {
        if (s == null || separator == null || separator.isEmpty() || s.isEmpty()) {
            return s;
        }
        final int idx = s.lastIndexOf(separator);
        if (idx < 0) {
            return s;
        }
        return s.substring(0, idx);
    }
}
