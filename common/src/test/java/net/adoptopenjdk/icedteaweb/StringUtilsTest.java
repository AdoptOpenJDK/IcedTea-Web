package net.adoptopenjdk.icedteaweb;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StringUtils.hasPrefixMatch;
import static net.adoptopenjdk.icedteaweb.StringUtils.splitIntoMultipleLines;
import static net.adoptopenjdk.icedteaweb.StringUtils.substringBeforeLast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests for {@link StringUtils}.
 */
public class StringUtilsTest {

    @Test
    public void nullEmptyAndWhitespaceOnlyShouldAllBeBlank() {
        // given
        final Stream<String> allBlanks = Stream.of(
                null,
                "",
                " ",
                "\t",
                "\n",
                "\r",
                "\t \n\r",
                "   ",
                "\n\n\t\n"
        );

        // then
        allBlanks.forEach(s -> assertThat(StringUtils.isBlank(s), is(true)));
    }

    @Test
    public void stringWithNonWhitespaceShouldNotBeBlank() {
        // given
        final Stream<String> allBlanks = Stream.of(
                "a",
                "1",
                "bbbb",
                "2222",
                ".",
                "    4    ",
                "\n\nxxxx\t\n"
        );

        // then
        allBlanks.forEach(s -> assertThat(StringUtils.isBlank(s), is(false)));
    }

    @Test
    public void shouldSplitLongStringCorrectly() {
        // given
        final String longString = "abcd1234efgh5678..";

        // when
        final List<String> result = splitIntoMultipleLines(longString, 4);

        // then
        assertThat(result, hasItems(
                "abcd",
                "1234",
                "efgh",
                "5678",
                ".."
        ));
    }

    @Test
    public void shouldNotSplitShortString() {
        // given
        final String shortString = "abcd";

        // when
        final List<String> result = splitIntoMultipleLines(shortString, 8);

        // then
        assertThat(result, hasItems(shortString));
    }

    @Test
    public void shouldReturnEmptyListForNullInput() {
        // when
        final List<String> result = splitIntoMultipleLines(null, 8);

        // then
        assertThat(result, is(empty()));
    }

    @Test
    public void shouldReturnListWithInputIfMaxCharsIsLessThanOne() {
        // given
        final Stream<Integer> invalidLength = Stream.of(0, -1, -10, -100);
        final String longString = "abcd1234efgh5678..";

        invalidLength.forEach(maxChars -> {
            // when
            final List<String> result = splitIntoMultipleLines(longString, maxChars);

            // then
            assertThat(result, hasItems(longString));
        });
    }

    @Test
    public void prefixShouldBeIncludedInArrayOfStrings() {
        assertTrue(hasPrefixMatch("Windows 7", "windows"));
        assertTrue(hasPrefixMatch("Windows  ", " Windows    "));
        assertTrue(hasPrefixMatch("Windows", "Windows"));
        assertTrue(hasPrefixMatch("  Windows  ", "Windows"));
        assertTrue(hasPrefixMatch("Windows  ", "Windows    "));
        assertTrue(hasPrefixMatch("Windows 7", "Windows 7"));
        assertTrue(hasPrefixMatch("MacOS"));
        assertTrue(hasPrefixMatch("MacOS", (String[]) null));
    }

    @Test
    public void prefixShouldNotBeIncludedInArrayOfStrings() {
        assertFalse(hasPrefixMatch("MacOS", "Linux", "Windows", null));
        assertFalse(hasPrefixMatch("Win7", "Win 7", "Win 7.1", "Windows10"));
        assertFalse(hasPrefixMatch("Windows Mobile", "Mobile"));
        assertFalse(hasPrefixMatch("Windows 7", "7"));
        assertFalse(hasPrefixMatch("Windows", "Linux", "MacOS", "Windows10-beta"));
    }

    @Test(expected = NullPointerException.class)
    public void testHasPrefixMatchWithNullPrefixString() {
        hasPrefixMatch(null, "windows");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasPrefixMatchWithEmptyPrefixString() {
        hasPrefixMatch("", "windows");
    }

    @Test
    public void testSubstringBeforeLast() {
        assertThat(substringBeforeLast("https://domain.com/substringtest", "/"), equalTo("https://domain.com"));
        assertThat(substringBeforeLast("https://domain.com/substringtest/", "/"), equalTo("https://domain.com/substringtest"));
    }

    @Test
    public void testSubstringBeforeLastWithNoStringMatch() {
        assertThat(substringBeforeLast("no slash", "/"), equalTo("no slash"));
        assertThat(substringBeforeLast("", "/"), is(blankOrNullString()));
        assertThat(substringBeforeLast(null, "/"), is(blankOrNullString()));
    }

    @Test
    public void testSubstringBeforeLastWithInvalidSeparator() {
        assertThat(substringBeforeLast("https://domain.com/substringtest", ""), equalTo("https://domain.com/substringtest"));
        assertThat(substringBeforeLast("https://domain.com/substringtest", null), equalTo("https://domain.com/substringtest"));
    }
}
