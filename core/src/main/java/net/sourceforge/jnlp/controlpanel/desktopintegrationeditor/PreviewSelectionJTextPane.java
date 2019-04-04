/*   Copyright (C) 2015 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package net.sourceforge.jnlp.controlpanel.desktopintegrationeditor;

import java.io.File;
import java.util.List;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import net.sourceforge.jnlp.util.logging.OutputController;

import static net.sourceforge.jnlp.runtime.Translator.R;

public class PreviewSelectionJTextPane extends JTextPane {

    private final JList iconsList;
    private final JList menuList;
    private final JList desktopList;
    private final JList generatedList;

    public PreviewSelectionJTextPane(JList iconsList, JList menuList, JList desktopList, JList generatedList) {
        this.iconsList = iconsList;
        this.menuList = menuList;
        this.desktopList = desktopList;
        this.generatedList = generatedList;
        this.setEditorKit(new HTMLEditorKit());
        this.setEditable(false);
    }

    private StringBuilder getMenus() {
        return getTextFiles(menuList.getSelectedValuesList());
    }

    private StringBuilder getDesktops() {
        return getTextFiles(desktopList.getSelectedValuesList());
    }

    private StringBuilder getGenerated() {
        return getTextFiles(generatedList.getSelectedValuesList());
    }

    private StringBuilder getHeader(boolean i, boolean d, boolean m, boolean g) {
        StringBuilder sb = new StringBuilder();
        if (i || d || m || g) {
            sb.append("<tr>");
        }
        if (i) {
            sb.append("<th>").append(R("DIMicons")).append(":</th>");
        }
        if (d) {
            sb.append("<th>").append(R("DIMdesktopItems")).append(":</th>");
        }
        if (m) {
            sb.append("<th>").append(R("DIMmenuItems")).append(":</th>");
        }
        if (g) {
            sb.append("<th>").append(R("DIMgeneratedJnlps")).append(":</th>");
        }

        if (i || d || m || g) {
            sb.append("</tr>");
        }
        return sb;
    }

    public void generatePreview() {
        try {
            StringBuilder sb = new StringBuilder("<html><table>");
            sb.append(getHeader(iconsList.getSelectedIndices().length > 0,
                    menuList.getSelectedIndices().length > 0,
                    desktopList.getSelectedIndices().length > 0,
                    generatedList.getSelectedIndices().length > 0)).append("<tr>");
            if (iconsList.getSelectedIndices().length > 0) {
                sb.append("<td>").append(getIcons()).append("</td>");
            }
            if (menuList.getSelectedIndices().length > 0) {
                sb.append("<td>").append(getMenus()).append("</td>");
            }
            if (desktopList.getSelectedIndices().length > 0) {
                sb.append("<td>").append(getDesktops()).append("</td>");
            }
            if (generatedList.getSelectedIndices().length > 0) {
                sb.append("<td>").append(getGenerated()).append("</td>");
            }
            sb.append("</tr></table></html>");
            this.setText(sb.toString());

        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }

    }

    private StringBuilder getIcons() {
        StringBuilder s = new StringBuilder();
        try {
            List l = iconsList.getSelectedValuesList();
            for (Object l1 : l) {
                File f = (File) l1;
                s.append("<small>").append(f.getAbsolutePath()).append("</small><br>");
                s.append("<img src='").append(f.toURI().toURL()).append("'></img><br>");

            }
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }
        return s;
    }

    private StringBuilder getTextFiles(List selectedValuesList) {
        StringBuilder s = new StringBuilder();
        for (Object i : selectedValuesList) {
            File f = (File) i;
            s.append("<small>").append(f.getAbsolutePath()).append("</small><br>");
            s.append("<pre>").append(FreeDesktopIntegrationEditorFrame.fileToString(f, true)).append("</pre><br>");

        }
        return s;

    }

}
