package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.testing.AnnotationConditionChecker;
import net.adoptopenjdk.icedteaweb.testing.annotations.WindowsIssue;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.LINE_SEPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class TeeOutputStreamTest {

    private static final String EOL = System.getProperty(LINE_SEPARATOR);

    private static final String[] TEST_STRINGS = new String[]{
            "Hello,\n\r\nWorld",
            "Hel你好lo \n World!",
            "ÆÆÆÆÆHello!\r",
            "नमस्तHello!\r",
            // watch out the following 2 strings are not equal
            "He\n\n\\llo chào",
            "He\n\n\\llo chào"
    };

    @Rule
    public AnnotationConditionChecker acc = new AnnotationConditionChecker();

    private TeeOutputStream tos;
    private List<String> loggedMessages;
    private ByteArrayOutputStream out;

    @Before
    public void setup() {
        final BasicOutputController outputController = new BasicOutputController() {
            @Override
            public void log(MessageWithHeader l) {
                loggedMessages.add(l.getMessage());
            }
        };

        loggedMessages = new ArrayList<>();
        out = new ByteArrayOutputStream();
        tos = new TeeOutputStream(new PrintStream(out, true), false, outputController);
    }

    @Test
    public void testPrintIsNotAutoFlushed() {
        String s = TEST_STRINGS[0];
        tos.print(s); //print should NOT be immediately flushed
        assertThat(loggedMessages, is(empty()));
        assertThat(out.toString(), is(s));
    }

    @Test
    public void testPrintLnIsAutoFlushed() {
        String s = TEST_STRINGS[0];
        tos.println(s); //println should be immediately flushed
        assertThat(loggedMessages, hasItems(s + EOL));
        assertThat(out.toString(), is(s + EOL));
    }

    @Test
    @WindowsIssue
    public void testPrint() {
        for (String s : TEST_STRINGS) {
            assertUnmodifiedByPrint(s);
            clearBuffers();
        }
    }

    @Test
    @WindowsIssue
    public void testWriteByteArrayString() {
        for (String s : TEST_STRINGS) {
            assertUnmodifiedByWrite(s);
            clearBuffers();
        }
    }

    @Test
    @WindowsIssue
    public void testWriteByteArrayStringInUtf8() { //last character missing
        for (String s : TEST_STRINGS) {
            assertUnmodifiedByWriteInUtf8(s);
            clearBuffers();
        }
    }

    @Test
    public void testWriteByte() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            if (i == '\n') {
                // handling of newline is OS dependent and covered in separate test
                continue;
            }

            byte b = (byte) i;
            tos.write(b);
            assertThat(loggedMessages, is(empty()));
            assertThat(out.toByteArray().length, is(1));
            assertThat(out.toByteArray()[0], is(b));

            tos.flush();
            assertThat(loggedMessages, hasItems(new String(new byte[]{b})));
            clearBuffers();
        }
    }

    @Test
    public void testWriteNewLineByte() {
        byte b = (byte) '\n';
        tos.write(b);

        if (EOL.length() == 1 && EOL.charAt(0) == b) {
            assertThat(loggedMessages, hasItems(new String(new byte[]{b})));
        } else {
            assertThat(loggedMessages, is(empty()));
        }

        assertThat(out.toByteArray().length, is(1));
        assertThat(out.toByteArray()[0], is(b));

        tos.flush();
        assertThat(loggedMessages, hasItems(new String(new byte[]{b})));
        }

    private void assertUnmodifiedByPrint(String s) {
        tos.print(s);
        assertThat(loggedMessages, is(empty()));
        assertThat(out.toString(), is(s));

        tos.flush();
        assertThat(loggedMessages, hasItems(s));
        assertThat(out.toString(), is(s));
    }

    private void assertUnmodifiedByWrite(String s) {
        final byte[] bytes = s.getBytes();
        tos.write(bytes, 0, bytes.length);
        assertThat(loggedMessages, is(empty()));
        assertThat(out.toString(), is(s));

        tos.flush();
        assertThat(loggedMessages, hasItems(s));
        assertThat(out.toString(), is(s));
    }

    private void assertUnmodifiedByWriteInUtf8(String s) {
        final byte[] bytes = s.getBytes(UTF_8);
        tos.write(bytes, 0, bytes.length);
        assertThat(loggedMessages, is(empty()));
        assertThat(out.toString(), is(s));

        tos.flush();
        assertThat(loggedMessages, hasItems(s));
        assertThat(out.toString(), is(s));
    }

    private void clearBuffers() {
        loggedMessages.clear();
        out.reset();
    }
}
