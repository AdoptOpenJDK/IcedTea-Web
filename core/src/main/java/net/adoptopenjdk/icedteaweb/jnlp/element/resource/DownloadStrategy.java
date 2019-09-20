package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.sourceforge.jnlp.Parser;

/**
 * Used for the download attribute to control whether a resource is downloaded eagerly or lazily.
 *
 * The value {@code progress} for the download attribute is a hint to the jnlp client that this jar
 * may contain the class defined by the progress-class attribute (see section 3.7.1).
 * This jar may be considered eager, and may be loaded first by the jnlp client so that the “progress-class”
 * found in it may be used to display the progress of the downloading of all other eager resources.
 *
 * @implSpec See <b>JSR-56, Section 4.4 Parts and Lazy Downloads</b>
 * for a detailed specification of this class.
 */
public enum DownloadStrategy {
    PROGRESS("progress"), // first eager - contains the managed application progress dialog.
    EAGER("eager"),
    LAZY("lazy"),

    ;

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
