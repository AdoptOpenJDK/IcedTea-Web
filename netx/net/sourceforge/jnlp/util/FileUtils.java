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

import java.io.File;
import java.io.IOException;

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
     * @param input
     * @return a sanitized version of the input
     */
    public static String sanitizeFileName(String filename) {

        for (int i = 0; i < INVALID_CHARS.length; i++)
            if (-1 != filename.indexOf(INVALID_CHARS[i]))
                filename = filename.replace(INVALID_CHARS[i], SANITIZED_CHAR);

        return filename;
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

        int affixLength = (visibleChars - OMITTED_LENGTH)/2;
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

}
