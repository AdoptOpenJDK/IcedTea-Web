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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class models a permission entry in a policy file which is not included
 * in the default set of permissions used by the PolicyEditor, ie, permissions
 * not defined in the enum PolicyEditorPermissions.
 */
public class CustomPermission implements Comparable<CustomPermission> {

    /* Matches eg 'permission java.io.FilePermission "${user.home}${/}*", "read";'
     * eg permissions that have a permission type, target, and actions set.
     */
    public static final Pattern ACTIONS_PERMISSION =
            Pattern.compile("\\s*permission\\s+([\\w\\.]+)\\s+\"([^\"]+)\",\\s*\"([^\"]*)\";.*");

    /* Matches eg 'permission java.lang.RuntimePermission "queuePrintJob";'
     * eg permissions that have a permission type and target, but no actions.
     */
    public static final Pattern TARGET_PERMISSION =
            Pattern.compile("\\s*permission\\s+([\\w\\.]+)\\s+\"([^\"]+)\";.*");

    public final String type, target, actions;

    /**
     * @param type eg java.io.FilePermission
     * @param target eg ${user.home}${/}*
     * @param actions eg read,write
     */
    public CustomPermission(final String type, final String target, final String actions) {
        this.type = type;
        this.target = target;
        this.actions = actions;
    }

    /**
     * Get a CustomPermission from a policy file permission entry string. This is the full
     * entry string, eg `permission java.io.FilePermission "${user.home}${/}* "read";`
     * @param string the permission entry string
     * @return a CustomPermission representing this string
     */
    public static CustomPermission fromString(final String string) {
        final String typeStr, targetStr, actionsStr;

        final Matcher actionMatcher = ACTIONS_PERMISSION.matcher(string);
        if (actionMatcher.matches()) {
            typeStr = actionMatcher.group(1);
            targetStr = actionMatcher.group(2);
            actionsStr = actionMatcher.group(3);
        } else {
            final Matcher targetMatcher = TARGET_PERMISSION.matcher(string);
            if (!targetMatcher.matches()) {
                return null;
            }
            typeStr = targetMatcher.group(1);
            targetStr = targetMatcher.group(2);
            actionsStr = "";
        }

        return new CustomPermission(typeStr, targetStr, actionsStr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("permission ");
        sb.append(type);
        sb.append(" \"");
        sb.append(target);
        sb.append("\"");

        if (!this.actions.equals(PermissionActions.NONE.rawString())) {
            sb.append(", \"");
            sb.append(actions);
            sb.append("\"");
        }

        sb.append(";");

        return sb.toString();
    }

    @Override
    public int compareTo(final CustomPermission o) {
        if (this == o) {
            return 0;
        }
        final int typeComparison = this.type.compareTo(o.type);
        if (typeComparison != 0) {
            return typeComparison;
        }
        final int targetComparison = this.target.compareTo(o.target);
        if (targetComparison != 0) {
            return targetComparison;
        }
        final int actionsComparison = this.actions.compareTo(o.actions);
        if (actionsComparison != 0) {
            return actionsComparison;
        }
        return 0;
    }
}
