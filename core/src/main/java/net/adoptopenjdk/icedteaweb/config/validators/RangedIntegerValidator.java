package net.adoptopenjdk.icedteaweb.config.validators;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks that the value is an Integer or Long (or a String representation
 * of one) that is within a desired range).
 */
public class RangedIntegerValidator implements ValueValidator {

    private final int low;

    private final int high;

    public RangedIntegerValidator(final int low, final int high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public void validate(final String value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Must not be null");
        }

        try {
            final long actualValue = Long.parseLong(value);
            if (actualValue < low || actualValue > high) {
                throw new IllegalArgumentException("Not in range from " + low + " to " + high);
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Must be an integer", e);
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleRangedIntegerValues", low, high);
    }

}
