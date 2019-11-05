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
import java.util.stream.Collectors;

import static java.lang.String.format;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_STRING;

/**
 * A version-string is a list of version-ranges separated by spaces. A version-range is either a version-id,
 * a version-id followed by a star (*), a version-id followed by a plus sign (+) , or two version-ranges
 * combined using an ampersand (&amp;). The star means prefix match, the plus sign means this version or
 * greater, and the ampersand means the logical and-ing of the two version-ranges.
 *
 * The syntax of version-strings is:
 *
 * <pre>
 *      version-string     ::=  version-range ( " " version-range) *
 *      version-range      ::=  simple-range ( "&amp;" simple-range) *
 *      simple-range       ::=  version-id | version-id modifier
 *      modifier           ::=  "+" | "*"
 * </pre>
 * <p>
 * See JSR-56 Specification, Appendix A.
 *
 * @see JNLPVersionPatterns
 */
public class VersionString {

    public static VersionString ANY_VERSION = fromString("0+");

    private final VersionRange[] versionRanges;

    private VersionString(final VersionRange[] versionRanges) {
        this.versionRanges = versionRanges;
    }

    /**
     * Construct a version-string by the given {@code versionString}.
     *
     * @param versionString a version-string
     * @return a versionString
     */
    public static VersionString fromString(final String versionString) {
        Assert.requireNonNull(versionString, "versionString");

        if (!versionString.matches(REGEXP_VERSION_STRING)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version-string according to JSR-56, Appendix A.", versionString));
        }

        final VersionRange[] ranges = Arrays.stream(versionString.split(REGEXP_SPACE))
                .map(VersionRange::fromString)
                .toArray(VersionRange[]::new);

        return new VersionString(ranges);
    }

    /**
     * @return {@code true} if this version-string consists of only a single version-range, {@code false} otherwise.
     */
    public boolean containsSingleVersionRange() {
        return versionRanges.length == 1;
    }

    /**
     * @return {@code true} if this version-string consists of a single version-range which is an exact match, {@code false} otherwise.
     */
    public boolean isExactVersion() {
        return versionRanges.length == 1 && versionRanges[0].isExactVersion();
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    public boolean contains(final String versionId) {
        return contains(VersionId.fromString(versionId));
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    public boolean contains(final VersionId versionId) {
        Assert.requireNonNull(versionId, "versionId");

        return Arrays.stream(versionRanges).anyMatch(range -> range.contains(versionId));
    }

    /**
     * Compares two version-ids in the context of this version-string.
     * This implementation follows the specification of a {@link java.util.Comparator}.
     *
     * If two or more version-id match the given version-string,
     * the JNLP Client should use the one matching the earlier version-range in the version-string.
     *
     * If two or more version-id match a given version-range,
     * the JNLP Client should use the one with the highest version-id.
     *
     * See JSR-56 Specification, Appendix A.
     *
     * @param versionId1 a version-id
     * @param versionId2 a version-id
     * @return a negative int if versionId1 is less than versionId2,
     *  a positive int if versionId1 is after than versionId2 or zero if they are equal.
     */
    int compare(final VersionId versionId1, final VersionId versionId2) {
        Assert.requireNonNull(versionId1, "versionId1");
        Assert.requireNonNull(versionId2, "versionId2");

        final int idxOfId1 = indexOfFirstRangeContaining(versionId1);
        final int idxOfId2 = indexOfFirstRangeContaining(versionId2);
        final int diff = idxOfId2 - idxOfId1;

        return diff != 0 ? diff : versionId1.compareTo(versionId2);
    }

    private int indexOfFirstRangeContaining(final VersionId versionId) {
        final int length = versionRanges.length;
        for (int i = 0; i < length; i++) {
            if (versionRanges[i].contains(versionId)) {
                return i;
            }
        }
        return length;
    }

    @Override
    public boolean equals(final Object otherVersionString) {
        if (otherVersionString == null || otherVersionString.getClass() != VersionString.class) {
            return false;
        }
        final VersionString other = (VersionString) otherVersionString;

        return Arrays.equals(versionRanges, other.versionRanges);
    }

    /**
     * Provides string representation of this version-string.
     *
     * @return a string representation of this version-string
     */
    @Override
    public String toString() {
        return Arrays.stream(versionRanges)
                .map(VersionRange::toString)
                .collect(Collectors.joining(" "));
    }
}
