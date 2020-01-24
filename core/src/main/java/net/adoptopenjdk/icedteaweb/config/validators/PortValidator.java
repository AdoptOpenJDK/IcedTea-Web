package net.adoptopenjdk.icedteaweb.config.validators;

/**
 * Checks that the value is a valid port.
 */
public class PortValidator extends RangedIntegerValidator {

    private static final int HIGHEST_PORT = 65535;
    private static final int LOWEST_PORT = 0;

    PortValidator() {
        super(LOWEST_PORT, HIGHEST_PORT);
    }

    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        if (value == null) {
            return; // null for a port tells ITW to use the default port
        }
        super.validate(value);
    }
}
