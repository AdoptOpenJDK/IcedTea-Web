package net.adoptopenjdk.icedteaweb.config.validators;

import java.net.URL;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks that the value is a URL
 */
public class UrlValidator implements ValueValidator {

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (value == null) {
            return;
        }
        try {
            new URL((String) value);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Not a valid URL", e);
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleUrlValues");
    }

}
