package net.adoptopenjdk.icedteaweb.jnlp.element.information;

/**
 * A short statement about the application. Description elements are optional.
 * The kind attribute defines how the description should be used. All descriptions
 * contain plain text. No formatting, such as HTML tags is supported.
 *
 * The kind attribute for the description element indicates the use of a description
 * element. The values are: i) one-line, for a one-line description, ii) short,
 * for a one paragraph description, and iii) tooltip, for a tool-tip description.
 *
 * @see DescriptionKind
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public class DescriptionDesc {
    public static final String DESCRIPTION_ELEMENT = "description";
    public static final String KIND_ATTRIBUTE = "kind";

    // TODO currently used to hold the constants, us this desc to fully represent the description elements
}
