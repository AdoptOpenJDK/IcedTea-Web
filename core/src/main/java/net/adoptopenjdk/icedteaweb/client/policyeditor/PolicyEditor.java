/*Copyright (C) 2014 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.client.policyeditor;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.about.AboutDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.FileDialogFactory;
import net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditorPermissions.Group;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.FileUtils.OpenFileResult;
import net.sourceforge.jnlp.util.ImageResources;
import net.sourceforge.jnlp.util.docprovider.PolicyEditorTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.security.provider.PolicyParser;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * This class provides a policy editing tool as a simpler alternate to
 * the JDK PolicyTool.
 */
public class PolicyEditor extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(PolicyEditor.class);

    private boolean closed = false;
    private final Map<PolicyEditorPermissions, JCheckBox> checkboxMap = new TreeMap<>();
    private final List<JCheckBoxWithGroup> groupBoxList = new ArrayList<>(Group.values().length);
    private final JScrollPane scrollPane = new JScrollPane();
    private final DefaultListModel<PolicyIdentifier> listModel = new DefaultListModel<>();
    private final JList<PolicyIdentifier> list = new JList<>(listModel);
    private final JButton okButton = new JButton(), closeButton = new JButton(),
            addEntryButton = new JButton(), removeEntryButton = new JButton();
    private final JFileChooser fileChooser;
    private CustomPolicyViewer cpViewer = null;
    /**
     * See showChangesSavedDialog/showCouldNotSaveDialog. This weak reference is needed because
     * there is a modal child dialog which can sometimes appear after the editor has been closed
     * and disposed. In this case, its parent should be set to 'null', but otherwise the parent
     * should be the editor so that the dialog is modal.
     */
    private final WeakReference<PolicyEditor> parentPolicyEditor = new WeakReference<>(this);
    public final PolicyEditorController policyEditorController = new PolicyEditorController();

    private final ActionListener okButtonAction, addEntryButtonAction,
            removeEntryButtonAction, newButtonAction, openButtonAction, openDefaultButtonAction, saveAsButtonAction, viewCustomButtonAction,
            modifyCodebaseButtonAction, modifyPrincipalsButtonAction, modifySignedByButtonAction,
            copyEntryButtonAction, pasteEntryButtonAction,
            policyEditorHelpButtonAction, aboutPolicyEditorButtonAction, aboutItwButtonAction;
    private final ActionListener closeButtonAction;

    private static class JCheckBoxWithGroup extends JCheckBox {

        private final Group group;

        private JCheckBoxWithGroup(Group group) {
            super(group.getTitle());
            this.group = group;
        }

        public Group getGroup() {
            return group;
        }

        private void setState(final Map<PolicyEditorPermissions, Boolean> map) {
            final List<ActionListener> backup = new LinkedList<>();
            for (final ActionListener l : this.getActionListeners()) {
                backup.add(l);
                this.removeActionListener(l);
            }
            final int i = group.getState(map);
            this.setBackground(getParent().getBackground());
            if (i > 0) {
                this.setSelected(true);
            }
            if (i < 0) {
                this.setSelected(false);
            }
            if (i == 0) {
                this.setBackground(Color.yellow);
                this.setSelected(false);
            }

            for (final ActionListener al : backup) {
                this.addActionListener(al);
            }
        }
    }

    private static class PrincipalsPanel extends JPanel {

        private final DefaultListModel<PolicyParser.PrincipalEntry> principals = new DefaultListModel<>();

        public PrincipalsPanel(final Collection<PolicyParser.PrincipalEntry> entries) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            for (final PolicyParser.PrincipalEntry principalEntry : entries) {
                principals.addElement(principalEntry);
            }
            final JList<PolicyParser.PrincipalEntry> principalsList = new JList<>(principals);
            final JScrollPane scrollPane = new JScrollPane(principalsList);
            final JButton addButton = new JButton(R("PEAddPrincipal"));
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final JTextField className = new JTextField();
                    final JTextField principalName = new JTextField();
                    final int option = JOptionPane.showConfirmDialog(
                            null,
                            new Object[]{R("PEPrincipalClassNameInputLabel"), className, R("PEPrincipalPrincipalNameInputLabel"), principalName},
                            R("PEAddPrincipal"),
                            JOptionPane.OK_CANCEL_OPTION
                    );
                    if (option == JOptionPane.OK_OPTION) {
                        final PolicyParser.PrincipalEntry entry = new PolicyParser.PrincipalEntry(className.getText(), principalName.getText());
                        principals.addElement(entry);
                    }
                }
            });
            final JButton removeButton = new JButton(R("PERemovePrincipal"));
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    principals.removeElement(principalsList.getSelectedValue());
                }
            });
            final JButton editButton = new JButton(R("PEEditPrincipal"));
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final PolicyParser.PrincipalEntry entry = principalsList.getSelectedValue();
                    if (entry == null) {
                        return;
                    }
                    final JTextField className = new JTextField();
                    final JTextField principalName = new JTextField();
                    className.setText(entry.getDisplayClass());
                    principalName.setText(entry.getDisplayName());
                    final int option = JOptionPane.showConfirmDialog(
                        null,
                        new Object[]{R("PEPrincipalClassNameInputLabel"), className, R("PEPrincipalPrincipalNameInputLabel"), principalName},
                        R("PEEditPrincipal"),
                        JOptionPane.OK_CANCEL_OPTION
                    );
                    if (option == JOptionPane.OK_OPTION) {
                        principals.removeElement(entry);
                        final PolicyParser.PrincipalEntry newEntry = new PolicyParser.PrincipalEntry(className.getText(), principalName.getText());
                        principals.addElement(newEntry);
                    }
                }
            });
            add(scrollPane);
            add(addButton);
            add(editButton);
            add(removeButton);
        }

        public List<PolicyParser.PrincipalEntry> getPrincipals() {
            final List<PolicyParser.PrincipalEntry> entries = new ArrayList<>(principals.size());
            for (final PolicyParser.PrincipalEntry entry : Collections.list(principals.elements())) {
                entries.add(entry);
            }
            return entries;
        }
    }

    public PolicyEditor(final String filepath) {
        super();
        setLayout(new GridBagLayout());

        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            final JCheckBox box = new JCheckBox();
            box.setText(perm.getName());
            box.setToolTipText(perm.getDescription());
            checkboxMap.put(perm, box);
        }

        setFile(filepath);
        addDefaultAllAppletsIdentifier();
        setChangesMade(false);

        fileChooser = new JFileChooser(policyEditorController.getFile());
        fileChooser.setFileHidingEnabled(false);

        okButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (policyEditorController.getFile() == null) {
                    final int choice = fileChooser.showOpenDialog(PolicyEditor.this);
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        PolicyEditor.this.setFile(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }

                // May still be null if user cancelled the file chooser
                if (policyEditorController.getFile() != null) {
                    setChangesMade(true);
                    savePolicyFile();
                }
            }
        };
        okButton.setText(R("ButApply"));
        okButton.addActionListener(okButtonAction);

        addEntryButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                addNewIdentifierInteractive();
            }
        };
        addEntryButton.setText(R("PEAddEntry"));
        addEntryButton.addActionListener(addEntryButtonAction);

        removeEntryButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                removeIdentifier(getSelectedPolicyIdentifier());
            }
        };
        removeEntryButton.setText(R("PERemoveEntry"));
        removeEntryButton.addActionListener(removeEntryButtonAction);

        newButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!promptOnSaveChangesMade(false)) {
                    return;
                }
                setFile(null);
                setChangesMade(false);
            }
        };

        openButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!promptOnSaveChangesMade(true)) {
                    return;
                }
                final int choice = fileChooser.showOpenDialog(PolicyEditor.this);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    PolicyEditor.this.setFile(fileChooser.getSelectedFile().getAbsolutePath());
                    openAndParsePolicyFile();
                }
            }
        };

        openDefaultButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (!promptOnSaveChangesMade(true)) {
                    return;
                }
                try {
                    PolicyEditor.this.setFile(getDefaultPolicyFilePath());
                    PolicyEditor.this.getFile().createNewFile();
                } catch (final IOException e) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                    return;
                }
                openAndParsePolicyFile();
            }
        };

        saveAsButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int choice = fileChooser.showSaveDialog(PolicyEditor.this);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    PolicyEditor.this.setFile(fileChooser.getSelectedFile().getAbsolutePath());
                    setChangesMade(true);
                    savePolicyFile();
                }
            }
        };

        modifyCodebaseButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (getSelectedPolicyIdentifier().equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
                    return;
                }
                final String oldCodebase = getSelectedPolicyIdentifier().getCodebase();
                String newCodebase;
                do {
                    newCodebase = JOptionPane.showInputDialog(PolicyEditor.this, R("PEModifyCodebase"), oldCodebase);
                    if (newCodebase == null) {
                        return;
                    }
                } while (!validateCodebase(newCodebase));
                modifyCodebase(getSelectedPolicyIdentifier(), newCodebase);
            }
        };

        modifyPrincipalsButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (getSelectedPolicyIdentifier().equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
                    return;
                }
                final PrincipalsPanel panel = new PrincipalsPanel(getSelectedPolicyIdentifier().getPrincipals());
                final int option = JOptionPane.showConfirmDialog(
                    null,
                    new Object[]{ R("PEPrincipalsInputLabel"), panel },
                    R("PEEntryPrompt"),
                    JOptionPane.OK_CANCEL_OPTION
                );
                if (option == JOptionPane.OK_OPTION) {
                    modifyPrincipals(getSelectedPolicyIdentifier(), panel.getPrincipals());
                }
            }
        };

        modifySignedByButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (getSelectedPolicyIdentifier().equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
                    return;
                }
                final String newSignedBy = JOptionPane.showInputDialog(PolicyEditor.this, R("PEModifySignedBy"), getSelectedPolicyIdentifier().getSignedBy());
                if (newSignedBy == null) {
                    return;
                }
                modifySignedBy(getSelectedPolicyIdentifier(), newSignedBy);
            }
        };

        copyEntryButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                copyEntry(getSelectedPolicyIdentifier());
            }
        };

        pasteEntryButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                PolicyIdentifier identifier = null;
                try {
                    identifier = PolicyEditorController.getPolicyEntryFromClipboard().getPolicyIdentifier();
                    pasteEntry(promptForPolicyIdentifier(identifier));
                } catch (final UnsupportedFlavorException ufe) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ufe);
                    showClipboardErrorDialog();
                } catch (final PolicyParser.ParsingException pe) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, pe);
                    showInvalidPolicyExceptionDialog(identifier);
                } catch (final IOException ioe) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
                    showCouldNotAccessClipboardDialog();
                }
            }
        };

        viewCustomButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
                    @Override
                    public void run() {
                        final PolicyIdentifier policyIdentifier = getSelectedPolicyIdentifier();
                        if (policyIdentifier == null) {
                            return;
                        }
                        if (cpViewer == null) {
                            cpViewer = new CustomPolicyViewer(PolicyEditor.this, policyIdentifier);
                            cpViewer.setVisible(true);
                        } else {
                            cpViewer.toFront();
                            cpViewer.repaint();
                        }
                    }
                });
            }
        };

        policyEditorHelpButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new PolicyEditorAboutDialog(R("PEHelpDialogTitle"), R("PEHelpDialogContent")).setVisible(true);
            }
        };

        aboutPolicyEditorButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                boolean modal = getModality();
                AboutDialog.display(modal, TextsProvider.POLICY_EDITOR, AboutDialog.ShowPage.HELP);
            }
        };

        aboutItwButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                boolean modal = getModality();
                AboutDialog.display(modal, TextsProvider.POLICY_EDITOR);
            }
        };

        closeButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                final Window parentWindow = SwingUtils.getWindowAncestor(PolicyEditor.this);
                if (parentWindow instanceof PolicyEditorWindow) {
                    ((PolicyEditorWindow) parentWindow).quit();
                }
            }
        };
        closeButton.setText(R("ButClose"));
        closeButton.addActionListener(closeButtonAction);

        setupLayout();
    }

    private static String getDefaultPolicyFilePath() {
        final String fullPath = PathsAndFiles.JAVA_POLICY.getFullPath();
        return new File(fullPath).getAbsolutePath();
    }

    private void addDefaultAllAppletsIdentifier() {
        addNewEntry(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
    }

    private boolean getModality() {
        boolean modal = false;
        Container parent = PolicyEditor.this;
        while (true) {
            if (parent == null) {
                break;
            }
            if (parent instanceof JDialog) {
                modal = ((JDialog) parent).isModal();
                break;
            }
            parent = parent.getParent();
        }
        return modal;
    }

    /**
     *
     * @param async use asynchronous saving, which displays a progress dialog, or use synchronous, which blocks the
     *              EDT but allows for eg the on-disk file to be changed without resorting to a busy-wait loop
     * @return false iff the user wishes to cancel the operation and keep the current editor state
     */
    private boolean promptOnSaveChangesMade(final boolean async) {
        if (policyEditorController.changesMade()) {
            final int save = JOptionPane.showConfirmDialog(this, R("PESaveChanges"));
            if (save == JOptionPane.YES_OPTION) {
                if (getFile() == null) {
                    final int choice = fileChooser.showSaveDialog(this);
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        this.setFile(fileChooser.getSelectedFile().getAbsolutePath());
                    } else if (choice == JFileChooser.CANCEL_OPTION) {
                        return false;
                    }
                }
                if (async) {
                    savePolicyFile();
                } else {
                    try {
                        policyEditorController.savePolicyFile();
                    } catch (final IOException e) {
                        showCouldNotSaveDialog();
                    }
                }
            } else if (save == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    public void setFile(final String filepath) {
        if (filepath != null) {
            policyEditorController.setFile(new File(filepath));
        } else {
            policyEditorController.setFile(null);
            resetEntries();
            addDefaultAllAppletsIdentifier();
        }
        setParentWindowTitle(getWindowTitleForStatus());
    }

    private void setParentWindowTitle(final String title) {
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                final Window parent = SwingUtils.getWindowAncestor(PolicyEditor.this);
                if (!(parent instanceof PolicyEditorWindow)) {
                    return;
                }
                final PolicyEditorWindow window = (PolicyEditorWindow) parent;
                window.setTitle(title);
            }
        });
    }

    private String getWindowTitleForStatus() {
        final String filepath;
        final File file = getFile();
        if (file != null) {
            filepath = file.getPath();
        } else {
            filepath = null;
        }
        final String titleAndPath;
        if (filepath != null) {
            titleAndPath = R("PETitleWithPath", filepath);
        } else {
            titleAndPath = R("PETitle");
        }
        final String result;
        if (policyEditorController.changesMade()) {
            result = R("PETitleWithChangesMade", titleAndPath);
        } else {
            result = titleAndPath;
        }
        return result;
    }

    private PolicyIdentifier getSelectedPolicyIdentifier() {
        return list.getSelectedValue();
    }

    private static void preparePolicyEditorWindow(final PolicyEditorWindow w, final PolicyEditor e) {
        w.setModalityType(ModalityType.MODELESS); //at least some default
        w.setPolicyEditor(e);
        w.setTitle(R("PETitle"));
        w.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        w.setJMenuBar(createMenuBar(w.getPolicyEditor()));
        setupPolicyEditorWindow(w.asWindow(), w.getPolicyEditor());
    }

    private static void setupPolicyEditorWindow(final Window window, final PolicyEditor editor) {
        window.add(editor);
        window.pack();
        editor.setVisible(true);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                ((PolicyEditorWindow) window).quit();
            }
        });
    }

    public interface PolicyEditorWindow {

        void setTitle(String s);

        void setDefaultCloseOperation(int i);

        PolicyEditor getPolicyEditor();

        void setPolicyEditor(PolicyEditor e);

        void setJMenuBar(JMenuBar menu);

        Window asWindow();

        void setModalityType(ModalityType modalityType);

        void quit();
    }

    private static class PolicyEditorFrame extends JFrame implements PolicyEditorWindow {

        private PolicyEditor editor;

        private PolicyEditorFrame(final PolicyEditor editor) {
            super();
            setIconImages(ImageResources.INSTANCE.getApplicationImages());
            preparePolicyEditorWindow(this, editor);
        }

        @Override
        public final void setTitle(String title) {
            super.setTitle(title);
        }

        @Override
        public final PolicyEditor getPolicyEditor() {
            return editor;
        }

        @Override
        public final void setPolicyEditor(final PolicyEditor e) {
            editor = e;
        }

        @Override
        public final void setDefaultCloseOperation(final int operation) {
            super.setDefaultCloseOperation(operation);
        }

        @Override
        public final void setJMenuBar(final JMenuBar menu) {
            super.setJMenuBar(menu);
        }

        @Override
        public final Window asWindow() {
            return this;
        }

        @Override
        public void setModalityType(final ModalityType type) {
            //no op for frame
        }

        @Override
        public void quit() {
            policyEditorWindowQuit(this);
        }
    }

    /*
     * Casting a Window to PolicyEditorWindow is not generally safe - be sure that
     * the argument passed to this method is actually a PolicyEditorDialog or PolicyEditorFrame.
     */
    private static void policyEditorWindowQuit(final Window window) {
        final PolicyEditor editor = ((PolicyEditorWindow) window).getPolicyEditor();
        editor.parentPolicyEditor.clear();
        if (editor.policyEditorController.changesMade()) {
            final int save = JOptionPane.showConfirmDialog(window, R("PESaveChanges"));
            if (save == JOptionPane.YES_OPTION) {
                if (editor.policyEditorController.getFile() == null) {
                    final int choice = editor.fileChooser.showSaveDialog(window);
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        editor.setFile(editor.fileChooser.getSelectedFile().getAbsolutePath());
                    } else if (choice == JFileChooser.CANCEL_OPTION) {
                        return;
                    }
                }
                try {
                    editor.policyEditorController.savePolicyFile();
                } catch (final IOException e) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                    editor.showCouldNotSaveDialog();
                    return;
                }
            } else if (save == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        editor.setClosed();
        window.dispose();
    }

    public static PolicyEditorWindow getPolicyEditorFrame(final String filepath) {
        return new PolicyEditorFrame(new PolicyEditor(filepath));
    }

    private static class PolicyEditorDialog extends JDialog implements PolicyEditorWindow {

        private PolicyEditor editor;

        private PolicyEditorDialog(final PolicyEditor editor) {
            super();
            preparePolicyEditorWindow(this, editor);
        }

        @Override
        public final void setTitle(final String title) {
            super.setTitle(title);
        }

        @Override
        public final PolicyEditor getPolicyEditor() {
            return editor;
        }

        @Override
        public final void setPolicyEditor(final PolicyEditor e) {
            editor = e;
        }

        @Override
        public final void setDefaultCloseOperation(final int operation) {
            super.setDefaultCloseOperation(operation);
        }

        @Override
        public final void setJMenuBar(final JMenuBar menu) {
            super.setJMenuBar(menu);
        }

        @Override
        public final Window asWindow() {
            return this;
        }

        @Override
        public void setModalityType(final ModalityType type) {
            super.setModalityType(type);
        }

        @Override
        public void quit() {
            policyEditorWindowQuit(this);
        }
    }

    public static PolicyEditorWindow getPolicyEditorDialog(final String filepath) {
        return new PolicyEditorDialog(new PolicyEditor(filepath));
    }

    private void setClosed() {
        closed = true;
    }

    /**
     * Check if the PolicyEditor instance has been visually closed
     * @return if the PolicyEditor instance has been closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Called by the Custom Policy Viewer on its parent Policy Editor when
     * the Custom Policy Viewer is closing
     */
    void customPolicyViewerClosing() {
        cpViewer = null;
    }

    /**
     * Add a new identifier to the editor's model.
     * @param identifier to be added
     */
    public void addNewEntry(final PolicyIdentifier identifier) {
        if (!validateCodebase(identifier.getCodebase())) {
            return;
        }
        policyEditorController.addIdentifier(identifier);
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                listModel.clear();
                for (final PolicyIdentifier identifier : policyEditorController.getIdentifiers()) {
                    listModel.addElement(identifier);
                }
                list.setSelectedValue(identifier, true);
                updateCheckboxes(identifier);
            }
        });
    }

    static PolicyIdentifier identifierFromCodebase(final String codebase) {
        if (codebase.isEmpty() || codebase.equals(Translator.R("PEGlobalSettings"))) {
            return PolicyIdentifier.ALL_APPLETS_IDENTIFIER;
        }
        return new PolicyIdentifier(null, Collections.<PolicyParser.PrincipalEntry>emptyList(), codebase);
    }

    private static boolean validateCodebase(final String codebase) {
        if (codebase == null || codebase.isEmpty()) {
            return true;
        }
        try {
            new URL(codebase);
        } catch (final MalformedURLException mue) {
            return false;
        }
        return true;
    }

    public File getFile() {
        return policyEditorController.getFile();
    }

    /**
     * Display an input dialog, which will disappear when the user enters a valid entry
     * or when the user presses cancel.
     */
    public void addNewIdentifierInteractive() {
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                final PolicyIdentifier identifier = promptForPolicyIdentifier(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
                if (identifier == null) {
                    return;
                }
                addNewEntry(identifier);
            }
        });
    }

    private PolicyIdentifier promptForPolicyIdentifier(final PolicyIdentifier initialValues) {
        PolicyIdentifier identifier = initialValues;
        final JTextField codebase = new JTextField();
        final JTextField signedBy = new JTextField();
        final PrincipalsPanel principalsPanel = new PrincipalsPanel(Collections.<PolicyParser.PrincipalEntry>emptySet());
        while (identifier.equals(initialValues) || identifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER) || !validateCodebase(codebase.getText())) {
            codebase.setText(identifier.getCodebase());
            signedBy.setText(identifier.getSignedBy());
            final int option = JOptionPane.showConfirmDialog(
                    PolicyEditor.this,
                    new Object[]{R("PECodebaseInputLabel"), codebase, R("PEPrincipalsInputLabel"), principalsPanel, R("PESignedByInputLabel"), signedBy},
                    R("PEEntryPrompt"),
                    JOptionPane.OK_CANCEL_OPTION
            );
            if (option == JOptionPane.OK_OPTION) {
                final String cb = codebase.getText().trim().isEmpty() ? null : codebase.getText().trim();
                final String sb = signedBy.getText().trim().isEmpty() ? null : signedBy.getText().trim();
                identifier = new PolicyIdentifier(sb, principalsPanel.getPrincipals(), cb);
                if (identifier.equals(initialValues)) {
                    JOptionPane.showMessageDialog(null, R("PEInvalidIdentifier"));
                } else if (identifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
                    JOptionPane.showMessageDialog(null, R("PEIdentifierMatchesAll"));
                }
            } else {
                return null;
            }
        }
        return identifier;
    }

    /**
     * Remove an identifier from the editor's model
     * @param identifier to be removed
     */
    public void removeIdentifier(final PolicyIdentifier identifier) {
        if (identifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
            return;
        }
        int previousIndex = list.getSelectedIndex() - 1;
        if (previousIndex < 0) {
            previousIndex = 0;
        }
        policyEditorController.removeIdentifier(identifier);
        final int fIndex = previousIndex;
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                listModel.removeElement(identifier);
                list.setSelectedIndex(fIndex);
            }
        });
        setChangesMade(true);
    }

    public void modifyCodebase(final PolicyIdentifier identifier, final String newCodebase) {
        final PolicyIdentifier newIdentifier = new PolicyIdentifier(identifier.getSignedBy(), identifier.getPrincipals(), newCodebase);
        replaceIdentifier(identifier, newIdentifier);
    }

    public void modifyPrincipals(final PolicyIdentifier identifier, final List<PolicyParser.PrincipalEntry> principalEntries) {
        final PolicyIdentifier newIdentifier = new PolicyIdentifier(identifier.getSignedBy(), principalEntries, identifier.getCodebase());
        replaceIdentifier(identifier, newIdentifier);
    }

    public void modifySignedBy(final PolicyIdentifier identifier, final String newSignedBy) {
        final PolicyIdentifier newIdentifier = new PolicyIdentifier(newSignedBy, identifier.getPrincipals(), identifier.getCodebase());
        replaceIdentifier(identifier, newIdentifier);
    }

    private void replaceIdentifier(final PolicyIdentifier oldIdentifier, final PolicyIdentifier newIdentifier) {
        if (oldIdentifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER) || newIdentifier.equals(PolicyIdentifier.ALL_APPLETS_IDENTIFIER)) {
            return;
        }

        final Map<PolicyEditorPermissions, Boolean> permissions = getPermissions(oldIdentifier);
        final Collection<PolicyParser.PermissionEntry> customPermissions = getCustomPermissions(oldIdentifier);

        removeIdentifier(oldIdentifier);
        addNewEntry(newIdentifier);

        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : permissions.entrySet()) {
            setPermission(newIdentifier, entry.getKey(), entry.getValue());
        }

        for (final PolicyParser.PermissionEntry permission : customPermissions) {
            addCustomPermission(newIdentifier, permission);
        }
        updateCheckboxes(newIdentifier);
    }

    /**
     * Copy an entry to the system clipboard
     * @param identifier the identifier to copy
     */
    public void copyEntry(final PolicyIdentifier identifier) {
        if (!policyEditorController.getIdentifiers().contains(identifier)) {
            return;
        }
        policyEditorController.copyPolicyEntryToClipboard(identifier);
    }

    /**
     * Paste a grant entry from the system clipboard with a new identifier
     */
    public void pasteEntry(final PolicyIdentifier identifier) throws UnsupportedFlavorException, PolicyParser.ParsingException, IOException {
        addNewEntry(identifier);
        final PolicyEntry policyEntry = PolicyEditorController.getPolicyEntryFromClipboard();
        final PolicyEntry newEntry = new PolicyEntry.Builder()
                .signedBy(identifier.getSignedBy())
                .principals(identifier.getPrincipals())
                .codebase(identifier.getCodebase())
                .permissions(policyEntry.getPermissions())
                .customPermissions(policyEntry.getCustomPermissions())
                .build();
        policyEditorController.addPolicyEntry(newEntry);
        setChangesMade(true);
        updateCheckboxes(identifier);
    }

    Set<String> getCodebases() {
        final Set<String> codebases = new HashSet<>();
        for (final PolicyIdentifier identifier : policyEditorController.getIdentifiers()) {
            if (isCodeBaseIdentifier(identifier)) {
                codebases.add(identifier.getCodebase());
            }
        }
        return codebases;
    }

    static boolean isCodeBaseIdentifier(final PolicyIdentifier identifier) {
        return (identifier.getSignedBy() == null || identifier.getSignedBy().isEmpty())
                && (identifier.getPrincipals() == null || identifier.getPrincipals().isEmpty())
                && identifier.getCodebase() != null;
    }

    public void setPermission(final PolicyIdentifier identifier, final PolicyEditorPermissions permission, final boolean state) {
        policyEditorController.setPermission(identifier, permission, state);
    }

    public Map<PolicyEditorPermissions, Boolean> getPermissions(final PolicyIdentifier identifier) {
        return policyEditorController.getPermissions(identifier);
    }

    public void addCustomPermission(final PolicyIdentifier identifier, final PolicyParser.PermissionEntry permission) {
        policyEditorController.addCustomPermission(identifier, permission);
    }

    public Collection<PolicyParser.PermissionEntry> getCustomPermissions(final PolicyIdentifier identifier) {
        return policyEditorController.getCustomPermissions(identifier);
    }

    public void clearCustomPermissions(final PolicyIdentifier identifier) {
        policyEditorController.clearCustomIdentifier(identifier);
    }

    /**
     * Update the checkboxes to show the permissions granted to the specified identifier
     * @param identifier whose permissions to display
     */
    private void updateCheckboxes(final PolicyIdentifier identifier) {
        SwingUtils.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                updateCheckboxesImpl(identifier);
            }
        });
    }

    private void updateCheckboxesImpl(final PolicyIdentifier identifier) {
        if (!listModel.contains(identifier)) {
            return;
        }
        final Map<PolicyEditorPermissions, Boolean> map = policyEditorController.getCopyOfPermissions().get(identifier);
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            final JCheckBox box = checkboxMap.get(perm);
            for (final ActionListener l : box.getActionListeners()) {
                box.removeActionListener(l);
            }
            final boolean state = policyEditorController.getPermission(identifier, perm);
            for (final JCheckBoxWithGroup jg : groupBoxList) {
                jg.setState(map);
            }
            box.setSelected(state);
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    setChangesMade(true);
                    policyEditorController.setPermission(identifier, perm, box.isSelected());
                    for (JCheckBoxWithGroup jg : groupBoxList) {
                        jg.setState(map);
                    }
                }
            });
        }
    }

    /**
     * Set a mnemonic key for a menu item or button
     * @param button the component for which to set a mnemonic
     * @param mnemonic the mnemonic to set
     */
    private static void setButtonMnemonic(final AbstractButton button, final String mnemonic) {
        if (mnemonic.length() != 1) {
            LOG.debug("Could not set mnemonic \"{}\" for {}", mnemonic, button);
            return;
        }
        final char ch = mnemonic.charAt(0);
        button.setMnemonic(ch);
    }

    private static void setMenuItemAccelerator(final JMenuItem menuItem, final String accelerator) {
        final KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
        menuItem.setAccelerator(ks);
    }

    private static JMenuBar createMenuBar(final PolicyEditor editor) {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu fileMenu = new JMenu(R("PEFileMenu"));
        setButtonMnemonic(fileMenu, R("PEFileMenuMnemonic"));

        final JMenuItem newItem = new JMenuItem(R("PENewMenuItem"));
        setButtonMnemonic(newItem, R("PENewMenuItemMnemonic"));
        setMenuItemAccelerator(newItem, R("PENewMenuItemAccelerator"));
        newItem.addActionListener(editor.newButtonAction);
        fileMenu.add(newItem);

        final JMenuItem openItem = new JMenuItem(R("PEOpenMenuItem"));
        setButtonMnemonic(openItem, R("PEOpenMenuItemMnemonic"));
        setMenuItemAccelerator(openItem, R("PEOpenMenuItemAccelerator"));
        openItem.addActionListener(editor.openButtonAction);
        fileMenu.add(openItem);

        final JMenuItem openDefaultItem = new JMenuItem(R("PEOpenDefaultMenuItem"));
        setButtonMnemonic(openDefaultItem, R("PEOpenDefaultMenuItemMnemonic"));
        setMenuItemAccelerator(openDefaultItem, R("PEOpenDefaultMenuItemAccelerator"));
        openDefaultItem.addActionListener(editor.openDefaultButtonAction);
        fileMenu.add(openDefaultItem);

        final JMenuItem saveItem = new JMenuItem(R("PESaveMenuItem"));
        setButtonMnemonic(saveItem, R("PESaveMenuItemMnemonic"));
        setMenuItemAccelerator(saveItem, R("PESaveMenuItemAccelerator"));
        saveItem.addActionListener(editor.okButtonAction);
        fileMenu.add(saveItem);

        final JMenuItem saveAsItem = new JMenuItem(R("PESaveAsMenuItem"));
        setButtonMnemonic(saveAsItem, R("PESaveAsMenuItemMnemonic"));
        setMenuItemAccelerator(saveAsItem, R("PESaveAsMenuItemAccelerator"));
        saveAsItem.addActionListener(editor.saveAsButtonAction);
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator();

        final JMenuItem exitItem = new JMenuItem(R("PEExitMenuItem"));
        setButtonMnemonic(exitItem, R("PEExitMenuItemMnemonic"));
        setMenuItemAccelerator(exitItem, R("PEExitMenuItemAccelerator"));
        exitItem.addActionListener(editor.closeButtonAction);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        final JMenu entryMenu = new JMenu(R("PEEntryMenu"));
        setButtonMnemonic(entryMenu, R("PEEntryMenuMnemonic"));

        final JMenuItem addEntryItem = new JMenuItem(R("PEAddEntryItem"));
        setButtonMnemonic(addEntryItem, R("PEAddEntryItemMnemonic"));
        setMenuItemAccelerator(addEntryItem, R("PEAddEntryItemAccelerator"));
        addEntryItem.addActionListener(editor.addEntryButtonAction);
        entryMenu.add(addEntryItem);

        final JMenuItem removeEntryItem = new JMenuItem(R("PERemoveEntryItem"));
        setButtonMnemonic(removeEntryItem, R("PERemoveEntryItemMnemonic"));
        setMenuItemAccelerator(removeEntryItem, R("PERemoveEntryItemAccelerator"));
        removeEntryItem.addActionListener(editor.removeEntryButtonAction);
        entryMenu.add(removeEntryItem);

        entryMenu.addSeparator();

        final JMenu modifySubmenuItem = new JMenu(R("PEModifySubmenuItem"));
        setButtonMnemonic(modifySubmenuItem, R("PEModifySubmenuItemMnemonic"));

        final JMenuItem modifyCodebaseItem = new JMenuItem(R("PEModifyCodebaseItem"));
        setButtonMnemonic(modifyCodebaseItem, R("PEModifyEntryCodebaseItemMnemonic"));
        setMenuItemAccelerator(modifyCodebaseItem, R("PEModifyEntryCodebaseItemAccelerator"));
        modifyCodebaseItem.addActionListener(editor.modifyCodebaseButtonAction);
        modifySubmenuItem.add(modifyCodebaseItem);

        final JMenuItem modifyPrincipalsItem = new JMenuItem(R("PEModifyPrincipalsItem"));
        setButtonMnemonic(modifyPrincipalsItem, R("PEModifyEntryPrincipalsItemMnemonic"));
        setMenuItemAccelerator(modifyPrincipalsItem, R("PEModifyEntryPrincipalsItemAccelerator"));
        modifyPrincipalsItem.addActionListener(editor.modifyPrincipalsButtonAction);
        modifySubmenuItem.add(modifyPrincipalsItem);

        final JMenuItem modifySignedByItem = new JMenuItem(R("PEModifySignedByItem"));
        setButtonMnemonic(modifySignedByItem, R("PEModifyEntrySignedByItemMnemonic"));
        setMenuItemAccelerator(modifySignedByItem, R("PEModifyEntrySignedByItemAccelerator"));
        modifySignedByItem.addActionListener(editor.modifySignedByButtonAction);
        modifySubmenuItem.add(modifySignedByItem);

        entryMenu.add(modifySubmenuItem);
        entryMenu.addSeparator();

        final JMenuItem copyEntryItem = new JMenuItem(R("PECopyEntryItem"));
        setButtonMnemonic(copyEntryItem, R("PECopyEntryItemMnemonic"));
        setMenuItemAccelerator(copyEntryItem, R("PECopyEntryItemAccelerator"));
        copyEntryItem.addActionListener(editor.copyEntryButtonAction);
        entryMenu.add(copyEntryItem);
        menuBar.add(entryMenu);

        final JMenuItem pasteEntryItem = new JMenuItem(R("PEPasteEntryItem"));
        setButtonMnemonic(pasteEntryItem, R("PEPasteEntryItemMnemonic"));
        setMenuItemAccelerator(pasteEntryItem, R("PEPasteEntryItemAccelerator"));
        pasteEntryItem.addActionListener(editor.pasteEntryButtonAction);
        entryMenu.add(pasteEntryItem);

        final JMenu viewMenu = new JMenu(R("PEViewMenu"));
        setButtonMnemonic(viewMenu, R("PEViewMenuMnemonic"));

        final JMenuItem customPermissionsItem = new JMenuItem(R("PECustomPermissionsItem"));
        setButtonMnemonic(customPermissionsItem, R("PECustomPermissionsItemMnemonic"));
        setMenuItemAccelerator(customPermissionsItem, R("PECustomPermissionsItemAccelerator"));
        customPermissionsItem.addActionListener(editor.viewCustomButtonAction);

        viewMenu.add(customPermissionsItem);
        menuBar.add(viewMenu);

        final JMenu helpMenu = new JMenu(R("PEHelpMenu"));
        setButtonMnemonic(helpMenu, R("PEHelpMenuMnemonic"));

        final JMenuItem aboutPolicyEditorItem = new JMenuItem(R("PEAboutPolicyEditorItem"));
        setButtonMnemonic(aboutPolicyEditorItem, R("PEAboutPolicyEditorItemMnemonic"));
        aboutPolicyEditorItem.addActionListener(editor.aboutPolicyEditorButtonAction);
        helpMenu.add(aboutPolicyEditorItem);

        final JMenuItem aboutITW = new JMenuItem(R("CPTabAbout"));
        //setButtonMnemonic(aboutPolicyEditorItem, R("PEAboutPolicyEditorItemMnemonic"));
        aboutITW.addActionListener(editor.aboutItwButtonAction);
        helpMenu.add(aboutITW);

        final JMenuItem policyEditorHelpItem = new JMenuItem(R("PEPolicyEditorHelpItem"));
        setButtonMnemonic(policyEditorHelpItem, R("PEPolicyEditorHelpItemMnemonic"));
        policyEditorHelpItem.addActionListener(editor.policyEditorHelpButtonAction);
        helpMenu.addSeparator();
        helpMenu.add(policyEditorHelpItem);

        menuBar.add(helpMenu);
        /*
         * JList has default Ctrl-C and Ctrl-V bindings, which we want to override with custom actions
         */
        final InputMap listInputMap = editor.list.getInputMap();
        final ActionMap listActionMap = editor.list.getActionMap();

        final Action listCopyOverrideAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                editor.copyEntryButtonAction.actionPerformed(e);
            }
        };

        final Action listPasteOverrideAction = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                editor.pasteEntryButtonAction.actionPerformed(e);
            }
        };

        listInputMap.put(copyEntryItem.getAccelerator(), "CopyEntryOverride");
        listActionMap.put("CopyEntryOverride", listCopyOverrideAction);
        listInputMap.put(pasteEntryItem.getAccelerator(), "PasteEntryOverride");
        listActionMap.put("PasteEntryOverride", listPasteOverrideAction);

        return menuBar;
    }

    /**
     * Lay out all controls, tooltips, etc.
     */
    private void setupLayout() {
        final JLabel checkboxLabel = new JLabel();
        checkboxLabel.setText(R("PECheckboxLabel"));
        checkboxLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        final GridBagConstraints checkboxLabelConstraints = new GridBagConstraints();
        checkboxLabelConstraints.gridx = 2;
        checkboxLabelConstraints.gridy = 0;
        checkboxLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(checkboxLabel, checkboxLabelConstraints);

        final GridBagConstraints checkboxConstraints = new GridBagConstraints();
        checkboxConstraints.anchor = GridBagConstraints.LINE_START;
        checkboxConstraints.fill = GridBagConstraints.HORIZONTAL;
        checkboxConstraints.weightx = 0;
        checkboxConstraints.weighty = 0;
        checkboxConstraints.gridx = 2;
        checkboxConstraints.gridy = 1;

        for (final JCheckBox box : checkboxMap.values()) {
            if (Group.anyContains(box, checkboxMap)) {
                //do not show boxes in any group
                continue;
            }
            add(box, checkboxConstraints);
            checkboxConstraints.gridx++;
            // Two columns of checkboxes
            if (checkboxConstraints.gridx > 3) {
                checkboxConstraints.gridx = 2;
                checkboxConstraints.gridy++;
            }
        }
        // add groups
        for (final Group g : Group.values()) {
            // no matter what, put group title on new line
            checkboxConstraints.gridy++;
            // all groups are in second column
            checkboxConstraints.gridx = 2;
            final JCheckBoxWithGroup groupCh = new JCheckBoxWithGroup(g);
            groupBoxList.add(groupCh);
            final JPanel groupPanel = new JPanel(new GridBagLayout());
            groupPanel.setBorder(new LineBorder(Color.black));
            groupCh.setToolTipText(R("PEGRightClick"));
            groupCh.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        toggleExpandedCheckboxGroupPanel(groupPanel);
                    }
                }
            });
            groupCh.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                        toggleExpandedCheckboxGroupPanel(groupPanel);
                    }
                }
            });
            groupCh.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final PolicyIdentifier identifier = getSelectedPolicyIdentifier();
                    if (identifier == null) {
                        return;
                    }
                    List<ActionListener> backup = new LinkedList<>();
                    for (final ActionListener l : groupCh.getActionListeners()) {
                        backup.add(l);
                        groupCh.removeActionListener(l);
                    }
                    for (final PolicyEditorPermissions p : groupCh.getGroup().getPermissions()) {
                        policyEditorController.setPermission(identifier, p, groupCh.isSelected());
                    }
                    setChangesMade(true);
                    updateCheckboxes(identifier);
                    for (final ActionListener al : backup) {
                        groupCh.addActionListener(al);
                    }

                }
            });
            add(groupCh, checkboxConstraints);
            // place panel with members below the title
            checkboxConstraints.gridy++;
            checkboxConstraints.gridx = 2;
            // spread group's panel over two columns
            checkboxConstraints.gridwidth = 2;
            checkboxConstraints.fill = GridBagConstraints.BOTH;
            add(groupPanel, checkboxConstraints);
            final GridBagConstraints groupCheckboxLabelConstraints = new GridBagConstraints();
            groupCheckboxLabelConstraints.anchor = GridBagConstraints.LINE_START;
            groupCheckboxLabelConstraints.weightx = 0;
            groupCheckboxLabelConstraints.weighty = 0;
            groupCheckboxLabelConstraints.gridx = 1;
            groupCheckboxLabelConstraints.gridy = 1;
            for (final PolicyEditorPermissions p : g.getPermissions()) {
                groupPanel.add(checkboxMap.get(p), groupCheckboxLabelConstraints);
                // Two columns of checkboxes
                groupCheckboxLabelConstraints.gridx++;
                if (groupCheckboxLabelConstraints.gridx > 2) {
                    groupCheckboxLabelConstraints.gridx = 1;
                    groupCheckboxLabelConstraints.gridy++;
                }
            }
            groupPanel.setVisible(false);
            // reset
            checkboxConstraints.gridwidth = 1;
        }

        final JLabel entriesListLabel = new JLabel(R("PEEntriesLabel"));
        entriesListLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        final GridBagConstraints listLabelConstraints = new GridBagConstraints();
        listLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        listLabelConstraints.gridx = 0;
        listLabelConstraints.gridy = 0;
        add(entriesListLabel, listLabelConstraints);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return; // ignore first click, act on release
                }
                updateCheckboxes(getSelectedPolicyIdentifier());
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(list);
        final GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.fill = GridBagConstraints.BOTH;
        listConstraints.weightx = 1;
        listConstraints.weighty = 1;
        listConstraints.gridheight = checkboxConstraints.gridy + 1;
        listConstraints.gridwidth = 2;
        listConstraints.gridx = 0;
        listConstraints.gridy = 1;
        add(scrollPane, listConstraints);

        final GridBagConstraints addEntryButtonConstraints = new GridBagConstraints();
        addEntryButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        addEntryButtonConstraints.gridx = 0;
        addEntryButtonConstraints.gridy = listConstraints.gridy + listConstraints.gridheight + 1;
        setButtonMnemonic(addEntryButton, R("PEAddEntryMnemonic"));
        add(addEntryButton, addEntryButtonConstraints);

        final GridBagConstraints removeEntryButtonConstraints = new GridBagConstraints();
        removeEntryButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        removeEntryButtonConstraints.gridx = addEntryButtonConstraints.gridx + 1;
        removeEntryButtonConstraints.gridy = addEntryButtonConstraints.gridy;
        setButtonMnemonic(removeEntryButton, R("PERemoveEntryMnemonic"));
        removeEntryButton.setPreferredSize(addEntryButton.getPreferredSize());
        add(removeEntryButton, removeEntryButtonConstraints);

        final GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        okButtonConstraints.gridx = removeEntryButtonConstraints.gridx + 2;
        okButtonConstraints.gridy = removeEntryButtonConstraints.gridy;
        add(okButton, okButtonConstraints);

        final GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        cancelButtonConstraints.gridx = okButtonConstraints.gridx + 1;
        cancelButtonConstraints.gridy = okButtonConstraints.gridy;
        add(closeButton, cancelButtonConstraints);

        setMinimumSize(getPreferredSize());
    }

    void setChangesMade(final boolean b) {
        policyEditorController.setChangesMade(b);
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                setParentWindowTitle(getWindowTitleForStatus());
            }
        });
    }

    private void resetEntries() {
        listModel.clear();
        policyEditorController.clear();
    }

    /**
     * @return whether this PolicyEditor is currently opening or saving a policy file to disk
     */
    public boolean isPerformingIO() {
        return policyEditorController.isPerformingIO();
    }

    public void openPolicyFileSynchronously() {
        if (getFile() == null) {
            return;
        }
        resetEntries();
        final OpenFileResult ofr = FileUtils.testFilePermissions(getFile());
        if (ofr == OpenFileResult.FAILURE || ofr == OpenFileResult.NOT_FILE) {
            addDefaultAllAppletsIdentifier();
            LOG.debug("Unable to open policy file");
        }
        if (ofr == OpenFileResult.CANT_WRITE) {
            LOG.debug("Opening file in read-only mode");
        }

        try {
            policyEditorController.openAndParsePolicyFile();
        } catch (IOException | PolicyParser.ParsingException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            return;
        }

        for (final PolicyIdentifier identifier : policyEditorController.getIdentifiers()) {
            if (!listModel.contains(identifier)) {
                listModel.addElement(identifier);
            }
        }
        addDefaultAllAppletsIdentifier();
        updateCheckboxes(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
        setChangesMade(false);
    }

    public void openAndParsePolicyFile() {
        if (getFile() == null) {
            return;
        }
        resetEntries();
        final OpenFileResult ofr = FileUtils.testFilePermissions(getFile());
        if (ofr == OpenFileResult.FAILURE || ofr == OpenFileResult.NOT_FILE) {
            addDefaultAllAppletsIdentifier();
            if (policyEditorController.getFile().exists()) {
                FileDialogFactory.showCouldNotOpenFilepathDialog(PolicyEditor.this, policyEditorController.getFile().getPath());
            }
            return;
        }
        if (ofr == OpenFileResult.CANT_WRITE) {
            FileDialogFactory.showReadOnlyDialog(PolicyEditor.this);
        }

        final Window parentWindow = SwingUtils.getWindowAncestor(this);
        final JDialog progressIndicator = new IndeterminateProgressDialog(parentWindow, "Loading...");
        final SwingWorker<Void, Void> openPolicyFileWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (parentWindow != null) {
                        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setLocationRelativeTo(parentWindow);
                                progressIndicator.setVisible(true);
                            }
                        });
                    }
                    policyEditorController.openAndParsePolicyFile();
                } catch (final FileNotFoundException fnfe) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, fnfe);
                    FileDialogFactory.showCouldNotOpenDialog(PolicyEditor.this, "Unable to open policy file");
                } catch (final IOException | PolicyParser.ParsingException e) {
                    LOG.error("Could not open file " + policyEditorController.getFile().getPath(), e);
                    FileDialogFactory.showCouldNotOpenDialog(PolicyEditor.this, "Unable to open policy file");
                }
                return null;
            }

            @Override
            public void done() {
                for (final PolicyIdentifier identifier : policyEditorController.getIdentifiers()) {
                    if (!listModel.contains(identifier)) {
                        listModel.addElement(identifier);
                    }
                }
                addDefaultAllAppletsIdentifier();
                updateCheckboxes(PolicyIdentifier.ALL_APPLETS_IDENTIFIER);
                progressIndicator.setVisible(false);
                progressIndicator.dispose();
                setChangesMade(false);
            }
        };
        openPolicyFileWorker.execute();
    }

    /**
     * Save the policy model into the file pointed to by the filePath field.
     */
    private void savePolicyFile() {
        final int overwriteChanges = checkPolicyChangesWithDialog();
        switch (overwriteChanges) {
            case JOptionPane.YES_OPTION:
                openAndParsePolicyFile();
                return;
            case JOptionPane.NO_OPTION:
                break;
            case JOptionPane.CANCEL_OPTION:
                return;
            default:
                break;
        }

        final Window parentWindow = SwingUtils.getWindowAncestor(this);
        final JDialog progressIndicator = new IndeterminateProgressDialog(parentWindow, "Saving...");
        final SwingWorker<Void, Void> savePolicyFileWorker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    if (parentWindow != null) {
                        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setLocationRelativeTo(parentWindow);
                                progressIndicator.setVisible(true);
                            }
                        });
                    }
                    policyEditorController.savePolicyFile();
                } catch (final IOException e) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                    showCouldNotSaveDialog();
                }
                return null;
            }

            @Override
            public void done() {
                showChangesSavedDialog();
                progressIndicator.setVisible(false);
                progressIndicator.dispose();
                setChangesMade(false);
            }
        };
        savePolicyFileWorker.execute();
    }

    /**
     * Show a dialog informing the user that their changes have been saved.
     */
    private void showChangesSavedDialog() {
        // This dialog is often displayed when closing the editor, and so PolicyEditor
        // may already be disposed when this dialog appears. Give a weak reference so
        // that this dialog doesn't prevent the JVM from exiting
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentPolicyEditor.get(), R("PEChangesSaved"));
            }
        });
    }

    /**
     * Show a dialog informing the user that their changes could not be saved.
     */
    private void showCouldNotSaveDialog() {
        // This dialog is often displayed when closing the editor, and so PolicyEditor
        // may already be disposed when this dialog appears. Give a weak reference so
        // that this dialog doesn't prevent the JVM from exiting
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentPolicyEditor.get(), R("PECouldNotSave"), R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showClipboardErrorDialog() {
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentPolicyEditor.get(), R("PEClipboardError"), R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showInvalidPolicyExceptionDialog(final PolicyIdentifier identifier) {
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentPolicyEditor.get(), R("PEInvalidPolicy", identifier.toString()), R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showCouldNotAccessClipboardDialog() {
        SwingUtils.invokeRunnableOrEnqueueLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentPolicyEditor.get(), R("PEClipboardAccessError"), R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Detect if the policy settings have changed, either on-disk or in-app.
     * If an on-disk change has occurred, update the Md5.
     * @return The user's choice (Yes/No/Cancel - see JOptionPane constants).
     * "Cancel" if the file hasn't changed but the user has made modifications
     * to the settings. "No" otherwise
     */
    private int checkPolicyChangesWithDialog() {
        boolean changed;
        try {
            changed = policyEditorController.fileHasChanged();
        } catch (FileNotFoundException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            JOptionPane.showMessageDialog(PolicyEditor.this, R("PEFileMissing"), R("PEFileModified"), JOptionPane.WARNING_MESSAGE);
            return JOptionPane.NO_OPTION;
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            changed = true;
        }
        if (changed) {
            String policyFilePath;
            try {
                policyFilePath = policyEditorController.getFile().getCanonicalPath();
            } catch (final IOException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                policyFilePath = policyEditorController.getFile().getPath();
            }
            return JOptionPane.showConfirmDialog(PolicyEditor.this, R("PEFileModifiedDetail", policyFilePath,
                    R("PEFileModified"), JOptionPane.YES_NO_CANCEL_OPTION));
        } else if (!policyEditorController.changesMade()) {
            //Return without saving or reloading
            return JOptionPane.CANCEL_OPTION;
        }
        return JOptionPane.NO_OPTION;
    }

    private void toggleExpandedCheckboxGroupPanel(final JPanel groupPanel) {
        groupPanel.setVisible(!groupPanel.isVisible());
        PolicyEditor.this.validate();
        final Window w = SwingUtils.getWindowAncestor(PolicyEditor.this);
        if (w != null) {
            w.pack();
        }
    }

    /**
     * Start a Policy Editor instance.
     * @param args "-file $FILENAME" and/or "-codebase $CODEBASE" are accepted flag/value pairs.
     * -file specifies a file path to be opened by the editor. If none is provided, the default
     * policy file location for the user is opened.
     * -help will print a help message and immediately return (no editor instance opens)
     */
    public static void main(final String[] args) {
        // setup Swing EDT tracing:
        SwingUtils.setup();

        final CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getPolicyEditorOptions());

        if (optionParser.hasOption(CommandLineOptions.VERBOSE)) {
            JNLPRuntime.setDebug(true);
        }

        if (optionParser.hasOption(CommandLineOptions.HELP1)) {
            final TextsProvider helpMessagesProvider = new PolicyEditorTextsProvider(UTF_8, new PlainTextFormatter(), true, true);
            String HELP_MESSAGE = "\n";
            if (JNLPRuntime.isDebug()) {
                HELP_MESSAGE = HELP_MESSAGE + helpMessagesProvider.writeToString();
            } else {
                HELP_MESSAGE = HELP_MESSAGE
                        + helpMessagesProvider.prepare().getSynopsis()
                        + helpMessagesProvider.getFormatter().getNewLine()
                        + helpMessagesProvider.prepare().getOptions()
                        + helpMessagesProvider.getFormatter().getNewLine();
            }
            OutputController.getLogger().printOut(HELP_MESSAGE);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            // not really important, so just ignore
        }

        final String filepath = getFilePathArgument(optionParser);
        final String codebase = getCodebaseArgument(optionParser);
        final String signedBy = getSignedByArgument(optionParser);
        final Set<PolicyParser.PrincipalEntry> principals = getPrincipalsArgument(optionParser);

        SwingUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                final PolicyEditorWindow frame = getPolicyEditorFrame(filepath);
                frame.getPolicyEditor().openPolicyFileSynchronously();
                frame.getPolicyEditor().addNewEntry(new PolicyIdentifier(signedBy, principals, codebase));
                frame.asWindow().setVisible(true);
            }
        });
    }

    static String getCodebaseArgument(final CommandLineOptionsParser optionParser) {
        if (optionParser.hasOption(CommandLineOptions.CODEBASE)) {
            final String codebase = optionParser.getParam(CommandLineOptions.CODEBASE);
            try {
                new URL(codebase);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid URL: " + codebase, e);
            }
            return codebase;
        } else {
            return null;
        }
    }

    static String getSignedByArgument(final CommandLineOptionsParser optionParser) {
        if (optionParser.hasOption(CommandLineOptions.SIGNEDBY)) {
            final String signedBy = optionParser.getParam(CommandLineOptions.SIGNEDBY);
            if (signedBy.isEmpty()) {
                throw new IllegalArgumentException("SignedBy cannot be empty");
            } else {
                return signedBy;
            }
        } else {
            return null;
        }
    }

    static Set<PolicyParser.PrincipalEntry> getPrincipalsArgument(final CommandLineOptionsParser optionParser) {
        if (optionParser.hasOption(CommandLineOptions.PRINCIPALS)) {
            final List<String> rawPrincipals = optionParser.getParams(CommandLineOptions.PRINCIPALS);
            final Set<PolicyParser.PrincipalEntry> principals = new HashSet<>();
            for (int i = 0; i < rawPrincipals.size(); i+= 2) {
                principals.add(new PolicyParser.PrincipalEntry(rawPrincipals.get(i), rawPrincipals.get(i + 1)));
            }
            return principals;
        } else {
            return Collections.emptySet();
        }
    }

    static String getFilePathArgument(final CommandLineOptionsParser optionParser) {
        final boolean openDefaultFile = optionParser.hasOption(CommandLineOptions.DEFAULTFILE);
        final boolean hasFileArgument = optionParser.hasOption(CommandLineOptions.FILE);
        final boolean hasMainArgument = optionParser.mainArgExists();
        if ((hasFileArgument && openDefaultFile) || (hasMainArgument && openDefaultFile)) {
            throw new IllegalArgumentException("Either -file (or simply a main argument) or -defaultfile may be specified, but not both");
        } else if (hasFileArgument && hasMainArgument) {
            throw new IllegalArgumentException("Either -file (or simply a main argument) or -defaultfile may be specified, but not both");
        }

        String filepath = null;
        if (hasFileArgument) {
            filepath = cleanFilePathArgument(optionParser.getParam(CommandLineOptions.FILE));
        } else if (hasMainArgument) {
            filepath = cleanFilePathArgument(optionParser.getMainArg());
        } else if (openDefaultFile) {
            filepath = getDefaultPolicyFilePath();
        }
        return filepath;
    }

    private static String cleanFilePathArgument(final String filepath) {
        if (filepath == null) {
            return null;
        } else if (filepath.isEmpty() || filepath.trim().isEmpty()) {
            return null;
        } else {
            return filepath;
        }
    }

    /**
     * Create a new PolicyEditor instance without passing argv. The returned instance is not
     * yet set visible.
     * @param filepath a policy file to open at start, or null if no file to load
     * @return a reference to a new PolicyEditor instance
     */
    public static PolicyEditor createInstance(final String filepath) {
        return new PolicyEditor(filepath);
    }

}
