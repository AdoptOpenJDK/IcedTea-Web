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
     * Checks if this version-string (list of exact version-ids or version ranges) contains the given {@code versionId}.
     *
     * @param versionId a version-id
     * @return {@code true} if this version-string contains the given {@code versionId}, {@code false} otherwise
     */
    private boolean contains(VersionId versionId) {
        return Arrays.stream(versionIds).anyMatch(vid -> vid.matches(versionId));
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