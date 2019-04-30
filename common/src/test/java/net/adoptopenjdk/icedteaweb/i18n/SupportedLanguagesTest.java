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

package net.adoptopenjdk.icedteaweb.i18n;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_LANGUAGE;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.DEFAULT_RESOURCE_BUNDLE_BASE_NAME;

public class SupportedLanguagesTest {
    private static final Locale locale_en = SupportedLanguages.ENGLISH.getLocale();
    private static final Locale locale_cs = SupportedLanguages.CZECH.getLocale();
    private static final Locale locale_de = SupportedLanguages.GERMAN.getLocale();
    private static final Locale locale_pl = SupportedLanguages.POLISH.getLocale();

    @Before
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
    }

    private void testMessageStringEquals(Locale locale, String key, String expected) {
        final String message = getMessage(locale, key);
        Assert.assertEquals(message, expected);
    }

    @Test
    @Ignore // only works if 'en' is fallback - fails on german system
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
    @Ignore // only works if 'en' is fallback - fails on german system
    public void testNonexistentLocalization() throws Exception {
        final String message_en = getMessage(locale_en, "Continue");
        final String message_abcd = getMessage(new Locale("abcd"), "Continue");
        Assert.assertEquals(message_en, message_abcd); // There is no abcd localization, should fall back to English
    }

    @Test
    @Ignore
    public void testDefaultLocalization() throws Exception {
        final String sysPropLang = System.getProperty(USER_LANGUAGE);
        final Locale sysPropLocale = new Locale(sysPropLang);

        final Locale defaultLocale = Locale.getDefault();

        final String sysPropMessage = getMessage(sysPropLocale, "LThreadInterruptedInfo");
        final String defaultMessage = getMessage(defaultLocale, "LThreadInterruptedInfo");
        final String implMessage = getMessage("LThreadInterruptedInfo");

        Assert.assertEquals(sysPropMessage, implMessage);
        Assert.assertEquals(defaultMessage, implMessage);
    }


    /**
     * Same as {@link #getMessage(Locale, String)}, using the current default Locale
     */
    private String getMessage(final String key) {
        return getMessage(Locale.getDefault(), key);
    }

    /**
     * Retrieve a localized message from resource file
     * @param locale the localization of Messages.properties to search
     * @param key
     * @return the message corresponding to the given key from the specified localization
     *
     * can throw wrapped IOException if the specified Messages localization is unavailable
     */
    private String getMessage(final Locale locale, final String key) {
        final ResourceBundle bundle = PropertyResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, locale);
        return bundle.getString(key);
    }

}
