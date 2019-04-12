/* 
 Copyright (C) 2009 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.ui.swing.dialogresults;

import org.junit.Assert;
import org.junit.Test;

public class YesNoSandboxTest {

    @Test
    public void yesReadWrite() {
        YesNoSandbox y = YesNoSandbox.yes();
        String s = y.writeValue();
        YesNoSandbox yy = YesNoSandbox.readValue(s);
        Assert.assertEquals(y.getValue(), yy.getValue());
        Assert.assertTrue(y.toBoolean());
    }

    @Test
    public void noReadWrite() {
        YesNoSandbox y = YesNoSandbox.no();
        String s = y.writeValue();
        YesNoSandbox yy = YesNoSandbox.readValue(s);
        Assert.assertEquals(y.getValue(), yy.getValue());
        Assert.assertFalse(y.toBoolean());

    }

    @Test
    public void sandboxReadWrite() {
        YesNoSandbox y = YesNoSandbox.sandbox();
        String s = y.writeValue();
        YesNoSandbox yy = YesNoSandbox.readValue(s);
        Assert.assertEquals(y.getValue(), yy.getValue());
        Assert.assertTrue(y.toBoolean());
    }

    @Test(expected = Exception.class)
    public void cancelRead() {
        YesNoSandbox y = YesNoSandbox.readValue(Primitive.CANCEL.name());
    }

    @Test(expected = Exception.class)
    public void nonsenseRead() {
        YesNoSandbox y = YesNoSandbox.readValue("blah");
    }
   

    public void noRead() {
        YesNoSandbox y = YesNoSandbox.readValue(Primitive.NO.name());
        Assert.assertFalse(y.toBoolean());
    }

    public void yesRead() {
        YesNoSandbox y = YesNoSandbox.readValue(Primitive.YES.name());
        Assert.assertTrue(y.toBoolean());
    }
    
    public void sandboxRead() {
        YesNoSandbox y = YesNoSandbox.readValue(Primitive.SANDBOX.name());
        Assert.assertTrue(y.toBoolean());
    }
}
