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
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SIMPLE_RANGE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.ASTERISK;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.NONE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.PLUS;

/**
 * A simple-range is either a version-id, a version-id followed by a star (*) or a version-id followed by a plus sign (+).
 * The star means prefix match, the plus sign means this version or greater.
 *
 * The syntax of version-strings is:
 *
 * <pre>
 *      simple-range       ::=  version-id | version-id modifier
 *      modifier           ::=  "+" | "*"
 * </pre>
 * <p>
 * See JSR-56 Specification, Appendix A.
 *
 * @see JNLPVersionPatterns
 */
class SimpleRange {

    private final VersionId versionId;
    private final VersionModifier modifier;

    private SimpleRange(VersionId versionId, VersionModifier modifier) {
        this.versionId = Assert.requireNonNull(versionId, "versionId");
        this.modifier = Assert.requireNonNull(modifier, "modifier");
    }

    /**
     * Checks whether this version-id represents a plain (exact) version without any postfix modifiers.
     * @return {@code true} if this version-id does not have any modifiers, {@code false} otherwise.
     */
    boolean isExactVersion() {
        return modifier == NONE;
    }

    boolean hasPrefixMatchModifier() {
        return modifier == ASTERISK;
    }

    boolean hasGreaterThanOrEqualMatchModifier() {
        return modifier == PLUS;
    }

    /**
     * Construct a simple-range by the given {@code simpleRange}.
     *
     * @param simpleRange a simple-range
     * @return a SimpleRange
     */
    public static SimpleRange fromString(String simpleRange) {
        Assert.requireNonNull(simpleRange, "simpleRange");

        if (!simpleRange.matches(REGEXP_SIMPLE_RANGE)) {
            throw new IllegalArgumentException(format("'%s' is not a valid simple-range according to JSR-56, Appendix A.", simpleRange));
        }

        final VersionId versionId = extractVersionId(simpleRange);
        final VersionModifier modifier = extractModifier(simpleRange);

        return new SimpleRange(versionId, modifier);
    }

    private static VersionModifier extractModifier(String simpleRange) {
        for (VersionModifier modifier : Arrays.asList(PLUS, ASTERISK)) {
            if (simpleRange.endsWith(modifier.symbol())) {
                return modifier;
            }
        }
        return NONE;
    }

    private static VersionId extractVersionId(String simpleRange) {
        final String exactId = simpleRange.replaceAll(REGEXP_MODIFIER + "$", "");
        return VersionId.fromString(exactId);
    }

    /**
     * Provides a string representation of this {@link SimpleRange}.
     *
     * @return a string representation of this simple-range
     */
    @Override
    public String toString() {
        return versionId.toString() + modifier.symbol();
    }

    boolean isEqualTo(final SimpleRange otherSimpleRange) {
        return equals(otherSimpleRange);
    }

    @Override
    public boolean equals(final Object otherSimpleRange) {
        if (otherSimpleRange == null || otherSimpleRange.getClass() != SimpleRange.class) {
            return false;
        }

        final SimpleRange other = (SimpleRange) otherSimpleRange;
        return versionId.equals(other.versionId) && modifier == other.modifier;
    }

    /**
     * Check if this simple-range contains the given {@code versionId}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-range contains {@code versionId}, {@code false} otherwise.
     */
    public boolean contains(final String otherVersionId) {
        return contains(VersionId.fromString(otherVersionId));
    }

    /**
     * Check if this simple-range contains the given {@code versionId}.
     *
     * @param otherVersionId a version-id
     * @return {@code true} if this version-range contains {@code versionId}, {@code false} otherwise.
     */
    public boolean contains(final VersionId otherVersionId) {
        Assert.requireNonNull(otherVersionId, "otherVersionId");

        if (isExactVersion()) {
            return versionId.isEqualTo(otherVersionId);
        }

        if (hasPrefixMatchModifier()) {
            return versionId.isPrefixMatchOf(otherVersionId);
        }

        if (hasGreaterThanOrEqualMatchModifier()) {
            return versionId.isLessThan(otherVersionId) || versionId.isEqualTo(otherVersionId);
        }

        throw new IllegalStateException("Simple range is neither exact, nor prefix, nor less");
    }

}
