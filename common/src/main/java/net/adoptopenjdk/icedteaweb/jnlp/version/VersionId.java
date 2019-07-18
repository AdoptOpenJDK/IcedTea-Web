// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
package net.adoptopenjdk.icedteaweb.jnlp.version;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.String.format;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_MODIFIER;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_VERSION_ID;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.AMPERSAND;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.ASTERISK;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.PLUS;

/**
 * A version-id specifies the version that is associated with a resource, such as a JAR file.
 * A version-id can be postfixed with a '+' to indicate a greater-than-or-equal match,
 * a "*" to indicated a prefix match when used within a {@link VersionString}.
 * A version-id with no postfix indicate an exact match (plain version).
 * <p></p>
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
 *
 * @see JNLPVersionSpecifications
 */
public class VersionId {
    private static final String ZERO_ELEMENT = "0";

    private final String versionId;
    private final VersionId compoundVersionId;

    private VersionId(String versionId, VersionId compoundVersionId) {
        this.versionId = versionId;
        this.compoundVersionId = compoundVersionId;
    }

    public boolean hasPrefixMatchModifier() {
        return versionId.endsWith(ASTERISK.symbol());
    }

    public boolean hasGreaterThanOrEqualMatchModifier() {
        return versionId.endsWith(PLUS.symbol());
    }

    /**
     * @return {@code true} if this version-id is a compound version range using an ampersand (&amp;), false otherwise
     */
    public boolean isCompoundVersion() {
        return compoundVersionId != null;
    }

    /**
     * Checks whether this version-id represents a plain (exact) version without any postfix modifiers.
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    public boolean isExactVersion() {
        return !hasPrefixMatchModifier() && !hasGreaterThanOrEqualMatchModifier() && !isCompoundVersion();
    }

    /**
     * Construct a version-id by the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return a version-id
     */
    public static VersionId fromString(String versionId) {
        Assert.requireNonNull(versionId, "versionId");

        if (versionId.contains(AMPERSAND.symbol())) {
            // As ampersand is a special modifier for compound versions, we need to check the compound parts here
            final String[] compoundVersions = versionId.split(AMPERSAND.symbol());

            String malformedVersion = null;
            if (compoundVersions.length < 2) {
                malformedVersion = versionId;
            }
            else {
                if (!compoundVersions[0].matches(REGEXP_VERSION_ID)) {
                    malformedVersion = compoundVersions[0];
                }
                if (!compoundVersions[1].matches(REGEXP_VERSION_ID)) {
                    malformedVersion = compoundVersions[1];
                }
            }
            if (malformedVersion != null) {
                throw new IllegalArgumentException(format("'%s' is not a valid compound version id according to JSR-56, Appendix A.", malformedVersion));
            }

            return new VersionId(compoundVersions[0], VersionId.fromString(compoundVersions[1]));
        }
        else if (!versionId.matches(REGEXP_VERSION_ID)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version id according to JSR-56, Appendix A.", versionId));
        }

        return new VersionId(versionId, null);
    }

    /**
     * Provides a string representation of this {@link VersionId}.
     *
     * @return a string representation of this version-id
     */
    @Override
    public String toString() {
        return (compoundVersionId != null) ? versionId + AMPERSAND.symbol() + compoundVersionId.toString() : versionId;
    }

    /**
     * Provides a string representation of the exact version of this {@link VersionId} (no modifiers and compound versions).
     *
     * @return a string representation of this version-id
     */
    public String toExactString() {
        return versionId.replaceAll(REGEXP_MODIFIER + "$", "");
    }

