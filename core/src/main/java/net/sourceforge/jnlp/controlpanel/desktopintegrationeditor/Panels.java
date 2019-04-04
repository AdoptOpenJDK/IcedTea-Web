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

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import net.sourceforge.jnlp.security.dialogs.SecurityDialogPanel;

import static net.sourceforge.jnlp.runtime.Translator.R;

public class Panels {

    public static JPanel createGeneratedPanel(JList list, ActionListener findOrphans) {
        return createIconsOrGeneratedPanel(list, findOrphans, R("DIMgeneratedJnlps"), bold(R("DIMgeneratedJnlpsTooltip")));
    }

    public static JPanel createIconsPanel(JList list, ActionListener findOrphans) {
        return createIconsOrGeneratedPanel(list, findOrphans, R("DIMicons"), bold(R("DIMiconsTooltip")));
    }

    private static JPanel createIconsOrGeneratedPanel(JList list, ActionListener findOrphans, String title, String tooltip) {
        JPanel iconsPanel = new JPanel(new BorderLayout());
        JLabel l = new JLabel(title);
        l.setToolTipText(createToolTip(tooltip, list));
        iconsPanel.add(l, BorderLayout.PAGE_START);
        JScrollPane scrollIcons = new JScrollPane();
        scrollIcons.setViewportView(list);
        iconsPanel.add(scrollIcons, BorderLayout.CENTER);
        JPanel iconsToolPanel = new JPanel(new BorderLayout());
        JButton findOrphansButton = new JButton(R("DIMorphans"));
        findOrphansButton.addActionListener(findOrphans);
        findOrphansButton.setToolTipText(R("DIMorphansTooltip"));
        iconsToolPanel.add(findOrphansButton, BorderLayout.CENTER);
        iconsPanel.add(iconsToolPanel, BorderLayout.PAGE_END);
        return iconsPanel;
    }

    public static JPanel createMenuPanel(JList list, ActionListener findIcons, ActionListener findGenerated) {
        return createDesktopOrMenuPanel(list, findIcons, findGenerated, R("DIMmenuItems"), bold(R("DIMmenuItemsTooltip")));
    }

    public static JPanel createDesktopPanel(JList list, ActionListener findIcons, ActionListener findGenerated) {
        StringBuilder sb = new StringBuilder();
        sb.append(R("DIMdesktopItemsTooltipL1")).append("<br>")
                .append(R("DIMdesktopItemsTooltipL2")).append(":" + "<ul>" + "<li>")
                .append(R("DIMdesktopItemsTooltipL3")).append("</li>" + "<li>")
                .append(R("DIMdesktopItemsTooltipL4")).append("</li>" + "<li>")
                .append(R("DIMdesktopItemsTooltipL5")).append("</li>" + "</ul>")
                .append(bold(R("DIMdesktopItemsTooltipL6")));
        return createDesktopOrMenuPanel(list, findIcons, findGenerated, R("DIMdesktopItems"), sb.toString());
    }

    private static JPanel createDesktopOrMenuPanel(JList list, ActionListener findIcons, ActionListener findGenerated, String title, String tooltip) {
        JPanel desktopPanel = new JPanel(new BorderLayout());
        JLabel l = new JLabel(title);
        l.setToolTipText(createToolTip(tooltip, list));
        desktopPanel.add(l, BorderLayout.PAGE_START);
        JScrollPane scrollDesktop = new JScrollPane();
        scrollDesktop.setViewportView(list);
        desktopPanel.add(scrollDesktop, BorderLayout.CENTER);
        JPanel desktopToolPanel = createDesktopOrMenuToolBox(findIcons, findGenerated);
        desktopPanel.add(desktopToolPanel, BorderLayout.PAGE_END);
        return desktopPanel;
    }

    private static String createToolTip(String tooltip, JList list) {
        if (tooltip != null) {
            JListUtils.FileListBasedJListModel model = (JListUtils.FileListBasedJListModel) (list.getModel());
            StringBuilder sb = new StringBuilder();
            sb.append("<ul><li>")
                    .append(model.getFile()).append("</li><br>" + "<li>")
                    .append(model.toString()).append("</li><br>" + "<li>")
                    .append(tooltip).append("</ul>");
            String tt = SecurityDialogPanel.htmlWrap(sb.toString());
            return tt;
        }
        return null;
    }

    private static JPanel createDesktopOrMenuToolBox(ActionListener findIcons, ActionListener findGenerated) {
        JPanel desktopToolPanel = new JPanel(new BorderLayout());
        JButton desktopFindGeneratedButton = new JButton(R("DIMgeneratedButton"));
        desktopFindGeneratedButton.setToolTipText(R("DIMgeneratedButtonTooltip"));
        desktopFindGeneratedButton.addActionListener(findGenerated);
        JButton desktopFindIconsButton = new JButton(R("DIMiconsButton"));
        desktopFindIconsButton.setToolTipText(R("DIMiconsButtonTooltip"));
        desktopFindIconsButton.addActionListener(findIcons);
        desktopToolPanel.add(desktopFindGeneratedButton, BorderLayout.LINE_END);
        desktopToolPanel.add(desktopFindIconsButton, BorderLayout.LINE_START);
        return desktopToolPanel;
    }

    static JSplitPane createQuadroSplit(int width, JPanel menusPanel, JPanel desktopsPanel, JPanel iconsPanel, JPanel generatedsPanel) {
        JSplitPane splitAllAndGenerated = new JSplitPane();
        JSplitPane splitIconsAndLists = new JSplitPane();
        JSplitPane splitLists = new JSplitPane();
        splitLists.setLeftComponent(menusPanel);
        splitLists.setRightComponent(desktopsPanel);
        splitIconsAndLists.setRightComponent(splitLists);
        splitIconsAndLists.setLeftComponent(iconsPanel);
        splitAllAndGenerated.setLeftComponent(splitIconsAndLists);
        splitAllAndGenerated.setRightComponent(generatedsPanel);
        splitAllAndGenerated.setDividerLocation(width / 5 * 4);
        splitIconsAndLists.setDividerLocation(width / 4);
        splitLists.setDividerLocation(width / 4);
        return splitAllAndGenerated;
    }

    private static String bold(String s) {
        return "<b>" + s + "</b>";
    }

}
