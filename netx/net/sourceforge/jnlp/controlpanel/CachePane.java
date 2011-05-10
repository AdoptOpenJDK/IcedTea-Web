/* CachePane.java -- Displays the specified folder and allows modification to its content.
Copyright (C) 2010 Red Hat

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
package net.sourceforge.jnlp.controlpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.jnlp.cache.CacheDirectory;
import net.sourceforge.jnlp.cache.DirectoryNode;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.PropertiesFile;

public class CachePane extends JPanel {

    JDialog parent;
    DeploymentConfiguration config;
    private String location;
    private JComponent defaultFocusComponent;
    DirectoryNode root;
    String[] columns = { Translator.R("CVCPColName"),
            Translator.R("CVCPColPath"),
            Translator.R("CVCPColType"),
            Translator.R("CVCPColDomain"),
            Translator.R("CVCPColSize"),
            Translator.R("CVCPColLastModified") };
    JTable cacheTable;

    /**
     * Creates a new instance of the CachePane.
     * 
     * @param parent The parent dialog that uses this pane.
     * @param config The DeploymentConfiguration file.
     */
    public CachePane(JDialog parent, DeploymentConfiguration config) {
        super(new BorderLayout());
        this.parent = parent;
        this.config = config;
        location = config.getProperty(DeploymentConfiguration.KEY_USER_CACHE_DIR);

        addComponents();
    }

    /**
     * Add components to the pane.
     */
    private void addComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cacheTable = new JTable(model);
        cacheTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cacheTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        cacheTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
        cacheTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(cacheTable);

        populateTable();

        TableRowSorter<DefaultTableModel> tableSorter = new TableRowSorter<DefaultTableModel>(model);
        tableSorter.setComparator(4, new Comparator<Long>() { // Comparator for size column.
            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        });
        tableSorter.setComparator(5, new Comparator<String>() { // Comparator for date column.
            @Override
            public int compare(String o1, String o2) {
                DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    Long time1 = format.parse(o1).getTime();
                    Long time2 = format.parse(o2).getTime();
                    return time1.compareTo(time2);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
        cacheTable.setRowSorter(tableSorter);

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

        List<JButton> buttons = new ArrayList<JButton>();

        JButton deleteButton = new JButton(Translator.R("CVCPButDelete"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileLock fl = null;
                File netxRunningFile = new File(config.getProperty(DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE));
                if (!netxRunningFile.exists()) {
                    try {
                        FileUtils.createParentDir(netxRunningFile);
                        FileUtils.createRestrictedFile(netxRunningFile, true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                try {
                    fl = FileUtils.getFileLock(netxRunningFile.getPath(), false, false);
                } catch (FileNotFoundException e1) {
                }

                int row = cacheTable.getSelectedRow();
                try {
                    if (fl == null) return;
                    if (row == -1 || row > cacheTable.getRowCount() - 1)
                        return;
                    int modelRow = cacheTable.convertRowIndexToModel(row);
                    DirectoryNode fileNode = ((DirectoryNode) cacheTable.getModel().getValueAt(modelRow, 0));
                    if (fileNode.getFile().delete()) {
                        updateRecentlyUsed(fileNode.getFile());
                        fileNode.getParent().removeChild(fileNode);
                        FileUtils.deleteWithErrMesg(fileNode.getInfoFile());
                        ((DefaultTableModel) cacheTable.getModel()).removeRow(modelRow);
                        cacheTable.getSelectionModel().setSelectionInterval(row, row);
                        CacheDirectory.cleanParent(fileNode);
                    }
                } catch (Exception exception) {
                    //ignore
                }

                if (fl != null) {
                    try {
                        fl.release();
                        fl.channel().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            private void updateRecentlyUsed(File f) {
                File recentlyUsedFile = new File(location + File.separator + "recently_used");
                PropertiesFile pf = new PropertiesFile(recentlyUsedFile);
                pf.load();
                Enumeration<Object> en = pf.keys();
                while (en.hasMoreElements()) {
                    String key = (String) en.nextElement();
                    if (pf.get(key).equals(f.getAbsolutePath())) {
                        pf.remove(key);
                    }
                }
                pf.store();
            }
        });
        buttons.add(deleteButton);

        JButton refreshButton = new JButton(Translator.R("CVCPButRefresh"));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateTable();
            }
        });
        buttons.add(refreshButton);

        JButton doneButton = new JButton(Translator.R("ButDone"));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.dispose();
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
        rightPanel.add(doneButton);
        buttonPanel.add(leftPanel);
        buttonPanel.add(rightPanel);

        return buttonPanel;
    }

    /**
     * Populate the table with fresh data. Any manual updates to the cache
     * directory will be updated in the table.
     */
    private void populateTable() {
        ((DefaultTableModel) cacheTable.getModel()).setRowCount(0); //Clears the table
        for (Object[] v : generateData(root))
            ((DefaultTableModel) cacheTable.getModel()).addRow(v);
    }

    /**
     * This creates the data for the table.
     * 
     * @param root The location of cache data.
     * @return ArrayList containing an Object array of data for each row in the table.
     */
    private ArrayList<Object[]> generateData(DirectoryNode root) {
        root = new DirectoryNode("Root", location, null);
        CacheDirectory.getDirStructure(root);
        ArrayList<Object[]> data = new ArrayList<Object[]>();

        for (DirectoryNode identifier : root.getChildren()) {
            for (DirectoryNode type : identifier.getChildren()) {
                for (DirectoryNode domain : type.getChildren()) {
                    for (DirectoryNode leaf : CacheDirectory.getLeafData(domain)) {
                        Object[] o = { leaf,
                                leaf.getFile().getAbsolutePath(),
                                type,
                                domain,
                                leaf.getFile().length(),
                                new SimpleDateFormat("MM/dd/yyyy").format(leaf.getFile().lastModified()) };
                        data.add(o);
                    }
                }
            }
        }

        return data;
    }

    /**
     * Put focus onto default button.
     */
    public void focusOnDefaultButton() {
        if (defaultFocusComponent != null) {
            defaultFocusComponent.requestFocusInWindow();
        }
    }
}
