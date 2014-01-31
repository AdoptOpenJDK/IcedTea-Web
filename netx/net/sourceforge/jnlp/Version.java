// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp;

import java.util.*;

/**
 * <p>
 * A JNLP Version string in the form "1.2-3_abc" followed by an
 * optional + (includes all later versions) or * (matches any
 * suffixes on versions).  More than one version can be included
 * in a string by separating them with spaces.
 * </p>
 * <p>
 * Version strings are divided by "._-" charecters into parts.
 * These parts are compared numerically if they can be parsed as
 * integers or lexographically as strings otherwise.  If the
 * number of parts is different between two version strings then
 * the smaller one is padded with zero or the empty string.  Note
 * that the padding in this version means that 1.2+ matches
 * 1.4.0-beta1, but may not in future versions.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class Version {

    // to do: web start does not match versions with a "-" like
    // "1.4-beta1" using the + modifier, change to mimic that
    // behavior.

    // also refactor into Version and VersionID classes so that
    // individual version ids can be easily modified to add/remove
    // "*" and "+" modifiers.

    /** separates parts of a version string */
    private static String seperators = ".-_";

    /** magic key for whether a version part was created due to normalization */
    private static String emptyString = new String("<EMPTY>"); // not intern'ed

    /** contains all the versions matched */
    private String versionString;

    /**
     * Create a Version object based on a version string (ie,
     * "1.2.3+ 4.56*").
     */
    public Version(String versions) {
        versionString = versions;
    }

    /**
     * Returns true if the version represents a <i>version-id</i> (a
     * single version number such as 1.2) and false otherwise.
     */
    public boolean isVersionId() {
        if (-1 != versionString.indexOf(" "))
            return false;

        return true;
    }

    /**
     * Returns true if all of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a version string
     */
    public boolean matches(String version) {
        return matches(new Version(version));
    }

    /**
     * Returns true if all of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a Version object
     */
    public boolean matches(Version version) {
        List<String> versionStrings = version.getVersionStrings();

        for (int i = 0; i < versionStrings.size(); i++) {
            if (!this.matchesSingle(versionStrings.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Returns true if any of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a version string
     */
    public boolean matchesAny(String version) {
        return matches(new Version(version));
    }

    /**
     * Returns true if any of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a Version object
     */
    public boolean matchesAny(Version version) {
        List<String> versionStrings = version.getVersionStrings();

        for (int i = 0; i < versionStrings.size(); i++) {
            if (this.matchesSingle(versionStrings.get(i)))
                return true;
        }

        return false;
    }

    /**
     * Returns whether a single version string is supported by this
     * Version.
     *
     * @param version a non-compound version of the form "1.2.3[+*]"
     */
    private boolean matchesSingle(String version) {
        List<String> versionStrings = this.getVersionStrings();
        for (int i = 0; i < versionStrings.size(); i++) {
            if (matches(version, versionStrings.get(i)))
                return true;
        }
        return false;
    }

    /**
     * Returns whether a single version string is supported by
     * another single version string.
     *
     * @param subversion a non-compound version without "+" or "*"
     * @param version a non-compound version optionally with "+" or "*"
     */
    private boolean matches(String subversion, String version) {
        List<String> subparts = getParts(subversion);
        List<String> parts = getParts(version);

        int maxLength = Math.max(subversion.length(), version.length());
        if (version.endsWith("*")) // star means rest of parts irrelevant: truncate them
            maxLength = parts.size();

        List<List<String>> versions = new ArrayList<List<String>>();
        versions.add(subparts);
        versions.add(parts);
        normalize(versions, maxLength);

        if (equal(subparts, parts))
            return true;

        if (version.endsWith("+") && greater(subparts, parts))
            return true;

        return false;
    }

    /**
     * Returns whether the parts of one version are equal to the
     * parts of another version.
     *
     * @param parts1 normalized version parts
     * @param parts2 normalized version parts
     */
    protected boolean equal(List<String> parts1, List<String> parts2) {
        for (int i = 0; i < parts1.size(); i++) {
            if (0 != compare(parts1.get(i), parts2.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Returns whether the parts of one version are greater than
     * the parts of another version.
     *
     * @param parts1 normalized version parts
     * @param parts2 normalized version parts
     */
    protected boolean greater(List<String> parts1, List<String> parts2) {
        //if (true) return false;

        for (int i = 0; i < parts1.size(); i++) {
            // if part1 > part2 then it's a later version, so return true
            if (compare(parts1.get(i), parts2.get(i)) > 0)
                return true;

            // if part1 < part2 then it's a ealier version, so return false
            if (compare(parts1.get(i), parts2.get(i)) < 0)
                return false;

            // if equal go to next part
        }

        // all parts were equal
        return false; // not greater than
    }

    /**
     * Compares two parts of a version string, by value if both can
     * be interpreted as integers or lexically otherwise.  If a part
     * is the result of normalization then it can be the Integer
     * zero or an empty string.
     *
     * Returns a value equivalent to part1.compareTo(part2);
     *
     * @param part1 a part of a version string
     * @param part2 a part of a version string
     * @return comparison of the two parts
     */
    protected int compare(String part1, String part2) {
        Integer number1 = Integer.valueOf(0);
        Integer number2 = Integer.valueOf(0);

        // compare as integers
        // for normalization key, compare exact object, not using .equals
        try {
            if (!(part1 == emptyString)) // compare to magic normalization key
                number1 = Integer.valueOf(part1);

            if (!(part2 == emptyString)) // compare to magic normalization key
                number2 = Integer.valueOf(part2);

            return number1.compareTo(number2);
        } catch (NumberFormatException ex) {
            // means to compare as strings
        }

        if (part1 == emptyString) // compare to magic normalization key
            part1 = "";
        if (part2 == emptyString) // compare to magic normalization key
            part2 = "";

        return part1.compareTo(part2);
    }

    /**
     * Normalize version strings so that they contain the same
     * number of constituent parts.
     *
     * @param versions list array of parts of a version string
     * @param maxLength truncate lists to this maximum length
     */
    protected void normalize(List<List<String>> versions, int maxLength) {
        int length = 0;
        for (List<String> vers : versions)
            length = Math.max(length, vers.size());

        if (length > maxLength)
            length = maxLength;

        for (List<String> vers : versions) {
            // remove excess elements
            while (vers.size() > length)
                vers.remove(vers.size() - 1);

            // add in empty pad elements
            while (vers.size() < length)
                vers.add(emptyString);
        }
    }

    /**
     * Return the individual version strings that make up a Version.
     */
    protected List<String> getVersionStrings() {
        ArrayList<String> strings = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(versionString, " ");
        while (st.hasMoreTokens())
            strings.add(st.nextToken());

        return strings;
    }

    /**
     * Return the constituent parts of a version string.
     *
     * @param oneVersion a single version id string (not compound)
     */
    protected List<String> getParts(String oneVersion) {
        ArrayList<String> strings = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(oneVersion, seperators + "+*");
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken());
        }

        return strings;
    }

    public String toString() {
        return versionString;
    }  

}
