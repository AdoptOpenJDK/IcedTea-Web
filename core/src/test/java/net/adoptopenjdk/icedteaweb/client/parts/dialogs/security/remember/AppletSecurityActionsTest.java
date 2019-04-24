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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.MatchingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppletSecurityActionsTest {

    //before 1.7 only those two were remembered. Using in legacy parsing tests
    public static final Class UATWA = UnsignedAppletTrustWarningPanel.class;
    public static final Class MACA = MatchingALACAttributePanel.class;

    @Test
    public void parseMultipleItemsCorrectLegacy() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(UATWA));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(MACA));
        assertEquals(2, a1.getActions().size());
    }

    @Test
    public void parseMultipleItemsCorrect() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("c1:A{};c2:N{};c3:y{};c4:n{};c8:n{};c9:y{};c10:N{};c11:A{};");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction("c1"));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction("c2"));
        assertEquals(ExecuteAppletAction.YES, a1.getAction("c3"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction("c4"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction("c8"));
        assertEquals(ExecuteAppletAction.YES, a1.getAction("c9"));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction("c10"));
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction("c11"));
        assertEquals(8, a1.getActions().size());
    }

    @Test
    public void checkToString() throws Exception {
        AppletSecurityActions as = new AppletSecurityActions();
        as.setAction("c1", new SavedRememberAction(ExecuteAppletAction.ALWAYS, ""));
        as.setAction("c2", new SavedRememberAction(ExecuteAppletAction.ALWAYS, null));
        as.setAction("c3", new SavedRememberAction(ExecuteAppletAction.ALWAYS, "item"));
        String saveString = as.toShortString();
        //order is not guaranteed
        Assert.assertTrue(saveString.contains("c3:A{item};"));
        Assert.assertTrue(saveString.contains("c1:A{};"));
        Assert.assertTrue(saveString.contains("c2:A{};"));
    }

    @Test
    public void checkToStringIsParseableBack() throws Exception {
        AppletSecurityActions as = new AppletSecurityActions();
        as.setAction("c1", new SavedRememberAction(ExecuteAppletAction.ALWAYS, ""));
        as.setAction("c2", new SavedRememberAction(ExecuteAppletAction.NEVER, null));
        as.setAction("c3", new SavedRememberAction(ExecuteAppletAction.YES, "item"));
        as.setAction("c2", new SavedRememberAction(ExecuteAppletAction.NO, "item2"));
        as.setAction("c3", new SavedRememberAction(ExecuteAppletAction.NO, null));
        Assert.assertTrue(as.getRealCount() == 3);
        String saveString = as.toShortString();
        AppletSecurityActions a2 = AppletSecurityActions.fromString(saveString);
        Assert.assertTrue(as.getRealCount() == a2.getRealCount());
        SavedRememberAction c1 = a2.getActionEntry("c1");
        Assert.assertTrue(c1.getSavedValue() == null  || c1.getSavedValue().equals(""));
        Assert.assertTrue(c1.getAction().equals(ExecuteAppletAction.ALWAYS));
        SavedRememberAction c2 = a2.getActionEntry("c2");
        Assert.assertTrue(c2.getSavedValue().equals("item2"));
        Assert.assertTrue(c2.getAction().equals(ExecuteAppletAction.NO));
        SavedRememberAction c3 = a2.getActionEntry("c3");
        Assert.assertTrue(c3.getSavedValue() == null || c3.getSavedValue().equals("")); //not yet  decided
        Assert.assertTrue(c3.getAction().equals(ExecuteAppletAction.NO));
    }

    @Test
    public void parseEmpty() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("");
        assertEquals(null, a1.getAction(UATWA));
        assertEquals(null, a1.getAction(MACA));
        assertEquals(0, a1.getActions().size());
    }

    @Test
    public void parseOkSetAndGetZero() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("");
        assertEquals(null, a1.getAction(MACA));
        assertEquals(0, a1.getActions().size());
        a1.setAction(MACA, new SavedRememberAction(ExecuteAppletAction.YES, "aa"));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(MACA));
        assertEquals("aa",  a1.getActionEntry(MACA).getSavedValue());
        assertEquals(1, a1.getActions().size());
    }

    @Test
    public void parseOkSetAndGet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("A");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(UATWA));
        assertEquals(1, a1.getActions().size());
        a1.setAction(UATWA, new SavedRememberAction(ExecuteAppletAction.NO, "U1"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(UATWA));
        assertEquals(1, a1.getActions().size());
        a1.setAction(MACA, new SavedRememberAction(ExecuteAppletAction.YES,"M1"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(UATWA));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(MACA));
        assertEquals(2, a1.getActions().size());
        a1.setAction(UATWA, new SavedRememberAction(ExecuteAppletAction.NO, "U2"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(UATWA));
        assertEquals(2, a1.getActions().size());
        a1.setAction("Another", new SavedRememberAction(ExecuteAppletAction.NEVER,"A1"));
        assertEquals(ExecuteAppletAction.NO, a1.getAction(UATWA));
        assertEquals(ExecuteAppletAction.YES, a1.getAction(MACA));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction("Another"));
        assertEquals(3, a1.getActions().size());

    }

    @Test(expected = NullPointerException.class)
    public void parseNotOkGet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        a1.getAction((Class)null);
    }

    @Test(expected = NullPointerException.class)
    public void parseNotOkSet() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANynsXsnyNA");
        a1.setAction((Class)null, new SavedRememberAction(ExecuteAppletAction.NO, ""));
    }

    @Test(expected = RuntimeException.class)
    public void parseMultipleItemsToSomeWrong() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("AQA");
    }

    @Test
    public void parseMultipleItemsFillMissing() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("AN");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(UATWA));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(MACA));
        assertEquals(null, a1.getAction("unset"));
        assertEquals(null, a1.getAction("unset"));
        //note, getters do not increase length
        assertEquals(2, a1.getActions().size());
    }

    @Test
    public void parseMultipleItemsSpaceEnd() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANXs AAA");
        assertEquals(ExecuteAppletAction.ALWAYS, a1.getAction(UATWA));
        assertEquals(ExecuteAppletAction.NEVER, a1.getAction(MACA));
        assertEquals(null, a1.getAction("no1"));
        assertEquals(null, a1.getAction("no2"));
        assertEquals(null, a1.getAction("no3"));
        assertEquals(null, a1.getAction("no4"));
        assertEquals(null, a1.getAction("no5"));
        assertEquals(2, a1.getActions().size());
    }

    @Test
    public void testIterator() throws Exception {
        AppletSecurityActions a1 = AppletSecurityActions.fromString("ANXs AAA");
        int i = 0;
        for (SavedRememberAction eaa : a1) {
            Assert.assertTrue(a1.getActions().contains(eaa.getAction()));
            i++;
        }
        assertEquals(a1.getRealCount(), i);
    }

}
