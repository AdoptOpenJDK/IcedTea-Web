/* MessagePropertiesTest.java
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

package net.sourceforge.jnlp.tools;

import java.util.Locale;
import net.sourceforge.jnlp.tools.MessageProperties;
import org.junit.Test;
import org.junit.Assert;

public class MessagePropertiesTest {

    private static final Locale locale_en = MessageProperties.SupportedLanguage.en.getLocale(),
            locale_cs = MessageProperties.SupportedLanguage.cs.getLocale(),
            locale_de = MessageProperties.SupportedLanguage.de.getLocale(),
            locale_pl = MessageProperties.SupportedLanguage.pl.getLocale();

    private void testMessageStringEquals(Locale locale, String key, String expected) {
        String message = MessageProperties.getMessage(locale, key);
        Assert.assertEquals(message, expected);
    }

    @Test
    public void testLocalization_en() throws Exception {
        testMessageStringEquals(locale_en, "Continue", "Do you want to continue?");
    }

    @Test
    public void testLocalization_cs() throws Exception {
        testMessageStringEquals(locale_cs, "Continue", "Chcete pokra\u010dovat?");
    }

    @Test
    public void testLocalization_de() throws Exception {
        testMessageStringEquals(locale_de, "Continue", "Soll fortgefahren werden?");
    }

    @Test
    public void testLocalization_pl() throws Exception {
        testMessageStringEquals(locale_pl, "Continue", "Czy chcesz kontynuowa\u0107?");
    }

    @Test
    public void testNonexistentLocalization() throws Exception {
        String message_en = MessageProperties.getMessage(locale_en, "Continue");
        String message_abcd = MessageProperties.getMessage(new Locale("abcd"), "Continue");
        Assert.assertEquals(message_en, message_abcd); // There is no abcd localization, should fall back to English
    }

    @Test
    public void testDefaultLocalization() throws Exception {
        String sysPropLang = System.getProperty("user.language");
        Locale sysPropLocale = new Locale(sysPropLang);

        Locale defaultLocale = Locale.getDefault();

        String sysPropMessage = MessageProperties.getMessage(sysPropLocale, "LThreadInterruptedInfo");
        String defaultMessage = MessageProperties.getMessage(defaultLocale, "LThreadInterruptedInfo");
        String implMessage = MessageProperties.getMessage("LThreadInterruptedInfo");

        Assert.assertEquals(sysPropMessage, implMessage);
        Assert.assertEquals(defaultMessage, implMessage);
    }

}
