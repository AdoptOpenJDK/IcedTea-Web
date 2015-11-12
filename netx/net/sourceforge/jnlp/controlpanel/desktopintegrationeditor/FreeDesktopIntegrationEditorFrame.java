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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.security.dialogs.SecurityDialogPanel;
import net.sourceforge.jnlp.util.XDesktopEntry;
import net.sourceforge.jnlp.util.logging.ConsoleOutputPaneModel;

import static net.sourceforge.jnlp.runtime.Translator.R;

public class FreeDesktopIntegrationEditorFrame extends JFrame {

    //gui
    private final javax.swing.JLabel title = new JLabel();
    private final javax.swing.JCheckBox selectRelativeRecordsFromOtherColumns = new JCheckBox();
    private final javax.swing.JButton removeSelectedButton = new JButton();
    private final javax.swing.JButton cleanAll = new JButton();
    private final javax.swing.JButton closeButton = new JButton();
    private final javax.swing.JButton reloadsListButton = new JButton();
    private final javax.swing.JButton selectAll = new JButton();

    //important ones
    private final javax.swing.JList menuList = new JListUtils.CustomRendererJList();
    private final javax.swing.JList desktopList = new JListUtils.CustomValidatingRendererJList();
    private final javax.swing.JList generatedList = new JListUtils.CustomRendererJList();
    private final javax.swing.JList iconsList = new JListUtils.CustomRendererWithIconJList();

    PreviewSelectionJTextPane previewPane = new PreviewSelectionJTextPane(iconsList, menuList, desktopList, generatedList);
    //gui end

    private final Blinker blinker;

