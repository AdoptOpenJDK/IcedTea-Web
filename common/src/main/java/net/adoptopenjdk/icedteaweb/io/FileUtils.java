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

package net.adoptopenjdk.icedteaweb.io;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.validator.DirectoryCheckResults;
import net.adoptopenjdk.icedteaweb.validator.DirectoryValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class contains a few file-related utility functions.
 *
 * @author Omair Majid
 */

public final class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private static final String MD5 = "MD5";

    private static final String WIN_DRIVE_LETTER_COLON_WILDCHAR = "WINDOWS_VERY_SPECIFIC_DOUBLEDOT";

    /**
     * Indicates whether a file was successfully opened. If not, provides specific reasons
     * along with a general failure case
     */
    public enum OpenFileResult {
        /**
         * The file was successfully opened
         */
        SUCCESS,
        /**
         * The file could not be opened, for non-specified reasons
         */
        FAILURE,
        /**
         * The file could not be opened because it did not exist and could not be created
         */
        CANT_CREATE,
        /**
         * The file can be opened but in read-only
         */
        CANT_WRITE,
        /**
         * The specified path pointed to a non-file filesystem object, ie a directory
         */
        NOT_FILE,
    }

    /**
     * list of characters not allowed in filenames
     */
    static final List<Character> INVALID_PATH = Arrays.asList(':', '*', '?', '"', '<', '>', '|', '[', ']', ';', '=', ',');
    private static final List<Character> INVALID_NAME = new ArrayList<>(INVALID_PATH);

    static {
        INVALID_NAME.add(0, '\\');
        INVALID_NAME.add(0, '/');
    }

    private static final char SANITIZED_CHAR = '_';

    /**
     * Clean up a string by removing characters that can't appear in a local
     * file name.
     *
     * @param path the path to sanitize
     * @return a sanitized version of the input which is suitable for using as a
     * file path
     */
    public static String sanitizePath(String path) {
        return sanitizePath(path, SANITIZED_CHAR);
    }

    public static String sanitizePath(String path, char substitute) {
        //on windows, we can receive both c:/path/ and c:\path\
        path = path.replace("\\", "/");
        if (OsUtil.isWindows() && path.matches("^[a-zA-Z]\\:.*")) {
            path = path.replaceFirst(":", WIN_DRIVE_LETTER_COLON_WILDCHAR);
        }
        for (int i = 0; i < INVALID_PATH.size(); i++) {
            if (-1 != path.indexOf(INVALID_PATH.get(i))) {
                path = path.replace(INVALID_PATH.get(i), substitute);
            }
        }
        if (OsUtil.isWindows()) {
            path = path.replaceFirst(WIN_DRIVE_LETTER_COLON_WILDCHAR, ":");
        }
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
        return sanitizeFileName(filename, SANITIZED_CHAR);
    }

    public static String sanitizeFileName(String filename, char substitute) {

        for (int i = 0; i < INVALID_NAME.size(); i++) {
            if (-1 != filename.indexOf(INVALID_NAME.get(i))) {
                filename = filename.replace(INVALID_NAME.get(i), substitute);
            }
        }

        return filename;
    }

    /**
     * Tries to create the ancestor directories of file f. Throws
     * an IOException if it can't be created (but not if it was
     * already there).
     *
     * @param f    file to provide parent directory
     * @param eMsg - the message to use for the exception. null
     *             if the file name is to be used.
     * @throws IOException if the directory can't be created and doesn't exist.
     */
    public static void createParentDir(File f, String eMsg) throws IOException {
        File parent = f.getParentFile();
        // warning, linux and windows behave differently. Below snippet will pass on win(security hole), fail on linux
        // warning  mkdir is canonicaling, but exists/isDirectory is not. So  where mkdirs return true, and really creates dir, isDirectory can still return false
        // can be seen on this example
        // mkdirs /a/b/../c
        // where b do not exists will lead creation of /a/c
        // but exists on /a/b/../c is false on linux  even afterwards
        // without hexing of .. paths,
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory " + (eMsg == null ? parent : eMsg));
        }
    }

    /**
     * Tries to create the ancestor directories of file f. Throws
     * an IOException if it can't be created (but not if it was
     * already there).
     *
     * @param f file which parent will be created
     * @throws IOException if the directory can't be created and doesn't exist.
     */
    public static void createParentDir(File f) throws IOException {
        createParentDir(f, null);
    }

    /**
     * Tries to delete file f. If the file exists but couldn't be deleted,
     * print an error message to stderr with the file name, or eMsg if eMsg
     * is not null.
     *
     * @param f    the file to be deleted
     * @param eMsg the message to print on failure (or null to print the
     *             the file name).
     */
    public static void deleteWithErrMesg(File f, String eMsg) {
        if (f.exists()) {
            if (!f.delete()) {
                LOG.error("Cannot delete file {}", eMsg == null ? f : eMsg);
            }
        }
    }

    /**
     * Ensure that the parent directory of the file exists and that we are
     * able to create and access files within this directory
     *
     * @param file    the {@link File} representing a Java Policy file to test
     * @param isDebug output debug information
     * @return a {@link DirectoryCheckResults} object representing the results of the test
     */
    private static DirectoryCheckResults testDirectoryPermissions(final File file, final boolean isDebug) {
        try {
            final File canonicalFile = file.getCanonicalFile();
            final File parentFile = canonicalFile.getParentFile();
            if (parentFile == null || !parentFile.exists()) {
                return null;
            }
            final List<File> policyDirectory = new ArrayList<>();
            policyDirectory.add(parentFile);
            final DirectoryValidator validator = new DirectoryValidator(policyDirectory);
            final DirectoryCheckResults result = validator.ensureDirs(isDebug);

            return result;
        } catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            return null;
        }
    }

    /**
     * Verify that a given file object points to a real, accessible plain file.
     *
     * @param file    the {@link File} to verify
     * @param isDebug output debug information
     * @return an {@link OpenFileResult} representing the accessibility level of the file
     */
    public static OpenFileResult testFilePermissions(File file, boolean isDebug) {
        if (file == null || !file.exists()) {
            return OpenFileResult.FAILURE;
        }
        try {
            file = file.getCanonicalFile();
        } catch (final IOException e) {
            return OpenFileResult.FAILURE;
        }
        final DirectoryCheckResults dcr = FileUtils.testDirectoryPermissions(file, isDebug);
        if (dcr != null && dcr.getFailures() == 0) {
            if (file.isDirectory()) {
                return OpenFileResult.NOT_FILE;
            }
            try {
                if (!file.exists() && !file.createNewFile()) {
                    return OpenFileResult.CANT_CREATE;
                }
            } catch (IOException e) {
                return OpenFileResult.CANT_CREATE;
            }
            final boolean read = file.canRead(), write = file.canWrite();
            if (read && write) {
                return OpenFileResult.SUCCESS;
            } else if (read) {
                return OpenFileResult.CANT_WRITE;
            } else {
                return OpenFileResult.FAILURE;
            }
        }
        return OpenFileResult.FAILURE;
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
     * @param path         a path that should be shorted
     * @param visibleChars the maximum number of characters that path should fit
     *                     into. Also the length of the returned string
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
     *             file or a directory.
     * @param base the directory under which the file and its subdirectories must be located
     * @throws IOException on an io exception or if trying to delete something
     *                     outside the base
     */
    public static void recursiveDelete(File file, File base) throws IOException {
        LOG.debug("Deleting: {}", file);

        if (!file.exists()) {
            return;
        }

        if (!(file.getCanonicalFile().toPath().startsWith(base.getCanonicalFile().toPath()))) {
            throw new IOException("Trying to delete a file outside Netx's basedir: "
                    + file.getCanonicalPath());
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    recursiveDelete(child, base);
                }
            }
        }

        if (!file.delete()) {
            throw new IOException("Unable to delete file: " + file);
        }

    }

    /**
     * This will return a lock to the file specified.
     *
     * @param path       File path to file we want to lock.
     * @param shared     Specify if the lock will be a shared lock.
     * @param allowBlock Specify if we should block when we can not get the
     *                   lock. Getting a shared lock will always block.
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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return lock;
    }

    public static String loadFileAsUtf8String(File f) throws IOException {
        return loadFileAsString(f, UTF_8);
    }

    public static String loadFileAsString(File f, Charset encoding) throws IOException {
        try (final FileInputStream is = new FileInputStream(f)) {
            return IOUtils.readContentAsString(is, encoding);
        }
    }

    /**
     * Save String into a file in UTF-8 encoding.
     *
     * @param content which will be saved to the file
     * @param f       file to be saved. No warnings provided
     * @throws IOException if save fails
     */
    public static void saveFileUtf8(final String content, final File f) throws IOException {
        try (final OutputStream out = new FileOutputStream(f)) {
            IOUtils.writeUtf8Content(out, content);
        }
    }

    public static byte[] getFileMD5Sum(final File file) throws NoSuchAlgorithmException, IOException {
        final MessageDigest md5;
        InputStream is = null;
        DigestInputStream dis = null;
        try {
            md5 = MessageDigest.getInstance(MD5);
            is = new FileInputStream(file);
            dis = new DigestInputStream(is, md5);

            md5.update(IOUtils.readContent(dis));
        } finally {
            if (is != null) {
                is.close();
            }
            if (dis != null) {
                dis.close();
            }
        }

        return md5.digest();
    }
}
