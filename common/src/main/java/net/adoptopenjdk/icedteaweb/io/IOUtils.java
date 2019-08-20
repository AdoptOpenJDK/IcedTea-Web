package net.adoptopenjdk.icedteaweb.io;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.StringUtils.splitIntoMultipleLines;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class IOUtils {

    /**
     * Converts a byte array into a Base64 string.
     *
     * @param bytes the bytes to convert
     * @return the Base64 representation of the input bytes.
     */
    public static String toBase64(final byte[] bytes) {
        final byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, StandardCharsets.ISO_8859_1);
    }


    /**
     * Converts a byte array into a Base64 string.
     * The output string is split into multiple lines such that
     * all but the last line have a given length.
     *
     * @param bytes           the bytes to convert
     * @param maxCharsPerLine the max num of chars per line
     * @return the Base64 representation of the input bytes.
     */
    public static String toBase64splitIntoMultipleLines(final byte[] bytes, final int maxCharsPerLine) {
        final String encoded = toBase64(bytes);
        final List<String> lines = splitIntoMultipleLines(encoded, maxCharsPerLine);
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Converts a Base64 string into a byte array.
     *
     * @param content the Base64 string
     * @return the byte array representation of the input string.
     */
    public static byte[] fromBase64(final String content) {
        final byte[] encoded = content.getBytes(StandardCharsets.ISO_8859_1);
        return Base64.getDecoder().decode(encoded);
    }

    /**
     * Converts a Base64 string into a byte array.
     * Additionally strips all white spaces from the input.
     *
     * @param content the Base64 string
     * @return the byte array representation of the input string.
     */
    public static byte[] fromBase64StripWhitespace(final String content) {
        final String stripped = content.replaceAll("\\s", "");
        return fromBase64(stripped);
    }

    /**
     * Copies the content of an input stream to an output stream.
     *
     * @param inputStream  the source to read from
     * @param outputStream the target to write to
     * @return the number of bytes copied
     * @throws IOException if reading from or writing to the streams failed
     */
    public static void copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, 1024);
    }

    /**
     * Copies the content of an input stream to an output stream.
     *
     * @param inputStream  the source to read from
     * @param outputStream the target to write to
     * @param bufferSize   the size of the buffer to allocate for copying
     * @return the number of bytes copied
     * @throws IOException if reading from or writing to the streams failed
     */
    public static void copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(outputStream, "outputStream");

        final byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.flush();
    }

     /**
     * Reads the content of an input stream into a byte array.
     *
     * @param inputStream the stream to read from
     * @return array of bytes read from the stream
     * @throws IOException if reading from the stream failed
     */
    public static byte[] readContent(final InputStream inputStream) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copy(inputStream, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Reads the content of an input stream into a string using UTF-8 encoding.
     *
     * @param inputStream the stream to read from
     * @return string read from the stream
     * @throws IOException if reading from the stream failed
     */
    public static String readContentAsUtf8String(final InputStream inputStream) throws IOException {
        return new String(readContent(inputStream), UTF_8);
    }

    /**
     * Reads the content of an input stream into a string.
     *
     * @param inputStream the stream to read from
     * @param encoding the character set to use when converting the bytes to characters
     * @return string read from the stream
     * @throws IOException if reading from the stream failed
     */
    public static String readContentAsString(final InputStream inputStream, final Charset encoding) throws IOException {
        return new String(readContent(inputStream), encoding);
    }

    public static void writeUtf8Content(final OutputStream outputStream, final String content) throws IOException {
        Assert.requireNonNull(outputStream, "outputStream");
        Assert.requireNonNull(content, "content");

        outputStream.write(content.getBytes(UTF_8));
        outputStream.flush();
    }

    public static void writeContent(final OutputStream outputStream, final byte[] rawData) throws IOException {
        Assert.requireNonNull(outputStream, "outputStream");
        Assert.requireNonNull(rawData, "rawData");

        outputStream.write(rawData);
        outputStream.flush();
    }
}
