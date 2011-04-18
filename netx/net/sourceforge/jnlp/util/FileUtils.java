// Copyright (C) 2009 Red Hat, Inc.
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

package net.sourceforge.jnlp.util;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * This class contains a few file-related utility functions.
 *
 * @author Omair Majid
 */

public final class FileUtils {

    /**
     * list of characters not allowed in filenames
     */
    private static final char INVALID_CHARS[] = { '\\', '/', ':', '*', '?', '"', '<', '>', '|' };

    private static final char SANITIZED_CHAR = '_';

    /**
     * Clean up a string by removing characters that can't appear in a local
     * file name.
     *
     * @param path
     *        the path to sanitize
     * @return a sanitized version of the input which is suitable for using as a
     *         file path
     */
    public static String sanitizePath(String path) {

        for (int i = 0; i < INVALID_CHARS.length; i++)
            if (INVALID_CHARS[i] != File.separatorChar)
                if (-1 != path.indexOf(INVALID_CHARS[i]))
                    path = path.replace(INVALID_CHARS[i], SANITIZED_CHAR);

        return path;
    }

    /**
     * Given an input, return a sanitized form of the input suitable for use as
     * a file/directory name
     *
     * @param filename the filename to sanitize.
     * @return a sanitized version of the input
     */
    public static String sanitizeFileName(String filename) {

        for (int i = 0; i < INVALID_CHARS.length; i++)
            if (-1 != filename.indexOf(INVALID_CHARS[i]))
                filename = filename.replace(INVALID_CHARS[i], SANITIZED_CHAR);

        return filename;
    }

    /**
     * Creates a new directory with minimum permissions. The directory is not
     * readable or writable by anyone other than the owner. The parent
     * directories are not created; they must exist before this is called.
     *
     * @throws IOException
     */
    public static void createRestrictedDirectory(File directory) throws IOException {
        createRestrictedFile(directory, true, true);
    }

    /**
     * Creates a new file with minimum permissions. The file is not readable or
     * writable by anyone other than the owner. If writeableByOnwer is false,
     * even the owner can not write to it.
     *
     * @throws IOException
     */
    public static void createRestrictedFile(File file, boolean writableByOwner) throws IOException {
        createRestrictedFile(file, false, writableByOwner);
    }

