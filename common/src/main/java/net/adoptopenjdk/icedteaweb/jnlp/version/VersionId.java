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

import static java.lang.String.format;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_MODIFIER;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_ID;

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
 *     modifier   ::=  `+` | '*'
 * </pre>
 *
 * See JSR-56 Specification, Appendix A.
 *
 * @see JNLPVersionPatterns
 */
public class VersionId {
    private static final String ZERO_ELEMENT = "0";

    private final String versionId;

    /**
     * A version-id can be described as a tuple of values. A version-id string is broken in parts for each
     * separator ('.', '-', or '_').
     * <p/>
     * For example, "1.3.0-rc2-w" becomes (1,3,0,rc2,w), and "1.2.2-001" becomes (1,2,2,001).
     */
    private final String[] tuple;

    private VersionId(String versionId) {
        this.versionId = versionId;
        this.tuple = versionId.replaceAll(REGEXP_MODIFIER + "$", "").split(REGEXP_SEPARATOR);
    }

    /**
     * Construct a version-id by the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return a version-id
     */
    public static VersionId fromString(String versionId) {
        Assert.requireNonNull(versionId, "versionId");

        if (!versionId.matches(REGEXP_VERSION_ID)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version id according to JSR-56, Appendix A.", versionId));
        }

        return new VersionId(versionId);
    }

    /**
     * Provides a string representation of this {@link VersionId}.
     *
     * @return a string representation of this version-id
     */
    @Override
    public String toString() {
        return versionId;
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

    @Override
    public boolean equals(final Object otherVersionId) {
        if (otherVersionId == null || otherVersionId.getClass() != VersionId.class) {
            return false;
        }
        final VersionId other = (VersionId) otherVersionId;

        final String[] tuple1 = asNormalizedTuple(other.tuple.length);
        final String[] tuple2 = other.asNormalizedTuple(tuple.length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object element1 = prepareForComparison(tuple1[i]);
            final Object element2 = prepareForComparison(tuple2[i]);

            if (!element1.equals(element2)) {
                return false;
            }
        }
        return true;
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
    boolean isPrefixMatchOf(VersionId otherVersionId) {
        final String[] tuple2 = otherVersionId.asNormalizedTuple(tuple.length);

        for (int i = 0; i < tuple.length; i++) {
            final Object element1 = prepareForComparison(tuple[i]);
            final Object element2 = prepareForComparison(tuple2[i]);

            if (!element1.equals(element2)) {
                return false; // no prefix matches als elements are different
            }
        }
        return true;
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
     * @param otherVersionRange a version-id
     * @return {@code true} if this version-id is greater than {@code otherVersionId}, {@code false} otherwise.
     */
    boolean isLessThan(VersionId otherVersionRange) {
        final String[] tuple1 = asNormalizedTuple(otherVersionRange.tuple.length);
        final String[] tuple2 = otherVersionRange.asNormalizedTuple(tuple.length);

        for (int i = 0; i < tuple1.length; i++) {
            final Object element1 = prepareForComparison(tuple1[i]);
            final Object element2 = prepareForComparison(tuple2[i]);

            if (element1.equals(element2)) {
                continue;
            }
            if (element1 instanceof Integer && element2 instanceof String) {
                return true;
            }
            if (element1 instanceof String && element2 instanceof Integer) {
                return false;
            }

            return ((Comparable)element1).compareTo(element2) < 0;
        }
        return false;
    }

    /**
     * Normalizes a tuple to compare two version-ids. The tuple is padded with 0 (zero element) entries at the end.
     * For example, to compare the tuples T1(1, 4) and T2(1, 4, 1) you need to normalize T2 to (1, 4, 0) first.
     *
     * NOTE: Package visible for testing - do not use this method outside of VersionId.
     *
     * @param normalizationLength the tuple length to normalize to
     * @return the normalized tuple
     */
    String[] asNormalizedTuple(final int normalizationLength) {
        if (tuple.length < normalizationLength) {
            String[] normalizedTuple = new String[normalizationLength];
            System.arraycopy(tuple, 0, normalizedTuple, 0, tuple.length);
            Arrays.fill(normalizedTuple, tuple.length, normalizedTuple.length, ZERO_ELEMENT);
            return normalizedTuple;
        }
        else {
            return Arrays.copyOf(tuple, tuple.length);
        }
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
    private static Object prepareForComparison(final String tupleValue) {
        try {
            return Integer.valueOf(tupleValue); // numeric, so return as integer
        }
        catch (NumberFormatException ex) {
            return tupleValue; // not numeric, as not parsable as Java int, so return as string
        }
    }

    /**
     * Only used for testing
     */
    String[] asTuple() {
        return tuple;
    }
}
