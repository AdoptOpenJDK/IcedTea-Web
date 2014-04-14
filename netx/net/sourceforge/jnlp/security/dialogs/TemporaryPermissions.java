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

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.PropertyPermission;

import javax.sound.sampled.AudioPermission;

import static net.sourceforge.jnlp.security.policyeditor.PolicyEditorPermissions.*;

public class TemporaryPermissions {

    // We can't use the PolicyEditorPermissions versions of these, because they rely on System Property expansion, which is perfomed
    // by the policy parser, but not by the Permissions constructors.
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    public static final FilePermission READ_LOCAL_FILES_PERMISSION = new FilePermission(USER_HOME, READ_LOCAL_FILES.getActions().rawString());
    public static final FilePermission WRITE_LOCAL_FILES_PERMISSION = new FilePermission(USER_HOME, WRITE_LOCAL_FILES.getActions().rawString());
    public static final FilePermission DELETE_LOCAL_FILES_PERMISSION = new FilePermission(USER_HOME, DELETE_LOCAL_FILES.getActions().rawString());
    public static final FilePermission READ_TMP_FILES_PERMISSION = new FilePermission(TMPDIR, READ_TMP_FILES.getActions().rawString());
    public static final FilePermission WRITE_TMP_FILES_PERMISSION = new FilePermission(TMPDIR, WRITE_TMP_FILES.getActions().rawString());
    public static final FilePermission DELETE_TMP_FILES_PERMISSION = new FilePermission(TMPDIR, DELETE_TMP_FILES.getActions().rawString());
    public static final FilePermission READ_SYSTEM_FILES_PERMISSION = new FilePermission(READ_SYSTEM_FILES.getTarget().target, READ_SYSTEM_FILES.getActions()
            .rawString());
    public static final FilePermission WRITE_SYSTEM_FILES_PERMISSION = new FilePermission(WRITE_SYSTEM_FILES.getTarget().target, WRITE_SYSTEM_FILES
            .getActions().rawString());

    public static final PropertyPermission READ_PROPERTIES_PERMISSION = new PropertyPermission(READ_PROPERTIES.getTarget().target, READ_PROPERTIES.getActions()
            .rawString());
    public static final PropertyPermission WRITE_PROPERTIES_PERMISSION = new PropertyPermission(WRITE_PROPERTIES.getTarget().target, WRITE_PROPERTIES
            .getActions().rawString());

    public static final FilePermission EXEC_PERMISSION = new FilePermission(EXEC_COMMANDS.getTarget().target, EXEC_COMMANDS.getActions().rawString());
    public static final RuntimePermission GETENV_PERMISSION = new RuntimePermission(GET_ENV.getTarget().target);

    public static final SocketPermission NETWORK_PERMISSION = new SocketPermission(NETWORK.getTarget().target, NETWORK.getActions().rawString());

    public static final ReflectPermission REFLECTION_PERMISSION = new ReflectPermission(JAVA_REFLECTION.getTarget().target);
    public static final RuntimePermission CLASSLOADER_PERMISSION = new RuntimePermission(GET_CLASSLOADER.getTarget().target);
    public static final RuntimePermission ACCESS_CLASS_IN_PACKAGE_PERMISSION = new RuntimePermission(ACCESS_CLASS_IN_PACKAGE.getTarget().target);
    public static final RuntimePermission ACCESS_DECLARED_MEMBERS_PERMISSION = new RuntimePermission(ACCESS_DECLARED_MEMBERS.getTarget().target);
    public static final RuntimePermission ACCESS_THREADS_PERMISSION = new RuntimePermission(ACCESS_THREADS.getTarget().target);
    public static final RuntimePermission ACCESS_THREADGROUPS_PERMISSION = new RuntimePermission(ACCESS_THREAD_GROUPS.getTarget().target);

    public static final AWTPermission AWT_PERMISSION = new AWTPermission(ALL_AWT.getTarget().target);
    public static final AudioPermission PLAY_AUDIO_PERMISSION = new AudioPermission(PLAY_AUDIO.getTarget().target);
    public static final AudioPermission RECORD_AUDIO_PERMISSION = new AudioPermission(RECORD_AUDIO.getTarget().target);
    public static final AWTPermission CLIPBOARD_PERMISSION = new AWTPermission(CLIPBOARD.getTarget().target);
    public static final RuntimePermission PRINT_PERMISSION = new RuntimePermission(PRINT.getTarget().target);

