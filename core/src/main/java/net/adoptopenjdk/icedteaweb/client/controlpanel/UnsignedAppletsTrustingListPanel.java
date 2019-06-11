/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.ExtendedAppletSecurityHelp;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UrlRegEx;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.impl.UnsignedAppletActionStorageExtendedImpl;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.ScreenFinder;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_HOME;

public class UnsignedAppletsTrustingListPanel extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(UnsignedAppletsTrustingListPanel.class);

    private JButton helpButton;
    private JButton deleteButton;
    private JButton addRowButton;
    private JButton validateTableButton;
    private JButton testUrlButton;
    private JButton invertSelectionButton;
    private JButton moveRowUpButton;
    private JButton moveRowDownButton;
    private JCheckBox askBeforeActionCheckBox;
    private JCheckBox filterRegexesCheckBox;
    private JComboBox<AppletSecurityLevel> mainPolicyComboBox;
    private JComboBox<String> deleteTypeComboBox;
    private JComboBox<String> viewFilter;
    private JLabel globalBehaviourLabel;
    private JLabel securityLevelLabel;
    private JScrollPane userTableScrollPane;
    private JTabbedPane mainTabPanel;
    private JTable userTable;
    private JScrollPane globalTableScrollPane;
    private JTable globalTable;
    private final UnsignedAppletActionStorageExtendedImpl customBackEnd;
    private final UnsignedAppletActionStorageExtendedImpl globalBackEnd;
    private final UnsignedAppletActionTableModel customModel;
    private final UnsignedAppletActionTableModel globalModel;
    private final ByPermanencyFilter customFilter;
    private final ByPermanencyFilter globalFilter;
    private final DeploymentConfiguration conf;
    private JTable currentTable;
    private UnsignedAppletActionTableModel currentModel;
    private String lastDoc;
    private String lastCode;


    /*
     * for testing and playing
     */
    public static void main(String args[]) {
        final String defaultDir = System.getProperty(USER_HOME) + "/Desktop/";
        final String defaultFileName1 = "terrorList1";
        final String defaultFileName2 = "terrorList2";
        final String defaultFile1 = defaultDir + defaultFileName1;
        final String defaultFile2 = defaultDir + defaultFileName2;
        SwingUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    JFrame f = new JFrame();
                    f.setLayout(new BorderLayout());
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    DeploymentConfiguration cc = new DeploymentConfiguration();
                    cc.load();
                    File ff1 = new File(defaultFile1);
                    File ff2 = new File(defaultFile2);
                    f.add(new UnsignedAppletsTrustingListPanel(ff2, ff1, cc));
                    f.pack();
                    f.setVisible(true);
                } catch (Exception ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                }
            }
        });
    }
    private final UnsignedAppletsTrustingListPanel self;

    public UnsignedAppletsTrustingListPanel(File globalSettings, File customSettings, DeploymentConfiguration conf) {
        customBackEnd = new UnsignedAppletActionStorageExtendedImpl(customSettings);
        globalBackEnd = new UnsignedAppletActionStorageExtendedImpl(globalSettings);
        customModel = new UnsignedAppletActionTableModel(customBackEnd);
        globalModel = new UnsignedAppletActionTableModel(globalBackEnd);
        customFilter = new ByPermanencyFilter(customModel);
        globalFilter = new ByPermanencyFilter(globalModel);
        self=this;
        this.conf = conf;
        reloadGui();
    }

    public static String appletItemsToCaption(List<UnsignedAppletActionEntry> ii, String caption) {
        StringBuilder sb = new StringBuilder();
        for (UnsignedAppletActionEntry i : ii) {
            sb.append(appletItemToCaption(i, caption)).append("\n");
        }
        return sb.toString();
    }

    public static String appletItemToCaption(UnsignedAppletActionEntry i, String caption) {
        return Translator.R("APPEXTSECguiPanelAppletInfoHederPart1", caption, i.getDocumentBase().getFilteredRegEx())
                + "\n  (" + Translator.R("APPEXTSECguiPanelAppletInfoHederPart2", i.getAppletSecurityActions().toString(), DateFormat.getInstance().format(i.getTimeStamp()))
                + "\n    " + Translator.R("APPEXTSECguiTableModelTableColumnDocumentBase") + ": " + i.getDocumentBase().getFilteredRegEx()
                + "\n    " + Translator.R("APPEXTSECguiTableModelTableColumnCodeBase") + ": " + i.getCodeBase().getFilteredRegEx()
                + "\n    " + Translator.R("APPEXTSECguiTableModelTableColumnArchives") + ": " + UnsignedAppletActionEntry.createArchivesString(i.getArchives());
    }

    public void removeSelectedFromTable(JTable table) {
        removeSelectedFromTable(table, askBeforeActionCheckBox.isSelected(), currentModel, this);
    }

    public static void removeSelectedFromTable(JTable table, boolean ask, UnsignedAppletActionTableModel data, Component forDialog) {
        int[] originalIndexes = table.getSelectedRows();
        List<Integer> newIndexes = new ArrayList<>(originalIndexes.length);
        for (int i = 0; i < originalIndexes.length; i++) {
            //we need to remap values first
            int modelRow = table.convertRowIndexToModel(originalIndexes[i]);
            newIndexes.add(modelRow);
        }
        //now to sort so we can incrementally dec safely
        Collections.sort(newIndexes);
        if (ask) {
            String s = Translator.R("APPEXTSECguiPanelConfirmDeletionOf", newIndexes.size()) + ": \n";
            UnsignedAppletActionEntry[] items = data.back.toArray();
            for (int i = 0; i < newIndexes.size(); i++) {
                Integer integer = newIndexes.get(i);
                s += appletItemToCaption(items[integer], "  ") + "\n";
            }
            int a = JOptionPane.showConfirmDialog(forDialog, s);
            if (a != JOptionPane.OK_OPTION) {
                return;
            }
        }
        int sub = 0;
        for (int i = 0; i < newIndexes.size(); i++) {
            Integer integer = newIndexes.get(i);
            data.removeRow(integer.intValue() + sub);
            sub--;
        }
    }

    public void removeAllItemsFromTable(JTable table, UnsignedAppletActionTableModel model) {
        table.clearSelection();

        if (askBeforeActionCheckBox.isSelected()) {
            UnsignedAppletActionEntry[] items = model.back.toArray();
            String s = Translator.R("APPEXTSECguiPanelConfirmDeletionOf", items.length) + ": \n";
            for (UnsignedAppletActionEntry item : items) {
                s += appletItemToCaption(item, "  ") + "\n";
            }
            int a = JOptionPane.showConfirmDialog(this, s);
            if (a != JOptionPane.OK_OPTION) {
                return;
            }
        }
        model.clear();
    }

    ListCellRenderer<Object> comboRendererWithToolTips = new DefaultListCellRenderer() {

        @Override
        public final Component getListCellRendererComponent(final JList<?> list,
                final Object value, final int index, final boolean isSelected,
                final boolean cellHasFocus) {
            if (value != null) {
                setToolTipText(value.toString());
            }
            return super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);
        }

    };

    private void initComponents() {

        userTableScrollPane = new JScrollPane();
        globalTableScrollPane = new JScrollPane();
        userTable = createTable(customModel);
        globalTable = createTable(globalModel);
        helpButton = new JButton();
        mainPolicyComboBox = new JComboBox<>(new AppletSecurityLevel[]{
            AppletSecurityLevel.DENY_ALL,
            AppletSecurityLevel.DENY_UNSIGNED,
            AppletSecurityLevel.ASK_UNSIGNED,
            AppletSecurityLevel.ALLOW_UNSIGNED
        });
        mainPolicyComboBox.setSelectedItem(AppletSecurityLevel.getDefault());
        mainPolicyComboBox.setRenderer(comboRendererWithToolTips);

        securityLevelLabel = new JLabel();
        globalBehaviourLabel = new JLabel();
        deleteTypeComboBox = new JComboBox<>();
        viewFilter = new JComboBox<>();
        deleteButton = new JButton();
        testUrlButton = new JButton();
        addRowButton = new JButton();
        validateTableButton = new JButton();
        askBeforeActionCheckBox = new JCheckBox();
        filterRegexesCheckBox = new JCheckBox();
        invertSelectionButton = new JButton();
        moveRowUpButton = new JButton();
        moveRowDownButton = new JButton();
        mainTabPanel = new JTabbedPane();

        userTableScrollPane.setViewportView(userTable);

        globalTableScrollPane.setViewportView(globalTable);

        helpButton.setText(Translator.R("APPEXTSECguiPanelHelpButton"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        mainPolicyComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainPolicyComboBoxActionPerformed(evt);
            }
        });

        viewFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userTable.getRowSorter().setSortKeys(null);
                userTable.getRowSorter().setSortKeys(null);
                int i = viewFilter.getSelectedIndex();
                switch (i) {
                    case 0:
                        customFilter.setRowFilter(ByPermanencyFilter.showPermanents);
                        globalFilter.setRowFilter(ByPermanencyFilter.showPermanents);
                        break;
                    case 1:
                        customFilter.setRowFilter(ByPermanencyFilter.showTemporarilyDecisions);
                        globalFilter.setRowFilter(ByPermanencyFilter.showTemporarilyDecisions);
                        break;
                    case 2:
                        customFilter.setRowFilter(ByPermanencyFilter.showAll);
                        globalFilter.setRowFilter(ByPermanencyFilter.showAll);
                        break;
                    case 3:
                        customFilter.setRowFilter(ByPermanencyFilter.showPermanentA);
                        globalFilter.setRowFilter(ByPermanencyFilter.showPermanentA);
                        break;
                    case 4:
                        customFilter.setRowFilter(ByPermanencyFilter.showPermanentN);
                        globalFilter.setRowFilter(ByPermanencyFilter.showPermanentN);
                        break;
                    case 5:
                        customFilter.setRowFilter(ByPermanencyFilter.showHasChosenYes);
                        globalFilter.setRowFilter(ByPermanencyFilter.showHasChosenYes);
                        break;
                    case 6:
                        customFilter.setRowFilter(ByPermanencyFilter.showHasChosenNo);
                        globalFilter.setRowFilter(ByPermanencyFilter.showHasChosenNo);
                        break;
                }

            }
        });

        securityLevelLabel.setText(Translator.R("APPEXTSECguiPanelSecurityLevel"));

        globalBehaviourLabel.setText(Translator.R("APPEXTSECguiPanelGlobalBehaviourCaption"));

        deleteTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
            Translator.R("APPEXTSECguiPanelDeleteMenuSelected"),
            Translator.R("APPEXTSECguiPanelDeleteMenuAllA"),
            Translator.R("APPEXTSECguiPanelDeleteMenuAllN"),
            Translator.R("APPEXTSECguiPanelDeleteMenuAlly"),
            Translator.R("APPEXTSECguiPanelDeleteMenuAlln"),
            Translator.R("APPEXTSECguiPanelDeleteMenuAllAll")}));
        deleteTypeComboBox.setRenderer(comboRendererWithToolTips);
        viewFilter.setModel(new DefaultComboBoxModel<>(new String[]{
            Translator.R("APPEXTSECguiPanelShowOnlyPermanent"),
            Translator.R("APPEXTSECguiPanelShowOnlyTemporal"),
            Translator.R("APPEXTSECguiPanelShowAll"),
            Translator.R("APPEXTSECguiPanelShowOnlyPermanentA"),
            Translator.R("APPEXTSECguiPanelShowOnlyPermanentN"),
            Translator.R("APPEXTSECguiPanelShowOnlyTemporalY"),
            Translator.R("APPEXTSECguiPanelShowOnlyTemporalN")}));
        viewFilter.setRenderer(comboRendererWithToolTips);
        deleteButton.setText(Translator.R("APPEXTSECguiPanelDeleteButton"));
        deleteButton.setToolTipText(Translator.R("APPEXTSECguiPanelDeleteButtonToolTip"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        testUrlButton.setText(Translator.R("APPEXTSECguiPanelTestUrlButton"));
        testUrlButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testUrlButtonActionPerformed(evt);
            }
        });

        addRowButton.setText(Translator.R("APPEXTSECguiPanelAddRowButton"));
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });

        validateTableButton.setText(Translator.R("APPEXTSECguiPanelValidateTableButton"));
        validateTableButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateTableButtonActionPerformed(evt);
            }
        });

        askBeforeActionCheckBox.setSelected(true);
        askBeforeActionCheckBox.setText(Translator.R("APPEXTSECguiPanelAskeforeActionBox"));

        filterRegexesCheckBox.setText(Translator.R("APPEXTSECguiPanelShowRegExesBox"));
        filterRegexesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterRegexesCheckBoxActionPerformed(evt);
            }
        });

        invertSelectionButton.setText(Translator.R("APPEXTSECguiPanelInverSelection"));
        invertSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertSelectionButtonActionPerformed(evt);
            }
        });

        moveRowUpButton.setText(Translator.R("APPEXTSECguiPanelMoveRowUp"));
        moveRowUpButton.setEnabled(false);
        moveRowUpButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRowUpButtonActionPerformed(evt);
            }
        });

        moveRowDownButton.setText(Translator.R("APPEXTSECguiPanelMoveRowDown"));
        moveRowDownButton.setEnabled(false);
        moveRowDownButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRowDownButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(mainTabPanel, GroupLayout.Alignment.LEADING, 0, 583, Short.MAX_VALUE)
                                .addComponent(globalBehaviourLabel, GroupLayout.Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(securityLevelLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mainPolicyComboBox, 0, 474, Short.MAX_VALUE))
                                .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(addRowButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(validateTableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(testUrlButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                                        .addComponent(moveRowDownButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(moveRowUpButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(deleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(deleteTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(invertSelectionButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(askBeforeActionCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(filterRegexesCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 93, Short.MAX_VALUE)
                                                        .addComponent(viewFilter, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(helpButton, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap()
                        .addComponent(globalBehaviourLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(securityLevelLabel)
                                .addComponent(mainPolicyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(deleteButton)
                                                .addComponent(deleteTypeComboBox)
                                                .addComponent(invertSelectionButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(askBeforeActionCheckBox)
                                                .addComponent(filterRegexesCheckBox)
                                                .addComponent(viewFilter)))
                                .addComponent(helpButton, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mainTabPanel, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(addRowButton)
                                .addComponent(validateTableButton)
                                .addComponent(testUrlButton)
                                .addComponent(moveRowUpButton)
                                .addComponent(moveRowDownButton)).addContainerGap()));

        JPanel userPanel = new JPanel(new BorderLayout());
        JPanel globalPanel = new JPanel(new BorderLayout());
        userPanel.add(userTableScrollPane);
        globalPanel.add(globalTableScrollPane);
        mainTabPanel.add(userPanel);
        mainTabPanel.add(globalPanel);
        mainTabPanel.setTitleAt(0, Translator.R("APPEXTSECguiPanelCustomDefs"));
        mainTabPanel.setTitleAt(1, Translator.R("APPEXTSECguiPanelGlobalDefs"));
        mainTabPanel.setToolTipTextAt(0, PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile().getAbsolutePath());
        mainTabPanel.setToolTipTextAt(1, PathsAndFiles.APPLET_TRUST_SETTINGS_SYS.getFile().getAbsolutePath());
        mainTabPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectCurrentTable();
            }
        });
    }

    private void selectCurrentTable() {
        switch (mainTabPanel.getSelectedIndex()) {
            case 0:
                currentModel = customModel;
                currentTable = userTable;
                break;
            case 1:
                currentModel = globalModel;
                currentTable = globalTable;
                break;
        }
        setButtons((!currentModel.back.isReadOnly()));
    }

    private void mainPolicyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            conf.setProperty(ConfigurationConstants.KEY_SECURITY_LEVEL, ((AppletSecurityLevel) mainPolicyComboBox.getSelectedItem()).toChars());
            conf.save();
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            JOptionPane.showMessageDialog(this, ex);
        }
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {

        if (deleteTypeComboBox.getSelectedIndex() == 0) {
            removeSelectedFromTable(currentTable);
        }
        if (deleteTypeComboBox.getSelectedIndex() == 1) {
            removeByBehaviour(ExecuteAppletAction.ALWAYS);
        }
        if (deleteTypeComboBox.getSelectedIndex() == 2) {
            removeByBehaviour(ExecuteAppletAction.NEVER);
        }
        if (deleteTypeComboBox.getSelectedIndex() == 3) {
            removeByBehaviour(ExecuteAppletAction.YES);
        }
        if (deleteTypeComboBox.getSelectedIndex() == 4) {
            removeByBehaviour(ExecuteAppletAction.NO);
        }
        if (deleteTypeComboBox.getSelectedIndex() == 5) {
            removeAllItemsFromTable(currentTable, customModel);
        }
    }

    private void testUrlButtonActionPerformed(java.awt.event.ActionEvent evt) {

        String s1 = JOptionPane.showInputDialog(Translator.R("APPEXTSECguiPanelDocTest"), lastDoc);
        String s2 = JOptionPane.showInputDialog(Translator.R("APPEXTSECguiPanelCodeTest"), lastCode);
        lastDoc = s1;
        lastCode = s2;
        try {
            List<UnsignedAppletActionEntry> i = currentModel.back.getMatchingItems(s1, s2, null);
            if (i == null || i.isEmpty()) {
                JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelNoMatch"));
            } else {
                JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelMatchingNote") + "\n" + appletItemsToCaption(i, Translator.R("APPEXTSECguiPanelMatched") + ": "));
            }
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelMatchingError", ex));
        }

    }

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {

        currentModel.addRow();
    }

    private void validateTableButtonActionPerformed(java.awt.event.ActionEvent evt) {

        File f = null;
        try {
            f = File.createTempFile("appletTable", "validation");
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelCanNOtValidate", ex.toString()));
            return;
        }
        try {
            currentModel.back.writeContentsLocked();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), UTF_8));
            currentModel.back.writeContent(bw);
            bw.flush();
            bw.close();
            UnsignedAppletActionStorageExtendedImpl copy = new UnsignedAppletActionStorageExtendedImpl(f);
            UnsignedAppletActionEntry[] items = copy.toArray();
            for (UnsignedAppletActionEntry unsignedAppletActionEntry : items) {
                if (unsignedAppletActionEntry.getDocumentBase() != null && !unsignedAppletActionEntry.getDocumentBase().getRegEx().trim().isEmpty()) {
                    Pattern p = Pattern.compile(unsignedAppletActionEntry.getDocumentBase().getRegEx());
                    p.matcher("someInput").find();
                } else {
                    throw new RuntimeException("All document-bases must be full");
                }
                if (unsignedAppletActionEntry.getCodeBase() != null && !unsignedAppletActionEntry.getCodeBase().getRegEx().trim().isEmpty()) {
                    Pattern p = Pattern.compile(unsignedAppletActionEntry.getCodeBase().getRegEx());
                    p.matcher("someInput").find();
                } else {
                    throw new RuntimeException("All code-bases must be full");
                }
                UnsignedAppletActionEntry.createArchivesString(UnsignedAppletActionEntry.createArchivesList(UnsignedAppletActionEntry.createArchivesString(unsignedAppletActionEntry.getArchives())));
            }
            JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelTableValid"));
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            JOptionPane.showMessageDialog(this, Translator.R("APPEXTSECguiPanelTableInvalid", ex.toString()));
        } finally {
            f.delete();
        }

    }

    private void filterRegexesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {

        reloadTable();
    }

    private void invertSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedIndexs = currentTable.getSelectedRows();
        currentTable.selectAll();

        for (int i = 0; i < currentTable.getRowCount(); i++) {
            for (int selectedIndex : selectedIndexs) {
                if (selectedIndex == i) {
                    currentTable.removeRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void moveRowUpButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int orig = currentTable.getSelectedRow();
        if (orig < 0 || orig >= currentTable.getRowCount()) {
            return;
        }
        int nw = 0;
        while (true) {
            int i = currentTable.convertRowIndexToModel(orig);
            int nwx = currentModel.moveUp(i);
            reloadTable();
            nw = currentTable.convertRowIndexToView(nwx);
            if (i == nwx) {
                break;
            }
            if (nw != orig) {
                break;
            }
        }
        //ItwLogger.getLogger().log(OutputController.Level.ERROR_ALL, orig+" "+i+" "+nwx+" "+nw+" ");
        if (nw != orig) {
            if (orig >= 1) {
                currentTable.getSelectionModel().setSelectionInterval(orig - 1, orig - 1);
            }
        } else {
            currentTable.getSelectionModel().setSelectionInterval(orig, orig);
        }
    }

    private void moveRowDownButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int orig = currentTable.getSelectedRow();
        if (orig < 0 || orig >= currentTable.getRowCount()) {
            return;
        }
        int nw = 0;
        while (true) {
            int i = currentTable.convertRowIndexToModel(orig);
            int nwx = currentModel.moveDown(i);
            reloadTable();
            nw = currentTable.convertRowIndexToView(nwx);
            if (i == nwx) {
                break;
            }
            if (nw != orig) {
                break;
            }
        }
        // OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, orig+" "+i+" "+nwx+" "+nw+" ");
        if (nw != orig) {
            if (orig < currentModel.getRowCount()) {
                currentTable.getSelectionModel().setSelectionInterval(orig + 1, orig + 1);
            }
        } else {
            currentTable.getSelectionModel().setSelectionInterval(orig, orig);
        }
    }

    private static void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JDialog d = new ExtendedAppletSecurityHelp(null, false);
        ScreenFinder.centerWindowsToCurrentScreen(d);
        d.setVisible(true);
    }

    private void setButtons(boolean b) {
        deleteButton.setEnabled(b);
        addRowButton.setEnabled(b);
        invertSelectionButton.setEnabled(b);
        moveRowUpButton.setEnabled(b);
        moveRowDownButton.setEnabled(b);
    }

    private JTable createTable(final TableModel model) {
        final JTable jt = new JTable() {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                int columnx = convertColumnIndexToModel(column);
                if (columnx == 0) {
                    //FIXME add proper editor her egoes jbutton, popupr dialog
                    return new DefaultCellEditor(new JTextField());
                }
                if (columnx == 2) {
                    column = convertColumnIndexToModel(column);
                    row = convertRowIndexToModel(row);
                    return new DefaultCellEditor(new MyTextField((UrlRegEx) (model.getValueAt(row, column))));
                }
                if (columnx == 3) {
                    column = convertColumnIndexToModel(column);
                    row = convertRowIndexToModel(row);
                    return new DefaultCellEditor(new MyTextField((UrlRegEx) (model.getValueAt(row, column))));
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                int columnx = convertColumnIndexToModel(column);
                if (columnx == 1) {
                    column = convertColumnIndexToModel(column);
                    row = convertRowIndexToModel(row);
                    return new UrlRegexCellRenderer.MyDateCellRenderer((Date) (model.getValueAt(row, column)));
                }
                if (columnx == 2) {
                    if (!filterRegexesCheckBox.isSelected()) {
                        column = convertColumnIndexToModel(column);
                        row = convertRowIndexToModel(row);
                        return new UrlRegexCellRenderer((UrlRegEx) (model.getValueAt(row, column)));
                    }
                }
                if (columnx == 3) {
                    if (!filterRegexesCheckBox.isSelected()) {
                        column = convertColumnIndexToModel(column);
                        row = convertRowIndexToModel(row);
                        return new UrlRegexCellRenderer((UrlRegEx) (model.getValueAt(row, column)));
                    }
                }
                return super.getCellRenderer(row, column);
            }
        };
        jt.setRowHeight(jt.getRowHeight() + jt.getRowHeight() / 2);
        jt.setModel(model);

        jt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 & jt.getSelectedRowCount() == 1) {
                    RememberableDialogueEditor d = new RememberableDialogueEditor(grabParentFrame(self), true, jt.getModel().getValueAt(jt.convertRowIndexToModel(jt.getSelectedRow()), 0));
                    d.setVisible(true);
                    AppletSecurityActions result = d.getResult();
                    d.dispose();
                    if (result != null) {
                        ((UnsignedAppletActionTableModel) (jt.getModel())).setValueAt(
                                result, jt.convertRowIndexToModel(jt.getSelectedRow()), 0);
                        userTableScrollPane.repaint();
                        globalTableScrollPane.repaint();
                    }
                }
            }

            private JFrame grabParentFrame(Component self) {
                Container parent = self.getParent();
                while (parent!=null){
                    if (parent instanceof  JFrame){
                        return (JFrame) parent;
                    }
                    parent=parent.getParent();

                }
                return null;
            }

        });
        jt.setToolTipText(Translator.R("EPEhelp5"));
        return jt;

    }

    private void reloadTable() {
        List<? extends SortKey> l = currentTable.getRowSorter().getSortKeys();
        currentTable.setModel(new DefaultTableModel());
        currentTable.setModel(currentModel);
        {
            currentTable.getRowSorter().setSortKeys(l);

        }

    }

    private void removeByBehaviour(ExecuteAppletAction unsignedAppletAction) {
        UnsignedAppletActionEntry[] items = currentModel.back.toArray();
        if (askBeforeActionCheckBox.isSelected()) {
            List<UnsignedAppletActionEntry> toBeDeleted = new ArrayList<>();
            for (UnsignedAppletActionEntry unsignedAppletActionEntry : items) {
                AppletSecurityActions actions = unsignedAppletActionEntry.getAppletSecurityActions();
                for (int j = 0; j < actions.getRealCount(); j++) {
//                    ExecuteAppletAction action = actions.getAction(j);
//                    if (action == unsignedAppletAction) {
//                        toBeDeleted.add(unsignedAppletActionEntry);
//                    }
                }
            }
            String s = Translator.R("APPEXTSECguiPanelConfirmDeletionOf", toBeDeleted.size()) + ": \n";
            for (UnsignedAppletActionEntry toBeDeleted1 : toBeDeleted) {
                s += appletItemToCaption(toBeDeleted1, "  ") + "\n";
            }
            int a = JOptionPane.showConfirmDialog(this, s);
            if (a != JOptionPane.OK_OPTION) {
                return;
            }
        }
        currentModel.removeByBehaviour(unsignedAppletAction);
    }

    private void reloadGui() {
        this.removeAll();
        initComponents();
        userTable.setRowSorter(customFilter);
        globalTable.setRowSorter(globalFilter);
        AppletSecurityLevel gs = AppletSecurityLevel.getDefault();
        String s = this.conf.getProperty(ConfigurationConstants.KEY_SECURITY_LEVEL);
        if (s != null) {
            gs = AppletSecurityLevel.fromString(s);
        }
        mainPolicyComboBox.setSelectedItem(gs);
        userTable.getSelectionModel().addListSelectionListener(new SingleSelectionListenerImpl(userTable));
        globalTable.getSelectionModel().addListSelectionListener(new SingleSelectionListenerImpl(globalTable));

        userTable.addKeyListener(new DeleteAdapter(userTable));
        globalTable.addKeyListener(new DeleteAdapter(globalTable));
        currentTable = userTable;
        currentModel = customModel;
        setButtons((!currentModel.back.isReadOnly()));
    }

    public static final class MyTextField extends JTextField {

        private final UrlRegEx keeper;

        private MyTextField(UrlRegEx urlRegEx) {
            if (urlRegEx == null) {
                keeper = UrlRegEx.exact("");
            } else {
                this.keeper = urlRegEx;
            }
            setText(keeper.getFilteredRegEx());
        }

        @Override
        public void setText(String t) {
            super.setText(keeper.getRegEx());
        }
    }

    public static final class UrlRegexCellRenderer extends DefaultTableCellRenderer {

        private final UrlRegEx keeper;

        private UrlRegexCellRenderer(UrlRegEx urlRegEx) {
            if (urlRegEx == null) {
                keeper = UrlRegEx.exact("");
            } else {
                this.keeper = urlRegEx;
            }
            setText(keeper.getFilteredRegEx());
        }

        @Override
        public void setText(String t) {
            if (keeper == null) {
                super.setText("");
            } else {
                super.setText(keeper.getFilteredRegEx());
            }
        }

        public static final class MyDateCellRenderer extends DefaultTableCellRenderer {

            private final Date keeper;

            private MyDateCellRenderer(Date d) {
                this.keeper = d;
                setText(DateFormat.getInstance().format(d));
            }

            @Override
            public void setText(String t) {
                if (keeper == null) {
                    super.setText("");
                } else {
                    super.setText(DateFormat.getInstance().format(keeper));
                }
            }
        }
    }

    private final class SingleSelectionListenerImpl implements ListSelectionListener {

        private final JTable table;

        public SingleSelectionListenerImpl(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (table.getSelectedRows().length == 1 && !currentModel.back.isReadOnly()) {
                moveRowUpButton.setEnabled(true);
                moveRowDownButton.setEnabled(true);
            } else {
                moveRowUpButton.setEnabled(false);
                moveRowDownButton.setEnabled(false);
            }
        }
    }

    private final class DeleteAdapter implements KeyListener {

        private final JTable table;

        public DeleteAdapter(JTable table) {
            this.table = table;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE && !currentModel.back.isReadOnly()) {
                removeSelectedFromTable(table, askBeforeActionCheckBox.isSelected(), (UnsignedAppletActionTableModel) table.getModel(), UnsignedAppletsTrustingListPanel.this);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private abstract static class MyCommonSorter extends RowFilter<UnsignedAppletActionTableModel, Integer> {

    }

    private static final class ByPermanencyFilter extends TableRowSorter<UnsignedAppletActionTableModel> {

        private static final class ShowAll extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                return true;
            }
        }

        private static final class ShowPermanents extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.ALWAYS) || o.equals(ExecuteAppletAction.NEVER)) {
                        return true;
                    }
                }
                return false;
            }
        }

        private static final class ShowPermanentA extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.ALWAYS)) {
                        return true;
                    }
                }
                return false;
            }

        }

        private static final class ShowPermanentN extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.NEVER)) {
                        return true;
                    }
                }
                return false;
            }
        }

        private static final class ShowTemporarilyDecisions extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.YES) || o.equals(ExecuteAppletAction.NO)) {
                        return true;
                    }
                }
                return false;
            }
        }

        private static final class ShowHasChosenYes extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.YES)) {
                        return true;
                    }
                }
                return false;
            }

        }

        private static final class ShowHasChosenNo extends MyCommonSorter {

            @Override
            public boolean include(Entry<? extends UnsignedAppletActionTableModel, ? extends Integer> entry) {
                AppletSecurityActions as = (AppletSecurityActions) entry.getModel().getValueAt(entry.getIdentifier(), 0);
                Collection<ExecuteAppletAction> l = as.getActions();
                for (ExecuteAppletAction o : l) {
                    if (o.equals(ExecuteAppletAction.NO)) {
                        return true;
                    }
                }
                return false;
            }

        }
        public static final ShowAll showAll = new ShowAll();
        public static final ShowPermanents showPermanents = new ShowPermanents();
        public static final ShowPermanentA showPermanentA = new ShowPermanentA();
        public static final ShowPermanentN showPermanentN = new ShowPermanentN();
        public static final ShowTemporarilyDecisions showTemporarilyDecisions = new ShowTemporarilyDecisions();
        public static final ShowHasChosenYes showHasChosenYes = new ShowHasChosenYes();
        public static final ShowHasChosenNo showHasChosenNo = new ShowHasChosenNo();

        public ByPermanencyFilter(UnsignedAppletActionTableModel model) {
            super(model);
            setRowFilter(showPermanents);
        }
    }
}
