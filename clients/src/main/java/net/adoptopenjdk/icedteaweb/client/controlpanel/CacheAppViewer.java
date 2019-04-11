/* CacheViewer.java -- Display the GUI for viewing and deleting cache files.
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

import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.util.ImageResources;
import net.adoptopenjdk.icedteaweb.ui.swing.ScreenFinder;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This class will provide a visual way of viewing cache ids.
 *
 *
 */
public class CacheAppViewer extends JDialog {

    private boolean initialized = false;
    private static final String dialogTitle = Translator.R("CVCPDialogTitle");
    private final DeploymentConfiguration config; // Configuration file which contains all the settings.

    /**
     * Creates a new instance of the cache viewer.
     *
     * @param config Deployment configuration file.
     */
    public CacheAppViewer(DeploymentConfiguration config) {
        super((Frame) null, dialogTitle, true); // Don't need a parent.
        this.setName("CacheViewer");
        SwingUtils.info(this);
        this.config = config;
        if (config == null) {
            throw new IllegalArgumentException("config: " + config);
        }
        setIconImages(ImageResources.INSTANCE.getApplicationImages());
        /* Prepare for adding components to dialog box */
        create();
    }

    private void create() {
        Container parentPane = getContentPane();
        Container mainPane = new JPanel();
        parentPane.setLayout(new BorderLayout());
        mainPane.setLayout(new GridLayout(2, 1));
        parentPane.add(mainPane);
        final JTextArea info = new JTextArea();
        info.setEditable(false);
        final JTabbedPane idTabs = new JTabbedPane();
        final JPanel jnlpPaths = new JPanel(new BorderLayout());
        jnlpPaths.setName("jnlp-path");
        final JPanel domains = new JPanel(new BorderLayout());
        domains.setName("domain");
        idTabs.add(jnlpPaths);
        idTabs.add(domains);
        final JButton delete = new JButton(Translator.R("TIFPDeleteFiles"));
        DummyCacheIdListModel jnlpPathsIds = new DummyCacheIdListModel(CacheUtil.getCacheIds(".*", true, false));
        DummyCacheIdListModel domainIds = new DummyCacheIdListModel(CacheUtil.getCacheIds(".*", false, true));
        final JList<CacheUtil.CacheId> appsByJnlpPath = new JList<>();
        final JList<CacheUtil.CacheId> appsByDomain = new JList<>();
        appsByJnlpPath.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appsByDomain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        delete.setEnabled(false);
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Dimension d = CacheAppViewer.this.getSize();
                SwingUtils.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (idTabs.getSelectedComponent()==jnlpPaths){
                            CacheUtil.clearCache(appsByJnlpPath.getSelectedValue().getId(), true, false);
                        }
                        if (idTabs.getSelectedComponent()==domains){
                            CacheUtil.clearCache(appsByDomain.getSelectedValue().getId(), false, true);
                        }
                        CacheAppViewer.this.getContentPane().removeAll();
                        CacheAppViewer.this.pack();
                        create();
                        CacheAppViewer.this.setSize(d);
                    }
                });
            }
        });
        appsByJnlpPath.setModel(jnlpPathsIds);
        appsByDomain.setModel(domainIds);
        appsByJnlpPath.addListSelectionListener(new DummyListSelectionListenerWithModel(info, appsByJnlpPath, delete));
        appsByDomain.addListSelectionListener(new DummyListSelectionListenerWithModel(info, appsByDomain, delete));
        jnlpPaths.add(mainPane.add(new JScrollPane(appsByJnlpPath)));
        domains.add(mainPane.add(new JScrollPane(appsByDomain)));
        mainPane.add(idTabs);
        idTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                appsByDomain.clearSelection();
                appsByJnlpPath.clearSelection();
            }
        });
        mainPane.add(new JScrollPane(info));
        parentPane.add(delete, BorderLayout.SOUTH);
        pack();

    }

    public void centerDialog() {
        ScreenFinder.centerWindowsToCurrentScreen(this);
    }
    
    private static class DummyListSelectionListenerWithModel implements ListSelectionListener {

        private final JTextArea info;
        private final JList<CacheUtil.CacheId> apps;
        private final JButton delete;

        public DummyListSelectionListenerWithModel(JTextArea info, JList<CacheUtil.CacheId> apps, JButton delete) {
            this.info = info;
            this.apps = apps;
            this.delete = delete;
        }
        
            @Override
            public void valueChanged(ListSelectionEvent e) {
                info.setText("");
                if (apps.getSelectedValue() != null) {
                    for (Object[] o : apps.getSelectedValue().getFiles()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < o.length; i++) {
                            Object object = o[i];
                            if (object == null) {
                                object = "??";
                            }
                            sb.append(object.toString()).append(" ;  ");
                        }
                        info.setText(info.getText() + sb.toString() + "\n");
                    }
                    delete.setEnabled(true);
                    delete.setText(Translator.R("TIFPDeleteFiles") + " - " + apps.getSelectedValue().getFiles().size());
                } else {
                    delete.setEnabled(false);
                    delete.setText(Translator.R("TIFPDeleteFiles"));
                }
            }
        }
    
    private static class DummyCacheIdListModel implements ListModel<CacheUtil.CacheId> {
        
        List<CacheUtil.CacheId> content;
        public DummyCacheIdListModel(List<CacheUtil.CacheId> content){
            this.content = content;
        }
            @Override
            public int getSize() {
                return content.size();
            }

            @Override
            public CacheUtil.CacheId getElementAt(int index) {
                return content.get(index);
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }

        };
}

