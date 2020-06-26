package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.StringUtils;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks that the value is not null or blank
 */
public class NotBlankValidator implements ValueValidator {

    @Override
    public void validate(final String value) throws IllegalArgumentException {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Must not be blank");
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVAnyNonBlankString");
    }

}
