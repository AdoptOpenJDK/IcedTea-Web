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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_STRING;

/**
 * A version string is a list of version ranges separated by spaces. A version range is either a version-id,
 * a version-id followed by a star (*), a version-id followed by a plus sign (+) , or two version-ranges
 * combined using an ampersand (&amp;). The star means prefix match, the plus sign means this version or
 * greater, and the ampersand means the logical and-ing of the two version-ranges. The syntax of
 * version-strings is:
 *
 * <pre>
 *      version-string     ::=  version-range ( " " element) *
 *      version-range      ::=  simple-range ( "&amp;" simple-range) *
 *      simple-range       ::=  version-id | version-id modifier
 *      modifier           ::=  `+` | '*'
 * </pre>
 * <p>
 * See JSR-56 Specification, Appendix A.
 *
 * @see JNLPVersionPatterns
 */
public class VersionString {

    private final VersionRange[] versionRanges;

    private VersionString(VersionRange[] versionRanges) {
        this.versionRanges = versionRanges;
    }

    /**
     * Construct a version-string by the given {@code versionString}.
     *
     * @param versionString a version-string
     * @return a versionString
     */
    public static VersionString fromString(final String versionString) {
        if (Objects.isNull(versionString) || !versionString.matches(REGEXP_VERSION_STRING)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version string according to JSR-56, Appendix A.", versionString));
        }

        final VersionRange[] versionRanges = Arrays.stream(versionString.split(REGEXP_SPACE))
                .map(VersionRange::fromString)
                .toArray(VersionRange[]::new);

        return new VersionString(versionRanges);
    }

    /**
     * @return {@code true} if this version-string contains only a single version id, false otherwise
     */
    public boolean containsSingleVersionId() {
        return versionRanges.length == 1;
    }

    /**
     * @return {@code true} if this version-string contains a single version-range which is an exact match, {@code false} otherwise.
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
     * @param versionRange a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    private boolean contains(final VersionId versionRange) {
        return Arrays.stream(versionRanges).anyMatch(vid -> vid.matches(versionRange));
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
