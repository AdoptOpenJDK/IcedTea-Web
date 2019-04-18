package net.adoptopenjdk.icedteaweb.io;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.StringUtils.splitIntoMultipleLines;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class IOUtils {

    public static String toBase64(final byte[] bytes) {
        final byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, StandardCharsets.ISO_8859_1);
    }

    public static String toBase64splitIntoMultipleLines(final byte[] bytes, final int maxCharsPerLine) {
        final String encoded = toBase64(bytes);
        final List<String> lines = splitIntoMultipleLines(encoded, maxCharsPerLine);
        return String.join(System.lineSeparator(), lines);
    }

    public static byte[] fromBase64(final String content) {
        final byte[] encoded = content.getBytes(StandardCharsets.ISO_8859_1);
        return Base64.getDecoder().decode(encoded);
    }

    public static byte[] fromBase64StripWhitespace(final String content) {
        final String stripped = content.replaceAll("\\s", "");
        return fromBase64(stripped);
    }

    public static long copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        return copy(inputStream, outputStream, 1024);
    }

    public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(outputStream, "outputStream");

        final byte[] buffer = new byte[bufferSize];
        long finalLength = 0;
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
            finalLength = finalLength  + len;
        }
        return finalLength;
    }

    public static void writeContent(final OutputStream outputStream, final byte[] rawData) throws IOException {
        Assert.requireNonNull(outputStream, "outputStream");
        Assert.requireNonNull(rawData, "rawData");
        outputStream.write(rawData);
        outputStream.flush();
    }

    public static byte[] unbox(final Byte[] array) {
        Assert.requireNonNull(array, "array");
        final byte[] result = new byte[array.length];
        int j=0;
        for(Byte b: array) {
            result[j++] = b.byteValue();
        }
        return result;
    }
}
