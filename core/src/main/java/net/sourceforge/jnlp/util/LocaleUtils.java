package net.sourceforge.jnlp.util;

import java.util.Locale;
import java.util.Objects;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;

public class LocaleUtils {

    public static final String EMPTY_STRING = "";

    public enum Match { LANG_COUNTRY_VARIANT, LANG_COUNTRY, LANG, GENERALIZED }

    /**
     * Returns a {@link Locale} from a locale string. Each locale is specified by a language identifier,
     * possibly country identifier, and possibly a variant. The syntax is as follows:
     * <pre>
     * locale ::= language [ "_" country [ "_" variant ] ]
     * </pre>
     *
     * @param localeString the locale string
     * @return locale of document
     */
    public static Locale getLocale(final String localeString) throws ParseException {
        if (Objects.isNull(localeString) || localeString.length() < 2) {
            throw new ParseException("Locale attribute of information element contains illegal locale: " + localeString);
        }

        final String language = localeString.substring(0, 2);
        final String country = (localeString.length() < 5) ? EMPTY_STRING : localeString.substring(3, 5);
        final String variant = (localeString.length() > 7) ? localeString.substring(6) : EMPTY_STRING;

        return new Locale(language, country, variant);
    }

    /**
     * @deprecated use {@link #localMatches(Locale, Match, Locale[])}
     */
    public static boolean localeMatches(final Locale requested, final Locale[] available, final Match matchLevel) {
        return localMatches(requested, matchLevel, available == null ? new Locale[0] : available);
    }

    /**
     * Returns whether a locale is matched by one of more other
     * locales. Only the non-empty language, country, and variant
     * codes are compared; for example, a requested locale of
     * Locale("","","") would always return true.
     *
     * @param requested the requested locale
     * @param matchLevel the depth with which to match locales.
     * @param available the available locales
     * @return {@code true} if {@code requested} matches any of {@code available}, or if
     * {@code available} is empty or {@code null}.
     * @see Locale
     * @see Match
     */
    public static boolean localMatches(final Locale requested, final Match matchLevel, final Locale... available) {
        Assert.requireNonNull(requested, "requested");
        Assert.requireNonNull(matchLevel, "matchLevel");
        Assert.requireNonNull(available, "available");

        if (matchLevel == Match.GENERALIZED) {
            return available.length == 0;
        }

        String language = requested.getLanguage();
        String country = requested.getCountry();
        String variant = requested.getVariant();

        for (Locale locale : available) {
            switch (matchLevel) {
                case LANG:
                    if (!language.isEmpty()
                            && language.equals(locale.getLanguage())
                            && locale.getCountry().isEmpty()
                            && locale.getVariant().isEmpty()) {
                        return true;
                    }
                    break;
                case LANG_COUNTRY:
                    if (!language.isEmpty()
                            && language.equals(locale.getLanguage())
                            && !country.isEmpty()
                            && country.equals(locale.getCountry())
                            && locale.getVariant().isEmpty()) {
                        return true;
                    }
                    break;
                case LANG_COUNTRY_VARIANT:
                    if (language.equals(locale.getLanguage())
                            && country.equals(locale.getCountry())
                            && variant.equals(locale.getVariant())) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}
