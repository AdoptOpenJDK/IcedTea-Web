/* 
Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.config;

import org.junit.Assert;
import org.junit.Test;

public class BasicValueValidatorsTests {

    //decomposed for testing
    public static boolean canBeWindows(String s) {
        return s.toLowerCase().contains("windows");
    }

    /**
     * guess the OS of user, if legal, or windows
     * @return
     */
    public static boolean isOsWindows() {
        return canBeWindows(System.getProperty("os.name"));
    }
    private static final BasicValueValidators.FilePathValidator pv = new BasicValueValidators.FilePathValidator();
    private final String neverLegal = "aaa/bb/cc";
    private final String winLegal = "C:\\aaa\\bb\\cc";
    private final String linuxLegal = "/aaa/bb/cc";

    @Test
    public void testWindowsDetction() {
        Assert.assertTrue(canBeWindows("blah windows blah"));
        Assert.assertTrue(canBeWindows("blah Windows blah"));
        Assert.assertTrue(canBeWindows("  WINDOWS 7"));
        Assert.assertFalse(canBeWindows("blah windy miracle blah"));
        Assert.assertFalse(canBeWindows("blah wind blah"));
        Assert.assertTrue(canBeWindows("windows"));
        Assert.assertFalse(canBeWindows("linux"));
        Assert.assertFalse(canBeWindows("blah mac blah"));
        Assert.assertFalse(canBeWindows("blah solaris blah"));
    }

    @Test
    public void testLinuxAbsoluteFilePathValidator() {
        if (!isOsWindows()) {
            Exception ex = null;
            try {
                pv.validate(linuxLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex == null);

            ex = null;
            try {
                pv.validate(neverLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex instanceof IllegalArgumentException);


            ex = null;
            try {
                pv.validate(winLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testWindowsAbsoluteFilePathValidator() {
        if (isOsWindows()) {
            Exception ex = null;
            try {
                pv.validate(winLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex == null);

            ex = null;
            try {
                pv.validate(neverLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex instanceof IllegalArgumentException);


            ex = null;
            try {
                pv.validate(linuxLegal);
            } catch (Exception eex) {
                ex = eex;
            }
            Assert.assertTrue(ex instanceof IllegalArgumentException);
        }
    }
}
