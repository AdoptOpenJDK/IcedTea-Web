/*Copyright (C) 2014 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp.security.policyeditor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a codebase entry in a policy file. This is defined as a policy entry block
 * which begins with they keyword "grant" and ends with the delimiter "};". If the entry
 * contains a "codeBase $CODEBASE" substring after the "grant" keyword, then this information
 * is also included in this entry. Other entry "metadata" such as Principal is not defined.
 * Within a codebase entry block, lines are recognized and modelled as either PolicyEditorPermissions
 * or CustomPermissions.
 */
public class PolicyEntry {

    private final String codebase;
    private final Set<PolicyEditorPermissions> permissions = new HashSet<>();
    private final Set<CustomPermission> customPermissions = new HashSet<>();

    public PolicyEntry(final String codebase, final Collection<PolicyEditorPermissions> permissions,
            final Collection<CustomPermission> customPermissions) {
        if (codebase == null) {
            this.codebase = "";
        } else {
            this.codebase = codebase;
        }
        this.permissions.addAll(permissions);
        this.permissions.remove(null);
        this.customPermissions.addAll(customPermissions);
        this.customPermissions.remove(null);
    }

    public String getCodebase() {
        return codebase;
    }

    public Set<PolicyEditorPermissions> getPermissions() {
        return permissions;
    }

    public Set<CustomPermission> getCustomPermissions() {
        return customPermissions;
    }

    public static PolicyEntry fromString(final String contents) throws InvalidPolicyException {
        final List<String> lines = Arrays.asList(contents.split("\\r?\\n+"));
        if (!validatePolicy(lines)) {
            throw new InvalidPolicyException();
        }

        String codebase = "";
        final Set<PolicyEditorPermissions> permissions = new HashSet<>();
        final Set<CustomPermission> customPermissions = new HashSet<>();

        boolean openBlock = false, commentBlock = false;
        for (final String line : lines) {
            // Matches eg `grant {` as well as `grant codeBase "http://redhat.com" {`
            final Pattern openBlockPattern = Pattern.compile("grant\\s*\"?\\s*(?:codeBase)?\\s*\"?([^\"\\s]*)\"?\\s*\\{");
            final Matcher openBlockMatcher = openBlockPattern.matcher(line);
            if (openBlockMatcher.matches()) {
                // Codebase URL
                codebase = openBlockMatcher.group(1);
                openBlock = true;
                continue;
            }

            // Matches '};', the closing block delimiter, with any amount of whitespace on either side
            boolean commentLine = false;
            if (line.matches("\\s*\\};\\s*")) {
                openBlock = false;
            }
            // Matches '/*', the start of a block comment
            if (line.matches(".*/\\*.*")) {
                commentBlock = true;
            }
            // Matches '*/', the end of a block comment, and '//', a single-line comment
            if (line.matches(".*\\*/.*")) {
                commentBlock = false;
            }
            if (line.matches(".*/\\*.*") && line.matches(".*\\*/.*")) {
                commentLine = true;
            }
            if (line.matches("\\s*//.*")) {
                commentLine = true;
            }

            if (!openBlock || commentBlock || commentLine) {
                continue;
            }

            final PolicyEditorPermissions perm = PolicyEditorPermissions.fromString(line);
            if (perm != null) {
                permissions.add(perm);
            } else {
                final CustomPermission cPerm = CustomPermission.fromString(line.trim());
                if (cPerm != null) {
                    customPermissions.add(cPerm);
                }
            }
        }
        return new PolicyEntry(codebase, permissions, customPermissions);
    }

    public static boolean validatePolicy(final String content) {
        return validatePolicy(Arrays.asList(content.split("\\r?\\n")));
    }

    public static boolean validatePolicy(final List<String> lines) {
        int openerCount = 0, closerCount = 0;
        for (final String line : lines) {
            final Pattern openBlockPattern = Pattern.compile("grant\\s*\"?\\s*(?:codeBase)?\\s*\"?([^\"\\s]*)\"?\\s*\\{");
            final Matcher openBlockMatcher = openBlockPattern.matcher(line);
            if (openBlockMatcher.matches()) {
                ++openerCount;
            }

            if (line.matches("\\s*\\};\\s*")) {
                ++closerCount;
            }
        }
        return (openerCount == 1) && (closerCount == 1);
    }

    @Override
    public String toString() {
        // Empty codebase is the default "All Applets" codebase. If there are no permissions
        // applied to it, then don't bother recording it in the policy file.
        if (codebase.isEmpty() && permissions.isEmpty() && customPermissions.isEmpty()) {
            return "";
        }
        final String newline = System.getProperty("line.separator");
        final StringBuilder result = new StringBuilder();

        result.append(newline);
        result.append("grant");
        if (!codebase.isEmpty()) {
            result.append(" codeBase \"");
            result.append(codebase);
            result.append("\"");
        }
        result.append(" {");
        result.append(newline);
        for (final PolicyEditorPermissions perm : permissions) {
            result.append("\t");
            result.append(perm.toPermissionString());
            result.append(newline);
        }
        for (final CustomPermission customPerm : customPermissions) {
            result.append("\t");
            result.append(customPerm.toString().trim());
            result.append(newline);
        }
        result.append("};");
        result.append(newline);
        return result.toString();
    }

}
