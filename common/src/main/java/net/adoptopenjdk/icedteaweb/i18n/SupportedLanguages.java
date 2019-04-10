package net.adoptopenjdk.icedteaweb.i18n;

import java.util.Locale;

public enum SupportedLanguages {
    en("en"), cs("cs"), de("de"), pl("pl");
    private Locale locale;

    SupportedLanguages(String lang) {
        this.locale = new Locale(lang);
    }

    public Locale getLocale() {
        return this.locale;
    }
}
