package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import net.sourceforge.jnlp.Parser;

/**
 * The kind attribute defines how a description should be used.
 *
 * Only one description element of each kind can be specified. A description element without a kind {@link #DEFAULT}
 * is used as a default value.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public enum DescriptionKind {
    /**
     * Use this DescriptionKind if the description of the application is going to appear in one row in a
     * list or a table.
     */
    ONE_LINE("one-line"),
    /**
     * Use this DescriptionKind if the description of the application is going to be displayed in a situation
     * where there is room for a paragraph.
     */
    SHORT("short"),
    /**
     * Use this DescriptionKind if the description of the application is intended to be used as a tooltip.
     */
    TOOLTIP("tooltip"),
    /**
     * Use this DescriptionKind if the description of the application does not specify a specific usage (Default).
     */
    DEFAULT("default");


    private final String value;

    DescriptionKind(final String value) {
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
