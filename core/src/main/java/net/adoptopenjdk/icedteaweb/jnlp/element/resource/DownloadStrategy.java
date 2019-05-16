package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.sourceforge.jnlp.Parser;

/**
 * Used for the download attribute to control whether a resource is downloaded eagerly or lazily.
 *
 * @implSpec See <b>JSR-56, Section 4.4 Parts and Lazy Downloads</b>
 * for a detailed specification of this class.
 */
public enum DownloadStrategy {
    EAGER("eager"),
    LAZY("lazy");

    private String value;

    DownloadStrategy(final String value) {
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
