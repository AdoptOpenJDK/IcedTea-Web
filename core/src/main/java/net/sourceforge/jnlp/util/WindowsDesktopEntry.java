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
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheLRUWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.DOUBLE_QUOTE;

/**
 * Based on https://github.com/DmitriiShamrikov/mslinks
 */
public class WindowsDesktopEntry implements GenericDesktopEntry {

    private final static Logger LOG = LoggerFactory.getLogger(WindowsDesktopEntry.class);

    private final JNLPFile file;
    private final String iconLocation;

    public WindowsDesktopEntry(JNLPFile file) {
        this.file = file;
        this.iconLocation = new XDesktopEntry(file).cacheAndGetIconLocation();
    }

    @Override
    public String getDesktopIconFileName() {
        return XDesktopEntry.getDesktopIconName(file) + ".lnk";
    }

    private String getDesktopLnkPath() {
        return System.getenv("userprofile") + "/Desktop/" + getDesktopIconFileName();
    }

    @Override
    public File getDesktopIconFile() {
        return new File(getDesktopLnkPath());
    }

    @Override
    public void createShortcutOnWindowsDesktop() throws IOException {
        String path = getDesktopLnkPath();
        String JavaWsBin = XDesktopEntry.getJavaWsBin();
        ShellLink sl = ShellLink.createLink(JavaWsBin).setCMDArgs(quoted(file.getSourceLocation()));
        if (iconLocation != null) {
            sl.setIconLocation(iconLocation);
        }
        sl.saveTo(path);
        // write shortcut path to list
        manageShortcutList(ManageMode.A, path);
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
            pathSuffix = XDesktopEntry.getDesktopIconName(file);
        }
                        
        final String path = System.getenv("userprofile") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/" + pathSuffix;        
        // check to see if menu dir exists and create if not
        final File menuDir = new File(path);
        if (!menuDir.exists()) {
            menuDir.mkdir();
        }
        final String JavaWsBin = XDesktopEntry.getJavaWsBin();
        final ShellLink sl = ShellLink.createLink(JavaWsBin).setCMDArgs(quoted(file.getSourceLocation()));
        // setup uninstall shortcut
        final ShellLink ul = ShellLink.createLink(JavaWsBin).setCMDArgs("-Xclearcache " + quoted(file.getFileLocation()));
        if (iconLocation != null) {
            sl.setIconLocation(iconLocation);
            ul.setIconLocation(iconLocation);
        }
        final String link = FileUtils.sanitizeFileName(file.getInformation().getTitle() + ".lnk", '-');
        sl.saveTo(path + "/" + link);
        ul.saveTo(path + "/Uninstall " + link);
        // write shortcuts to list
        manageShortcutList(ManageMode.A, path + "/" + link);
        manageShortcutList(ManageMode.A, path + "/Uninstall " + link);
    }

    private void manageShortcutList(ManageMode mode, String path) throws IOException {
        if (!CacheLRUWrapper.getInstance().getWindowsShortcutList().exists()) {
            CacheLRUWrapper.getInstance().getWindowsShortcutList().createNewFile();
        }

        if (ManageMode.A == mode) {
            List<String> lines = Files.readAllLines(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath(), UTF_8);
            Iterator it = lines.iterator();
            String sItem = "";
            String sPath;
            Boolean fAdd = true;
            // check to see if line exists, if not add it
            while (it.hasNext()) {
                sItem = it.next().toString();
                String[] sArray = sItem.split(",");
                String application = sArray[0]; //??
                sPath = sArray[1];
                if (sPath.equalsIgnoreCase(path)) {
                    // it exists don't add
                    fAdd = false;
                    break;
                }
            }
            if (fAdd) {
                LOG.debug("Adding sCut to list = ", sItem);
                String scInfo = file.getFileLocation().toString() + ",";
                scInfo += path + "\r\n";
                Files.write(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath(), scInfo.getBytes(), StandardOpenOption.APPEND);
            }
        }
    }

    private String getFavIcon() {
        return XDesktopEntry.getFavIcon(file);
    }

    @Override
    public void createDesktopShortcuts(AccessWarningPaneComplexReturn.ShortcutResult menu, AccessWarningPaneComplexReturn.ShortcutResult desktop, boolean isSigned) {
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

    private static enum ManageMode {
        //append?
        A
    }

    private String quoted(URL url) {
        return DOUBLE_QUOTE + url.toExternalForm() + DOUBLE_QUOTE;
    }

}
