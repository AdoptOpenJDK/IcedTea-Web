package net.adoptopenjdk.icedteaweb;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StringUtils.splitIntoMultipleLines;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


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
}
