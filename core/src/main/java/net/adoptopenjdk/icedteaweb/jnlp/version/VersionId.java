package net.adoptopenjdk.icedteaweb.jnlp.version;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.String.format;

/**
 * A version-id is an exact version that is associated with a resource, such as a JAR file.
 *
 * The version-id used in this specification must conform to the following syntax:
 *
 * <pre>
 *     version-id ::= string ( separator string ) *
 *     string     ::= char ( char ) *
 *     char       ::= any ASCII character except a space, an ampersand, a separator, or a modifier.
 *     separator  ::= "." | "-" | "_"
 * </pre>
 *
 * See JSR-56 Specification, Appendix A.
 */
public class VersionId {
    // separator characters
    static final String DOT = ".";
    static final String MINUS = "-";
    static final String UNDERSCORE = "_";

    // other illegal characters
    static final String AMPERSAND = "&";
    static final String SPACE = " ";

    // modifiers
    static final String PLUS = "+";
    static final String ASTERISK = "*";

    // regular expressions according to JSR-56, Appendix A
    // https://regex101.com/
    private static final String REGEXP_MODIFIER = "[\\*\\+]";
    static final String REGEXP_SEPARATOR = "[._-]";
    static final String REGEXP_CHAR = "[^\\s&-._\\*\\+]";
    static final String REGEXP_STRING = REGEXP_CHAR + "+";
    static final String REGEXP_VERSION_ID = "(" + REGEXP_STRING
            + "(" + REGEXP_SEPARATOR + REGEXP_STRING + ")*)"
            + "(" + REGEXP_MODIFIER +"?)";

    private static final String ZERO_ELEMENT = "0";

    private final String versionId;

    private VersionId(String versionId) {
        this.versionId = versionId;
    }

    boolean hasPrefixMatchModifier() {
        return versionId.endsWith(ASTERISK);
    }

    boolean hasGreaterThanOrEqualMatchModifier() {
        return versionId.endsWith(PLUS);
    }

    /**
     * Exact version-ids are just plain version-ids and must not have any postfix modifiers.
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    public boolean isExactVersionId() {
        return !(hasPrefixMatchModifier() || hasGreaterThanOrEqualMatchModifier());
    }

    /**
     * Construct a version-id by the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return a version-id
     */
    public static VersionId fromString(String versionId) {
        if (Objects.isNull(versionId) || !versionId.matches(REGEXP_VERSION_ID)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version id according to JSR-56, Appendix A.", versionId));
        }
        return new VersionId(versionId);
    }

    /**
     * Provides string representation of this version-id including modifier (if any).
     *
     * @return a string representation of this version-id
     */
    @Override
    public String toString() {
        return versionId;
    }

    /**
     * A version-id can be described as a tuple of values. A version-id string is broken in parts for each
     * separator ('.', '-', or '_'). Modifiers are ignored.
     * <p/>
     * For example, "1.3.0-rc2-w" becomes (1,3,0,rc2,w), and "1.2.2-001+" becomes (1,2,2,001).
     */
    String[] asTuple() {
        return versionId.replaceAll(REGEXP_MODIFIER + "$", "").split(VersionId.REGEXP_SEPARATOR);
    }

    /**
     * Normalizes a tuple to compare two version-ids. The tuple is padded with 0 (zero element) entries at the end.
     * For example, to compare the tuples T1(1, 4) and T2(1, 4, 1) you need to normalize T2 to (1, 4, 0) first.
     *
     * @param normalizationLength the tuple length to normalize to
     * @return the normalized tuple
     */
     String[] asNormalizedTuple(final int normalizationLength) {
        final String[] tuple = asTuple();
        if (tuple.length < normalizationLength) {
            String[] normalizedTuple = new String[normalizationLength];
            System.arraycopy(tuple, 0, normalizedTuple, 0, tuple.length);
            Arrays.fill(normalizedTuple, tuple.length, normalizedTuple.length, ZERO_ELEMENT);
            return normalizedTuple;
        }
        else {
            return tuple;
        }
    }

