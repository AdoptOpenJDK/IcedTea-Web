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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.PathsAndFiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.jvm.JvmUtils.getJavaWsBin;

/**
 * This class builds a (freedesktop.org) desktop entry out of a {@link JNLPFile}
 * . This entry can be used to install desktop shortcuts. See xdg-desktop-icon
 * (1) and http://standards.freedesktop.org/desktop-entry-spec/latest/ for more
 * information
 *
 * @author Omair Majid
 *
 *
 * This class builds also (freedesktop.org) menu entry out of a {@link JNLPFile}
 * Few notes valid November 2014:
 *    Mate/gnome 2/xfce - no meter of exec or icon put icon to defined/"others" Category
 *                      - name is as expected Name's value
 *                      - if removed, xfce kept icon until login/logout
 *    kde 4 - unknown Cathegory is sorted to Lost &amp; Found -thats bad
 *          - if icon is not found, nothing shows
 *          - name is GENERIC NAME and then little name
 *    Gnome 3 shell - exec must be valid program!
 *                  - also had issues with icon
 *
 * conclusion:
 *  - backup icon to .config
 *  - use "Network" category
 *  - force valid launcher

 * @author (not so proudly) Jiri Vanek
 */

public class XDesktopEntry implements GenericDesktopEntry {

    private final static Logger LOG = LoggerFactory.getLogger(XDesktopEntry.class);

    private JNLPFile file = null;
    private int iconSize = -1;
    private String iconLocation = null;

    //in pixels
    private static final int[] VALID_ICON_SIZES = new int[] { 16, 22, 32, 48, 64, 128 };
    private static final String GIF = "gif";
    private static final String JPG = "jpg";
    private static final String JPEG = "jpeg";
    private static final String PNG = "png";
    //browsers we try to find  on path for html shortcut
    public static final String[] BROWSERS = new String[]{"firefox", "midori", "epiphany", "opera", "chromium", "chrome", "konqueror"};
    public static final String FAVICON = "favicon.ico";

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
     * Returns the contents of the {@link XDesktopEntry} as a String.
     * @param menu whether to create this icon to menu
     * @param info result of user's interference
     * @return string with desktop shortcut specification
     */
    String getContent(boolean menu, AccessWarningPaneComplexReturn.ShortcutResult info) {
        File generatedJnlp = null;

        String fileContents = "[Desktop Entry]\n";
        fileContents += "Version=1.0\n";
        fileContents += "Name=" + getShortcutName() + "\n";
        fileContents += "GenericName=Java Web Start Application\n";
        fileContents += "Comment=" + sanitize(file.getInformation().getDescription()) + "\n";
        if (menu) {
            //keeping the default category because of KDE
            String menuString = "Categories=Network;";
            if (file.getInformation().getShortcut() != null
                    && file.getInformation().getShortcut().getMenu() != null
                    && file.getInformation().getShortcut().getMenu().getSubMenu() != null
                    && !file.getInformation().getShortcut().getMenu().getSubMenu().trim().isEmpty()) {
                menuString += file.getInformation().getShortcut().getMenu().getSubMenu().trim() + ";";
            }
            menuString += "Java;Javaws;";
            fileContents += menuString + "\n";
        }
        fileContents += "Type=Application\n";
        if (iconLocation != null) {
            fileContents += "Icon=" + iconLocation + "\n";
        } else {
            fileContents += "Icon=" + JAVAWS + "\n";

        }
        if (file.getInformation().getVendor() != null) {
            fileContents += "X-Vendor=" + sanitize(file.getInformation().getVendor()) + "\n";
        }
        String exec;
        exec = "Exec=" + getJavaWsBin() + " \"" + file.getSourceLocation() + "\"\n";
        fileContents += exec;
        return fileContents;
    }

    public static String getBrowserBin() {
        String pathResult = JvmUtils.findOnPath(BROWSERS);
        if (pathResult != null) {
            return pathResult;
        } else {
            return "browser_not_found";
        }

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
        input = FileUtils.sanitizeFileName(input, '-');
        //return first line or replace new lines by space?
        return input.split("\\R")[0];
    }

