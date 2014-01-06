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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;

/** Test various corner cases of the parser */
public class BASE64DecoderTest {

    private static final String sunClassE = "sun.misc.BASE64Encoder";
      
    @Test
    public void testEmbededBase64Decoder() throws Exception {
        final byte[] data = BASE64EncoderTest.encoded;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Decoder e2 = new BASE64Decoder();
        e2.decodeBuffer(new ByteArrayInputStream(data), out2);
        byte[] decoded = out2.toByteArray();
        Assert.assertEquals(BASE64EncoderTest.sSrc, new String(decoded, "utf-8"));



    }

    @Test
    /*
     * This test will fail, in case taht sun.misc.BASE64Encoder will be removed from builders java
     */
    public void testEmbededBase64DecoderAgainstSunOne() throws Exception {
        final byte[] data = BASE64EncoderTest.encoded;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Decoder e2 = new BASE64Decoder();
        e2.decodeBuffer(new ByteArrayInputStream(data), out2);
        byte[] encoded2 = out2.toByteArray();
        Object encoder = BASE64EncoderTest.createInsatnce(sunClassE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BASE64EncoderTest.getAndInvokeMethod(encoder, "encodeBuffer", encoded2, out);
        Assert.assertArrayEquals(data, out.toByteArray());
        Assert.assertArrayEquals(BASE64EncoderTest.encoded, out.toByteArray());

    }
    
     @Test
    public void testEmbededBase64DecoderAgainstEmbededEncoder() throws Exception {
        final byte[] data = BASE64EncoderTest.encoded;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        BASE64Decoder e2 = new BASE64Decoder();
        e2.decodeBuffer(new ByteArrayInputStream(data), out2);
        byte[] encoded2 = out2.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encoder.encodeBuffer(encoded2, out);
        Assert.assertArrayEquals(data, out.toByteArray());
        Assert.assertArrayEquals(BASE64EncoderTest.encoded, out.toByteArray());

    }

  
}
