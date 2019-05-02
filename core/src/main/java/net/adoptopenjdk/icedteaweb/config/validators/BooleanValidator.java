package net.adoptopenjdk.icedteaweb.config.validators;

import java.util.Locale;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks if a value is a valid boolean
 */
public class BooleanValidator implements ValueValidator {

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if(value instanceof Boolean) {
            return;
        }
        if (value instanceof String) {
            final String lower = ((String) value).toLowerCase(Locale.ENGLISH);
            if (lower.equals(Boolean.TRUE.toString())
                    || (lower.equals(Boolean.FALSE.toString()))) {
                return;
            }
        }
        throw new IllegalArgumentException("Not a boolean value: " + value);
    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleBooleanValues", Boolean.TRUE.toString(), Boolean.FALSE.toString());
    }
}
