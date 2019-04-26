package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;

import java.util.Arrays;

/**
 * Checks that the value is one of the acceptable single String values
 * or an acceptable combination of String values
 */
public class MultipleStringValueValidator implements ValueValidator {

    private final String[] singleOptions;

    private final String[] comboOptions;

    public MultipleStringValueValidator(final String[] singleOptions, final String[] comboOptions) {
        this.singleOptions = singleOptions;
        this.comboOptions = comboOptions;
    }

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Must be a string");
        }

        final String stringVal = (String) value;
        boolean found = false;
        for (final String knownVal : singleOptions) {
            if (knownVal.equals(stringVal)) {
                found = true;
                break;
            }
        }

        if (!found) {
            final String[] possibleCombo = ValidatorUtils.splitCombination(stringVal);
            for (final String val : possibleCombo) {
                if (comboOptionsContains(val)) {
                    found = true;
                } else {
                    throw new IllegalArgumentException(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE);
                }
            }
        }

        if (!found) {
            throw new IllegalArgumentException(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE);
        }
    }

    private boolean comboOptionsContains(final String possibleVal) {
        for (final String value : comboOptions) {
            if (value.equals(possibleVal)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPossibleValues() {
        return "(Values that can be used alone only): " + Arrays.toString(singleOptions) +
                " (Values that can be used in combination separated by the delimiter \""
                + ValidatorUtils.DELIMITER + "\" with no space expected ): " + Arrays.toString(comboOptions);
    }

}
