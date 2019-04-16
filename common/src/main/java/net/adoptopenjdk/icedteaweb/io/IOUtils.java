package net.adoptopenjdk.icedteaweb.io;

import dev.rico.internal.core.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Copied from RICO (https://github.com/rico-projects/rico)
 */
public class IOUtils {

    public static String toBase64(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] fromBase64(final String content) {
        return Base64.getDecoder().decode(content);
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
