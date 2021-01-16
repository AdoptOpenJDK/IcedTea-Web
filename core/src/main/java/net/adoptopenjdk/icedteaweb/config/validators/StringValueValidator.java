package net.adoptopenjdk.icedteaweb.config.validators;

import java.util.Arrays;

/**
 * Checks that the value is one of the acceptable String values
 */
public class StringValueValidator implements ValueValidator {

    private final String[] options;

    public StringValueValidator(final String[] acceptableOptions) {
        options = acceptableOptions;
    }

    @Override
    public void validate(final String value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Must not be null");
        }

        boolean found = false;
        for (String knownVal : options) {
            if (knownVal.equals(value)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Not a valid value: " + value);
        }
    }

    @Override
    public String getPossibleValues() {
        return Arrays.toString(options);
    }

}
