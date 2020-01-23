package net.adoptopenjdk.icedteaweb.config.validators;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Checks that the value is a valid port.
 */
public class PortValidator implements ValueValidator {

    private static final int HIGHEST_PORT = 65535;
    private static final int LOWEST_PORT = 0;

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (value == null) {
            return;
        }
        try {
            final RangedIntegerValidator rangedIntegerValidator = new RangedIntegerValidator(LOWEST_PORT, HIGHEST_PORT);
            rangedIntegerValidator.validate(value);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Not a valid port", e);
        }
    }

    @Override
    public String getPossibleValues() {
        return R("VVPossibleRangedIntegerValues", 0, 65535);
    }

}
