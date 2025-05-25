// Copyright (C) 2009 Red Hat, Inc.
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

package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_DISABLE_RESTRICTED_FILES;

/**
 * This class contains a method to create restricted files.
 *
 * @author Omair Majid
 */

public final class RestrictedFileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RestrictedFileUtils.class);

    private static final List<String> WIN_PRINCIPAL_SIDS = Arrays.asList(
            "S-1-5-18" /*NT AUTHORITY\SYSTEM*/,
            "S-1-5-32-544" /*BUILTIN\Administrators*/);

    /**
     * Creates a new directory with minimum permissions. The directory is not
     * readable or writable by anyone other than the owner. The parent
     * directories are not created; they must exist before this is called.
     *
     * @param directory directory to be created
     * @throws IOException if IO fails
     */
    public static void createRestrictedDirectory(File directory) throws IOException {
        createRestrictedFile(directory, true);
    }

    /**
     * Creates a new file with minimum permissions. The file is not readable or
     * writable by anyone other than the owner. If writeableByOwner is false,
     * even the owner can not write to it.
     *
     * @param file path to file
     * @throws IOException if IO fails
     */
    public static void createRestrictedFile(File file) throws IOException {
        createRestrictedFile(file, false);
    }

    /**
     * Creates a new file or directory with minimum permissions. The file is not
     * readable or writable by anyone other than the owner. If writeableByOwner
     * is false, even the owner can not write to it. If isDir is true, then the
     * directory can be executed by the owner
     */
    private static void createRestrictedFile(File file, boolean isDir) throws IOException {

        final String disableRestrictedFiles = JNLPRuntime.getConfiguration().getProperty(KEY_SECURITY_DISABLE_RESTRICTED_FILES);
        if (parseBoolean(disableRestrictedFiles)) {
            createFileOrDir(file, isDir);
            return;
        }

        final File tempFile = new File(file.getCanonicalPath() + ".temp");

        createFileOrDir(tempFile, isDir);

        try {
            if (OsUtil.isWindows()) {
                // prepare ACL flags
                Set<AclEntryFlag> flags = new LinkedHashSet<>();
                if (tempFile.isDirectory()) {
                    flags.add(AclEntryFlag.DIRECTORY_INHERIT);
                    flags.add(AclEntryFlag.FILE_INHERIT);
                }

                // prepare ACL permissions
                Set<AclEntryPermission> permissions = new LinkedHashSet<>(Arrays.asList(
                        AclEntryPermission.READ_DATA,
                        AclEntryPermission.READ_NAMED_ATTRS,
                        AclEntryPermission.EXECUTE,
                        AclEntryPermission.READ_ATTRIBUTES,
                        AclEntryPermission.READ_ACL,
                        AclEntryPermission.SYNCHRONIZE,
                        AclEntryPermission.WRITE_DATA,
                        AclEntryPermission.APPEND_DATA,
                        AclEntryPermission.WRITE_NAMED_ATTRS,
                        AclEntryPermission.DELETE_CHILD,
                        AclEntryPermission.WRITE_ATTRIBUTES,
                        AclEntryPermission.DELETE,
                        AclEntryPermission.WRITE_ACL,
                        AclEntryPermission.WRITE_OWNER
                ));

                // filter ACL's leaving only root and owner
                AclFileAttributeView view = Files.getFileAttributeView(tempFile.toPath(), AclFileAttributeView.class);
                List<AclEntry> list = new ArrayList<>();
                for (AclEntry ae : view.getAcl()) {
                    if (principalInWinSIDS(ae.principal())) {
                        list.add(AclEntry.newBuilder()
                                .setType(AclEntryType.ALLOW)
                                .setPrincipal(ae.principal())
                                .setPermissions(permissions)
                                .setFlags(flags)
                                .build());
                    }
                }
                // Add permissions for the owner
                list.add(AclEntry.newBuilder()
                        .setType(AclEntryType.ALLOW)
                        .setPrincipal(view.getOwner())
                        .setPermissions(permissions)
                        .setFlags(flags)
                        .build());
                // apply ACL
                view.setAcl(list);
            } else {
                // remove all permissions
                if (!tempFile.setExecutable(false, false)) {
                    throw new IOException("Removing execute permissions on file " + tempFile + " failed ");
                }
                if (!tempFile.setReadable(false, false)) {
                    throw new IOException("Removing read permission on file " + tempFile + " failed ");
                }
                if (!tempFile.setWritable(false, false)) {
                    throw new IOException("Removing write permissions on file " + tempFile + " failed ");
                }

                // allow owner to read
                if (!tempFile.setReadable(true, true)) {
                    throw new IOException("Acquiring read permissions on file " + tempFile + " failed");
                }

                // allow owner to write
                if (!tempFile.setWritable(true, true)) {
                    throw new IOException("Acquiring write permissions on file " + tempFile + " failed");
                }

                // allow owner to enter directories
                if (isDir && !tempFile.setExecutable(true, true)) {
                    throw new IOException("Acquiring execute permissions on file " + tempFile + " failed");
                }
            }

            // rename this file. Unless the file is moved/renamed, any program that
            // opened the file right after it was created might still be able to
            // read the data.
            if (!tempFile.renameTo(file)) {
                throw new IOException("Cannot rename " + tempFile + " to " + file);
            }
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                LOG.error("Cannot delete file [{}]", tempFile);
            }
        }
    }

    private static void createFileOrDir(File file, boolean isDir) throws IOException {
        if (isDir) {
            if (!file.mkdir()) {
                throw new IOException("Cannot create directory {} " + file);
            }
        } else {
            if (!file.createNewFile()) {
                throw new IOException("Cannot create file {} " + file);
            }
        }
    }

    public static boolean principalInWinSIDS(Principal principal) {
        return WIN_PRINCIPAL_SIDS.contains(getSIDForPrincipal(principal));
    }

    public static String getSIDForPrincipal(Principal principal) {
        try {
            Method method = findMethod(principal.getClass(), "sidString");
            if (method != null) {
                method.setAccessible(true);
                return (String) method.invoke(principal);
            }
        } catch (Exception e) {
            LOG.debug("No SID for {}", principal.getName());
        }
        return "";
    }

    private static Method findMethod(Class<?> clazz, String methodName) {
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(methodName);
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
