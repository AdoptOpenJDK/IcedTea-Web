package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.StringUtils;

import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks that the value is one of the acceptable String values
 */
public class NotBlankValidator implements ValueValidator {

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Must be a string");
        }

        if (StringUtils.isBlank((String) value)) {
            throw new IllegalArgumentException("Must not be blank");
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVAnyNonBlankString");
    }

}
