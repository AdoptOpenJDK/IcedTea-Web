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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import net.sourceforge.jnlp.IconDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.StreamEater;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * This class builds a (freedesktop.org) desktop entry out of a {@link JNLPFile}
 * . This entry can be used to install desktop shortcuts. See xdg-desktop-icon
 * (1) and http://standards.freedesktop.org/desktop-entry-spec/latest/ for more
 * information
 *
 * @author Omair Majid
 *
 */
public class XDesktopEntry {

    public static final String JAVA_ICON_NAME = "java";

    private JNLPFile file = null;
    private int iconSize = -1;
    private String iconLocation = null;

    private int[] VALID_ICON_SIZES = new int[] { 16, 22, 32, 48, 64, 128 };

    /**
     * Create a XDesktopEntry for the given JNLP file
     *
     * @param file a {@link JNLPFile} that indicates the application to launch
     */
    public XDesktopEntry(JNLPFile file) {
        this.file = file;

        /* looks like a good initial value */
        iconSize = VALID_ICON_SIZES[2];
    }

    /**
     * Returns the contents of the {@link XDesktopEntry} through the
     * {@link Reader} interface.
     */
    public Reader getContentsAsReader() {

        String cacheDir = JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR);
        File cacheFile = CacheUtil.getCacheFile(file.getSourceLocation(), null);

        String fileContents = "[Desktop Entry]\n";
        fileContents += "Version=1.0\n";
        fileContents += "Name=" + sanitize(file.getTitle()) + "\n";
        fileContents += "GenericName=Java Web Start Application\n";
        fileContents += "Comment=" + sanitize(file.getInformation().getDescription()) + "\n";
        fileContents += "Type=Application\n";
        if (iconLocation != null) {
            fileContents += "Icon=" + iconLocation + "\n";
        } else {
            fileContents += "Icon=" + JAVA_ICON_NAME + "\n";

        }
        if (file.getInformation().getVendor() != null) {
            fileContents += "Vendor=" + sanitize(file.getInformation().getVendor()) + "\n";
        }

        //Shortcut executes the jnlp from cache and system preferred java..
        fileContents += "Exec=" + "javaws" + " \"" + cacheFile.getAbsolutePath() + "\"\n";

        return new StringReader(fileContents);

    }

    /**
     * Sanitizes a string so that it can be used safely in a key=value pair in a
     * desktop entry file.
     *
     * @param input a String to sanitize
     * @return a string safe to use as either the key or the value in the
     * key=value pair in a desktop entry file
     */
    private static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        /* key=value pairs must be a single line */
        return input.split("\n")[0];
    }

    /**
     * Get the size of the icon (in pixels) for the desktop shortcut
     */
    public int getIconSize() {
        return iconSize;
    }

    /**
     * Set the icon size to use for the desktop shortcut
     *
     * @param size the size (in pixels) of the icon to use. Commonly used sizes
     *        are of 16, 22, 32, 48, 64 and 128
     */
    public void setIconSize(int size) {
        iconSize = size;
    }

    /**
     * Create a desktop shortcut for this desktop entry
     */
    public void createDesktopShortcut() {
        try {
            cacheIcon();
            installDesktopLauncher();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Install this XDesktopEntry into the user's desktop as a launcher
     */
    private void installDesktopLauncher() {
        File shortcutFile = new File(JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_USER_TMP_DIR)
                + File.separator + FileUtils.sanitizeFileName(file.getTitle()) + ".desktop");
        try {

            if (!shortcutFile.getParentFile().isDirectory() && !shortcutFile.getParentFile().mkdirs()) {
                throw new IOException(shortcutFile.getParentFile().toString());
            }

            FileUtils.createRestrictedFile(shortcutFile, true);

            /*
             * Write out a Java String (UTF-16) as a UTF-8 file
             */

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(shortcutFile),
                    Charset.forName("UTF-8"));
            Reader reader = getContentsAsReader();

            char[] buffer = new char[1024];
            int ret = 0;
            while (-1 != (ret = reader.read(buffer))) {
                writer.write(buffer, 0, ret);
            }

            reader.close();
            writer.close();

            /*
             * Install the desktop entry
             */

            String[] execString = new String[] { "xdg-desktop-icon", "install", "--novendor",
                    shortcutFile.getCanonicalPath() };
            if (JNLPRuntime.isDebug()) {
                System.err.println("Execing: " + Arrays.toString(execString));
            }
            Process installer = Runtime.getRuntime().exec(execString);
            new StreamEater(installer.getInputStream()).start();
            new StreamEater(installer.getErrorStream()).start();

            try {
                installer.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!shortcutFile.delete()) {
                throw new IOException("Unable to delete temporary file:" + shortcutFile);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cache the icon for the desktop entry
     */
    private void cacheIcon() {

        URL iconLocation = file.getInformation().getIconLocation(IconDesc.SHORTCUT, iconSize,
                iconSize);

        if (iconLocation == null) {
            iconLocation = file.getInformation().getIconLocation(IconDesc.DEFAULT, iconSize,
                    iconSize);
        }

        if (iconLocation != null) {
            String location = CacheUtil.getCachedResource(iconLocation, null, UpdatePolicy.SESSION)
                    .toString();
            if (!location.startsWith("file:")) {
                throw new RuntimeException("Unable to cache icon");
            }

            this.iconLocation = location.substring("file:".length());

            if (JNLPRuntime.isDebug()) {
                System.err.println("Cached desktop shortcut icon: " + this.iconLocation);
            }
        }
    }

}
