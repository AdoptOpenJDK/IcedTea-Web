package net.adoptopenjdk.icedteaweb.ui.swing.dialogresults;

import org.junit.Assert;
import org.junit.Test;

/* 
 Copyright (C) 2008 Red Hat, Inc.

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
public class AccessWarningPaneComplexReturnTest {

    @Test
    public void AccessWarningPaneComplexReturnTestReadWrite1() {
        AccessWarningPaneComplexReturn aw1 = new AccessWarningPaneComplexReturn(true);
        Assert.assertEquals(aw1.getRegularReturn().getValue(), Primitive.YES);
        Assert.assertEquals(aw1.getRegularReturn().getValue(), new AccessWarningPaneComplexReturn(Primitive.YES).getRegularReturn().getValue());
        String s1 = aw1.writeValue();
        AccessWarningPaneComplexReturn aw11 = AccessWarningPaneComplexReturn.readValue(s1);
        Assert.assertEquals(aw1.getRegularReturn().getValue(), aw11.getRegularReturn().getValue());
        Assert.assertEquals(aw1.getDesktop(), aw11.getDesktop());
        Assert.assertEquals(aw1.getMenu(), aw11.getMenu());
        Assert.assertEquals(null, aw11.getDesktop());
        Assert.assertEquals(null, aw11.getMenu());
    }

    public void AccessWarningPaneComplexReturnTestReadWrite2() {
        AccessWarningPaneComplexReturn aw2 = new AccessWarningPaneComplexReturn(false);
        Assert.assertEquals(aw2.getRegularReturn().getValue(), Primitive.NO);
        Assert.assertEquals(aw2.getRegularReturn().getValue(), new AccessWarningPaneComplexReturn(Primitive.NO).getRegularReturn().getValue());
        String s2 = aw2.writeValue();
        AccessWarningPaneComplexReturn aw22 = AccessWarningPaneComplexReturn.readValue(s2);
        Assert.assertEquals(aw2.getRegularReturn().getValue(), aw22.getRegularReturn().getValue());
        Assert.assertEquals(aw2.getDesktop(), aw22.getDesktop());
        Assert.assertEquals(aw2.getMenu(), aw22.getMenu());
        Assert.assertEquals(null, aw22.getDesktop());
        Assert.assertEquals(null, aw22.getMenu());

    }
    
      @Test(expected = Exception.class)
    public void AccessWarningPaneComplexReturnTestReadWriteBad1() {
        AccessWarningPaneComplexReturn accessWarningPaneComplexReturn = new AccessWarningPaneComplexReturn(Primitive.CANCEL);
}
    @Test(expected = Exception.class)
    public void AccessWarningPaneComplexReturnTestReadWriteBad2() {
        AccessWarningPaneComplexReturn accessWarningPaneComplexReturn = new AccessWarningPaneComplexReturn(Primitive.SANDBOX);
}
    
    @Test
    public void AccessWarningPaneComplexReturnTestReadWrite3() {
        AccessWarningPaneComplexReturn aw1 = new AccessWarningPaneComplexReturn(true);
        aw1.setDesktop(new AccessWarningPaneComplexReturn.ShortcutResult(true));
        aw1.setMenu(new AccessWarningPaneComplexReturn.ShortcutResult(false));
        String s1 = aw1.writeValue();
        AccessWarningPaneComplexReturn aw11 = AccessWarningPaneComplexReturn.readValue(s1);
        Assert.assertEquals(aw1.getRegularReturn().getValue(), aw11.getRegularReturn().getValue());
        Assert.assertEquals(aw1.getDesktop(), aw11.getDesktop());
        Assert.assertEquals(aw1.getMenu(), aw11.getMenu());
        Assert.assertNotEquals(null, aw11.getDesktop());
        Assert.assertNotEquals(null, aw11.getMenu());
        Assert.assertEquals(true, aw11.getDesktop().isCreate());
        Assert.assertEquals(false, aw11.getMenu().isCreate());
    }
    
     @Test
    public void AccessWarningPaneComplexReturnTestReadWrite4() {
        AccessWarningPaneComplexReturn aw1 = new AccessWarningPaneComplexReturn(true);
        aw1.setDesktop(new AccessWarningPaneComplexReturn.ShortcutResult("b1", true, AccessWarningPaneComplexReturn.Shortcut.BROWSER, false));
        aw1.setMenu(new AccessWarningPaneComplexReturn.ShortcutResult("b2",false, AccessWarningPaneComplexReturn.Shortcut.JAVAWS_HTML, true));
        String s1 = aw1.writeValue();
        AccessWarningPaneComplexReturn aw11 = AccessWarningPaneComplexReturn.readValue(s1);
        Assert.assertEquals(aw1.getRegularReturn().getValue(), aw11.getRegularReturn().getValue());
        Assert.assertEquals(aw1.getDesktop(), aw11.getDesktop());
        Assert.assertEquals(aw1.getMenu(), aw11.getMenu());
        Assert.assertNotEquals(null, aw11.getDesktop());
        Assert.assertNotEquals(null, aw11.getMenu());
        Assert.assertEquals(false, aw11.getDesktop().isCreate());
        Assert.assertEquals(true, aw11.getMenu().isCreate());
        Assert.assertEquals("b1", aw11.getDesktop().getBrowser());
        Assert.assertEquals("b2", aw11.getMenu().getBrowser());
        Assert.assertEquals(AccessWarningPaneComplexReturn.Shortcut.BROWSER, aw11.getDesktop().getShortcutType());
        Assert.assertEquals(AccessWarningPaneComplexReturn.Shortcut.JAVAWS_HTML, aw11.getMenu().getShortcutType());
        Assert.assertEquals(true, aw11.getDesktop().isFixHref());
        Assert.assertEquals(false, aw11.getMenu().isFixHref());
    }
}