    public static final Collection<Permission> ALL_PERMISSIONS, FILE_PERMISSIONS, PROPERTY_PERMISSIONS, NETWORK_PERMISSIONS, EXEC_PERMISSIONS,
            REFLECTION_PERMISSIONS, MEDIA_PERMISSIONS;
    static {
        final Collection<Permission> all = new HashSet<>(), file = new HashSet<>(), property = new HashSet<>(),
              network = new HashSet<>(), exec = new HashSet<>(), reflection = new HashSet<>(), media = new HashSet<>();

        file.add(READ_LOCAL_FILES_PERMISSION);
        file.add(WRITE_LOCAL_FILES_PERMISSION);
        file.add(DELETE_LOCAL_FILES_PERMISSION);
        file.add(READ_TMP_FILES_PERMISSION);
        file.add(WRITE_TMP_FILES_PERMISSION);
        file.add(DELETE_TMP_FILES_PERMISSION);
        file.add(READ_SYSTEM_FILES_PERMISSION);
        file.add(WRITE_SYSTEM_FILES_PERMISSION);
        FILE_PERMISSIONS = Collections.unmodifiableCollection(file);

        property.add(READ_PROPERTIES_PERMISSION);
        property.add(WRITE_PROPERTIES_PERMISSION);
        PROPERTY_PERMISSIONS = Collections.unmodifiableCollection(property);

        exec.add(EXEC_PERMISSION);
        exec.add(GETENV_PERMISSION);
        EXEC_PERMISSIONS = Collections.unmodifiableCollection(exec);

        network.add(NETWORK_PERMISSION);
        NETWORK_PERMISSIONS = Collections.unmodifiableCollection(network);

        reflection.add(REFLECTION_PERMISSION);
        reflection.add(CLASSLOADER_PERMISSION);
        reflection.add(ACCESS_CLASS_IN_PACKAGE_PERMISSION);
        reflection.add(ACCESS_DECLARED_MEMBERS_PERMISSION);
        reflection.add(ACCESS_THREADS_PERMISSION);
        reflection.add(ACCESS_THREADGROUPS_PERMISSION);
        REFLECTION_PERMISSIONS = Collections.unmodifiableCollection(reflection);

        media.add(AWT_PERMISSION);
        media.add(PLAY_AUDIO_PERMISSION);
        media.add(RECORD_AUDIO_PERMISSION);
        media.add(CLIPBOARD_PERMISSION);
        media.add(PRINT_PERMISSION);
        MEDIA_PERMISSIONS = Collections.unmodifiableCollection(media);

        all.addAll(file);
        all.addAll(property);
        all.addAll(exec);
        all.addAll(network);
        all.addAll(reflection);
        all.addAll(media);
        ALL_PERMISSIONS = Collections.unmodifiableCollection(all);
    }

    private static final Collection<Permission> allMinus(final Collection<Permission> permissions) {
        return subtract(ALL_PERMISSIONS, permissions);
    }

    private static Collection<Permission> sum(final Permission... permissions) {
        final Collection<Permission> result = new HashSet<>(Arrays.asList(permissions));
        return Collections.unmodifiableCollection(result);
    }

    private static Collection<Permission> sum(final Collection<Permission> a, final Collection<Permission> b) {
        final Collection<Permission> result = new HashSet<>();
        result.addAll(a);
        result.addAll(b);
        return Collections.unmodifiableCollection(result);
    }

    private static final Collection<Permission> subtract(final Collection<Permission> from, final Collection<Permission> remove) {
        final Collection<Permission> result = new HashSet<>(from);
        result.removeAll(remove);
        return Collections.unmodifiableCollection(result);
    }

    public static Collection<Permission> noFileAccess() {
        return allMinus(FILE_PERMISSIONS);
    }

    public static Collection<Permission> noNetworkAccess() {
        return allMinus(Arrays.asList(new Permission[] { NETWORK_PERMISSION }));
    }

    public static Collection<Permission> noFileOrNetworkAccess() {
        return subtract(allMinus(FILE_PERMISSIONS), NETWORK_PERMISSIONS);
    }

    public static Collection<Permission> allFileAccessAndProperties() {
        return sum(FILE_PERMISSIONS, PROPERTY_PERMISSIONS);
    }

    public static Collection<Permission> readLocalFilesAndProperties() {
        return sum(READ_LOCAL_FILES_PERMISSION, READ_PROPERTIES_PERMISSION);
    }

    public static Collection<Permission> reflectionOnly() {
        return REFLECTION_PERMISSIONS;
    }

    public static Collection<Permission> allMedia() {
        return MEDIA_PERMISSIONS;
    }

    public static Collection<Permission> audioOnly() {
        return sum(PLAY_AUDIO_PERMISSION, RECORD_AUDIO_PERMISSION);
    }

    public static Collection<Permission> clipboardOnly() {
        return sum(CLIPBOARD_PERMISSION);
    }

    public static Collection<Permission> printOnly() {
        return sum(PRINT_PERMISSION);
    }

}
