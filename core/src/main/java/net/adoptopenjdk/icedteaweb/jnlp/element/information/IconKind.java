package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import java.util.Arrays;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.Parser;

/**
 * The icon kind attribute defines how an icon should be used.
 * <p>
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

    public static IconKind fromString(String name) throws IllegalArgumentException {
        Assert.requireNonNull(name, "name");

        return Arrays.stream(IconKind.values()).filter(kind -> kind.value.equals(name)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No enum constant " + IconKind.class.getCanonicalName() + "." + name)
        );
    }
}
