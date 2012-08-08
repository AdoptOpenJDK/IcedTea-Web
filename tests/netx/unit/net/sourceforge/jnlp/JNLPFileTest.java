/* JNLPFileTest.java
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

package net.sourceforge.jnlp;

import java.util.Locale;

import net.sourceforge.jnlp.JNLPFile.Match;
import net.sourceforge.jnlp.mock.MockJNLPFile;

import org.junit.Assert;
import org.junit.Test;

public class JNLPFileTest {
    Locale jvmLocale = new Locale("en", "CA", "utf8");
    MockJNLPFile file = new MockJNLPFile(jvmLocale);

    @Test
    public void testCompareAll() {
        Locale[] correctAvailable = { new Locale("en", "CA", "utf8") };
        Assert.assertTrue("Entire locale should match but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY_VARIANT));

        Locale[] mismatchedAvailable = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match variant but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY_VARIANT));
    }

    @Test
    public void testLangAndCountry() {
        Locale[] correctAvailable = { new Locale("en", "CA") };
        Assert.assertTrue("Should match language and country, ignoring variant but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY));

        Locale[] mismatchedAvailable = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match country but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY));

        Locale[] extraMismatched = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match because of extra variant but did.",
                file.localeMatches(jvmLocale, extraMismatched, Match.LANG_COUNTRY));
    }

    @Test
    public void testLangOnly() {
        Locale[] correctAvailable = { new Locale("en") };
        Assert.assertTrue("Should match only language but did not.",
                file.localeMatches(jvmLocale, correctAvailable, Match.LANG));

        Locale[] mismatchedAvailable = { new Locale("fr", "CA", "utf8") };
        Assert.assertFalse("Should not match language but did.",
                file.localeMatches(jvmLocale, mismatchedAvailable, Match.LANG));

        Locale[] extraMismatched = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match because of extra country but did.",
                file.localeMatches(jvmLocale, extraMismatched, Match.LANG));
    }

    @Test
    public void testNoLocalAvailable() {
        Assert.assertTrue("Null locales should match but did not.",
                file.localeMatches(jvmLocale, null, Match.GENERALIZED));

        Locale[] emptyAvailable = {};
        Assert.assertTrue("Empty locales list should match but did not.",
                file.localeMatches(jvmLocale, emptyAvailable, Match.GENERALIZED));

        Locale[] mismatchAvailable = { new Locale("fr", "FR", "utf16") };
        Assert.assertFalse("Locales list should not match generalized case but did.",
                file.localeMatches(jvmLocale, mismatchAvailable, Match.GENERALIZED));
    }
}
