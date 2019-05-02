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
    public void validate(final Object value) throws IllegalArgumentException {

        long actualValue = 0;
        try {
            if (value instanceof String) {
                actualValue = Long.valueOf((String) value);
            } else if (value instanceof Integer) {
                actualValue = (Integer) value;
            } else if (value instanceof Long) {
                actualValue = (Long) value;
            } else {
                throw new IllegalArgumentException("Must be an integer");
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Must be an integer", e);

        }

        if (actualValue < low || actualValue > high) {
            throw new IllegalArgumentException("Not in range from " + low + " to " + high);
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleRangedIntegerValues", low, high);
    }

}
