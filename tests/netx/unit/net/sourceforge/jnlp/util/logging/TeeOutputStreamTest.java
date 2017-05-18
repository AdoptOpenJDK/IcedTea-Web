package net.sourceforge.jnlp.util.logging;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.WindowsIssue;

import static org.junit.Assert.assertTrue;

public class TeeOutputStreamTest {

    private PrintStream teePrintStream;
    private TeeOutputStream tos;


    @Before
    public void setup() {
        teePrintStream = new PrintStream(new ByteArrayOutputStream(), true);
        tos = new TeeOutputStream(teePrintStream, false);
    }
    @Test
    public void testPrintLn() throws IOException {
        String s = "Hel你好lo \n World!";
        tos.println(s); //println should be immediately flushed
        assertTrue(tos.getByteArrayOutputStream().toString().isEmpty());
    }

    @Test
    @WindowsIssue
    public void testPrint() throws IOException {
        if (Charset.defaultCharset().toString().toLowerCase().startsWith("windows")) {
            String s = "ÆÆÆÆÆHello!\r";
            tos.print(s);
            assertTrue(tos.getByteArrayOutputStream().toString().equals(s));
        } else {
            String s = "नमस्तHello!\r"; //first five symbols are printed as "?" by windows' default character encoding
            tos.print(s);
            assertTrue(tos.getByteArrayOutputStream().toString().equals(s));
        }

    }

    @Test
    @WindowsIssue
    public void testWriteByteArrayString() throws IOException {
        if (Charset.defaultCharset().toString().toLowerCase().startsWith("windows")) {
            String s = "He\n\n\\llo chào";
            tos.write(s.getBytes(), 0, s.getBytes().length);
            assertTrue(tos.getByteArrayOutputStream().toString().equals(s));
        } else {
            String s = "He\n\n\\llo chào"; //grave accent as "?" by windows' default character encoding
            tos.write(s.getBytes(), 0, s.getBytes().length);
            assertTrue(tos.getByteArrayOutputStream().toString().equals(s));
        }
    }

    @Test
    @WindowsIssue
    @KnownToFail
    public void testWriteByteArrayString2() throws IOException { //last character missing
        String s = "He\n\n\\llo chào"; //grave accent as "?" by windows' default character encoding
        tos.write(s.getBytes("utf-8"), 0, s.getBytes().length);
        assertTrue(tos.getByteArrayOutputStream().toString("utf-8").equals(s));
    }
    
    @Test
    public void testWriteByte() throws IOException {
        byte b = 5;
        tos.write(b);
        assertTrue(byteArrayEquals(b, tos.getByteArrayOutputStream().toByteArray()));
    }

    @Test
    public void testFlush() throws IOException {
        String s = "Hello";
        tos.print(s);
        assertTrue(!tos.getByteArrayOutputStream().toString().isEmpty());
        tos.flush();
        assertTrue(tos.getByteArrayOutputStream().toString().isEmpty());
    }

    private boolean byteArrayEquals(byte b, byte[] arr) {
        for (byte i : arr) {
            if (b != i) {
                return false;
            }
        }
        return true;
    }
}
