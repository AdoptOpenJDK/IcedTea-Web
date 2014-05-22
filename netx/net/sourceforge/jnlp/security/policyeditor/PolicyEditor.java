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

package net.sourceforge.jnlp.security.policyeditor;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.jnlp.security.policyeditor.PolicyEditorPermissions.Group;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.FileUtils.OpenFileResult;
import net.sourceforge.jnlp.util.MD5SumWatcher;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * This class provides a policy editing tool as a simpler alternate to
 * the JDK PolicyTool. It is much simpler than PolicyTool - only
 * a handful of pre-defined permissions can be enabled or disabled,
 * on a per-codebase basis. There are no considerations for Principals,
 * who signed the code, or custom permissions.
 * 
 * This editor has a very simple idea of a policy file's contents. If any
 * entries are found which it does not recognize, eg 'grant' blocks which
 * have more than zero or one simple codeBase attributes, or 'Principal'
 * or other attributes assigned to the "grant block", or any other type
 * of complication to a "grant block" beyond a single codebase,
 * then all of these pieces of data are disregarded. When the editor saves
 * its work, all of this unrecognized data will be overwritten. Since
 * the editor has no way to display any of these contents anyway, it would
 * be potentially dangerous to allow this information to persist in the
 * policy file even after it has been edited and saved, as this would mean
 * the policy file contents may not be what the user thinks they are.
 * 
 * Comments in policy files are loosely supported, using both block-style
 * comment delimiters and double slashes. Block comments may not, however,
 * be placed on a line with "functional" text on the same line. To be
 * safe, comments should not be adjacent to "functional" text in the file
 * unless those lines are intended to be disregarded, ie commented out.
 * Comments will *not* be preserved when PolicyEditor next saves to the
 * file.
 */
public class PolicyEditor extends JPanel {

    /**
     * Command line switch to print a help message.
     */
    public static final String HELP_FLAG = "-help";

    /**
     * Command line switch to specify the location of the policy file.
     * If not given, then the default DeploymentConfiguration path is used.
     */
    public static final String FILE_FLAG = "-file";

    /**
     * Command line switch to specify a new codebase entry to be made.
     * Can only be used once, presently.
     */
    public static final String CODEBASE_FLAG = "-codebase";

    private static final String HELP_MESSAGE = "Usage:\t" + R("PEUsage") + "\n\n"
            + "  " + HELP_FLAG + "\t\t\t" + R("PEHelpFlag") + "\n"
            + "  " + FILE_FLAG + "\t\t\t" + R("PEFileFlag") + "\n"
            + "  " + CODEBASE_FLAG + "\t\t" + R("PECodebaseFlag") + "\n";

    private static final String AUTOGENERATED_NOTICE = "/* DO NOT MODIFY! AUTO-GENERATED */";

    private File file;
    private boolean changesMade = false;
    private boolean closed = false;
    private final Map<String, Map<PolicyEditorPermissions, Boolean>> codebasePermissionsMap = new HashMap<>();
    private final Map<String, Set<CustomPermission>> customPermissionsMap = new HashMap<>();
    private final Map<PolicyEditorPermissions, JCheckBox> checkboxMap = new TreeMap<>();
    private final List<JCheckBoxWithGroup> groupBoxList = new ArrayList<>(Group.values().length);
    private final JScrollPane scrollPane = new JScrollPane();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final JButton okButton = new JButton(), closeButton = new JButton(),
            addCodebaseButton = new JButton(), removeCodebaseButton = new JButton();
    private final JFileChooser fileChooser;
    private CustomPolicyViewer cpViewer = null;
    private final WeakReference<PolicyEditor> weakThis = new WeakReference<>(this);
    private MD5SumWatcher fileWatcher;

    private final ActionListener okButtonAction, addCodebaseButtonAction,
            removeCodebaseButtonAction, openButtonAction, saveAsButtonAction, viewCustomButtonAction;
    private ActionListener closeButtonAction;

    private static class JCheckBoxWithGroup extends JCheckBox {

        private final PolicyEditorPermissions.Group group;

        private JCheckBoxWithGroup(Group g) {
            super(g.getTitle());
            group = g;
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

    public PolicyEditor(final String filepath) {
        super();
        setLayout(new GridBagLayout());

        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            final JCheckBox box = new JCheckBox();
            box.setText(perm.getName());
            box.setToolTipText(perm.getDescription());
            checkboxMap.put(perm, box);
        }

        if (filepath != null) {
            file = new File(filepath);
            openAndParsePolicyFile();
        } else {
            resetCodebases();
        }

        fileChooser = new JFileChooser(file);
        fileChooser.setFileHidingEnabled(false);

        okButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (file == null) {
                    final int choice = fileChooser.showOpenDialog(weakThis.get());
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                    }
                }

