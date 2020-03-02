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

import mslinks.ShellLink;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.LazyLoaded;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.JNLPFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.DOUBLE_QUOTE;
import static net.sourceforge.jnlp.util.WindowsShortcutManager.getWindowsShortcutsFile;

/**
 * Based on https://github.com/DmitriiShamrikov/mslinks
 */
public class WindowsDesktopEntry implements GenericDesktopEntry {

    private static final Logger LOG = LoggerFactory.getLogger(WindowsDesktopEntry.class);

    private final JNLPFile file;
    private final LazyLoaded<String> iconLocation;

    public WindowsDesktopEntry(JNLPFile file) {
        this.file = file;
        this.iconLocation = new LazyLoaded<>(() -> new XDesktopEntry(file).cacheAndGetIconLocation());
    }

    private String getShortcutFileName() {
        return getShortcutName() + ".lnk";
    }

    private String getShortcutName() {
        return sanitize(file.getShortcutName());
    }

    private String sanitize(String fileName) {
        if (fileName != null) {/* key=value pairs must be a single line */
            //return first line or replace new lines by space?
            return FileUtils.sanitizeFileName(fileName, '-').split("\\R")[0];
        }
        return "";
    }

    private String getDesktopLnkPath() {
        return System.getenv("userprofile") + "/Desktop/" + getShortcutFileName();
    }

    @Override
    public File getDesktopIconFile() {
        return new File(getDesktopLnkPath());
    }

    @Override
    public void createShortcutOnWindowsDesktop() throws IOException {
        ShellLink sl = ShellLink.createLink(getJavaWsBin()).setCMDArgs(quoted(file.getSourceLocation()));
        if (iconLocation.get() != null) {
            sl.setIconLocation(iconLocation.get());
        }
        final String path = getDesktopLnkPath();
        sl.saveTo(path);
        // write shortcut path to list
        manageShortcutList(path);
    }

    private String getJavaWsBin() throws FileNotFoundException {
        final String javaWsBin = JvmUtils.getJavaWsBin();

        // first look for exe
        final String javaWsBinExe = javaWsBin + ".exe";
        if (new File(javaWsBinExe).exists()) {
            LOG.debug("For Shortcut Returning EXE : {}", javaWsBinExe);
            return javaWsBinExe;
        }

        if (new File(javaWsBin).exists()) {
            LOG.debug("For Shortcut Returning {}", javaWsBin);
            return javaWsBin;
        }

        LOG.debug("Could not find the javaws binary to create desktop shortcut");
        throw new FileNotFoundException("Could not find the javaws binary to create desktop shortcut");
    }


    @Override
    public void createWindowsMenu() throws IOException {
        // create menu item
        String pathSuffix;
        try {
            pathSuffix = file.getInformation().getShortcut().getMenu().getSubMenu();
        }
        catch (NullPointerException npe) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, npe);
            pathSuffix = null;
        }        
        if (pathSuffix == null) {
            pathSuffix = getShortcutName();
        }
                        
        final String path = System.getenv("userprofile") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/" + pathSuffix;        
        // check to see if menu dir exists and create if not
        final File menuDir = new File(path);
        if (!menuDir.exists()) {
            menuDir.mkdir();
        }
        final String JavaWsBin = getJavaWsBin();
        final ShellLink sl = ShellLink.createLink(JavaWsBin).setCMDArgs(quoted(file.getSourceLocation()));
        // setup uninstall shortcut
        final ShellLink ul = ShellLink.createLink(JavaWsBin).setCMDArgs("-Xclearcache " + quoted(file.getFileLocation()));
        if (iconLocation.get() != null) {
            sl.setIconLocation(iconLocation.get());
            ul.setIconLocation(iconLocation.get());
        }
        final String link = getShortcutFileName();
        sl.saveTo(path + "/" + link);
        ul.saveTo(path + "/Uninstall " + link);
        // write shortcuts to list
        manageShortcutList(path + "/" + link);
        manageShortcutList(path + "/Uninstall " + link);
    }

    private void manageShortcutList(String path) throws IOException {
        final File shortcutFile = getWindowsShortcutsFile();
        if (!shortcutFile.exists() && !shortcutFile.createNewFile()) {
            LOG.warn("could not create file for shortcut manager: {}", shortcutFile);
            return;
        }
        LOG.debug("Using WindowsShortCutManager {}", shortcutFile);
        final List<String> lines = readAllLine(shortcutFile);
        if (needToAddNewShortcutEntry(path, lines)) {
            final String scInfo = file.getFileLocation().toString() + "," + path;
            LOG.debug("Adding Shortcut to list: {}", scInfo);
            lines.add(scInfo);
            final String content = String.join("\r\n", lines);
            FileUtils.saveFileUtf8(content, shortcutFile);
        }
    }

    private List<String> readAllLine(File shortcutFile) throws IOException {
        try {
            LOG.debug("Reading Shortcuts with UTF-8");
            return Files.readAllLines(shortcutFile.toPath(), UTF_8);
        } catch (MalformedInputException me) {
            LOG.debug("Fallback to reading Shortcuts with default encoding {}", Charset.defaultCharset().name());
            return Files.readAllLines(shortcutFile.toPath(), Charset.defaultCharset());
        }
    }

    private boolean needToAddNewShortcutEntry(String path, List<String> lines) {
        return lines.stream()
                .map(line -> line.split(","))
                .map(array -> array[1])
                .anyMatch(sPath -> sPath.equalsIgnoreCase(path));
    }

    @Override
    public void createDesktopShortcuts(AccessWarningPaneComplexReturn.ShortcutResult menu, AccessWarningPaneComplexReturn.ShortcutResult desktop) {
        throw new UnsupportedOperationException("not supported on windows like systems");
    }

    @Override
    public void refreshExistingShortcuts(boolean desktop, boolean menu) {
        throw new UnsupportedOperationException("not supported on windows like systems");
    }

    @Override
    public File getGeneratedJnlpFileName() {
        throw new UnsupportedOperationException("not supported on windows like systems");
    }

    @Override
    public File getLinuxMenuIconFile() {
        throw new UnsupportedOperationException("not supported on windows like systems");
    }

    private String quoted(URL url) {
        return DOUBLE_QUOTE + url.toExternalForm() + DOUBLE_QUOTE;
    }
}
