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

import static java.lang.String.format;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_VERSION_STRING;

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
 * @see JNLPVersionSpecifications
 */
public class VersionString {

    private final VersionId[] versionIds;

    private VersionString(VersionId[] versionIds) {
        this.versionIds = versionIds;
    }

    /**
     * Construct a version-string by the given {@code versionString}.
     *
     * @param versionString a version-string
     * @return a versionString
     */
    public static VersionString fromString(String versionString) {
        if (Objects.isNull(versionString) || !versionString.matches(REGEXP_VERSION_STRING)) {
            throw new IllegalArgumentException(format("'%s' is not a valid version string according to JSR-56, Appendix A.", versionString));
        }

        final VersionId[] versionIds = Arrays.stream(versionString.split(REGEXP_SPACE))
                .map(VersionId::fromString)
                .toArray(VersionId[]::new);

        return new VersionString(versionIds);
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    public boolean contains(String versionId) {
        return contains(VersionId.fromString(versionId));
    }

    /**
     * @return {@code true} if this version-string contains only a single version id, false otherwise
     */
    public boolean containsSingleVersionId() {
        return versionIds.length == 1;
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    private boolean contains(VersionId versionId) {
        return Arrays.stream(versionIds).anyMatch(vid -> vid.matches(versionId));
    }

    /**
     * Check if the given version-string contains the given version-id
     *
     * @param versionString the version-string
     * @param versionId the version-id
     * @return {@code true} if the given version-string contains the given version-id, false otherwise
     */
    static public boolean contains(String versionString, String versionId) {
        return (VersionString.fromString(versionString)).contains(versionId);
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains a version-id
     * greater than the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains a version-id greater than the
     * given {@code versionId}, {@code false} otherwise
     */
    public boolean containsGreaterThan(String versionId) {
        return containsGreaterThan(VersionId.fromString(versionId));
    }

    /**
     * Checks if this version-string (list of exact version-ids or version ranges) contains a version-id
     * greater than the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains a version-id greater than the
     * given {@code versionId}, {@code false} otherwise
     */
    private boolean containsGreaterThan(VersionId versionId) {
        return Arrays.stream(versionIds).anyMatch(vid -> vid.isGreaterThan(versionId));
    }

    /**
     * Provides string representation of this version-string.
     *
     * @return a string representation of this version-string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (VersionId versionId : versionIds) {
            sb.append(versionId.toString());
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}