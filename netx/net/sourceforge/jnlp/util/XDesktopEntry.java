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

import net.sourceforge.jnlp.util.logging.OutputController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
        fileContents += "Name=" + getDesktopIconName() + "\n";
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

        //Shortcut executes the jnlp as it was with system preferred java. It should work fine offline
        fileContents += "Exec=" + "javaws" + " \"" + file.getSourceLocation() + "\"\n";

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

    public File getShortcutTmpFile() {
        String userTmp = JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_USER_TMP_DIR);
        File shortcutFile = new File(userTmp + File.separator + FileUtils.sanitizeFileName(file.getTitle()) + ".desktop");
        return shortcutFile;
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
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    /**
     * Install this XDesktopEntry into the user's desktop as a launcher
     */
    private void installDesktopLauncher() {
        File shortcutFile = getShortcutTmpFile();
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
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Execing: " + Arrays.toString(execString));
            Process installer = Runtime.getRuntime().exec(execString);
            new StreamEater(installer.getInputStream()).start();
            new StreamEater(installer.getErrorStream()).start();

            try {
                installer.waitFor();
            } catch (InterruptedException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }

            if (!shortcutFile.delete()) {
                throw new IOException("Unable to delete temporary file:" + shortcutFile);
            }

        } catch (FileNotFoundException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
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

            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Cached desktop shortcut icon: " + this.iconLocation);
        }
    }

    public String getDesktopIconName() {
        return sanitize(file.getTitle());
    }

    public File getLinuxDesktopIconFile() {
        return new File(findFreedesktopOrgDesktopPathCatch() + "/" + getDesktopIconName() + ".desktop");
    }

    private static String findFreedesktopOrgDesktopPathCatch() {
        try {
            return findFreedesktopOrgDesktopPath();
        } catch (Exception ex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            return System.getProperty("user.home") + "/Desktop/";
        }
    }

    /**
     * Instead of having all this parsing of user-dirs.dirs and replacing
     * variables we can execute `echo $(xdg-user-dir DESKTOP)` and it will do
     * all the job in case approaches below become failing
     *
     * @return variables (if declared) and quotation marks (unless escaped) free
     * path
     * @throws IOException if no file do not exists or key with desktop do not
     * exists
     */
    private static String findFreedesktopOrgDesktopPath() throws IOException {
        File userDirs = new File(System.getProperty("user.home") + "/.config/user-dirs.dirs");
        if (!userDirs.exists()) {
            return System.getProperty("user.home") + "/Desktop/";
        }
        return getFreedesktopOrgDesktopPathFrom(userDirs);
    }

    private static String getFreedesktopOrgDesktopPathFrom(File userDirs) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(userDirs));
        try {
            return getFreedesktopOrgDesktopPathFrom(r);
        } finally {
            r.close();
        }

    }
    static final String XDG_DESKTOP_DIR = "XDG_DESKTOP_DIR";

    static String getFreedesktopOrgDesktopPathFrom(BufferedReader r) throws IOException {
        while (true) {
            String s = r.readLine();
            if (s == null) {
                throw new IOException("End of user-dirs found, but no " + XDG_DESKTOP_DIR + " key found");
            }
            s = s.trim();
            if (s.startsWith(XDG_DESKTOP_DIR)) {
                if (!s.contains("=")) {
                    throw new IOException(XDG_DESKTOP_DIR + " has no value");
                }
                String[] keyAndValue = s.split("=");
                keyAndValue[1] = keyAndValue[1].trim();
                String filteredQuotes = filterQuotes(keyAndValue[1]);
                return evaluateLinuxVariables(filteredQuotes);
            }
        }
    }
    private static final String MIC = "MAGIC_QUOTIN_ITW_CONSTANT_FOR_DUMMIES";

    private static String filterQuotes(String string) {
        //get rid of " but not of 
        String s = string.replaceAll("\\\\\"", MIC);
        s = s.replaceAll("\"", "");
        s = s.replaceAll(MIC, "\\\"");
        return s;
    }

    private static String evaluateLinuxVariables(String orig) {
        return evaluateLinuxVariables(orig, System.getenv());
    }

    private static String evaluateLinuxVariables(String orig, Map<String, String> variables) {
        Set<Entry<String, String>> env = variables.entrySet();
        List<Entry<String, String>> envVariables = new ArrayList<Entry<String, String>>(env);
        Collections.sort(envVariables, new Comparator<Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                return o2.getKey().length() - o1.getKey().length();
            }
        });
        while (true) {
            String before = orig;
            for (Entry<String, String> entry : envVariables) {
                orig = orig.replaceAll("\\$" + entry.getKey(), entry.getValue());
            }
            if (before.equals(orig)) {
                return orig;
            }
        }

    }
}
