/*
Copyright (C) 2012 Red Hat, Inc.

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

package sun.applet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import netscape.javascript.JSObject;

import org.junit.Test;

public class PluginAppletSecurityContextTest {

    private static PluginAppletSecurityContext dummySecurityContext() {
        return new PluginAppletSecurityContext(0, false);
    }

    @Test
    public void toIDStringNullTest() {
        PluginAppletSecurityContext pasc = dummySecurityContext();
        assertEquals("literalreturn null",
                pasc.toObjectIDString(null, Object.class, false));
    }

    @Test
    public void toIDStringVoidTest() {
        PluginAppletSecurityContext pasc = dummySecurityContext();
        assertEquals("literalreturn void",
                pasc.toObjectIDString(null, Void.TYPE, false));

        assertFalse("literalreturn void".equals(pasc.toObjectIDString(null,
                Void.class, false)));
    }

    @Test
    public void toIDStringIntegralTest() {
        // NB: the special .TYPE classes here represent primitives
        PluginAppletSecurityContext pasc = dummySecurityContext();

        // Test both unboxing allowed and not allowed to be sure it doesn't
        // alter result
        // although it really shouldn't
        for (boolean unboxPrimitives : new Boolean[] { false, true }) {
            assertEquals("literalreturn true", pasc.toObjectIDString(
                    new Boolean(true), Boolean.TYPE, unboxPrimitives));

            assertEquals("literalreturn 1", pasc.toObjectIDString(new Byte(
                    (byte) 1), Byte.TYPE, unboxPrimitives));

            assertEquals("literalreturn 1", pasc.toObjectIDString(
                    new Character((char) 1), Character.TYPE, unboxPrimitives));

            assertEquals("literalreturn 1", pasc.toObjectIDString(new Short(
                    (short) 1), Short.TYPE, unboxPrimitives));

            assertEquals("literalreturn 1", pasc.toObjectIDString(
                    new Integer(1), Integer.TYPE, unboxPrimitives));

            assertEquals("literalreturn 1", pasc.toObjectIDString(new Long(1),
                    Long.TYPE, unboxPrimitives));
        }
    }

    @Test
    public void toIDStringBoxedIntegralNoUnboxingTest() {
        PluginAppletSecurityContext pasc = dummySecurityContext();

        assertFalse("literalreturn true".equals(pasc.toObjectIDString(
                new Boolean(true), Boolean.class, false)));

        assertFalse("literalreturn 1".equals(pasc.toObjectIDString(new Byte(
                (byte) 1), Byte.class, false)));

        assertFalse("literalreturn 1".equals(pasc.toObjectIDString(
                new Character((char) 1), Character.class, false)));

        assertFalse("literalreturn 1".equals(pasc.toObjectIDString(new Short(
                (short) 1), Short.class, false)));

        assertFalse("literalreturn 1".equals(pasc.toObjectIDString(new Integer(
                1), Integer.class, false)));

        assertFalse("literalreturn 1".equals(pasc.toObjectIDString(new Long(1),
                Long.class, false)));
    }

    @Test
    public void toIDStringBoxedIntegralWithUnboxingTest() {
        PluginAppletSecurityContext pasc = dummySecurityContext();

        assertEquals("literalreturn true",
                pasc.toObjectIDString(new Boolean(true), Boolean.class, true));

        assertEquals("literalreturn 1",
                pasc.toObjectIDString(new Byte((byte) 1), Byte.class, true));

        assertEquals("literalreturn 1", pasc.toObjectIDString(new Character(
                (char) 1), Character.class, true));

        assertEquals("literalreturn 1",
                pasc.toObjectIDString(new Short((short) 1), Short.class, true));

        assertEquals("literalreturn 1",
                pasc.toObjectIDString(new Integer(1), Integer.class, true));

        assertEquals("literalreturn 1",
                pasc.toObjectIDString(new Long(1), Long.class, true));
    }

    @Test
    public void toIDStringFloatingPoint() {
        final int prefixLength = "literalreturn ".length();

        // NB: the special .TYPE classes here represent primitives
        PluginAppletSecurityContext pasc = dummySecurityContext();

        // Test both unboxing allowed and not allowed to be sure it doesn't
        // alter result
        // although it really shouldn't
        for (boolean unboxPrimitives : new Boolean[] { false, true }) {
            {
                final float testFloat = 3.141592f;
                String idString = pasc.toObjectIDString(new Float(testFloat),
                        Float.TYPE, unboxPrimitives);
                String floatRepr = idString.substring(prefixLength);
                assertTrue(testFloat == Float.parseFloat(floatRepr));
            }
            {
                final double testDouble = 3.141592;
                String idString = pasc.toObjectIDString(new Double(testDouble),
                        Double.TYPE, unboxPrimitives);
                String doubleRepr = idString.substring(prefixLength);
                assertTrue(testDouble == Double.parseDouble(doubleRepr));
            }

        }
        {
            final float testFloat = 3.141592f;
            String idString = pasc.toObjectIDString(new Float(testFloat),
                    Float.class, true);
            String floatRepr = idString.substring(prefixLength);
            assertTrue(testFloat == Float.parseFloat(floatRepr));
        }
        {
            final double testDouble = 3.141592;
            String idString = pasc.toObjectIDString(new Double(testDouble),
                    Double.class, true);
            String doubleRepr = idString.substring(prefixLength);
            assertTrue(testDouble == Double.parseDouble(doubleRepr));
        }
        {
            final float testFloat = 3.141592f;
            String idString = pasc.toObjectIDString(new Float(testFloat),
                    Float.class, false);
            assertFalse(idString.startsWith("literalreturn "));
        }
        {
            final double testDouble = 3.141592;
            String idString = pasc.toObjectIDString(new Double(testDouble),
                    Double.class, false);
            assertFalse(idString.startsWith("literalreturn "));
        }
    }

    // FIXME: How can we get the permissions to do this?
    // @Test
    // public void toIDStringJSObject() {
    // PluginAppletSecurityContext pasc = dummySecurityContext();
    //
    // long testReference = 1;
    // assertEquals("literalreturn 1", pasc.toObjectIDString(new JSObject(
    // testReference), JSObject.class, false));
    // }

    @Test
    public void toIDStringArbitraryObject() {
        PluginAppletSecurityContext pasc = dummySecurityContext();

        final Object testObject = new Object();
        String idString = pasc.toObjectIDString(testObject,
                testObject.getClass(), false);

        assertFalse(idString.startsWith("literalreturn"));
        assertFalse(idString.startsWith("jsobject"));
    }
}
