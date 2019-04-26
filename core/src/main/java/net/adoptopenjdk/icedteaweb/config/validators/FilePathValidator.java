package net.adoptopenjdk.icedteaweb.config.validators;

import java.io.File;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks if a value is a valid file path (not a valid file!). The actual
 * file may or may not exist
 */
//package private for testing purposes
public class FilePathValidator implements ValueValidator {

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (value == null) {
            return;
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Value should be string!");
        }

        final String possibleFile = (String) value;

        if (!new File(possibleFile).isAbsolute()) {
            throw new IllegalArgumentException("File must be absolute");
        }

    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleFileValues");
    }

}