    private void setListeners() {
        removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                FreeDesktopIntegrationEditorFrame.this.removeSelected();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        reloadsListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                populateLists();
            }
        });

        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectAll();
            }
        });

        cleanAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cleanAll();
            }
        });

    }

    private void setTexts() {
        this.setTitle(R("DIMtitle"));
        closeButton.setText(R("ButClose"));
        removeSelectedButton.setText(R("DIMremoveSelected"));
        selectRelativeRecordsFromOtherColumns.setText(R("DIMselectRelativeRecordsFromOtherColumns"));
        reloadsListButton.setText(R("DIMreloadLists"));
        selectAll.setText(R("DIMselectAll"));
        cleanAll.setText(R("DIMclearSelection"));
        title.setText(SecurityDialogPanel.htmlWrap("<p>" + R("DIMdescription") + "</p>"));
    }

    private JPanel createMainControls() {
        JPanel mainControls = new JPanel(new GridLayout(1, 2));
        mainControls.add(closeButton);
        mainControls.add(removeSelectedButton);
        return mainControls;
    }

    private JPanel createMiddleToolBox() {
        JPanel middleToolBox = new JPanel(new GridLayout(1, 2));
        middleToolBox.add(selectRelativeRecordsFromOtherColumns);
        middleToolBox.add(reloadsListButton);
        middleToolBox.add(selectAll);
        middleToolBox.add(cleanAll);
        return middleToolBox;
    }

    private JPanel createPreviewPanel(JTextPane previewPane) {
        JPanel previewPanel = new JPanel(new BorderLayout());
        JScrollPane jScrollPane2 = new JScrollPane();
        jScrollPane2.setViewportView(previewPane);
        previewPanel.add(jScrollPane2, BorderLayout.CENTER);
        createMiddleToolBox();
        previewPanel.add(createMiddleToolBox(), BorderLayout.PAGE_START);
        return previewPanel;
    }

    private JSplitPane createListsLayout() {
        JPanel menusPanel = Panels.createMenuPanel(menuList, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectSomeRelatives(menuList.getSelectedValuesList(), iconsList);
            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectSomeRelatives(menuList.getSelectedValuesList(), generatedList);
            }
        }
        );
        JPanel desktopsPanel = Panels.createDesktopPanel(desktopList, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectSomeRelatives(desktopList.getSelectedValuesList(), iconsList);
            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectSomeRelatives(desktopList.getSelectedValuesList(), generatedList);
            }
        }
        );
        JPanel iconsPanel = Panels.createIconsPanel(iconsList, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                findOrphans(iconsList, allItemsAsFiles(menuList), allItemsAsFiles(desktopList));
            }
        });
        JPanel generatedsPanel = Panels.createGeneratedPanel(generatedList, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                findOrphans(generatedList, allItemsAsFiles(menuList), allItemsAsFiles(desktopList));
            }
        });
        return Panels.createQuadroSplit(expectedWidth, menusPanel, desktopsPanel, iconsPanel, generatedsPanel);
    }

    private void setLayout() {
        createMainControls();
        getContentPane().add(createMainControls(), BorderLayout.PAGE_END);
        JSplitPane splitListsAndPreview = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitListsAndPreview.setLeftComponent(createListsLayout());
        splitListsAndPreview.setRightComponent(createPreviewPanel(previewPane));
        getContentPane().add(splitListsAndPreview, BorderLayout.CENTER);
        getContentPane().add(title, BorderLayout.PAGE_START);
        splitListsAndPreview.setDividerLocation(expectedHeight / 2);
    }

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FreeDesktopIntegrationEditorFrame().setVisible(true);
            }
        });
    }

    private boolean selecting = false;
    private final int expectedWidth = 800;
    private final int expectedHeight = 600;

    public FreeDesktopIntegrationEditorFrame() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(expectedWidth, expectedHeight);
        populateLists();
        setTexts();
        setListeners();
        setLayout();
        selectRelativeRecordsFromOtherColumns.setSelected(true);

        ListSelectionListener generatePreviewListener = new GeneratePreviewListener();

        iconsList.addListSelectionListener(generatePreviewListener);
        desktopList.addListSelectionListener(generatePreviewListener);
        menuList.addListSelectionListener(generatePreviewListener);
        generatedList.addListSelectionListener(generatePreviewListener);
        blinker = new Blinker(selectRelativeRecordsFromOtherColumns);

    }

    private void populateLists() {
        menuList.setModel(new JListUtils.InfrastructureFileDescriptorListingBasedJListModel(PathsAndFiles.MENUS_DIR));
        desktopList.setModel(new JListUtils.FileListBasedJListModel(new File(XDesktopEntry.findFreedesktopOrgDesktopPathCatch()), "(?i)^.*\\.desktop$") {

            @Override
            public String toString() {
                return R("DIMguessedDesktop");
            }
        });
        iconsList.setModel(new JListUtils.InfrastructureFileDescriptorListingBasedJListModel(PathsAndFiles.ICONS_DIR));
        generatedList.setModel(new JListUtils.InfrastructureFileDescriptorListingBasedJListModel(PathsAndFiles.GEN_JNLPS_DIR));
    }

    private void cleanAll() {
        selecting = true;
        try {
            clearAll();
        } finally {
            selecting = false;
        }
    }

    private void clearAll() {
        desktopList.clearSelection();
        menuList.clearSelection();
        generatedList.clearSelection();
        iconsList.clearSelection();
        previewPane.setText(R("DIMselectionPreview"));
    }

    private void removeSelected() {
        int a = getTotal(
                objectListToFileList(iconsList.getSelectedValuesList()),
                objectListToFileList(menuList.getSelectedValuesList()),
                objectListToFileList(desktopList.getSelectedValuesList()),
                objectListToFileList(generatedList.getSelectedValuesList())
        );
        if (a <= 0) {
            return;
        }
        int x = JOptionPane.showConfirmDialog(this, R("DIMaskBeforeDelete", a));
        if (x == JOptionPane.OK_OPTION || x == JOptionPane.YES_OPTION) {
            removeSelected(
                    objectListToFileList(iconsList.getSelectedValuesList()),
                    objectListToFileList(menuList.getSelectedValuesList()),
                    objectListToFileList(desktopList.getSelectedValuesList()),
                    objectListToFileList(generatedList.getSelectedValuesList())
            );
            populateLists();
        }
    }

    private void selectAll() {
        selecting = true;
        try {
            selectAll(menuList);
            selectAll(desktopList);
            selectAll(iconsList);
            selectAll(generatedList);
        } finally {
            selecting = false;
        }
        previewPane.generatePreview();
    }

    public List<File> allItemsAsFiles(JList l) {
        return allItemsAsFiles(l.getModel());
    }

    public List<File> allItemsAsFiles(ListModel l) {
        List<File> r = new ArrayList<>(l.getSize());
        for (int i = 0; i < l.getSize(); i++) {
            r.add((File) l.getElementAt(i));

        }
        return r;
    }

    private List<File> objectListToFileList(List l) {
        List<File> r = new ArrayList(l.size());
        for (Object l1 : l) {
            r.add((File) l1);
        }
        return r;
    }

    private void removeSelected(List<File>... a) {
        for (List<File> list : a) {
            for (File file : list) {
                file.delete();

            }
        }
    }

    private int getTotal(List<File>... a) {
        int i = 0;
        for (List<File> list : a) {
            i+=list.size();
        }
        return i;
    }

    private void findOrphans(JList possibleOrphans, List<File>... whereItCanBe) {
        selecting = true;
        if (selectRelativeRecordsFromOtherColumns.isSelected()) {
            clearAll();
            blinker.blink();
        }
        try {
            possibleOrphans.clearSelection();
            List<File> l = allItemsAsFiles(possibleOrphans);
            for (int i = 0; i < l.size(); i++) {
                File file = l.get(i);
                boolean found = false;
                for (List<File> lf : whereItCanBe) {
                    if (found) {
                        break;
                    }
                    for (File f : lf) {
                        String s = fileToString(f, false);
                        if (s.contains(file.getAbsolutePath())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                   possibleOrphans.addSelectionInterval(i, i);
                }
            }
        } finally {
            selecting = false;
        }
        previewPane.generatePreview();
    }

    private void selectSomeRelatives(List selected, JList target) {
        selecting = true;
        try {
            selectFileFromShortcuts(selected, target);
        } finally {
            selecting = false;
        }
        previewPane.generatePreview();
    }

    private void selectAll(JList list) {
        int start = 0;
        int end = list.getModel().getSize() - 1;
        if (end >= 0) {
            list.setSelectionInterval(start, end);
        }
    }

    private class GeneratePreviewListener implements ListSelectionListener {

        public GeneratePreviewListener() {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (selecting) {
                return;
            }
            try {
                selecting = true;
                if (selectRelativeRecordsFromOtherColumns.isSelected()) {
                    blinker.blink();
                    selectRelatives(e.getSource());

                }
            } finally {
                selecting = false;
            }

            previewPane.generatePreview();
        }
    }

    private void selectRelatives(Object source) {
        if (source instanceof JList) {
            int[] indexes = ((JList) (source)).getSelectedIndices();
            clearAll();
            ((JList) (source)).setSelectedIndices(indexes);
        }

        for (int x = 1; x <= 3; x++) {
            //we dont wont recurse, so sending copies in
            selectShortcutsByFiles(
                    objectListToFileList(iconsList.getSelectedValuesList()),
                    objectListToFileList(generatedList.getSelectedValuesList())
            );
            selectFilesByShortcuts(
                    objectListToFileList(menuList.getSelectedValuesList()),
                    objectListToFileList(desktopList.getSelectedValuesList())
            );
        }
    }

    static String fileToString(File f, boolean escape) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(f))) {

            StringBuilder sb = new StringBuilder();

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    return sb.toString();
                }
                if (escape) {
                    line = ConsoleOutputPaneModel.escapeHtmlForJTextPane(line);
                }
                sb.append(line).append("\n");
            }

        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private void selectShortcutsByFiles(List<File> icons, List<File> jnlps) {
        selectShortcutsWithFiles(icons, desktopList);
        selectShortcutsWithFiles(icons, menuList);
        selectShortcutsWithFiles(jnlps, desktopList);
        selectShortcutsWithFiles(jnlps, menuList);
    }

    private void selectFilesByShortcuts(List<File> menu, List<File> desktop) {
        selectFileFromShortcuts(desktop, iconsList);
        selectFileFromShortcuts(desktop, generatedList);
        selectFileFromShortcuts(menu, iconsList);
        selectFileFromShortcuts(menu, generatedList);
    }

    private void selectShortcutsWithFiles(List<File> icons, JList list) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
            File item = (File) list.getModel().getElementAt(i);
            String s = fileToString(item, false);
            for (File icon : icons) {
                if (s.contains(icon.getAbsolutePath())) {
                    list.addSelectionInterval(i, i);
                }
            }

        }
    }

    private void selectFileFromShortcuts(List<File> shortcuts, JList files) {
        for (File shortcut : shortcuts) {
            String s = fileToString(shortcut, false);
            for (int i = 0; i < files.getModel().getSize(); i++) {
                File item = (File) files.getModel().getElementAt(i);
                if (s.contains(item.getAbsolutePath())) {
                    files.addSelectionInterval(i, i);
                }

            }
        }

    }
}
