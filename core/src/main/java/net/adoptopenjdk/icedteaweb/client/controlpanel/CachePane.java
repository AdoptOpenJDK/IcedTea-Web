/* CachePane.java -- Displays the specified folder and allows modification to its content.
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
package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.resources.cache.CachedFile;
import net.adoptopenjdk.icedteaweb.resources.cache.ResourceInfo;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.RestrictedFileUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CachePane extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(CachePane.class);

    private final JDialog parent;

    private final DeploymentConfiguration config;

    private JTable cacheTable;

    private JButton deleteButton, refreshButton, doneButton, cleanAll, infoButton;

    /**
     * Creates a new instance of the CachePane.
     *
     * @param parent The parent dialog that uses this pane.
     * @param config configuration tobe worked on
     */
    public CachePane(JDialog parent, DeploymentConfiguration config) {
        super(new BorderLayout());
        this.parent = parent;
        this.config = config;
        addComponents();
    }

    /**
     * Add components to the pane.
     */
    private void addComponents() {
        final JPanel topPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        final TableModel model = new CacheTableModel();

        cacheTable = new JTable(model);
        cacheTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cacheTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            final public void valueChanged(ListSelectionEvent listSelectionEvent) {
                // If no row has been selected, disable the delete button, else enable it
                if (cacheTable.getSelectionModel().isSelectionEmpty()) {
                    deleteButton.setEnabled(false);
                    infoButton.setEnabled(false);
                }
                else {
                    deleteButton.setEnabled(true);
                    infoButton.setEnabled(true);
                }
            }
        });
        cacheTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        cacheTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
        cacheTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(cacheTable);

        TableRowSorter<TableModel> tableSorter = new TableRowSorter<>(model);
        final Comparator<Comparable<?>> comparator = new Comparator<Comparable<?>>() { // General purpose Comparator
            @Override
            @SuppressWarnings("unchecked")
            public final int compare(final Comparable a, final Comparable b) {
                return a.compareTo(b);
            }
        };
        tableSorter.setComparator(1, comparator); // Comparator for path column.
        tableSorter.setComparator(4, comparator); // Comparator for size column.
        tableSorter.setComparator(5, comparator); // Comparator for modified column.
        cacheTable.setRowSorter(tableSorter);
        final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public final Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                switch (column) {
                    case 1: // Path column
                    // Render absolute path
                    super.setText(((File)value).getAbsolutePath());
                    break;
                    case 4: // Size column
                    // Render size formatted to default locale's number format
                    super.setText(NumberFormat.getInstance().format(value));
                    break;
                    case 5: // last modified column
                    // Render modify date formatted to default locale's date format
                    super.setText(DateFormat.getDateInstance().format(value));
                }

                return this;
            }
        };
        // TableCellRenderer for path column
        cacheTable.getColumn(CacheTableModel.columns[1]).setCellRenderer(tableCellRenderer);
        // TableCellRenderer for size column
        cacheTable.getColumn(CacheTableModel.columns[4]).setCellRenderer(tableCellRenderer);
        // TableCellRenderer for last modified column
        cacheTable.getColumn(CacheTableModel.columns[5]).setCellRenderer(tableCellRenderer);

        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        topPanel.add(scrollPane, c);
        this.add(topPanel, BorderLayout.CENTER);
        this.add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Create the buttons panel.
     *
     * @return JPanel containing the buttons.
     */
    private Component createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        List<JButton> buttons = new ArrayList<>();

        this.infoButton = new JButton(Translator.R("ButMoreInformation"));
        infoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog jd = new JDialog(parent, true);
                JTextArea t = new JTextArea();
                t.setEditable(false);

                int row = cacheTable.getSelectedRow();
                try {
                    int modelRow = cacheTable.convertRowIndexToModel(row);
                    CachedFile cachedFile = ((CachedFile) cacheTable.getModel().getValueAt(modelRow, 0));
                    t.setText("content-length: " + cachedFile.getSize() + "\n" +
                            "last-updated: " + cachedFile.getDownloadedAt() + "\n" +
                            "last-modified: " + cachedFile.getLastModified()
                    );
                } catch (Exception ex) {
                    t.setText(ex.toString());
                }
                jd.add(t);
                jd.setSize(300, 300);
                jd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                jd.setVisible(true);
            }
        });
        infoButton.setEnabled(false);
        buttons.add(infoButton);

        this.deleteButton = new JButton(Translator.R("CVCPButDelete"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableButtons();
                // Delete on AWT thread after this action has been performed
                // in order to allow the cache viewer to update itself
                invokeLaterDelete();
            }
        });
        deleteButton.setEnabled(false);
        buttons.add(deleteButton);

        this.cleanAll = new JButton(Translator.R("CVCPCleanCache"));
        cleanAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                disableButtons();
                // Delete on AWT thread after this action has been performed
                // in order to allow the cache viewer to update itself
                invokeLaterDeleteAll();
            }
        });
        buttons.add(cleanAll);

        this.refreshButton = new JButton(Translator.R("CVCPButRefresh"));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableButtons();
                // Populate cacheTable on AWT thread after this action event has been performed
                invokeLaterPopulateTable();
            }
        });
        refreshButton.setEnabled(false);
        buttons.add(refreshButton);

        this.doneButton = new JButton(Translator.R("ButDone"));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                        new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
            }
        });

        int maxWidth = 0;
        int maxHeight = 0;
        for (JButton button : buttons) {
            maxWidth = Math.max(button.getMinimumSize().width, maxWidth);
            maxHeight = Math.max(button.getMinimumSize().height, maxHeight);
        }

        int wantedWidth = maxWidth + 10;
        int wantedHeight = maxHeight;
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(wantedWidth, wantedHeight));
            leftPanel.add(button);
        }

        doneButton.setPreferredSize(new Dimension(wantedWidth, wantedHeight));
        doneButton.setEnabled(false);
        rightPanel.add(doneButton);
        buttonPanel.add(leftPanel);
        buttonPanel.add(rightPanel);

        return buttonPanel;
    }

    /**
     * Posts an event to the event queue to delete the currently selected
     * resource in {@link CachePane#cacheTable} after the {@code CachePane} and
     * {@link CacheViewer} have been instantiated and painted.
     * @see CachePane#cacheTable
     */
    private  void invokeLaterDelete() {
        SwingUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FileLock fl = null;
                    File netxRunningFile = new File(PathsAndFiles.MAIN_LOCK.getFullPath(config));
                    if (!netxRunningFile.exists()) {
                        try {
                            FileUtils.createParentDir(netxRunningFile);
                            RestrictedFileUtils.createRestrictedFile(netxRunningFile);
                        } catch (IOException e1) {
                            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e1);
                        }
                    }

                    try {
                        fl = FileUtils.getFileLock(netxRunningFile.getPath(), false, false);
                    } catch (FileNotFoundException e1) {
                    }

                    int row = cacheTable.getSelectedRow();
                    try {
                        if (fl == null) {
                            JOptionPane.showMessageDialog(parent, Translator.R("CCannotClearCache"));
                            return;
                        }
                        int modelRow = cacheTable.convertRowIndexToModel(row);
                        CachedFile cachedFile = ((CachedFile) cacheTable.getModel().getValueAt(modelRow, 0));
                        Cache.deleteFromCache(cachedFile);
                        ((CacheTableModel) cacheTable.getModel()).removeRow(modelRow);
                        cacheTable.getSelectionModel().clearSelection();
                    } catch (Exception exception) {
                        // ignore
                    }

                    if (fl != null) {
                        try {
                            fl.release();
                            fl.channel().close();
                        } catch (IOException e1) {
                            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e1);
                        }
                    }
                } catch (Exception exception) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exception);
                } finally {
                    restoreDisabled();
                }
            }
        });
    }

    private void invokeLaterDeleteAll() {
        SwingUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    visualCleanCache(parent);
                    populateTable();
                } catch (Exception exception) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exception);
                } finally {
                    restoreDisabled();
                }
            }
        });
    }

    /**
     * Posts an event to the event queue to populate the
     * {@link CachePane#cacheTable} after the {@code CachePane} and
     * {@link CacheViewer} have been instantiated and painted.
     * @see CachePane#populateTable
     */
    final void invokeLaterPopulateTable() {
        SwingUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    populateTable();
                    // Disable cacheTable when no data to display, so no events are generated
                    if (cacheTable.getModel().getRowCount() == 0) {
                        cacheTable.setEnabled(false);
                        cacheTable.setBackground(SystemColor.control);
                        // No data in cacheTable, so nothing to delete
                        deleteButton.setEnabled(false);
                        infoButton.setEnabled(false);
                    } else {
                        cacheTable.setEnabled(true);
                        cacheTable.setBackground(SystemColor.text);
                    }
                } catch (Exception exception) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exception);
                } finally {
                    refreshButton.setEnabled(true);
                    doneButton.setEnabled(true);
                    cleanAll.setEnabled(true);
                }
            }
        });
    }

    /**
     * Populate the table with fresh data. Any manual updates to the cache
     * directory will be updated in the table.
     */
    private void populateTable() {
        try {
            // Populating the cacheTable may take a while, so indicate busy by cursor
            parent.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            CacheTableModel tableModel;
            (tableModel = (CacheTableModel)cacheTable.getModel()).clear(); //Clears the table
            final List<CacheIdInfo> cacheIdInfos = Cache.getDomainCacheIds();
            for (CacheIdInfo cacheIdInfo : cacheIdInfos) {
                tableModel.addAll(cacheIdInfo.getFileInfos());
            }

        } catch (Exception exception) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, exception);
        } finally {
            // Reset cursor
            parent.getContentPane().setCursor(Cursor.getDefaultCursor());
        }
    }

    public void disableButtons() {
        // may take a while, so indicate busy by cursor
        parent.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Disable dialog and buttons while operating
        deleteButton.setEnabled(false);
        infoButton.setEnabled(false);
        refreshButton.setEnabled(false);
        doneButton.setEnabled(false);
        cleanAll.setEnabled(false);
    }

    public void restoreDisabled() {
        cleanAll.setEnabled(true);
        // If nothing selected then keep deleteButton disabled
        if (!cacheTable.getSelectionModel().isSelectionEmpty()) {
            deleteButton.setEnabled(true);
            infoButton.setEnabled(true);
        }
        // Enable buttons
        refreshButton.setEnabled(true);
        doneButton.setEnabled(true);
        // If cacheTable is empty disable it and set background
        // color to indicate being disabled
        if (cacheTable.getModel().getRowCount() == 0) {
            cacheTable.setEnabled(false);
            cacheTable.setBackground(SystemColor.control);
        }
        // Reset cursor
        parent.getContentPane().setCursor(Cursor.getDefaultCursor());
    }

    public static void visualCleanCache(Component parent) {
        try {
            boolean success = Cache.clearCache();
            if (!success) {
                JOptionPane.showMessageDialog(parent, Translator.R("CCannotClearCache"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, Translator.R("CCannotClearCache"));
        }
    }
}