    @Override
    public boolean equals(final Object otherVersionId) {
        if (Objects.isNull(otherVersionId) || !(otherVersionId instanceof VersionId)) {
            return false;
        }
        VersionId other = (VersionId) otherVersionId;

        final String[] tuple1 = this.asNormalizedTuple(other.asTuple().length);
        final String[] tuple2 = other.asNormalizedTuple(this.asTuple().length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object tuple1Element = prepareForNormalizedComparision(tuple1[i]);
            final Object tuple2Element = prepareForNormalizedComparision(tuple2[i]);
            if (!tuple1Element.equals(tuple2Element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Match this version-id against {@code otherVersionId} considering
     * {@link #hasPrefixMatchModifier()} and {@link #hasGreaterThanOrEqualMatchModifier()}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id matches {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isMatchOf(final String otherVersionId) {
         return isMatchOf(VersionId.fromString(otherVersionId));
    }

    /**
     * Match this version-id against {@code otherVersionId} considering
     * {@link #hasPrefixMatchModifier()} and {@link #hasGreaterThanOrEqualMatchModifier()}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id matches {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isMatchOf(final VersionId otherVersionId) {
        if (this.hasPrefixMatchModifier())
            return this.isPrefixMatchOf(otherVersionId);
        else {
            if (this.hasGreaterThanOrEqualMatchModifier()) {
                return otherVersionId.isGreaterThanOrEqual(this);
            }
            else {
                return this.isExactMatchOf(otherVersionId);
            }
        }
    }

    /**
     * A is an exact isMatchOf of B if, when represented as normalized tuples, the elements of A are
     * the same as the elements of B.
     * <p/>
     * For example, given the above definition "1.2.2-004" will be an exact isMatchOf for "1.2.2.4",
     * and "1.3" is an exact isMatchOf of "1.3.0".
     * <p/>
     * See JSR-56 Specification, Appendix A.2 Exact Match.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is an exact isMatchOf of {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isExactMatchOf(VersionId otherVersionId) {
        return isEqualTo(otherVersionId);
    }

    /**
     * A is a prefix isMatchOf of B if, when represented as tuples, the elements of A are the same as the
     * first elements of B. The padding with 0 (zero element) entries ensures that B has at least as
     * many elements as A.
     * <p/>
     * For example, given the above definition "1.2.1" will be a prefix isMatchOf to "1.2.1-004", but not
     * to "1.2.0" or "1.2.10". The padding step ensures that "1.2.0.0" is a prefix of "1.2". Note that
     * prefix matching and ordering are distinct: "1.3" is greater than "1.2", and less than "1.4",
     * but not a prefix of either.
     * <p/>
     * See JSR-56 Specification, Appendix A.2 Prefix Match.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is a prefix isMatchOf of {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isPrefixMatchOf(VersionId otherVersionId) {
        final String[] tuple1 = this.asTuple();
        final String[] tuple2 = otherVersionId.asNormalizedTuple(tuple1.length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object tuple1Element = prepareForNormalizedComparision(tuple1[i]);
            final Object tuple2Element = prepareForNormalizedComparision(tuple2[i]);

            if (tuple1Element.equals(tuple2Element)) {
                // continue, as elements are equal
            }
            else {
                return false; // no prefix isMatchOf als elements are different
            }
        }
        return true;
    }

    /**
     * Compares whether this version-id is equal to the {@code otherVersionId}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this equals to {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isEqualTo(final VersionId otherVersionId) {
        return equals(otherVersionId);
    }

    /**
     * Compares whether this version-id is greater than {@code otherVersionId}.
     * <p/>
     * A is greater than B if, when represented as normalized tuples, there exists some element
     * of A which is greater than the corresponding element of B, and all earlier elements of A
     * are the same as in B (see JSR-56 Specification, Appendix A.1 Ordering).
     * <p/>
     * Two numeric elements are compared numerically. Two alphanumeric elements are compared
     * lexicographically according to the Unicode value of the individual characters. When one
     * element is numeric and the other is alphanumeric, the alphanumeric element is greater
     * than the numeric element. Numeric elements have lower precedence than non-numeric
     * elements.
     * <p/>
     * See JSR-56 Specification, Appendix A
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is greater than {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isGreaterThan(VersionId otherVersionId) {
        final String[] tuple1 = this.asNormalizedTuple(otherVersionId.asTuple().length);
        final String[] tuple2 = otherVersionId.asNormalizedTuple(this.asTuple().length);

        for (int i = 0; i < tuple1.length; i++) {
            final String tuple1Element = tuple1[i];
            final String tuple2Element = tuple2[i];
            final Object tuple1ElementObject = prepareForNormalizedComparision(tuple1Element);
            final Object tuple2ElementObject = prepareForNormalizedComparision(tuple2Element);
            if (tuple1ElementObject.equals(tuple2ElementObject)) {
                // continue, as long as the elements are equal
            }
            else {
                if (tuple1ElementObject instanceof Integer && tuple2ElementObject instanceof Integer) {
                    return (Integer) tuple1ElementObject > (Integer) tuple2ElementObject;
                }
                else {
                    return tuple1Element.compareTo(tuple2Element) > 0;
                }

            }
        }
        return false; // equal but not greater
    }

    /**
     * Compares whether this version-id is greater than {@code otherVersionId}.
     * <p/>
     * A is greater than B if, when represented as normalized tuples, there exists some element
     * of A which is greater than the corresponding element of B, and all earlier elements of A
     * are the same as in B (see JSR-56 Specification, Appendix A.1 Ordering).
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is greater than or equal to {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isGreaterThanOrEqual(VersionId otherVersionId) {
        return isGreaterThan(otherVersionId) || isEqualTo(otherVersionId);
    }

    /**
     * This method provides a tuple value as correct object (numeric or alphanumeric) for normalized comparison.
     * <p/>
     * Each element in a tuple is either numeric or alphanumeric. An elements is numeric if it can be parsed
     * as Java int, otherwise it is alphanumeric. Two numeric elements are compared numerically. Two
     * alphanumeric elements are compared lexicographically according to the Unicode value of the
     * individual characters. When one element is numeric and the other is alphanumeric, the alphanumeric
     * element is greater than the numeric element. Numeric elements have lower precedence than non-numeric
     * elements.
     * <p/>
     * See JSR-56 Specification, Appendix A
     *
     * @param tupleValue the raw tuple value
     * @return the tuple value suited for normalized comparision
     */
    private static Object prepareForNormalizedComparision(final String tupleValue) {
        if (tupleValue.length() > 0 && tupleValue.charAt(0) != '-') {
            try {
                return Integer.valueOf(tupleValue);
            }
            catch (NumberFormatException ex) { /* not numeric, as not parsable as Java int */ }
        }
        return tupleValue;
    }
}
