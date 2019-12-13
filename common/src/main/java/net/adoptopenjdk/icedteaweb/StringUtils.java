package net.adoptopenjdk.icedteaweb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class StringUtils {

    public static final String EMPTY_STRING = "";

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
     *  If a candidate is a prefix of the heyStack, then this is a match. If no candidates are specified, it matches any heyStack.
     *
     * @implSpec See <b>JSR-56, Section 4. Application Resources - Overview</b>
     * for a detailed specification of this use case.
     *
     * @param heyStack the string to match candidates against
     * @param candidates the strings to test as possible prefixes
     * @return true if the any of the candidates is a prefix match of the heystack or if there are no candidates.
     */
    public static boolean hasPrefixMatch(final String heyStack, final String... candidates) {
        Assert.requireNonBlank(heyStack, "heyStack");

        if (candidates == null) {
            return true;
        }
        final List<String> trimmedCandidates = Arrays.stream(candidates)
                .filter(str -> !isBlank(str))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (trimmedCandidates.isEmpty()) {
            return true;
        }

        final String trimmedHeyStack = heyStack.trim().toLowerCase();
        return trimmedCandidates.stream().anyMatch(trimmedHeyStack::startsWith);
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

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
