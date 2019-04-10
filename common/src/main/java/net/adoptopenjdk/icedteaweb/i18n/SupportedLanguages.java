package net.adoptopenjdk.icedteaweb.i18n;

import java.util.Locale;

public enum SupportedLanguages {
    ENGLISH("en"),
    CZECH("cs"),
    GERMAN("de"),
    POLISH("pl");

    private Locale locale;

    SupportedLanguages(String language) {
        try {
            this.locale = new Locale(language);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unknown language.");
        }
    }

    public Locale getLocale() {
        return this.locale;
    }
}
