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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.filechooser.FileSystemView;

import net.sourceforge.jnlp.IconDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.dialogresults.AccessWarningPaneComplexReturn;

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
public class XDesktopEntry {

    public static final String JAVA_ICON_NAME = "javaws";

    private JNLPFile file = null;
    private int iconSize = -1;
    private String iconLocation = null;

    //in pixels
    private static final int[] VALID_ICON_SIZES = new int[] { 16, 22, 32, 48, 64, 128 };
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
     * Returns the contents of the {@link XDesktopEntry} through the
     * {@link Reader} interface.
     * @param menu whether to create this icon to menu
     * @param info result of user's interference
     * @param isSigned whether the app is signed
     * @return reader with desktop shortcut specification
     */
    public Reader getContentsAsReader(boolean menu, AccessWarningPaneComplexReturn.ShortcutResult info, boolean isSigned) {

        File generatedJnlp = null;
        if (file instanceof PluginBridge && (info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.GENERATED_JNLP || info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JNLP_HREF)) {
            try {
                String content = ((PluginBridge) file).toJnlp(isSigned, info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JNLP_HREF, info.isFixHref());
                generatedJnlp = getGeneratedJnlpFileName();
                FileUtils.saveFile(content, generatedJnlp);
            } catch (Exception ex) {
                OutputController.getLogger().log(ex);
            }
        }
        
        String fileContents = "[Desktop Entry]\n";
        fileContents += "Version=1.0\n";
        fileContents += "Name=" + getDesktopIconName() + "\n";
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
            fileContents += "Icon=" + JAVA_ICON_NAME + "\n";

        }
        if (file.getInformation().getVendor() != null) {
            fileContents += "X-Vendor=" + sanitize(file.getInformation().getVendor()) + "\n";
        }
        String exec;
        String title = "xdesktop writing";
        if (JNLPRuntime.isWebstartApplication()) {
            String htmlSwitch = "";
            if (JNLPRuntime.isHtml()){
                htmlSwitch = " "+OptionsDefinitions.OPTIONS.HTML.option;
            }
            exec = "Exec="
                    + getJavaWsBin() + htmlSwitch + " \"" + file.getSourceLocation() + "\"\n";
            fileContents += exec;
        } else {
            if (info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.BROWSER) {
                String browser = info.getBrowser();
                if (browser == null) {
                    browser = getBrowserBin();
                }
                exec = "Exec="
                        + browser + " \"" + file.getSourceLocation() + "\"\n";
                fileContents += exec;
            } else if ((info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.GENERATED_JNLP
                    || info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JNLP_HREF) && generatedJnlp != null) {
                exec =  "Exec="
                        + getJavaWsBin() + " \"" + generatedJnlp.getAbsolutePath() + "\"\n";
                fileContents += exec;
                title = title + " (generated jnlp)";
            } else if (info.getShortcutType() == AccessWarningPaneComplexReturn.ShortcutResult.Shortcut.JAVAWS_HTML) {
                exec =  "Exec="
                        + getJavaWsBin() + " -html  \"" + file.getSourceLocation() + "\"\n";
                fileContents += exec;
            } else {
                exec = "Exec="
                        + getBrowserBin() + " \"" + file.getSourceLocation() + "\"\n";
                fileContents += exec;
            }
        }
        OutputController.getLogger().log(title + " " + exec);
        return new StringReader(fileContents);

    }

    public static String getBrowserBin() {
        String pathResult = findOnPath(BROWSERS);
        if (pathResult != null) {
            return pathResult;
        } else {
            return "browser_not_found";
        }
        
    }
    
    private String getJavaWsBin() {
        //Shortcut executes the jnlp as it was with system preferred java. It should work fine offline
        //absolute - works in case of self built
        String exec = System.getProperty("icedtea-web.bin.location");
        String pathResult = findOnPath(new String[]{"javaws", System.getProperty("icedtea-web.bin.name")});
        if (pathResult != null) {
            return pathResult;
        }
        if (exec != null) {
            return exec;
        }
        return "javaws";
    }
    
    
    private static String findOnPath(String[] bins) {
        String exec = null;
        //find if one of binaries is on path
        String path = System.getenv().get("PATH");
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv().get("path");
        }
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv().get("Path");
        }
        if (path != null && !path.trim().isEmpty()) {
            //relative - works with alternatives
            String[] paths = path.split(File.pathSeparator);
            outerloop:
            for (String bin : bins) {
                //when property is not set
                if (bin == null) {
                    continue;
                }
                for (String p : paths) {
                    if (new File(p, bin).exists()) {
                        exec = bin;
                        break outerloop;
                    }
                }

            }
        }
        return exec;
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
        return input.split("\n")[0];
    }

    /**
     * @return the size of the icon (in pixels) for the desktop shortcut
     */
    public int getIconSize() {
        return iconSize;
    }

    public File getShortcutTmpFile() {
        String userTmp = PathsAndFiles.TMP_DIR.getFullPath();
        File shortcutFile = new File(userTmp + File.separator + getDesktopIconFileName());
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
     * @param menu how to create in menu
     * @param desktop how to create on desktop
     * @param isSigned if it is signed
     */
    public void createDesktopShortcuts(AccessWarningPaneComplexReturn.ShortcutResult menu, AccessWarningPaneComplexReturn.ShortcutResult desktop, boolean isSigned) {
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
                    OutputController.getLogger().log(ex);
                    //default icon will be used later
                }
            }
            if (isDesktop) {
                installDesktopLauncher(desktop, isSigned);
            }
            if (isMenu) {
                installMenuLauncher(menu, isSigned);
            }
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    /**
     * Install this XDesktopEntry into the user's menu.
     */
    private void installMenuLauncher(AccessWarningPaneComplexReturn.ShortcutResult info, boolean isSigned) {
        //TODO add itweb-settings tab which alows to remove inidividual items/icons
        try {
            File f = getLinuxMenuIconFile();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f),
                    Charset.forName("UTF-8")); Reader reader = getContentsAsReader(true, info, isSigned)) {

                char[] buffer = new char[1024];
                int ret = 0;
                while (-1 != (ret = reader.read(buffer))) {
                    writer.write(buffer, 0, ret);
                }

            }
            OutputController.getLogger().log("Menu item created: " + f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }
    
    /**
     * Install this XDesktopEntry into the user's desktop as a launcher.
     */
    private void installDesktopLauncher(AccessWarningPaneComplexReturn.ShortcutResult info, boolean isSigned) {
        File shortcutFile = getShortcutTmpFile();
        try {

            if (!shortcutFile.getParentFile().isDirectory() && !shortcutFile.getParentFile().mkdirs()) {
                throw new IOException(shortcutFile.getParentFile().toString());
            }

            FileUtils.createRestrictedFile(shortcutFile, true);

            try ( /*
             * Write out a Java String (UTF-16) as a UTF-8 file
             */ OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(shortcutFile),
                     Charset.forName("UTF-8")); Reader reader = getContentsAsReader(false, info, isSigned)) {
                
                char[] buffer = new char[1024];
                int ret = 0;
                while (-1 != (ret = reader.read(buffer))) {
                    writer.write(buffer, 0, ret);
                }
                
            }

            /*
             * Install the desktop entry
             */

            String[] execString = new String[] { "xdg-desktop-icon", "install", "--novendor",
                    shortcutFile.getCanonicalPath() };
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Execing: " + Arrays.toString(execString));
            ProcessBuilder pb = new ProcessBuilder(execString);
            pb.inheritIO();
            Process installer = pb.start();

            StreamUtils.waitForSafely(installer);

            if (!shortcutFile.delete()) {
                throw new IOException("Unable to delete temporary file:" + shortcutFile);
            }

        } catch (FileNotFoundException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }


    public void refreshExistingShortcuts(boolean desktop, boolean menu) {
        //TODO TODO TODO TODO TODO TODO TODO TODO 
        //check existing jnlp files
        //check luncher 
        //get where it poiints
        //try all supported  shortcuts methods
        //choose the one which have most similar result to exisitng ones

    }

    public File getGeneratedJnlpFileName() {
        String name = FileUtils.sanitizeFileName(file.createJnlpTitle());
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

        URL uiconLocation = file.getInformation().getIconLocation(IconDesc.SHORTCUT, iconSize,
                iconSize);

        if (uiconLocation == null) {
            uiconLocation = file.getInformation().getIconLocation(IconDesc.DEFAULT, iconSize,
                    iconSize);
        }

        String location = null;
        if (uiconLocation != null) {
            //this throws npe, if url (specified in jnlp) points to 404
            URL urlLocation = CacheUtil.getCachedResourceURL(uiconLocation, null, UpdatePolicy.SESSION);
            if (urlLocation == null) {
                cantCache();
            }
            location = urlLocation.toString();
            if (!location.startsWith("file:")) {
                cantCache();
            }
        } else {
            //try favicon.ico
            try {
                URL favico = new URL(
                        file.getCodeBase().getProtocol(),
                        file.getCodeBase().getHost(),
                        file.getCodeBase().getPort(),
                        "/" + FAVICON);
                JNLPFile.openURL(favico, null, UpdatePolicy.ALWAYS);
                //this MAY throw npe, if url (specified in jnlp) points to 404
                URL urlLocation = CacheUtil.getCachedResourceURL(favico, null, UpdatePolicy.SESSION);
                if (urlLocation == null) {
                    cantCache();
                }
                location = urlLocation.toString();
                if (!location.startsWith("file:")) {
                    cantCache();
                }
            } catch (IOException ex) {
                //favicon 404 or similar
                OutputController.getLogger().log(ex);
            }
        }
        if (location != null) {
            String origLocation = location.substring("file:".length());
            this.iconLocation = origLocation;
            // icons are never unisntalled by itw. however, system clears them on its own.. soemtimes.
            // once the -Xcelarcache is run, system MAY clean it later, and so image wil be lost.
            // copy icon somewhere where -Xclearcache can not
            PathsAndFiles.ICONS_DIR.getFile().mkdirs();
            File source = new File(origLocation);
            String targetName = source.getName();
            if (targetName.equals(FAVICON)) {
                targetName = file.getCodeBase().getHost() + ".ico";
            }
            File target = new File(PathsAndFiles.ICONS_DIR.getFile(), targetName);
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.iconLocation = target.getAbsolutePath();
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Cached desktop shortcut icon: " + target + " ,  With source from: " + origLocation);
        }
    }

    private void cantCache() throws NonFileProtocolException {
        throw new NonFileProtocolException("Unable to cache icon");
    }

    private String getDesktopIconName() {
        return sanitize(file.createJnlpTitle());
    }

    public File getDesktopIconFile() {
            return new File(getDesktop(), getDesktopIconFileName());
    }
    public static File getDesktop(){
        if (JNLPRuntime.isWindows()) {
            FileSystemView filesys = FileSystemView.getFileSystemView();
            return filesys.getHomeDirectory();
        } else {
            return new File(findFreedesktopOrgDesktopPathCatch());
        }
    }

    public File getLinuxMenuIconFile() {
        return new File(findAndVerifyJavawsMenuDir() + "/" + getDesktopIconFileName());
    }
    
    private String getDesktopIconFileName() {
        return getDesktopIconName() + ".desktop";
    }

    private static String findAndVerifyGeneratedJnlpDir() {
        return findAndVerifyBasicDir(PathsAndFiles.GEN_JNLPS_DIR.getFile(), " directroy for stroing generated jnlps cannot be created. You may expect failure");
    }

    private static String findAndVerifyJavawsMenuDir() {
        return findAndVerifyBasicDir(PathsAndFiles.MENUS_DIR.getFile(), " directroy for stroing menu entry cannot be created. You may expect failure");
    }

    private static String findAndVerifyBasicDir(File f, String message) {
        String fPath = f.getAbsolutePath();
        if (!f.exists()) {
            if (!f.mkdirs()) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, fPath + message);
            }
        }
        return fPath;
    }

    public static String findFreedesktopOrgDesktopPathCatch() {
        try {
            return findFreedesktopOrgDesktopPath();
        } catch (Exception ex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            return System.getProperty("user.home") + "/Desktop";
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
}