    File getShortcutTmpFile() {
        String userTmp = PathsAndFiles.TMP_DIR.getFullPath();
        File shortcutFile = new File(userTmp + File.separator + getShortcutFileName());
        return shortcutFile;
    }

    /**
     * Create a desktop shortcut for this desktop entry
     * @param menu how to create in menu
     * @param desktop how to create on desktop
     */
    @Override
    public void createDesktopShortcuts(AccessWarningPaneComplexReturn.ShortcutResult menu, AccessWarningPaneComplexReturn.ShortcutResult desktop) {
        boolean isDesktop = false;
        if (desktop != null && desktop.isCreate()) {
            isDesktop = true;
        }
        boolean isMenu = false;
        if (menu != null && menu.isCreate()) {
            isMenu = true;
        }
        try {
            if (isMenu || isDesktop) {
                try {
                    cacheIcon();
                } catch (NonFileProtocolException ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    //default icon will be used later
                }
            }
            if (isDesktop) {
                installDesktopLauncher(desktop);
            }
            if (isMenu) {
                installMenuLauncher(menu);
            }
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    /**
     * Install this XDesktopEntry into the user's menu.
     */
    private void installMenuLauncher(AccessWarningPaneComplexReturn.ShortcutResult info) {
        //TODO add itweb-settings tab which allows to remove individual items/icons
        try {
            File f = getLinuxMenuIconFile();
            FileUtils.saveFileUtf8(getContent(true, info), f);
            LOG.info("Menu item created: {}", f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    /**
     * Install this XDesktopEntry into the user's desktop as a launcher.
     */
    private void installDesktopLauncher(AccessWarningPaneComplexReturn.ShortcutResult info) {
        File shortcutFile = getShortcutTmpFile();
        try {

            if (!shortcutFile.getParentFile().isDirectory() && !shortcutFile.getParentFile().mkdirs()) {
                throw new IOException(shortcutFile.getParentFile().toString());
            }

            FileUtils.createRestrictedFile(shortcutFile);
            FileUtils.saveFileUtf8(getContent(false, info), shortcutFile);

            /*
             * Install the desktop entry
             */

            String[] execString = new String[] { "xdg-desktop-icon", "install", "--novendor",
                    shortcutFile.getCanonicalPath() };
            LOG.debug("Executing: " + Arrays.toString(execString));
            ProcessBuilder pb = new ProcessBuilder(execString);
            pb.inheritIO();
            Process installer = pb.start();

            ProcessUtils.waitForSafely(installer);

            if (!shortcutFile.delete()) {
                throw new IOException("Unable to delete temporary file:" + shortcutFile);
            }

        } catch (FileNotFoundException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }


    @Override
    public void refreshExistingShortcuts(boolean desktop, boolean menu) {
        //TODO TODO TODO TODO TODO TODO TODO TODO
        //check existing jnlp files
        //check launcher
        //get where it points
        //try all supported  shortcuts methods
        //choose the one which have most similar result to existing ones

    }

    @Override
    public File getGeneratedJnlpFileName() {
        String name = FileUtils.sanitizeFileName(file.getShortcutName());
        while (name.endsWith(".jnlp")) {
            name = name.substring(0, name.length() - 5);
        }
        name += ".jnlp";
        return new File(findAndVerifyGeneratedJnlpDir(), name);
    }

    private class NonFileProtocolException extends Exception {

        private NonFileProtocolException(String unable_to_cache_icon) {
            super(unable_to_cache_icon);
        }

    }

    /**
     * Cache the icon for the desktop entry
     */
    private void cacheIcon() throws IOException, NonFileProtocolException {

        URL uiconLocation = file.getInformation().getIconLocation(IconKind.SHORTCUT, iconSize,
                iconSize);

        if (uiconLocation == null) {
            uiconLocation = file.getInformation().getIconLocation(IconKind.DEFAULT, iconSize,
                    iconSize);
        }

        File cacheFile;
        if (uiconLocation != null) {
            cacheFile  = CacheUtil.downloadAndGetCacheFile(uiconLocation, null);
        } else {
            cacheFile = downloadFavIcon(file);
        }

        if (cacheFile == null) {
            cantCache();
        }

        this.iconLocation = cacheFile.getAbsolutePath();
        // icons are never uninstalled by itw. however, system clears them on its own.. sometimes.
        // once the -Xclearcache is run, system MAY clean it later, and so image wil be lost.
        // copy icon somewhere where -Xclearcache can not
        PathsAndFiles.ICONS_DIR.getFile().mkdirs();
        String targetName = cacheFile.getName();
        if (targetName.equals(FAVICON)) {
            targetName = file.getNotNullProbableCodeBase().getHost() + ".ico";
        }
        File target = null;
        if (OsUtil.isWindows() &&
            (targetName.toLowerCase().endsWith(GIF)  ||
             targetName.toLowerCase().endsWith(JPG)  ||
             targetName.toLowerCase().endsWith(JPEG) ||
             targetName.toLowerCase().endsWith(PNG))) {
            target = convertToIco(cacheFile, targetName);
        }
        if (target == null) {
            target = new File(PathsAndFiles.ICONS_DIR.getFile(), targetName);
            Files.copy(cacheFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        this.iconLocation = target.getAbsolutePath();
        LOG.debug("Cached desktop shortcut icon: " + target + " ,  With source from: " + cacheFile.getAbsolutePath());
    }

    private File convertToIco(final File source, final String targetName) {
        try {
            BufferedImage img = ImageIO.read(source);
            short bitCount = (short)img.getColorModel().getPixelSize();
            // Images with less than 32 bit color depth
            // are very hard to produce in ico format.
            // Therefore, the image is converted if necessary
            if (bitCount < 32)
            {
                final BufferedImage dest = new BufferedImage(
                                        img.getWidth(), img.getHeight(),
                                        BufferedImage.TYPE_INT_ARGB);
                final ColorConvertOp cco = new ColorConvertOp(
                                        img.getColorModel().getColorSpace(),
                                        dest.getColorModel().getColorSpace(),
                                        null);
                img = cco.filter(img, dest);
                bitCount = (short)img.getColorModel().getPixelSize();
            }
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            boolean written = ImageIO.write(img, PNG, bos);
            if (!written)
                return null;

            final byte width  = (byte)(img.getWidth()  < 256 ? img.getWidth()   : 0);
            final byte height = (byte)(img.getHeight() < 256 ? img.getHeight()  : 0);
            final byte colorCount = (byte)(bitCount < 8 ? Math.pow(2, bitCount) : 0);
            final byte[] imgBytes = bos.toByteArray();
            final int offset = 22;
            final int fileSize = imgBytes.length + offset;
            final ByteBuffer bytes = ByteBuffer.allocate(fileSize);
            bytes.order(ByteOrder.LITTLE_ENDIAN);

            // Header
            bytes.putShort((short) 0);      // Reserved, must be 0
            bytes.putShort((short) 1);      // Image type: 1 for ico
            bytes.putShort((short) 1);      // Number of images in the file
            // Directory Entry
            bytes.put(width);               // Image width
            bytes.put(height);              // Image height
            bytes.put(colorCount);          // Number of colors
            bytes.put((byte) 0);            // Reserved, must be 0
            bytes.putShort((short) 1);      // Number of color planes
            bytes.putShort(bitCount);       // Number of bits per pixel
            bytes.putInt(imgBytes.length);  // Image size
            bytes.putInt(offset);           // Image offset
            // Image
            bytes.put(imgBytes);            // Image data

            final File target = new File(PathsAndFiles.ICONS_DIR.getFile(),
                                         targetName.substring(0,
                                         targetName.lastIndexOf('.')) + ".ico");
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(bytes.array());
                fos.flush();
            }
            return target;
        }
        catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            return null;
        }
    }

    String cacheAndGetIconLocation() {
        try {
            cacheIcon();
        } catch (NonFileProtocolException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return this.iconLocation;
    }
    static List<String> possibleFavIconLocations(String path) {
        while (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        List<String> r = new ArrayList<>();
        do {
            r.add(path);
            int last = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
            if (last >= 0) {
                path = path.substring(0, last);
            }
        } while (path.contains("/") || path.contains("\\"));
        if (!r.contains("")) {
            r.add("");
        }
        return r;
    }

    private static URL favUrl(String delimiter, String path, JNLPFile file) throws MalformedURLException {
        final String separator = path.endsWith(delimiter) ? "" : delimiter;
        return new URL(
                file.getNotNullProbableCodeBase().getProtocol(),
                file.getNotNullProbableCodeBase().getHost(),
                file.getNotNullProbableCodeBase().getPort(),
                path + separator + FAVICON);
    }

    private static File downloadFavIcon(JNLPFile file) {
        try {
            for (String path : possibleFavIconLocations(file.getNotNullProbableCodeBase().getPath())) {
                URL favico = favUrl("/", path, file);
                //JNLPFile.openURL(favico, null, UpdatePolicy.ALWAYS);
                //this MAY throw npe, if url (specified in jnlp) points to 404
                //the below works just fine
                File cacheFile = CacheUtil.downloadAndGetCacheFile(favico, null);
                if (cacheFile != null) {
                    return cacheFile;
                }
            }
            //the icon is much more likely to be found behind / then behind \/
            //So rather duplicating the code here, then wait double time if the icon will be at the start of the path
            for (String path : possibleFavIconLocations(file.getNotNullProbableCodeBase().getPath())) {
                URL favico = favUrl("\\", path, file);
                File cacheFile = CacheUtil.downloadAndGetCacheFile(favico, null);
                if (cacheFile != null) {
                    return cacheFile;
                }
            }
        } catch (Exception ex) {
            //favicon 404 or similar
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        return null;
    }

    private void cantCache() throws NonFileProtocolException {
        throw new NonFileProtocolException("Unable to cache icon");
    }

    private String getShortcutName() {
        return sanitize(file.getShortcutName());
    }

    @Override
    public File getDesktopIconFile() {
            return new File(getDesktop(), getShortcutFileName());
    }

    static File getDesktop() {
        return new File(findFreedesktopOrgDesktopPathCatch());
    }

    @Override
    public File getLinuxMenuIconFile() {
        return new File(findAndVerifyJavawsMenuDir() + "/" + getShortcutFileName());
    }

    private String getShortcutFileName() {
        return getShortcutName() + ".desktop";
    }

    private static String findAndVerifyGeneratedJnlpDir() {
        return findAndVerifyBasicDir(PathsAndFiles.GEN_JNLPS_DIR.getFile(), "directory for storing generated jnlps cannot be created. You may expect failure");
    }

    private static String findAndVerifyJavawsMenuDir() {
        return findAndVerifyBasicDir(PathsAndFiles.MENUS_DIR.getFile(), "directory for storing menu entry cannot be created. You may expect failure");
    }

    private static String findAndVerifyBasicDir(File f, String message) {
        String fPath = f.getAbsolutePath();
        if (!f.exists()) {
            if (!f.mkdirs()) {
                LOG.error(fPath + " - " + message);
            }
        }
        return fPath;
    }

    public static String findFreedesktopOrgDesktopPathCatch() {
        try {
            return findFreedesktopOrgDesktopPath();
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return JavaSystemProperties.getUserHome() + "/Desktop";
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
        File userDirs = new File(JavaSystemProperties.getUserHome() + "/.config/user-dirs.dirs");
        if (!userDirs.exists()) {
            return JavaSystemProperties.getUserHome() + "/Desktop/";
        }
        return getFreedesktopOrgDesktopPathFrom(userDirs);
    }

    private static String getFreedesktopOrgDesktopPathFrom(File userDirs) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(userDirs))) {
            return getFreedesktopOrgDesktopPathFrom(r);
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
        List<Entry<String, String>> envVariables = new ArrayList<>(variables.entrySet());
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

     @Override
    public void createShortcutOnWindowsDesktop() {
        throw new UnsupportedOperationException("not supported on linux like systems");
    }

    @Override
    public void createWindowsMenu() {
        throw new UnsupportedOperationException("not supported on linux like systems");
    }
}
