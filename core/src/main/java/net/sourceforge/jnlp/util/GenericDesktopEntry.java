/* 
Copyright (C) 2013 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package net.sourceforge.jnlp.util;

import java.io.File;
import java.io.IOException;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;

/**
 *
 * This is very weird interface, as two implementing classes have empty
 * intersection. The interface exists only because windows implementation depends
 * on mslink.jar, and thus is optional. todo. unify the X and win
 * implementations so this interface have sense
 */
public interface GenericDesktopEntry {

    //linux
    void createDesktopShortcuts(AccessWarningPaneComplexReturn.ShortcutResult menu, AccessWarningPaneComplexReturn.ShortcutResult desktop, boolean isSigned);

    void refreshExistingShortcuts(boolean desktop, boolean menu);

    File getGeneratedJnlpFileName();

    File getLinuxMenuIconFile();

    //windows
    void createShortcutOnWindowsDesktop() throws IOException;

    void createWindowsMenu() throws IOException;

    //shared!
    String getDesktopIconFileName();

    File getDesktopIconFile();
}
