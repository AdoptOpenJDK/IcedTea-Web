/* Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.dialogs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jnlp.security.policyeditor.PermissionActions;
import net.sourceforge.jnlp.security.policyeditor.PolicyEditorPermissions;
import net.sourceforge.jnlp.util.logging.OutputController;

public class TemporaryPermissions {

    // Look for expandable properties in targets, eg ${user.home} or ${java.io.tmpdir}
    private static final Pattern expandablePattern = Pattern.compile("\\$\\{([a-zA-Z0-9\\.}]+)*\\}");

    public static Collection<Permission> getPermissions(final PolicyEditorPermissions... editorPermissions) {
        return getPermissions(Arrays.asList(editorPermissions));
    }

    public static Collection<Permission> getPermissions(final Collection<PolicyEditorPermissions> editorPermissions) {
        final Collection<Permission> col = new HashSet<Permission>();
        for (final PolicyEditorPermissions editorPerm : editorPermissions) {
            col.add(getPermission(editorPerm));
        }
        return Collections.unmodifiableCollection(col);
    }

    public static Collection<Permission> getPermissions(final PolicyEditorPermissions.Group permissionsGroup) {
        return getPermissions(permissionsGroup.getPermissions());
    }

    public static Permission getPermission(final PolicyEditorPermissions editorPermission) {
        try {
            final Class<?> clazz = Class.forName(editorPermission.getType().type);
            final Constructor<?> ctor;
            final Permission perm;
            String target = editorPermission.getTarget().target;

            Matcher m = expandablePattern.matcher(target);
            while (m.find()) {
                // Expand any matches by reading from System properties, eg ${java.io.tmpdir} is /tmp on most systems
                target = m.replaceFirst(System.getProperty(m.group(1)));
                m = expandablePattern.matcher(target);
            }

            if (editorPermission.getActions().equals(PermissionActions.NONE)) {
                ctor = clazz.getDeclaredConstructor(new Class[] { String.class });
                ctor.setAccessible(true);
                perm = (Permission) ctor.newInstance(target);
            } else {
                ctor = clazz.getDeclaredConstructor(new Class[] { String.class, String.class });
                ctor.setAccessible(true);
                perm = (Permission) ctor.newInstance(target, collectionToString(editorPermission.getActions().getActions()));
            }
            return perm;
        } catch (final ClassNotFoundException | SecurityException | NoSuchMethodException
                | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            OutputController.getLogger().log(e);
            return null;
        }
    }

    private static String collectionToString(final Collection<String> col) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (final String str : col) {
            sb.append(str);
            if (count < col.size() - 1) {
                sb.append(",");
            }
            ++count;
        }
        return sb.toString();
    }

}