                // May still be null if user cancelled the file chooser
                if (file != null) {
                    savePolicyFile();
                }
            }
        };
        okButton.setText(R("ButApply"));
        okButton.addActionListener(okButtonAction);

        addCodebaseButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                interactivelyAddCodebase();
            }
        };
        addCodebaseButton.setText(R("PEAddCodebase"));
        addCodebaseButton.addActionListener(addCodebaseButtonAction);

        removeCodebaseButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                removeCodebase(getSelectedCodebase());
            }
        };
        removeCodebaseButton.setText(R("PERemoveCodebase"));
        removeCodebaseButton.addActionListener(removeCodebaseButtonAction);

        openButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (changesMade) {
                    final int save = JOptionPane.showConfirmDialog(weakThis.get(), R("PESaveChanges"));
                    if (save == JOptionPane.YES_OPTION) {
                        if (file == null) {
                            final int choice = fileChooser.showSaveDialog(weakThis.get());
                            if (choice == JFileChooser.APPROVE_OPTION) {
                                file = fileChooser.getSelectedFile();
                            } else if (choice == JFileChooser.CANCEL_OPTION) {
                                return;
                            }
                        }
                        savePolicyFile();
                    } else if (save == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                final int choice = fileChooser.showOpenDialog(weakThis.get());
                if (choice == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    openAndParsePolicyFile();
                }
            }
        };

        saveAsButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int choice = fileChooser.showSaveDialog(weakThis.get());
                if (choice == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    changesMade = true;
                    savePolicyFile();
                }
            }
        };

        viewCustomButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String codebase = getSelectedCodebase();
                        if (codebase == null) {
                            return;
                        }
                        if (cpViewer == null) {
                            cpViewer = new CustomPolicyViewer(weakThis.get(), codebase, customPermissionsMap.get(codebase));
                            cpViewer.setVisible(true);
                        } else {
                            cpViewer.toFront();
                            cpViewer.repaint();
                        }
                    }
                });
            }
        };

        setAccelerators();

        setupLayout();
    }

    private String getSelectedCodebase() {
        final String codebase = list.getSelectedValue();
        if (codebase == null || codebase.isEmpty()) {
            return null;
        }
        if (codebase.equals(R("PEGlobalSettings"))) {
            return "";
        }
        return codebase;
    }

    private static void preparePolicyEditorWindow(final PolicyEditorWindow w, final PolicyEditor e) {
        w.setModalityType(ModalityType.MODELESS); //at least some default
        w.setPolicyEditor(e);
        w.setTitle(R("PETitle"));
        w.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        w.setJMenuBar(createMenuBar(w.asWindow(), w.getPolicyEditor()));
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
                window.dispose();
            }
        });

        editor.closeButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                ((PolicyEditorWindow) window).quit();
            }
        };
        editor.closeButton.setText(R("ButClose"));
        editor.closeButton.addActionListener(editor.closeButtonAction);

        final Action saveAct = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                editor.savePolicyFile();
            }
        };
        editor.setAccelerator(R("PEOkButtonMnemonic"), ActionEvent.ALT_MASK, saveAct, "OkButtonAccelerator");

        final Action quitAct = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ((PolicyEditorWindow) window).quit();
            }
        };
        editor.setAccelerator(R("PECancelButtonMnemonic"), ActionEvent.ALT_MASK, quitAct, "CancelButtonAccelerator");

        final Action escAct = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ((PolicyEditorWindow) window).quit();
            }
        };
        editor.setAccelerator(KeyEvent.VK_ESCAPE, ActionEvent.ALT_MASK, escAct, "ExitOnEscape");
    }

    public static interface PolicyEditorWindow {

        public void setTitle(String s);

        public void setDefaultCloseOperation(int i);

        public PolicyEditor getPolicyEditor();

        public void setPolicyEditor(PolicyEditor e);

        public void setJMenuBar(JMenuBar menu);

        public Window asWindow();

        public void setModalityType(ModalityType modalityType);

        public void quit();
    }

    private static class PolicyEditorFrame extends JFrame implements PolicyEditorWindow {

        private PolicyEditor editor;

        private PolicyEditorFrame(final PolicyEditor editor) {
            super();
            preparePolicyEditorWindow((PolicyEditorWindow) this, editor);
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
            if (editor.changesMade) {
                final int save = JOptionPane.showConfirmDialog(this, R("PESaveChanges"));
                if (save == JOptionPane.YES_OPTION) {
                    editor.savePolicyFile();
                } else if (save == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            editor.weakThis.clear();
            editor.setClosed();
            dispose();
        }

    }

    public static PolicyEditorWindow getPolicyEditorFrame(final String filepath) {
        return new PolicyEditorFrame(new PolicyEditor(filepath));
    }

    private static class PolicyEditorDialog extends JDialog implements PolicyEditorWindow {

        private PolicyEditor editor;

        private PolicyEditorDialog(final PolicyEditor editor) {
            super();
            preparePolicyEditorWindow((PolicyEditorWindow) this, editor);
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
            if (editor.changesMade) {
                final int save = JOptionPane.showConfirmDialog(this, R("PESaveChanges"));
                if (save == JOptionPane.YES_OPTION) {
                    editor.savePolicyFile();
                } else if (save == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            editor.weakThis.clear();
            editor.setClosed();
            dispose();
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
     * Set keyboard accelerators for each major function in the editor
     */
    private void setAccelerators() {
        setAddCodebaseAccelerator();
        setRemoveCodebaseAccelerator();
    }

    /**
     * Set a key accelerator
     * @param trigger the accelerator key
     * @param modifiers Alt, Ctrl, or other modifiers to be held with the trigger
     * @param action to be performed
     * @param identifier an identifier for the action
     */
    private void setAccelerator(final String trigger, final int modifiers, final Action action, final String identifier) {
        final int trig;
        try {
            trig = Integer.parseInt(trigger);
        } catch (final NumberFormatException nfe) {
            OutputController.getLogger().log("Unable to set accelerator action \""
                    + identifier + "\" for trigger \"" + trigger + "\"");
            OutputController.getLogger().log(nfe);
            return;
        }
        setAccelerator(trig, modifiers, action, identifier);
    }

    /**
     * Set a key accelerator
     * @param trigger the accelerator key
     * @param modifiers Alt, Ctrl, or other modifiers to be held with the trigger
     * @param action to be performed
     * @param identifier an identifier for the action
     */
    private void setAccelerator(final int trigger, final int modifiers, final Action action, final String identifier) {
        final KeyStroke key = KeyStroke.getKeyStroke(trigger, modifiers);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, identifier);
        this.getActionMap().put(identifier, action);
    }

    /**
     * Add an accelerator for adding new codebases
     */
    private void setAddCodebaseAccelerator() {
        final Action act = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                interactivelyAddCodebase();
            }
        };
        setAccelerator(R("PEAddCodebaseMnemonic"), ActionEvent.ALT_MASK, act, "AddCodebaseAccelerator");
    }

    /**
     * Add an accelerator for removing the selected codebase
     */
    private void setRemoveCodebaseAccelerator() {
        final Action act = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                removeCodebase(getSelectedCodebase());
            }
        };
        setAccelerator(R("PERemoveCodebaseMnemonic"), ActionEvent.ALT_MASK, act, "RemoveCodebaseAccelerator");
    }

    /**
     * Add a new codebase to the editor's model. If the codebase is not a valid URL,
     * the codebase is not added.
     * @param codebase to be added
     */
    public void addNewCodebase(final String codebase) {
        try {
            new URL(codebase);
        } catch (MalformedURLException mfue) {
            OutputController.getLogger().log("Could not add codebase " + codebase);
            OutputController.getLogger().log(mfue);
            return;
        }
        final boolean existingCodebase = initializeMapForCodebase(codebase);
        final String model;
        if (codebase.isEmpty()) {
            model = R("PEGlobalSettings");
        } else {
            model = codebase;
        }
        if (!existingCodebase) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listModel.addElement(model);
                }
            });
            changesMade = true;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                list.setSelectedValue(model, true);
                updateCheckboxes(codebase);
            }
        });
    }

    /**
     * Add a collection of codebases to the editor.
     * @param codebases the collection of codebases to be added
     */
    public void addNewCodebases(final Collection<String> codebases) {
        for (final String codebase : codebases) {
            addNewCodebase(codebase);
        }
    }

    /**
     * Add an array of codebases to the editor.
     * @param codebases the array of codebases to be added
     */
    public void addNewCodebases(final String[] codebases) {
        addNewCodebases(Arrays.asList(codebases));
    }

    /**
     * Display an input dialog, which will disappear when the user enters a valid URL
     * or when the user presses cancel. If an invalid URL is entered, the dialog reappears.
     * When a valid URL is entered, it is used to create a new codebase entry in the editor's
     * policy file model.
     */
    public void interactivelyAddCodebase() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String codebase = "";
                boolean stopAsking = false;
                while (!stopAsking) {
                    codebase = JOptionPane.showInputDialog(weakThis.get(), R("PECodebasePrompt"), "http://");
                    if (codebase == null) {
                        return;
                    }
                    try {
                        final URL u = new URL(codebase);
                        if (u.getProtocol() != null && u.getHost() != null) {
                            stopAsking = true;
                        }
                    } catch (final MalformedURLException mfue) {
                        // ignore - loop/ask again
                    }
                }
                addNewCodebase(codebase);
            }
        });
    }

    /**
     * Remove a codebase from the editor's model
     * @param codebase to be removed
     */
    public void removeCodebase(final String codebase) {
        if (codebase.equals(R("PEGlobalSettings")) || codebase.isEmpty()) {
            return;
        }
        int previousIndex = list.getSelectedIndex() - 1;
        if (previousIndex < 0) {
            previousIndex = 0;
        }
        codebasePermissionsMap.remove(codebase);
        final int fIndex = previousIndex;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                listModel.removeElement(codebase);
                list.setSelectedIndex(fIndex);
            }
        });
        changesMade = true;
    }

    /**
     * @return the set of Codebase entries in the policy file
     */
    public Set<String> getCodebases() {
        return new HashSet<String>(codebasePermissionsMap.keySet());
    }

    /**
     * @param codebase the codebase to query
     * @return a map of permissions to whether these permissions are set for the given codebase
     */
    public Map<PolicyEditorPermissions, Boolean> getPermissions(final String codebase) {
        final Map<PolicyEditorPermissions, Boolean> permissions = codebasePermissionsMap.get(codebase);
        if (permissions != null) {
            return new HashMap<PolicyEditorPermissions, Boolean>(permissions);
        } else {
            final Map<PolicyEditorPermissions, Boolean> blank = new HashMap<>();
            for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
                blank.put(perm, false);
            }
            return blank;
        }
    }

    /**
     * @param codebase the codebase to query
     * @return a collection of CustomPermissions granted to the given codebase
     */
    public Collection<CustomPermission> getCustomPermissions(final String codebase) {
        final Collection<CustomPermission> permissions = customPermissionsMap.get(codebase);
        if (permissions != null) {
            return new HashSet<CustomPermission>(permissions);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Update the checkboxes to show the permissions granted to the specified codebase
     * @param codebase whose permissions to display
     */
    private void updateCheckboxes(final String codebase) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                updateCheckboxesImpl(codebase);
            } else {
                updateCheckboxesInvokeAndWait(codebase);
            }
        } catch (InterruptedException ex) {
            OutputController.getLogger().log(ex);
        } catch (InvocationTargetException ex) {
            OutputController.getLogger().log(ex);
        }
    }

    private void updateCheckboxesInvokeAndWait(final String codebase) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                updateCheckboxesImpl(codebase);
            }
        });

    }

    private void updateCheckboxesImpl(final String codebase) {
        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            final JCheckBox box = checkboxMap.get(perm);
            for (final ActionListener l : box.getActionListeners()) {
                box.removeActionListener(l);
            }
            initializeMapForCodebase(codebase);
            final Map<PolicyEditorPermissions, Boolean> map = codebasePermissionsMap.get(codebase);
            final boolean state;
            if (map != null) {
                final Boolean s = map.get(perm);
                if (s != null) {
                    state = s;
                } else {
                    state = false;
                }
            } else {
                state = false;
            }
            for (final JCheckBoxWithGroup jg : groupBoxList) {
                jg.setState(map);
            }
            box.setSelected(state);
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    changesMade = true;
                    map.put(perm, box.isSelected());
                    for (JCheckBoxWithGroup jg : groupBoxList) {
                        jg.setState(map);
                    }
                }
            });
        }
    }

    /**
     * Set a mnemonic key for a menu item or button
     * @param component the component for which to set a mnemonic
     * @param mnemonic the mnemonic to set
     */
    private static void setComponentMnemonic(final AbstractButton component, final String mnemonic) {
        final int trig;
        try {
            trig = Integer.parseInt(mnemonic);
        } catch (final NumberFormatException nfe) {
            OutputController.getLogger().log(nfe);
            return;
        }
        component.setMnemonic(trig);
    }

    private static JMenuBar createMenuBar(final Window window, final PolicyEditor editor) {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu fileMenu = new JMenu(R("PEFileMenu"));
        setComponentMnemonic(fileMenu, R("PEFileMenuMnemonic"));

        final JMenuItem openItem = new JMenuItem(R("PEOpenMenuItem"));
        setComponentMnemonic(openItem, R("PEOpenMenuItemMnemonic"));
        openItem.setAccelerator(KeyStroke.getKeyStroke(openItem.getMnemonic(), ActionEvent.CTRL_MASK));
        openItem.addActionListener(editor.openButtonAction);
        fileMenu.add(openItem);

        final JMenuItem saveItem = new JMenuItem(R("PESaveMenuItem"));
        setComponentMnemonic(saveItem, R("PESaveMenuItemMnemonic"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(saveItem.getMnemonic(), ActionEvent.CTRL_MASK));
        saveItem.addActionListener(editor.okButtonAction);
        fileMenu.add(saveItem);

        final JMenuItem saveAsItem = new JMenuItem(R("PESaveAsMenuItem"));
        setComponentMnemonic(saveAsItem, R("PESaveAsMenuItemMnemonic"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(saveAsItem.getMnemonic(), ActionEvent.CTRL_MASK));
        saveAsItem.addActionListener(editor.saveAsButtonAction);
        fileMenu.add(saveAsItem);

        final JMenuItem exitItem = new JMenuItem(R("PEExitMenuItem"));
        setComponentMnemonic(exitItem, R("PEExitMenuItemMnemonic"));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(exitItem.getMnemonic(), ActionEvent.CTRL_MASK));
        exitItem.addActionListener(editor.closeButtonAction);
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                window.dispose();
            }
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        final JMenu viewMenu = new JMenu(R("PEViewMenu"));
        setComponentMnemonic(viewMenu, R("PEViewMenuMnemonic"));

        final JMenuItem customPermissionsItem = new JMenuItem(R("PECustomPermissionsItem"));
        setComponentMnemonic(customPermissionsItem, R("PECustomPermissionsItemMnemonic"));
        customPermissionsItem.setAccelerator(KeyStroke.getKeyStroke(customPermissionsItem.getMnemonic(), ActionEvent.ALT_MASK));
        customPermissionsItem.addActionListener(editor.viewCustomButtonAction);

        viewMenu.add(customPermissionsItem);
        menuBar.add(viewMenu);

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
            if (PolicyEditorPermissions.Group.anyContains(box, checkboxMap)) {
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
        //add groups
        for (final PolicyEditorPermissions.Group g : PolicyEditorPermissions.Group.values()) {
            //no metter what, put group title on new line
            checkboxConstraints.gridy++;
            //all groups are in second column
            checkboxConstraints.gridx = 2;
            final JCheckBoxWithGroup groupCh = new JCheckBoxWithGroup(g);
            groupBoxList.add(groupCh);
            final JPanel groupPanel = new JPanel(new GridBagLayout());
            groupPanel.setBorder(new LineBorder(Color.black));
            groupCh.setToolTipText(R("PEGrightClick"));
            groupCh.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        groupPanel.setVisible(!groupPanel.isVisible());
                        PolicyEditor.this.validate();
                        Container c = PolicyEditor.this.getParent();
                        // find the window and repack it
                        while (!(c instanceof Window)) {
                            if (c == null) {
                                return;
                            }
                            c = c.getParent();
                        }
                        final Window w = (Window) c;
                        w.pack();

                    }
                }
            });
            groupCh.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final String codebase = getSelectedCodebase();
                    if (codebase == null) {
                        return;
                    }
                    List<ActionListener> backup = new LinkedList<>();
                    for (final ActionListener l : groupCh.getActionListeners()) {
                        backup.add(l);
                        groupCh.removeActionListener(l);
                    }
                    final Map<PolicyEditorPermissions, Boolean> map = codebasePermissionsMap.get(codebase);
                    for (final PolicyEditorPermissions p : groupCh.getGroup().getPermissions()) {
                        map.put(p, groupCh.isSelected());
                    }
                    changesMade = true;
                    updateCheckboxes(codebase);
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
            //reset
            checkboxConstraints.gridwidth = 1;
        }

        final JLabel codebaseListLabel = new JLabel(R("PECodebaseLabel"));
        codebaseListLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        final GridBagConstraints listLabelConstraints = new GridBagConstraints();
        listLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        listLabelConstraints.gridx = 0;
        listLabelConstraints.gridy = 0;
        add(codebaseListLabel, listLabelConstraints);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return; // ignore first click, act on release
                }
                final String codebase = getSelectedCodebase();
                if (codebase == null) {
                    return;
                }
                updateCheckboxes(codebase);
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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

        final GridBagConstraints addCodebaseButtonConstraints = new GridBagConstraints();
        addCodebaseButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        addCodebaseButtonConstraints.gridx = 0;
        addCodebaseButtonConstraints.gridy = listConstraints.gridy + listConstraints.gridheight + 1;
        setComponentMnemonic(addCodebaseButton, R("PEAddCodebaseMnemonic"));
        add(addCodebaseButton, addCodebaseButtonConstraints);

        final GridBagConstraints removeCodebaseButtonConstraints = new GridBagConstraints();
        removeCodebaseButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        removeCodebaseButtonConstraints.gridx = addCodebaseButtonConstraints.gridx + 1;
        removeCodebaseButtonConstraints.gridy = addCodebaseButtonConstraints.gridy;
        setComponentMnemonic(removeCodebaseButton, R("PERemoveCodebaseMnemonic"));
        removeCodebaseButton.setPreferredSize(addCodebaseButton.getPreferredSize());
        add(removeCodebaseButton, removeCodebaseButtonConstraints);

        final GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        okButtonConstraints.gridx = removeCodebaseButtonConstraints.gridx + 2;
        okButtonConstraints.gridy = removeCodebaseButtonConstraints.gridy;
        setComponentMnemonic(okButton, R("PEOkButtonMnemonic"));
        add(okButton, okButtonConstraints);

        final GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        cancelButtonConstraints.gridx = okButtonConstraints.gridx + 1;
        cancelButtonConstraints.gridy = okButtonConstraints.gridy;
        setComponentMnemonic(closeButton, R("PECancelButtonMnemonic"));
        add(closeButton, cancelButtonConstraints);

        setMinimumSize(getPreferredSize());
    }

    /**
     * Update the custom permissions map. Used by the Custom Policy Viewer to update its parent
     * PolicyEditor to changes it has made
     * @param codebase the codebase for which changes were made
     * @param permissions the permissions granted to this codebase
     */
    void updateCustomPermissions(final String codebase, final Collection<CustomPermission> permissions) {
        changesMade = true;
        customPermissionsMap.get(codebase).clear();
        customPermissionsMap.get(codebase).addAll(permissions);
    }

    private void resetCodebases() {
        listModel.clear();
        codebasePermissionsMap.clear();
        customPermissionsMap.clear();

        initializeMapForCodebase("");
        listModel.addElement(R("PEGlobalSettings"));
        list.setSelectedValue(R("PEGlobalSettings"), true);
        updateCheckboxes("");
    }

    /**
     * Open the file pointed to by the filePath field. This is either provided by the
     * "-file" command line flag, or if none given, comes from DeploymentConfiguration.
     */
    private void openAndParsePolicyFile() {
        new Thread() {
            @Override
            public void run() {
                resetCodebases();

                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (final IOException e) {
                        OutputController.getLogger().log(e);
                        // If this fails we'll end up handling it a few lines down anyway.
                    }
                }
                final OpenFileResult ofr = FileUtils.testFilePermissions(file);
                if (ofr == OpenFileResult.FAILURE || ofr == OpenFileResult.NOT_FILE) {
                    FileUtils.showCouldNotOpenFilepathDialog(weakThis.get(), file.getPath());
                    return;
                }
                if (ofr == OpenFileResult.CANT_WRITE) {
                    FileUtils.showReadOnlyDialog(weakThis.get());
                }
                final String contents;
                try {
                    fileWatcher = new MD5SumWatcher(file);
                    fileWatcher.update();
                    // User-level policy files are expected to be short enough that loading them in as a String
                    // should not actually be *too* bad, and it's easy to work with.
                    contents = FileUtils.loadFileAsString(file);
                } catch (final IOException e) {
                    OutputController.getLogger().log(e);
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, R("RCantOpenFile", file.getPath()));
                    FileUtils.showCouldNotOpenDialog(weakThis.get(), R("PECouldNotOpen"));
                    return;
                }
                codebasePermissionsMap.clear();
                customPermissionsMap.clear();
                // Split on newlines, both \r\n and \n style, for platform-independence
                final String[] lines = contents.split("\\r?\\n+");
                String codebase = "";
                final FileLock fileLock;
                try {
                    fileLock = FileUtils.getFileLock(file.getAbsolutePath(), false, true);
                } catch (final FileNotFoundException e) {
                    OutputController.getLogger().log(e);
                    FileUtils.showCouldNotOpenDialog(weakThis.get(), R("PECouldNotOpen"));
                    return;
                }
                boolean openBlock = false, commentBlock = false;
                for (final String line : lines) {
                    // Matches eg `grant {` as well as `grant codeBase "http://redhat.com" {`
                    final Pattern openBlockPattern = Pattern.compile("grant\\s*\"?\\s*(?:codeBase)?\\s*\"?([^\"\\s]*)\"?\\s*\\{");
                    final Matcher openBlockMatcher = openBlockPattern.matcher(line);
                    if (openBlockMatcher.matches()) {
                        // Codebase URL
                        codebase = openBlockMatcher.group(1);
                        initializeMapForCodebase(codebase);
                        listModel.addElement(codebase);
                        openBlock = true;
                    }

                    // Matches '};', the closing block delimiter, with any amount of whitespace on either side
                    boolean commentLine = false;
                    if (line.matches("\\s*\\};\\s*")) {
                        openBlock = false;
                    }
                    // Matches '/*', the start of a block comment
                    if (line.matches(".*/\\*.*")) {
                        commentBlock = true;
                    }
                    // Matches '*/', the end of a block comment, and '//', a single-line comment
                    if (line.matches(".*\\*/.*")) {
                        commentBlock = false;
                    }
                    if (line.matches(".*/\\*.*") && line.matches(".*\\*/.*")) {
                        commentLine = true;
                    }
                    if (line.matches("\\s*//.*")) {
                        commentLine = true;
                    }

                    if (!openBlock || commentBlock || commentLine) {
                        continue;
                    }
                    final PolicyEditorPermissions perm = PolicyEditorPermissions.fromString(line);
                    if (perm != null) {
                        codebasePermissionsMap.get(codebase).put(perm, true);
                        updateCheckboxes(codebase);
                    } else {
                        final CustomPermission cPerm = CustomPermission.fromString(line.trim());
                        if (cPerm != null) {
                            customPermissionsMap.get(codebase).add(cPerm);
                        }
                    }
                }
                list.setSelectedValue(R("PEGlobalSettings"), true);
                updateCheckboxes("");
                try {
                    fileLock.release();
                } catch (final IOException e) {
                    OutputController.getLogger().log(e);
                }
            }
        }.run(); // #run() to make IO synchronous right now. #start() can be used to make it async instead.
        // http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2014-March/026886.html
        // TODO: use SwingWorker and give some visual indication that IO is occurring
    }

    /**
     * Ensure that the model contains a specified mapping. No action is taken
     * if there already is a map with this key
     * @param codebase for which a permissions mapping is required
     * @return true iff there was already an entry for this codebase
     */
    private boolean initializeMapForCodebase(final String codebase) {
        if (codebasePermissionsMap.containsKey(codebase) || customPermissionsMap.containsKey(codebase)) {
            return true;
        }

        if (codebasePermissionsMap.get(codebase) == null) {
            final Map<PolicyEditorPermissions, Boolean> map = new HashMap<>();
            for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
                map.put(perm, false);
            }
            codebasePermissionsMap.put(codebase, map);
        }

        if (customPermissionsMap.get(codebase) == null) {
            final Set<CustomPermission> set = new HashSet<>();
            customPermissionsMap.put(codebase, set);
        }

        return false;
    }

    /**
     * Save the policy model into the file pointed to by the filePath field.
     */
    private void savePolicyFile() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final int response = checkPolicyChangesWithDialog();
                    switch (response) {
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
                } catch (final FileNotFoundException e) {
                    // File on disk has been somehow removed since we first checked. Attempt to save to it
                    // anyway then. If we can't, then the failure simply occurs later in this method
                    OutputController.getLogger().log(e);
                } catch (final IOException e) {
                    OutputController.getLogger().log(e);
                    showCouldNotSaveDialog();
                    return;
                }
                final StringBuilder sb = new StringBuilder();
                sb.append(AUTOGENERATED_NOTICE);
                sb.append("\n/* Generated by PolicyEditor at ")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()))
                    .append(" */")
                    .append(System.getProperty("line.separator"));
                final Set<PolicyEditorPermissions> enabledPermissions = new HashSet<PolicyEditorPermissions>();
                FileLock fileLock;
                try {
                    fileLock = FileUtils.getFileLock(file.getAbsolutePath(), false, true);
                } catch (final FileNotFoundException e) {
                    OutputController.getLogger().log(e);
                    showCouldNotSaveDialog();
                    return;
                }
                for (final String codebase : codebasePermissionsMap.keySet()) {
                    enabledPermissions.clear();
                    for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : codebasePermissionsMap.get(codebase).entrySet()) {
                        if (entry.getValue()) {
                            enabledPermissions.add(entry.getKey());
                        }
                    }
                    sb.append(new PolicyEntry(codebase, enabledPermissions, customPermissionsMap.get(codebase)).toString());
                }
                try {
                    fileLock.release();
                } catch (final IOException e) {
                    OutputController.getLogger().log(e);
                }

                try {
                    FileUtils.saveFile(sb.toString(), file);
                    if (fileWatcher == null) {
                        fileWatcher = new MD5SumWatcher(file);
                    }
                    fileWatcher.update();
                    changesMade = false;
                    showChangesSavedDialog();
                } catch (final IOException e) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, R("RCantWriteFile", file.getPath()));
                    showCouldNotSaveDialog();
                }
            }
        }.run(); // #run() to make IO synchronous right now. #start() can be used to make it async instead.
        // http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2014-March/026886.html
        // TODO: use SwingWorker and give some visual indication that IO is occurring
    }

    /**
     * Show a dialog informing the user that their changes have been saved.
     */
    private void showChangesSavedDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(weakThis.get(), R("PEChangesSaved"));
            }
        });
    }

    /**
     * Show a dialog informing the user that their changes could not be saved.
     */
    private void showCouldNotSaveDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(weakThis.get(), R("PECouldNotSave"), R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Detect if the policy settings have changed, either on-disk or in-app.
     * If an on-disk change has occurred, update the Md5.
     * @return The user's choice (Yes/No/Cancel - see JOptionPane constants). 
     * "Cancel" if the file hasn't changed but the user has made modifications
     * to the settings. "No" otherwise
     * @throws IOException if the file cannot be read
     */
    private int checkPolicyChangesWithDialog() throws IOException {
        if (fileWatcher == null) {
            if (file != null) {
                fileWatcher = new MD5SumWatcher(file);
            }
            return JOptionPane.NO_OPTION;
        }
        boolean changed;
        try {
            changed = fileWatcher.update();
        } catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(weakThis.get(), R("PEFileMissing"), R("PEFileModified"), JOptionPane.WARNING_MESSAGE);
            return JOptionPane.NO_OPTION;
        }
        if (changed) {
            return JOptionPane.showConfirmDialog(weakThis.get(), R("PEFileModifiedDetail", file.getCanonicalPath()),
                    R("PEFileModified"), JOptionPane.YES_NO_CANCEL_OPTION);
        } else if (!changesMade) {
            //Return without saving or reloading
            return JOptionPane.CANCEL_OPTION;
        }
        return JOptionPane.NO_OPTION;
    }

    /**
     * Start a Policy Editor instance.
     * @param args "-file $FILENAME" and/or "-codebase $CODEBASE" are accepted flag/value pairs.
     * -file specifies a file path to be opened by the editor. If none is provided, the default
     * policy file location for the user is opened.
     * -codebase specifies (a) codebase(s) to start the editor with. If the entry already exists,
     * it will be selected. If it does not exist, it will be created, then selected. Multiple
     * codebases can be used, separated by spaces.
     * -help will print a help message and immediately return (no editor instance opens)
     */
    public static void main(final String[] args) {
        final Map<String, String> argsMap = argsToMap(args);

        if (argsMap.containsKey(HELP_FLAG)) {
            System.out.println(HELP_MESSAGE);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            // not really important, so just ignore
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String filepath = argsMap.get(FILE_FLAG);
                if (filepath == null && args.length == 1) {
                    // maybe the user just forgot the -file flag, so try to open anyway
                    filepath = args[0];
                }
                final PolicyEditorWindow frame = getPolicyEditorFrame(filepath);
                frame.asWindow().setVisible(true);
                final String codebaseStr = argsMap.get(CODEBASE_FLAG);
                if (codebaseStr != null) {
                    final String[] urls = codebaseStr.split(" ");
                    frame.getPolicyEditor().addNewCodebases(urls);
                }
            }
        });
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

    /**
     * Create a Map out of argv
     * @param args command line flags and parameters given to the program
     * @return a Map representation of the command line arguments
     */
    static Map<String, String> argsToMap(final String[] args) {
        final List<String> argsList = Arrays.<String> asList(args);
        final Map<String, String> map = new HashMap<>();

        if (argsList.contains(HELP_FLAG)) {
            map.put(HELP_FLAG, null);
        }

        if (argsList.contains(FILE_FLAG)) {
            map.put(FILE_FLAG, argsList.get(argsList.indexOf(FILE_FLAG) + 1));
        }

        if (argsList.contains(CODEBASE_FLAG)) {
            final int flagIndex = argsList.indexOf(CODEBASE_FLAG);
            final StringBuilder sb = new StringBuilder();
            for (int i = flagIndex + 1; i < argsList.size(); ++i) {
                final String str = argsList.get(i);
                if (str.equals(HELP_FLAG) || str.equals(CODEBASE_FLAG) || str.equals(FILE_FLAG)) {
                    break;
                }
                sb.append(str);
                sb.append(" ");
            }
            map.put(CODEBASE_FLAG, sb.toString().trim());
        }
        return map;
    }

}
