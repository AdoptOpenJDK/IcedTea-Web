package net.adoptopenjdk.icedteaweb.encoding;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class Base64Util {

    public static void decodeBuffer(final InputStream aStream, final OutputStream bStream) throws IOException {
        try(final InputStream wrappedStream = Base64.getDecoder().wrap(aStream)) {
            IOUtils.copy(wrappedStream, bStream);
        }

    }

    public static byte[] decodeBuffer(final String inputString) throws IOException {
        return IOUtils.fromBase64(inputString);
    }

    public static ByteBuffer decodeBufferToByteBuffer(final String inputString) throws IOException {
        return ByteBuffer.wrap(decodeBuffer(inputString));
    }

    private static byte decodeBuffer(InputStream in)[] throws IOException {
        try(ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            decodeBuffer(in, outStream);
            return (outStream.toByteArray());
        }
    }

    public static ByteBuffer decodeBufferToByteBuffer(final InputStream in) throws IOException {
        return ByteBuffer.wrap(decodeBuffer(in));
    }

    public static void encode(final ByteBuffer buffer, final OutputStream stream) throws IOException {
        Assert.requireNonNull(buffer, "buffer");
        encodeBuffer(buffer.array(), stream);
    }

    public static String encode(final ByteBuffer buffer) {
        Assert.requireNonNull(buffer, "buffer");
        return encodeBuffer(buffer.array());
    }

    public static void encodeBuffer(final byte[] buffer, final OutputStream stream) throws IOException {
        try(final OutputStream wrappedStream = Base64.getEncoder().wrap(stream)) {
            IOUtils.writeContent(wrappedStream, buffer);
        }
    }

    public static String encodeBuffer(final byte[] buffer) {
        return IOUtils.toBase64(buffer);
    }

    public static void encodeBuffer(final ByteBuffer buffer, final OutputStream stream) throws IOException {
        Assert.requireNonNull(buffer, "buffer");
        encodeBuffer(buffer.array(), stream);
    }

    public static String encodeBuffer(final ByteBuffer buffer) {
        Assert.requireNonNull(buffer, "buffer");
        return encodeBuffer(buffer.array());
    }
}
