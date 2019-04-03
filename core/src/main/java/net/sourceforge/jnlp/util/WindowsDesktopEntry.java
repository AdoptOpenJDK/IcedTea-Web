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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import mslinks.ShellLink;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheLRUWrapper;
import net.sourceforge.jnlp.security.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Based on https://github.com/DmitriiShamrikov/mslinkshttps://github.com/DmitriiShamrikov/mslinks
 */
public class WindowsDesktopEntry implements GenericDesktopEntry {

    private final JNLPFile file;

    public WindowsDesktopEntry(JNLPFile file) {
        this.file = file;
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
        String favIcon = getFavIcon();
        ShellLink sl = ShellLink.createLink(JavaWsBin).setCMDArgs(file.getSourceLocation().toString());
        if (favIcon != null) {
            favIcon = favIcon.substring(6);
            sl.setIconLocation(favIcon);
        }
        sl.saveTo(path);
        // write shortcut path to list
        manageShortcutList(ManageMode.A, path);
    }

    @Override
    public void createWindowsMenu() throws IOException {
        // create menu item
        // see if menu is defined in jnlp, else don't do it
        String path = file.getInformation().getShortcut().getMenu().getSubMenu();
        if (path != null) {
            path = System.getenv("userprofile") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/" + path;
            // check to see if menu dir exists and create if not
            File menuDir = new File(path);
            if (!menuDir.exists()) {
                menuDir.mkdir();
            }
            String JavaWsBin = XDesktopEntry.getJavaWsBin();
            String favIcon = getFavIcon();
            ShellLink sl = ShellLink.createLink(JavaWsBin).setCMDArgs(file.getSourceLocation().toString());
            // setup uninstall shortcut
            ShellLink ul = ShellLink.createLink(JavaWsBin).setCMDArgs("-Xclearcache " + file.getFileLocation().toString());
            if (favIcon != null) {
                favIcon = favIcon.substring(6);
                sl.setIconLocation(favIcon);
                ul.setIconLocation(favIcon);
            }
            sl.saveTo(path + "/" + file.getInformation().getTitle()+ ".lnk");
            ul.saveTo(path + "/Uninstall " + file.getInformation().getTitle() + ".lnk");
            // write shortcuts to list
            manageShortcutList(ManageMode.A, path + "/" + file.getInformation().getTitle() + ".lnk");
            manageShortcutList(ManageMode.A, path + "/Uninstall " + file.getInformation().getTitle() + ".lnk");
        }
    }

    private void manageShortcutList(ManageMode mode, String path) throws IOException {
        if (!CacheLRUWrapper.getInstance().getWindowsShortcutList().exists()) {
            CacheLRUWrapper.getInstance().getWindowsShortcutList().createNewFile();
        }

        if (ManageMode.A == mode) {
            List<String> lines = Files.readAllLines(CacheLRUWrapper.getInstance().getWindowsShortcutList().toPath(), Charset.forName("UTF-8"));
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
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Adding sCut to list = " + sItem);
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
        throw new UnsupportedOperationException("not suported on windows like systems");
    }

    @Override
    public void refreshExistingShortcuts(boolean desktop, boolean menu) {
        throw new UnsupportedOperationException("not suported on windows like systems");
    }

    @Override
    public File getGeneratedJnlpFileName() {
        throw new UnsupportedOperationException("not suported on windows like systems");
    }

    @Override
    public File getLinuxMenuIconFile() {
        throw new UnsupportedOperationException("not suported on windows like systems");
    }

    private static enum ManageMode {
        //appned?
        A
    }

}
