package net.sourceforge.jnlp.util;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

import static net.sourceforge.jnlp.util.LocaleUtils.*;

public class LocaleUtilsTest {
    Locale jvmLocale = new Locale("en", "CA", "utf8");

    @Test
    public void testCompareAll() {
        Locale[] correctAvailable = { new Locale("en", "CA", "utf8") };
        Assert.assertTrue("Entire locale should match but did not.",
                localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY_VARIANT));

        Locale[] mismatchedAvailable = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match variant but did.",
                localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY_VARIANT));
    }

    @Test
    public void testLangAndCountry() {
        Locale[] correctAvailable = { new Locale("en", "CA") };
        Assert.assertTrue("Should match language and country, ignoring variant but did not.",
                localeMatches(jvmLocale, correctAvailable, Match.LANG_COUNTRY));

        Locale[] mismatchedAvailable = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match country but did.",
                localeMatches(jvmLocale, mismatchedAvailable, Match.LANG_COUNTRY));

        Locale[] extraMismatched = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match because of extra variant but did.",
                localeMatches(jvmLocale, extraMismatched, Match.LANG_COUNTRY));
    }

    @Test
    public void testLangOnly() {
        Locale[] correctAvailable = { new Locale("en") };
        Assert.assertTrue("Should match only language but did not.",
                localeMatches(jvmLocale, correctAvailable, Match.LANG));

        Locale[] mismatchedAvailable = { new Locale("fr", "CA", "utf8") };
        Assert.assertFalse("Should not match language but did.",
                localeMatches(jvmLocale, mismatchedAvailable, Match.LANG));

        Locale[] extraMismatched = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match because of extra country but did.",
                localeMatches(jvmLocale, extraMismatched, Match.LANG));
    }

    @Test
    public void testNoLocalAvailable() {
        // TODO we should rethink this decision, should always hand in an empty array of locales
        Assert.assertTrue("Null locales should match but did not.",
                localeMatches(jvmLocale, null, Match.GENERALIZED));

        Locale[] emptyAvailable = {};
        Assert.assertTrue("Empty locales list should match but did not.",
                localeMatches(jvmLocale, emptyAvailable, Match.GENERALIZED));

        Locale[] mismatchAvailable = { new Locale("fr", "FR", "utf16") };
        Assert.assertFalse("Locales list should not match generalized case but did.",
                localeMatches(jvmLocale, mismatchAvailable, Match.GENERALIZED));
    }
}
