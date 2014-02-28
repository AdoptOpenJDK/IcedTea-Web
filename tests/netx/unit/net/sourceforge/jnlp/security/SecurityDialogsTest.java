/*Copyright (C) 2013 Red Hat, Inc.

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

package net.sourceforge.jnlp.security;

import static net.sourceforge.jnlp.security.SecurityDialogs.getIntegerResponseAsAppletAction;
import static net.sourceforge.jnlp.security.SecurityDialogs.getIntegerResponseAsBoolean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static net.sourceforge.jnlp.security.SecurityDialogs.AppletAction.*;

import org.junit.Test;

public class SecurityDialogsTest {

    @Test
    public void testGetIntegerResponseAsBoolean() throws Exception {
        Object nullRef = null;
        Object objRef = new Object();
        Float floatRef = new Float(0.0f);
        Double doubleRef = new Double(0.0d);
        Long longRef = new Long(0);
        Byte byteRef = new Byte((byte)0);
        Short shortRef = new Short((short)0);
        String strRef = "0";
        Integer intRef1 = new Integer(5);
        Integer intRef2 = new Integer(0);

        assertFalse("null reference should have resulted in false", getIntegerResponseAsBoolean(nullRef));
        assertFalse("Object reference should have resulted in false", getIntegerResponseAsBoolean(objRef));
        assertFalse("Float reference should have resulted in false", getIntegerResponseAsBoolean(floatRef));
        assertFalse("Double reference should have resulted in false", getIntegerResponseAsBoolean(doubleRef));
        assertFalse("Long reference should have resulted in false", getIntegerResponseAsBoolean(longRef));
        assertFalse("Byte reference should have resulted in false", getIntegerResponseAsBoolean(byteRef));
        assertFalse("Short reference should have resulted in false", getIntegerResponseAsBoolean(shortRef));
        assertFalse("String reference should have resulted in false", getIntegerResponseAsBoolean(strRef));
        assertFalse("Non-0 Integer reference should have resulted in false", getIntegerResponseAsBoolean(intRef1));
        assertTrue("0 Integer reference should have resulted in true", getIntegerResponseAsBoolean(intRef2));
    }

    @Test
    public void testGetIntegerResponseAsAppletAction() throws Exception {
        Object nullRef = null;
        Object objRef = new Object();
        Float floatRef = new Float(0.0f);
        Double doubleRef = new Double(0.0d);
        Long longRef = new Long(0);
        Byte byteRef = new Byte((byte) 0);
        Short shortRef = new Short((short) 0);
        String strRef = "0";
        Integer intRef1 = new Integer(0);
        Integer intRef2 = new Integer(1);
        Integer intRef3 = new Integer(2);
        Integer intRef4 = new Integer(3);

        assertEquals("null reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(nullRef), CANCEL);
        assertEquals("Object reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(objRef), CANCEL);
        assertEquals("Float reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(floatRef), CANCEL);
        assertEquals("Double reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(doubleRef), CANCEL);
        assertEquals("Long reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(longRef), CANCEL);
        assertEquals("Byte reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(byteRef), CANCEL);
        assertEquals("Short reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(shortRef), CANCEL);
        assertEquals("String reference should have resulted in CANCEL", getIntegerResponseAsAppletAction(strRef), CANCEL);
        assertEquals("Integer reference 0 should have resulted in RUN", getIntegerResponseAsAppletAction(intRef1), RUN);
        assertEquals("Integer reference 1 should have resulted in SANDBOX", getIntegerResponseAsAppletAction(intRef2), SANDBOX);
        assertEquals("Integer reference 2 should have resulted in CANCEL", getIntegerResponseAsAppletAction(intRef3), CANCEL);
        assertEquals("Integer reference 3 should have resulted in CANCEL", getIntegerResponseAsAppletAction(intRef4), CANCEL);
    }
}
