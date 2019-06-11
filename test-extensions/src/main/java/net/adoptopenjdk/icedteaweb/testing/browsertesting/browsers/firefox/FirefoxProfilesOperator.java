/* FirefoxProfilesOperator.java
Copyright (C) 2011,2012 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.testing.browsertesting.browsers.firefox;

import net.adoptopenjdk.icedteaweb.testing.ServerAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_HOME;

/**
 * This class is able to backup and restore firefox profiles.
 *
 */
public class FirefoxProfilesOperator {

    private File backupDir;
    private File sourceDir;
    private boolean backuped = false;
    private final FilenameFilter firefoxProfilesFilter = (dir, name) -> name.endsWith(".default") || name.equals("profiles.ini");


    public void backupProfiles() throws IOException {
        if (backuped) {
            return;
        }
        sourceDir = new File(System.getProperty(USER_HOME) + "/.mozilla/firefox/");
        final File f = File.createTempFile("backupedFirefox_", "_profiles.default");
        f.delete();
        f.mkdir();
        backupDir = f;
        final String message = "Backuping firefox profiles from " + sourceDir.getAbsolutePath() + " to " + backupDir.getAbsolutePath();
        ServerAccess.logOutputReprint(message);
        copyDirs(sourceDir, backupDir, firefoxProfilesFilter);
        backuped = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                restoreProfiles();
            } catch (final Exception ex) {
                ServerAccess.logException(ex);
            }
        }));

    }

    public void restoreProfiles() throws IOException {
        if (!backuped) {
            return;
        }
        try {
            removeProfiles();
        } catch (final Exception ex) {
            ServerAccess.logException(ex);
        }
        final String message = ("Restoring all firefox profiles in " + sourceDir.getAbsolutePath() + " from in " + backupDir.getAbsolutePath());
        ServerAccess.logOutputReprint(message);
        copyDirs(backupDir, sourceDir, firefoxProfilesFilter);

    }

    private void removeProfiles() throws IOException {
        if (!backuped) {
            return;
        }
        final String message = ("Removing all firefox profiles from " + sourceDir.getAbsolutePath() + " backup available in " + backupDir.getAbsolutePath());
        ServerAccess.logOutputReprint(message);
        final File[] oldProfiles = sourceDir.listFiles(firefoxProfilesFilter);
        for (final File file : oldProfiles) {
            deleteRecursively(file);

        }

    }

    private void copyDirs(final File sourceDir, final File backupDir, final FilenameFilter firefoxProfilesFilter) throws IOException {
        final File[] profiles = sourceDir.listFiles(firefoxProfilesFilter);
        for (final File file : profiles) {
            copyRecursively(file, backupDir);
        }
    }

    public static void copyFile(final File from, final File to) throws IOException {
        if (!from.isFile()) {
            to.delete();
            return;
        }

        try (final FileInputStream is = new FileInputStream(from)) {
            try (final FileOutputStream fos = new FileOutputStream(to)) {
                try (final FileChannel fIn = is.getChannel()) {
                    try (final FileChannel fOut = fos.getChannel()) {
                        fIn.transferTo(0, fIn.size(), fOut);
                    }
                }
            }
        }
    }

    private static void deleteRecursively(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        boolean d = f.delete();
        if (!d) {
            throw new IOException("Failed to delete file: " + f);
        }
    }

    private static void copyRecursively(final File srcFileDir, final File destDir) throws IOException {
        if (srcFileDir.isDirectory()) {
            final File nwDest = new File(destDir, srcFileDir.getName());
            nwDest.mkdir();
            for (final File c : srcFileDir.listFiles()) {
                copyRecursively(c, nwDest);
            }
        } else {
            copyFile(srcFileDir, new File(destDir, srcFileDir.getName()));
        }

    }

    public static void main(final String[] args) throws IOException {
        final FirefoxProfilesOperator ff = new FirefoxProfilesOperator();
        ff.restoreProfiles();
        ff.backupProfiles();
        ff.restoreProfiles();
        ff.backupProfiles();

    }
}
