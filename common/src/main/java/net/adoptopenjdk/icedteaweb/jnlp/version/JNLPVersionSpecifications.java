package net.adoptopenjdk.icedteaweb.jnlp.version;

/**
 * This interface specifies regular expressions to define version ids and version strings
 * according to JSR-56, Appendix A.
 *
 * In case of mental regexp overflow https://regex101.com/ might help.
 */
public interface JNLPVersionSpecifications {

    // A version id is an exact version that is associated with a resource, such as a JAR file.
    //
    // The version-id used in this specification must conform to the following syntax:
    //
    // <pre>
    //     version-id ::= string ( separator string ) *
    //     string     ::= char ( char ) *
    //     char       ::= any ASCII character except a space, an ampersand, a separator, or a modifier.
    //     separator  ::= "." | "-" | "_"
    // </pre>
    //
    // This specification is implemented by the following regular expressions:
    //
    String REGEXP_MODIFIER = "[\\*\\+]";
    String REGEXP_SEPARATOR = "[._-]";
    String REGEXP_CHAR = "[^\\s&-._\\*\\+]";
    String REGEXP_STRING = REGEXP_CHAR + "+";
    String REGEXP_VERSION_ID = "(" + REGEXP_STRING + "(" + REGEXP_SEPARATOR + REGEXP_STRING + ")*)" + "(" + REGEXP_MODIFIER +"?)";

    // A version string is a list of version ranges separated by spaces. A version range is either a version-id,
    // a version-id followed by a star (*), a version-id followed by a plus sign (+) , or two version-ranges
    // combined using an ampersand (&amp;). The star means prefix match, the plus sign means this version or
    // greater, and the ampersand means the logical and-ing of the two version-ranges. The syntax of
    // version-strings is:
    //
    // <pre>
    //      version-string     ::=  version-range ( " " element) *
    //      version-range      ::=  simple-range ( "&amp;" simple-range) *
    //      simple-range       ::=  version-id | version-id modifier
    //      modifier           ::=  `+` | '*'
    // </pre>
    //
    // This specification is implemented by the following regular expressions:
    //
    String REGEXP_SPACE = "[\\s]";
    String REGEXP_SIMPLE_RANGE = "(" + REGEXP_VERSION_ID + ")";
    String REGEXP_VERSION_RANGE = "(" + REGEXP_SIMPLE_RANGE + "(&" + REGEXP_SIMPLE_RANGE + ")*)";
    String REGEXP_VERSION_STRING = "(" + REGEXP_VERSION_RANGE + "((" + REGEXP_SPACE + ")" + REGEXP_VERSION_RANGE + ")*)";

}