    /**
     * A version-id can be described as a tuple of values. A version-id string is broken in parts for each
     * separator ('.', '-', or '_'). Modifiers are ignored.
     * <p/>
     * For example, "1.3.0-rc2-w" becomes (1,3,0,rc2,w), and "1.2.2-001+" becomes (1,2,2,001).
     */
    String[] asTuple() {
        return versionId.replaceAll(REGEXP_MODIFIER + "$", "").split(REGEXP_SEPARATOR);
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
        final VersionId other = (VersionId) otherVersionId;

        if (compoundVersionId != null) {
            return this.compoundVersionId.equals(other.compoundVersionId == null? other : other.compoundVersionId);
        }

        final String[] tuple1 = this.asNormalizedTuple(other.asTuple().length);
        final String[] tuple2 = other.asNormalizedTuple(this.asTuple().length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object tuple1Element = prepareForNormalizedComparison(tuple1[i]);
            final Object tuple2Element = prepareForNormalizedComparison(tuple2[i]);
            if (!tuple1Element.equals(tuple2Element)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if {@code otherVersionId} is a match with this version-id considering $
     * {@link #hasPrefixMatchModifier()} and {@link #hasGreaterThanOrEqualMatchModifier()}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id matches {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean matches(final String otherVersionId) {
        return matches(VersionId.fromString(otherVersionId));
    }

    /**
     * Check if {@code otherVersionId} is a match with this version-id considering
     * {@link #hasPrefixMatchModifier()} and {@link #hasGreaterThanOrEqualMatchModifier()}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id matches {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean matches(final VersionId otherVersionId) {
        Assert.requireNonNull(otherVersionId, "otherVersionId");

        if (compoundVersionId != null) {
            return compoundVersionId.matches(otherVersionId);
        }

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
     * A is an exact matches of B if, when represented as normalized tuples, the elements of A are
     * the same as the elements of B.
     * <p/>
     * For example, given the above definition "1.2.2-004" will be an exact matches for "1.2.2.4",
     * and "1.3" is an exact matches of "1.3.0".
     * <p/>
     * See JSR-56 Specification, Appendix A.2 Exact Match.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is an exact matches of {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isExactMatchOf(VersionId otherVersionId) {
        return isEqualTo(otherVersionId);
    }

    /**
     * A is a prefix matches of B if, when represented as tuples, the elements of A are the same as the
     * first elements of B. The padding with 0 (zero element) entries ensures that B has at least as
     * many elements as A.
     * <p/>
     * For example, given the above definition "1.2.1" will be a prefix matches to "1.2.1-004", but not
     * to "1.2.0" or "1.2.10". The padding step ensures that "1.2.0.0" is a prefix of "1.2". Note that
     * prefix matching and ordering are distinct: "1.3" is greater than "1.2", and less than "1.4",
     * but not a prefix of either.
     * <p/>
     * See JSR-56 Specification, Appendix A.2 Prefix Match.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-id is a prefix matches of {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean isPrefixMatchOf(VersionId otherVersionId) {
        if (compoundVersionId != null) {
            if (!compoundVersionId.isPrefixMatchOf(otherVersionId)) {
                return false;
            }
        }

        final String[] tuple1 = this.asTuple();
        final String[] tuple2 = otherVersionId.asNormalizedTuple(tuple1.length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object tuple1Element = prepareForNormalizedComparison(tuple1[i]);
            final Object tuple2Element = prepareForNormalizedComparison(tuple2[i]);

            if (!tuple1Element.equals(tuple2Element)) {
                return false; // no prefix matches als elements are different
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
        if (compoundVersionId != null) {
            return compoundVersionId.isGreaterThan(otherVersionId);
        }

        final String[] tuple1 = this.asNormalizedTuple(otherVersionId.asTuple().length);
        final String[] tuple2 = otherVersionId.asNormalizedTuple(this.asTuple().length);

        for (int i = 0; i < tuple1.length; i++) {
            final String tuple1Element = tuple1[i];
            final String tuple2Element = tuple2[i];
            final Object tuple1ElementObject = prepareForNormalizedComparison(tuple1Element);
            final Object tuple2ElementObject = prepareForNormalizedComparison(tuple2Element);
            if (!tuple1ElementObject.equals(tuple2ElementObject)) {
                if (tuple1ElementObject instanceof Integer && tuple2ElementObject instanceof Integer) {
                    return ((Integer) tuple1ElementObject > (Integer) tuple2ElementObject);
                }
                else {
                    return tuple1Element.compareTo(tuple2Element) > 0;
                }
            }
        }
        return false;
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
        final boolean greaterThan = isGreaterThan(otherVersionId);
        final boolean equalTo = isEqualTo(otherVersionId);

        return greaterThan || equalTo;
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
     * @return the tuple value suited for normalized comparison
     */
    private static Object prepareForNormalizedComparison(final String tupleValue) {
        if (tupleValue.length() > 0 && tupleValue.charAt(0) != '-') {
            try {
                return Integer.valueOf(tupleValue); // numeric, so return as integer
            }
            catch (NumberFormatException ex) {
                return tupleValue; // not numeric, as not parsable as Java int, so return as string
            }
        }
        return tupleValue;
    }
}
