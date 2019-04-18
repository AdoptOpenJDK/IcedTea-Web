package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import net.sourceforge.jnlp.Parser;

/**
 * The icon kind attribute defines how an icon should be used.
 *
 * Only one description element of each kind can be specified.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public enum IconKind {
    DEFAULT("default"),
    SELECTED("selected"),
    DISABLED("disabled"),
    ROLLOVER("rollover"),
    SPLASH("splash"),
    SHORTCUT("shortcut");

    private final String value;

    IconKind(final String value) {
        this.value = value;
    }

    /**
     * The attribute value name as used in the JSR-56 specification or the {@link Parser}.
     *
     * @return the attribute value name
     */
    public String getValue() {
        return value;
    }
}
