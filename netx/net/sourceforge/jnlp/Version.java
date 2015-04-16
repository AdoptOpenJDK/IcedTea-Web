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
import javax.swing.JOptionPane;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;

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
    
    /**
     * This is special case of version, used only for checking jre version. If
     * jre do not match, in strict not-headless mode the dialog with
     * confirrmation appears If jre do not match, in strict headless mode the
     * exception is thrown If jre match, or non-strict mode is run, then only
     * message is printed
     *
     */
    public static class JreVersion extends Version {

        public static boolean warned = false;

        public JreVersion(String v, boolean strict) {
            this(v, strict, JNLPRuntime.isHeadless());
        }

        /*
         *  for testing purposes
         */
        JreVersion(String v, boolean strict, boolean headless) {
            super(v);
            boolean match = matchesJreVersion();
            if (!match) {
                String s = Translator.R("JREversionDontMatch", getJreVersion(), v);
                String e = "Strict run is  deffined, and your JRE - " + getJreVersion() + " - dont match requested JRE(s) - " + v;
                if (strict) {
                    if (!headless) {
                        if (!warned) {
                            int r = JOptionPane.showConfirmDialog(null, s + "\n" + Translator.R("JREContinueDialogSentence2"), Translator.R("JREContinueDialogSentenceTitle"), JOptionPane.YES_NO_OPTION);
                            if (r == JOptionPane.NO_OPTION) {
                                throw new RuntimeException(e);
                            }
                            warned = true;
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                } else {
                    OutputController.getLogger().log(OutputController.Level.WARNING_ALL, s);
                }
            } else {
                OutputController.getLogger().log("good - your JRE - " + getJreVersion() + " - match requested JRE - " + v);
            }
        }

        public boolean matchesJreVersion() {
            return matches(getJreVersion());
        }

        private String getJreVersion() {
            return System.getProperty("java.version");
        }

    }

    // to do: web start does not match versions with a "-" like
    // "1.4-beta1" using the + modifier, change to mimic that
    // behavior.

    // also refactor into Version and VersionID classes so that
    // individual version ids can be easily modified to add/remove
    // "*" and "+" modifiers.

    /** separates parts of a version string */
    private static final String seperators = ".-_";

    /** magic key for whether a version part was created due to normalization */
    private static final String emptyString = new String("<EMPTY>"); // not intern'ed

    /** contains all the versions matched */
    private final String versionString;

    /**
     * Create a Version object based on a version string (ie,
     * "1.2.3+ 4.56*").
     * @param versions string describing version
     */
    public Version(String versions) {
        versionString = versions;
    }

    /**
     * @return true if the version represents a <i>version-id</i> (a
     * single version number such as 1.2) and false otherwise.
     */
    public boolean isVersionId() {
        return -1 == versionString.indexOf(" ");
    }

    /**
     * @return true if all of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a version string
     */
    public boolean matches(String version) {
        return matches(new Version(version));
    }

    /**
     * @return true if all of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a Version object
     */
    public boolean matches(Version version) {
        List<String> versionStrings = version.getVersionStrings();

        for (String versionString1 : versionStrings) {
            if (!this.matchesSingle(versionString1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if any of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a version string
     */
    public boolean matchesAny(String version) {
        return matches(new Version(version));
    }

    /**
     * @return true if any of this version's version-ids match one
     * or more of the specifed version's version-id.
     *
     * @param version a Version object
     */
    public boolean matchesAny(Version version) {
        List<String> versionStrings = version.getVersionStrings();

        for (String versionString1 : versionStrings) {
            if (this.matchesSingle(versionString1)) {
                return true;
            }
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
        for (String versionString1 : versionStrings) {
            if (matches(version, versionString1)) {
                return true;
            }
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

        List<List<String>> versions = new ArrayList<>();
        versions.add(subparts);
        versions.add(parts);
        normalize(versions, maxLength);

        if (equal(subparts, parts))
            return true;

        return version.endsWith("+") && greater(subparts, parts);
    }

    /**
     * @return whether the parts of one version are equal to the
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
     * @return whether the parts of one version are greater than
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
        Integer number1 = 0;
        Integer number2 = 0;

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
     * @return the individual version strings that make up a Version.
     */
    protected List<String> getVersionStrings() {
        ArrayList<String> strings = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(versionString, " ");
        while (st.hasMoreTokens())
            strings.add(st.nextToken());

        return strings;
    }

    /**
     * @return the constituent parts of a version string.
     *
     * @param oneVersion a single version id string (not compound)
     */
    protected List<String> getParts(String oneVersion) {
        ArrayList<String> strings = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(oneVersion, seperators + "+*");
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken());
        }

        return strings;
    }

    @Override
    public String toString() {
        return versionString;
    }  

}