    /**
     * Tries to create the ancestor directories of file f. Throws
     * an IOException if it can't be created (but not if it was
     * already there).
     * @param f
     * @param eMsg - the message to use for the exception. null
     * if the file name is to be used.
     * @throws IOException if the directory can't be created and doesn't exist.
     */
    public static void createParentDir(File f, String eMsg) throws IOException {
        File parent = f.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException(R("RCantCreateDir",
                    eMsg == null ? parent : eMsg));
        }
    }

    /**
     * Tries to create the ancestor directories of file f. Throws
     * an IOException if it can't be created (but not if it was
     * already there).
     * @param f
     * @throws IOException if the directory can't be created and doesn't exist.
     */
    public static void createParentDir(File f) throws IOException {
        createParentDir(f, null);
    }

    /**
     * Tries to delete file f. If the file exists but couldn't be deleted,
     * print an error message to stderr with the file name, or eMsg if eMsg
     * is not null.
     * @param f the file to be deleted
     * @param eMsg the message to print on failure (or null to print the
     * the file name).
     */
    public static void deleteWithErrMesg(File f, String eMsg) {
        if (f.exists()) {
            if (!f.delete()) {
                System.err.println(R("RCantDeleteFile", eMsg == null ? f : eMsg));
            }
        }
    }

    /**
     * Tries to delete file f. If the file exists but couldn't be deleted,
     * print an error message to stderr with the file name.
     * @param f the file to be deleted
     */
    public static void deleteWithErrMesg(File f) {
        deleteWithErrMesg(f, null);
    }

    /**
     * Creates a new file or directory with minimum permissions. The file is not
     * readable or writable by anyone other than the owner. If writeableByOnwer
     * is false, even the owner can not write to it. If isDir is true, then the
     * directory can be executed by the owner
     *
     * @throws IOException
     */
    private static void createRestrictedFile(File file, boolean isDir, boolean writableByOwner) throws IOException {

        File tempFile = null;

        tempFile = new File(file.getCanonicalPath() + ".temp");

        if (isDir) {
            if (!tempFile.mkdir()) {
                throw new IOException(R("RCantCreateDir", tempFile));
            }
        } else {
            if (!tempFile.createNewFile()) {
                throw new IOException(R("RCantCreateFile", tempFile));
            }
        }

        // remove all permissions
        if (!tempFile.setExecutable(false, false)) {
            throw new IOException(R("RRemoveXPermFailed", tempFile));
        }
        if (!tempFile.setReadable(false, false)) {
            throw new IOException(R("RRemoveRPermFailed", tempFile));
        }
        if (!tempFile.setWritable(false, false)) {
            throw new IOException(R("RRemoveWPermFailed", tempFile));
        }

        // allow owner to read
        if (!tempFile.setReadable(true, true)) {
            throw new IOException(R("RGetRPermFailed", tempFile));
        }

        // allow owner to write
        if (writableByOwner && !tempFile.setWritable(true, true)) {
            throw new IOException(R("RGetWPermFailed", tempFile));
        }

        // allow owner to enter directories
        if (isDir && !tempFile.setExecutable(true, true)) {
            throw new IOException(R("RGetXPermFailed", tempFile));
        }

        // rename this file. Unless the file is moved/renamed, any program that
        // opened the file right after it was created might still be able to
        // read the data.
        if (!tempFile.renameTo(file)) {
            throw new IOException(R("RCantRename", tempFile, file));
        }

    }

    /**
     * Returns a String that is suitable for using in GUI elements for
     * displaying (long) paths to users.
     *
     * @param path a path that should be shortened
     * @return a shortened path suitable for displaying to the user
     */
    public static String displayablePath(String path) {
        final int DEFAULT_LENGTH = 40;
        return displayablePath(path, DEFAULT_LENGTH);
    }

    /**
     * Return a String that is suitable for using in GUI elements for displaying
     * paths to users. If the path is longer than visibleChars, it is truncated
     * in a display-friendly way
     *
     * @param path a path that should be shorted
     * @param visibleChars the maximum number of characters that path should fit
     *        into. Also the length of the returned string
     * @return a shortened path that contains limited number of chars
     */
    public static String displayablePath(String path, int visibleChars) {
        /*
         * use a very simple method: prefix + "..." + suffix
         *
         * where prefix is the beginning part of path (as much as we can squeeze in)
         * and suffix is the end path of path
         */

        if (path == null || path.length() <= visibleChars) {
            return path;
        }

        final String OMITTED = "...";
        final int OMITTED_LENGTH = OMITTED.length();
        final int MIN_PREFIX_LENGTH = 4;
        final int MIN_SUFFIX_LENGTH = 4;
        /*
         * we want to show things other than OMITTED. if we have too few for
         * suffix and prefix, then just return as much as we can of the filename
         */
        if (visibleChars < (OMITTED_LENGTH + MIN_PREFIX_LENGTH + MIN_SUFFIX_LENGTH)) {
            return path.substring(path.length() - visibleChars);
        }

        int affixLength = (visibleChars - OMITTED_LENGTH) / 2;
        String prefix = path.substring(0, affixLength);
        String suffix = path.substring(path.length() - affixLength);

        return prefix + OMITTED + suffix;
    }

    /**
     * Recursively delete everything under a directory. Works on either files or
     * directories
     *
     * @param file the file object representing what to delete. Can be either a
     *        file or a directory.
     * @param base the directory under which the file and its subdirectories must be located
     * @throws IOException on an io exception or if trying to delete something
     *         outside the base
     */
    public static void recursiveDelete(File file, File base) throws IOException {
        if (JNLPRuntime.isDebug()) {
            System.err.println("Deleting: " + file);
        }

        if (!(file.getCanonicalPath().startsWith(base.getCanonicalPath()))) {
            throw new IOException("Trying to delete a file outside Netx's basedir: "
                    + file.getCanonicalPath());
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                recursiveDelete(children[i], base);
            }
        }
        if (!file.delete()) {
            throw new IOException("Unable to delete file: " + file);
        }

    }

    /**
     * This will return a lock to the file specified.
     * 
     * @param path File path to file we want to lock.
     * @param shared Specify if the lock will be a shared lock.
     * @param allowBlock Specify if we should block when we can not get the
     *            lock. Getting a shared lock will always block.
     * @return FileLock if we were successful in getting a lock, otherwise null.
     * @throws FileNotFoundException If the file does not exist.
     */
    public static FileLock getFileLock(String path, boolean shared, boolean allowBlock) throws FileNotFoundException {
        RandomAccessFile rafFile = new RandomAccessFile(path, "rw");
        FileChannel fc = rafFile.getChannel();
        FileLock lock = null;
        try {
            if (!shared) {
                if (allowBlock) {
                    lock = fc.lock(0, Long.MAX_VALUE, false);
                } else {
                    lock = fc.tryLock(0, Long.MAX_VALUE, false);
                }
            } else { // We want shared lock. This will block regardless if allowBlock is true or not.
                // Test to see if we can get a shared lock.
                lock = fc.lock(0, 1, true); // Block if a non exclusive lock is being held.
                if (!lock.isShared()) { // This lock is an exclusive lock. Use alternate solution.
                    FileLock tempLock = null;
                    for (long pos = 1; tempLock == null && pos < Long.MAX_VALUE - 1; pos++) {
                        tempLock = fc.tryLock(pos, 1, false);
                    }
                    lock.release();
                    lock = tempLock; // Get the unique exclusive lock.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lock;
    }
}
