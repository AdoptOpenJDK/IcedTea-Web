package net.adoptopenjdk.icedteaweb.encoding;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Base64UtilTests {

    private static final String UTF_STRING = "abcdefgHIJKLMNOPQrstuvwxyz1234567890\r\n"
            + "-=+_))(**&&&^^%%$$##@@!!~{}][\":'/\\.,><\n"
            + "+ěšěčřžýáíé=ů/úěřťšďňéíáč";

    private static final Byte[] PART_1 = {89, 87, 74, 106, 90, 71, 86, 109, 90,
            48, 104, 74, 83, 107, 116, 77, 84, 85, 53, 80, 85, 70, 70, 121, 99, 51,
            82, 49, 100, 110, 100, 52, 101, 88, 111, 120, 77, 106, 77, 48, 78, 84,
            89, 51, 79, 68, 107, 119, 68, 81, 111, 116, 80, 83, 116, 102, 75, 83,
            107, 111, 75, 105, 111, 109, 74, 105, 90, 101, 88, 105, 85, 108, 74, 67,
            81, 106};
    private static final Byte[] PART_2 = {73, 48, 66, 65, 73, 83, 70, 43, 101, 51, 49, 100, 87, 121,
            73, 54, 74, 121, 57, 99, 76, 105, 119, 43, 80, 65, 111, 114, 120, 74,
            118, 70, 111, 99, 83, 98, 120, 73, 51, 70, 109, 99, 87, 43, 119, 55, 51,
            68, 111, 99, 79, 116, 119, 54, 107, 57, 120, 97, 56, 118, 119, 55, 114,
            69, 109, 56, 87, 90, 120, 97, 88, 70, 111, 99, 83, 80};
    private static final Byte[] PART_3 = {120, 89, 106, 68, 113, 99, 79, 116, 119, 54, 72, 69, 106, 81, 61, 61};

    private byte[] getEncodedData() {
        final List<Byte> encoded = new ArrayList<>();
        encoded.addAll(Arrays.asList(PART_1));
        encoded.addAll(Arrays.asList(PART_2));
        encoded.addAll(Arrays.asList(PART_3));
        return IOUtils.unbox(encoded.toArray(new Byte[0]));
    }

    @Test
    public void testEmbededBase64Decoder() throws Exception {
        //given
        final byte[] data = getEncodedData();
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        //when
        Base64Util.decodeBuffer(in, out);
        final byte[] decoded = out.toByteArray();

        //then
        Assert.assertEquals(UTF_STRING, new String(decoded, "utf-8"));
    }

    @Test
    public void testEmbededBase64DecoderAgainstEmbededEncoder() throws Exception {
        //given
        final byte[] data = getEncodedData();
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        //when
        Base64Util.decodeBuffer(in, out);
        final byte[] decoded = out.toByteArray();

        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        Base64Util.encodeBuffer(decoded, out2);
        final byte[] encoded = out2.toByteArray();

        //then
        Assert.assertArrayEquals(data, encoded);
    }





    @Test
    public void testEmbededBase64Encoder() throws Exception {
        //given
        final byte[] data = UTF_STRING.getBytes("utf-8");
        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();

        //when
        Base64Util.encodeBuffer(data, out2);
        final byte[] encoded2 = out2.toByteArray();

        //then
        Assert.assertArrayEquals(getEncodedData(), encoded2);
    }


    @Test
    public void testEmbededBase64EncoderAgainstEbededDecoder() throws Exception {
        //given
        final byte[] data = UTF_STRING.getBytes("utf-8");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        //when
        Base64Util.encodeBuffer(data, out);
        final byte[] encoded = out.toByteArray();
        final byte[] decoded = Base64Util.decodeBuffer(new String(encoded, "utf-8"));

        //then
        //Assert.assertArrayEquals(data, decoded);
        Assert.assertEquals(UTF_STRING, new String(decoded, "utf-8"));
    }


}
