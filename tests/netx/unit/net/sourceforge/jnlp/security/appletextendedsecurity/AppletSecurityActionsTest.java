/*   Copyright (C) 2014 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppletSecurityActionsTest {

    @Test
    public void parseMultipleItemsCorrect() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(0));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(1));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(2));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(3));
        assertEquals(ExecuteAppletAction.SANDBOX, a1.getAction(4));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(5));
        assertEquals(ExecuteAppletAction.SANDBOX, a1.getAction(6));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(7));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(8));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(9));
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(10));
        assertEquals(11, a1.getActions().size());
    }

    @Test
    public void parseEmpty() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("");
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(0));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(10));
        assertEquals(0, a1.getActions().size());
    }

    @Test
    public void parseOkSetAndGetZero() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("");
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(0));
        assertEquals(0, a1.getActions().size());
        a1.setAction(0, ExecuteAppletAction.YES);
        assertEquals(ExecuteAppletAction.YES, a1.getAction(0));
        assertEquals(1, a1.getActions().size());
    }

    @Test
    public void parseOkSetAndGet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("s");
        assertEquals(ExecuteAppletAction.SANDBOX, a1.getAction(0));
        assertEquals(1, a1.getActions().size());
        a1.setAction(0, ExecuteAppletAction.NO);
        assertEquals(ExecuteAppletAction.NO, a1.getAction(0));
        assertEquals(1, a1.getActions().size());
        a1.setAction(1, ExecuteAppletAction.YES);
        assertEquals(ExecuteAppletAction.NO, a1.getAction(0));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(1));
        assertEquals(2, a1.getActions().size());
        a1.setAction(0, ExecuteAppletAction.NO);
        assertEquals(ExecuteAppletAction.NO, a1.getAction(0));
        assertEquals(2, a1.getActions().size());
        a1.setAction(4, ExecuteAppletAction.NEVER);
        assertEquals(ExecuteAppletAction.NO, a1.getAction(0));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(1));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(2));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(3));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(3));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(4));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(5));//default again
        assertEquals(5, a1.getActions().size());

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void parseNotOkGet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        a1.getAction(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void parseNotOkSet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        a1.setAction(-1, ExecuteAppletAction.NO);
    }

    @Test(expected = RuntimeException.class)
    public void parseMultipleItemsToSomeWrong() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("AQA");
    }

    @Test
    public void parseMultipleItemsFillMissing() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("AN");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(0));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(1));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(2));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(3));
        //note, getters do not increase length
        assertEquals(2, a1.getActions().size());
    }

    @Test
    public void parseMultipleItemsSpaceEnd() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANXs AAA");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(0));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(1));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(2));
        assertEquals(ExecuteAppletAction.SANDBOX, a1.getAction(3));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(4));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(5));
        assertEquals(ExecuteAppletAction.UNSET, a1.getAction(10));
        assertEquals(4, a1.getActions().size());
    }
    
    @Test
    public void testIterator() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANXs AAA");
        int i = 0;
        for (ExecuteAppletAction eaa : a1) {
            assertEquals(a1.getAction(i), eaa);
            i++;
        }
        assertEquals(a1.getRealCount(), i);
    }

}
