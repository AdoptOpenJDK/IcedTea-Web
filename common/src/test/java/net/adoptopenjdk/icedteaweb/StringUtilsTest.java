package net.adoptopenjdk.icedteaweb;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StringUtils.hasPrefixMatch;
import static net.adoptopenjdk.icedteaweb.StringUtils.splitIntoMultipleLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;


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
        boolean result = hasPrefixMatch("Windows", new String[]{"Linux", "MacOS", "Windows10-beta"});
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("Windows", new String[]{"Windows"});
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("  Windows  ", new String[]{"Windows"});
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("Windows  ", new String[]{"Windows    "});
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("Windows 7", new String[]{"Windows7"}); // just compare the first prefix token
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("MacOS", new String[]{});
        org.junit.Assert.assertTrue(result);

        result = hasPrefixMatch("MacOS", null);
        Assert.assertTrue(result);
    }

    @Test
    public void prefixShouldNotBeIncludedInArrayOfStrings() {
        boolean result = hasPrefixMatch("MacOS", new String[]{"Linux", "Windows", null});
        org.junit.Assert.assertFalse(result);

        result = hasPrefixMatch("Win7", new String[]{"Win 7", "Win 7.1", "Windows10"});
        org.junit.Assert.assertFalse(result);

        result = hasPrefixMatch("Windows Mobile", new String[]{"Mobile"});
        org.junit.Assert.assertFalse(result);

        result = hasPrefixMatch("Windows 7", new String[]{"7"});
        org.junit.Assert.assertFalse(result);

        result = hasPrefixMatch("Windows 7", new String[]{"windows"});
        org.junit.Assert.assertFalse(result);

        result = hasPrefixMatch("Windows  ", new String[]{" Windows    "});
        org.junit.Assert.assertFalse(result);
    }

    @Test(expected = NullPointerException.class)
    public void testHasPrefixMatchWithNullPrefixString() {
        hasPrefixMatch(null, new String[]{"windows"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasPrefixMatchWithEmptyPrefixString() {
        hasPrefixMatch("", new String[]{"windows"});
    }

    @Test
    public void testSubstringBeforeLast() {
        String source = StringUtils.substringBeforeLast("https://domain.com/substringtest", "/");
        Assert.assertThat(source, equalTo("https://domain.com"));

        source = StringUtils.substringBeforeLast("https://domain.com/substringtest/", "/");
        Assert.assertThat(source, equalTo("https://domain.com/substringtest"));
    }

   @Test
    public void testSubstringBeforeLastWithNoStringMatch() {
        String source = StringUtils.substringBeforeLast("no slash", "/");
        Assert.assertThat(source, equalTo("no slash"));

        source = StringUtils.substringBeforeLast("", "/");
        Assert.assertThat(source, is(blankOrNullString()));

        source = StringUtils.substringBeforeLast(null, "/");
        Assert.assertThat(source, is(blankOrNullString()));
    }

    @Test
    public void testSubstringBeforeLastWithInvalidSeparator() {
        String source = StringUtils.substringBeforeLast("https://domain.com/substringtest", "");
        Assert.assertThat(source, equalTo("https://domain.com/substringtest"));

        source = StringUtils.substringBeforeLast("https://domain.com/substringtest", null);
        Assert.assertThat(source, equalTo("https://domain.com/substringtest"));
    }
}
