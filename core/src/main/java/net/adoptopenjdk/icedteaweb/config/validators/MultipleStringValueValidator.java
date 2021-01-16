package net.adoptopenjdk.icedteaweb.config.validators;

import java.util.Arrays;
import java.util.List;

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
    public void validate(final String value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Must not be null");
        }

        boolean found = false;
        for (final String knownVal : singleOptions) {
            if (knownVal.equals(value)) {
                found = true;
                break;
            }
        }

        if (!found) {
            final List<String> possibleCombo = ValidatorUtils.splitCombination(value);
            for (final String val : possibleCombo) {
                if (comboOptionsContains(val)) {
                    found = true;
                } else {
                    throw new IllegalArgumentException("Invalid value '" + val + "' in combo");
                }
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Invalid value found: '" + value + "'");
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
