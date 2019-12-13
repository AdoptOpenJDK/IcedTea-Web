package net.sourceforge.jnlp.util;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

import static net.sourceforge.jnlp.util.LocaleUtils.*;

public class LocaleUtilsTest {
    Locale jvmLocale = new Locale("en", "CA", "utf8");

    @Test
    public void testCompareAll() {
        final Locale[] correctAvailable = { new Locale("en", "CA", "utf8") };
        Assert.assertTrue("Entire locale should match but did not.",
                localMatches(jvmLocale, Match.LANG_COUNTRY_VARIANT, correctAvailable));

        final Locale[] mismatchedAvailable = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match variant but did.",
                localMatches(jvmLocale, Match.LANG_COUNTRY_VARIANT, mismatchedAvailable));
    }

    @Test
    public void testLangAndCountry() {
        final Locale[] correctAvailable = { new Locale("en", "CA") };
        Assert.assertTrue("Should match language and country, ignoring variant but did not.",
                localMatches(jvmLocale, Match.LANG_COUNTRY, correctAvailable));

        final Locale[] mismatchedAvailable = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match country but did.",
                localMatches(jvmLocale, Match.LANG_COUNTRY, mismatchedAvailable));

        final Locale[] extraMismatched = { new Locale("en", "CA", "utf16") };
        Assert.assertFalse("Should not match because of extra variant but did.",
                localMatches(jvmLocale, Match.LANG_COUNTRY, extraMismatched));
    }

    @Test
    public void testLangOnly() {
        final Locale[] correctAvailable = { new Locale("en") };
        Assert.assertTrue("Should match only language but did not.",
                localMatches(jvmLocale, Match.LANG, correctAvailable));

        final Locale[] mismatchedAvailable = { new Locale("fr", "CA", "utf8") };
        Assert.assertFalse("Should not match language but did.",
                localMatches(jvmLocale, Match.LANG, mismatchedAvailable));

        final Locale[] extraMismatched = { new Locale("en", "EN") };
        Assert.assertFalse("Should not match because of extra country but did.",
                localMatches(jvmLocale, Match.LANG, extraMismatched));
    }

    @Test
    public void testNoLocalAvailable() {
        // TODO we should rethink this decision, should always hand in an empty array of locales
        Assert.assertTrue("Null locales should match but did not.",
                localMatches(jvmLocale, Match.GENERALIZED));

        final Locale[] emptyAvailable = {};
        Assert.assertTrue("Empty locales list should match but did not.",
                localMatches(jvmLocale, Match.GENERALIZED, emptyAvailable));

        final Locale[] mismatchAvailable = { new Locale("fr", "FR", "utf16") };
        Assert.assertFalse("Locales list should not match generalized case but did.",
                localMatches(jvmLocale, Match.GENERALIZED, mismatchAvailable));
    }
}
