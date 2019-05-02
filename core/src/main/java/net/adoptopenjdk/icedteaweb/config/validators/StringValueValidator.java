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
    public void validate(final Object value) throws IllegalArgumentException {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Must be a string");
        }

        String stringVal = (String) value;
        boolean found = false;
        for (String knownVal : options) {
            if (knownVal.equals(stringVal)) {
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
