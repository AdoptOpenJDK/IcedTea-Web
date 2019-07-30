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
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.AMPERSAND;

/**
 * A version-id specifies the version that is associated with a resource, such as a JAR file.
 * A version-id can be postfixed with a '+' to indicate a greater-than-or-equal match,
 * a "*" to indicated a prefix match when used within a {@link VersionString}.
 * A version-id with no postfix indicate an exact match (plain version).
 * <p></p>
 * The version-id used in this specification must conform to the following syntax:
 *
 * <pre>
 *     version-range ::=  simple-range ( "&amp;" simple-range) *
 * </pre>
 * <p>
 * See JSR-56 Specification, Appendix A.
 *
 * @see JNLPVersionPatterns
 */
public class VersionRange {

    private final String versionRange;
    private final SimpleRange[] ranges;

    private VersionRange(String versionId, SimpleRange[] ranges) {
        this.versionRange = versionId;
        this.ranges = ranges;
    }

    /**
     * @return {@code true} if this version-id is a compound version range using an ampersand (&amp;), false otherwise
     */
    boolean isCompoundVersion() {
        return ranges.length > 1;
    }

    /**
     * Checks whether this version-id represents a plain (exact) version without any postfix modifiers.
     *
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    boolean isExactVersion() {
        return ranges.length == 1 && ranges[0].isExactVersion();
    }

    /**
     * Checks whether this version-id represents a plain (exact) version without any postfix modifiers.
     *
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    boolean hasPrefixMatchModifier() {
        return ranges.length == 1 && ranges[0].hasPrefixMatchModifier();
    }

    /**
     * Checks whether this version-id represents a plain (exact) version without any postfix modifiers.
     *
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    boolean hasGreaterThanOrEqualMatchModifier() {
        return ranges.length == 1 && ranges[0].hasGreaterThanOrEqualMatchModifier();
    }

    /**
     * Construct a version-id by the given {@code versionId}.
     *
     * @param versionRange a version-id
     * @return a version-id
     */
    public static VersionRange fromString(String versionRange) {
        Assert.requireNonNull(versionRange, "versionId");

        // a version range must not end with an ampersand
        if (versionRange.endsWith(AMPERSAND.symbol())) {
            throw new IllegalArgumentException(format("'%s' is not a valid compound version id according to JSR-56, Appendix A.", versionRange));
        }

        final SimpleRange[] ranges = Arrays.stream(versionRange.split(AMPERSAND.symbol()))
                .map(SimpleRange::fromString)
                .toArray(SimpleRange[]::new);

        return new VersionRange(versionRange, ranges);
    }

    /**
     * Check if {@code versionId} is a match with this version-range considering.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-range matches {@code versionId}, {@code false} otherwise.
     */
    public boolean matches(final String versionId) {
        return matches(VersionId.fromString(versionId));
    }

    /**
     * Check if {@code otherVersionId} is a match with this version-id considering.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-id matches {@code otherVersionId}, {@code false} otherwise.
     */
    public boolean matches(final VersionId versionId) {
        Assert.requireNonNull(versionId, "versionId");

        return Arrays.stream(ranges).allMatch(simpleRange -> simpleRange.matches(versionId));
    }

    boolean isEqualTo(final VersionRange otherVersionRange) {
        return equals(otherVersionRange);
    }

    @Override
    public boolean equals(final Object otherVersionRange) {
        if (otherVersionRange == null || otherVersionRange.getClass() != VersionRange.class) {
            return false;
        }
        final VersionRange other = (VersionRange) otherVersionRange;

        return Arrays.equals(ranges, other.ranges);
    }

    /**
     * Provides a string representation of this {@link VersionRange}.
     *
     * @return a string representation of this version-id
     */
    @Override
    public String toString() {
        return versionRange;
    }
}
