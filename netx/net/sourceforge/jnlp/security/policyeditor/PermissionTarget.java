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

/**
 * Defines the set of targets required for the default permissions
 */
public enum PermissionTarget {

    NONE(""),
    ALL("*"),
    ALL_FILES("<<ALL FILES>>"),
    USER_HOME("${user.home}"),
    TMPDIR("${java.io.tmpdir}"),
    CLIPBOARD("accessClipboard"),
    PRINT("queuePrintJob"),
    PLAY("play"),
    RECORD("record"),
    REFLECT("suppressAccessChecks"),
    GETENV("getenv.*"),
    ACCESS_THREADS("modifyThread"),
    ACCESS_THREAD_GROUPS("modifyThreadGroup"),
    ACCESS_CLASS_IN_PACKAGE("accessClassInPackage.*"),
    DECLARED_MEMBERS("accessDeclaredMembers"),
    CLASSLOADER("getClassLoader");

    public final String target;

    private PermissionTarget(final String target) {
        this.target = target;
    }

    /**
     * If there is any target that matches the string, return it.
     * If no matches, return NONE;
     * @param string a permission target value
     * @return the closest matching default targets value
     */
    public static PermissionTarget fromString(final String string) {
        for (final PermissionTarget target : PermissionTarget.values()) {
            if (string.trim().equals(target.target)) {
                return target;
            }
        }
        return NONE;
    }
}
