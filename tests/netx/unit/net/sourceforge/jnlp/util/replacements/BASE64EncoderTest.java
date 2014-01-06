/* BASE64EncoderTest.java
Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */
package net.sourceforge.jnlp.util.replacements;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

/** Test various corner cases of the parser */
public class BASE64EncoderTest {

    static final String sSrc = "abcdefgHIJKLMNOPQrstuvwxyz1234567890\r\n"
            + "-=+_))(**&&&^^%%$$##@@!!~{}][\":'/\\.,><\n"
            + "+ěšěčřžýáíé=ů/úěřťšďňéíáč";
    static final byte[] encoded = {89, 87, 74, 106, 90, 71, 86, 109, 90,
        48, 104, 74, 83, 107, 116, 77, 84, 85, 53, 80, 85, 70, 70, 121, 99, 51,
        82, 49, 100, 110, 100, 52, 101, 88, 111, 120, 77, 106, 77, 48, 78, 84,
        89, 51, 79, 68, 107, 119, 68, 81, 111, 116, 80, 83, 116, 102, 75, 83,
        107, 111, 75, 105, 111, 109, 74, 105, 90, 101, 88, 105, 85, 108, 74, 67,
        81, 106, 10, 73, 48, 66, 65, 73, 83, 70, 43, 101, 51, 49, 100, 87, 121,
        73, 54, 74, 121, 57, 99, 76, 105, 119, 43, 80, 65, 111, 114, 120, 74,
        118, 70, 111, 99, 83, 98, 120, 73, 51, 70, 109, 99, 87, 43, 119, 55, 51,
        68, 111, 99, 79, 116, 119, 54, 107, 57, 120, 97, 56, 118, 119, 55, 114,
        69, 109, 56, 87, 90, 120, 97, 88, 70, 111, 99, 83, 80, 10, 120, 89, 106,
        68, 113, 99, 79, 116, 119, 54, 72, 69, 106, 81, 61, 61, 10};
    
    private static final String sunClassD = "sun.misc.BASE64Decoder";

    @Test
    public void testEmbededBase64Encoder() throws Exception {
        final byte[] data = sSrc.getBytes("utf-8");
//        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
//        sun.misc.BASE64Encoder e1 = new sun.misc.BASE64Encoder();
//        e1.encode(data, out1);
//        byte[] encoded1 = out1.toByteArray();
//        ServerAccess.logErrorReprint(Arrays.toString(encoded1));
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Encoder e2 = new BASE64Encoder();
        e2.encodeBuffer(data, out2);
        byte[] encoded2 = out2.toByteArray();
        Assert.assertArrayEquals(encoded, encoded2);
//      ServerAccess.logErrorReprint(Arrays.toString(encoded2));



    }

    @Test
    /*
     * This test will fail, in case taht sun.misc.BASE64Decoder will be removed from builders java
     */
    public void testEmbededBase64EncoderAgainstSunOne() throws Exception {
        final byte[] data = sSrc.getBytes("utf-8");

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Encoder e2 = new BASE64Encoder();
        e2.encodeBuffer(data, out2);
        byte[] encoded2 = out2.toByteArray();
        Object decoder = createInsatnce(sunClassD);
        byte[] decoded = (byte[]) (getAndInvokeMethod(decoder, "decodeBuffer", new String(encoded2, "utf-8")));
        Assert.assertArrayEquals(data, decoded);
        Assert.assertEquals(sSrc, new String(decoded, "utf-8"));
    }
    
      @Test
    public void testEmbededBase64EncoderAgainstEbededDecoder() throws Exception {
        final byte[] data = sSrc.getBytes("utf-8");
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Encoder e2 = new BASE64Encoder();
        e2.encodeBuffer(data, out2);
        byte[] encoded2 = out2.toByteArray();
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] decoded = decoder.decodeBuffer(new String(encoded2, "utf-8"));
        Assert.assertArrayEquals(data, decoded);
        Assert.assertEquals(sSrc, new String(decoded, "utf-8"));
    }

    static Object createInsatnce(String ofCalss) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Class<?> classDefinition = Class.forName(ofCalss);
        return classDefinition.newInstance();

    }

    static Object getAndInvokeMethod(Object instance, String methodName, Object... params) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?>[] cs = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            Object object = params[i];
            cs[i] = object.getClass();
            if (object instanceof OutputStream) {
                cs[i] = OutputStream.class;
            }
        }
        Method m = instance.getClass().getMethod(methodName, cs);
        return m.invoke(instance, params);

    }
}
