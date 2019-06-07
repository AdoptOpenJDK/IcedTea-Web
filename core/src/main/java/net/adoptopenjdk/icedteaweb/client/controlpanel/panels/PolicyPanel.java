/* Copyright (C) 2014 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.FileDialogFactory;
import net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor;
import net.adoptopenjdk.icedteaweb.client.policyeditor.PolicyEditor.PolicyEditorWindow;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.FileUtils.OpenFileResult;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_HOME;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Implements a Policy Settings panel for the itweb-settings control panel.
 * This gives the user information about custom user-level JNLP Policy files,
 * as well as offering a way to launch a policy file editor with the correct
 * file path to the user's personal policy file location presupplied.
 */
public class PolicyPanel extends NamedBorderPanel {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyPanel.class);

    private PolicyEditorWindow policyEditor = null;

    public PolicyPanel(final DeploymentConfiguration config) {
        super(R("CPHeadPolicy"), new GridBagLayout());

        final JLabel aboutLabel = new JLabel("<html>" + R("CPPolicyDetail") + "</html>");

        final String fileUrlString = PathsAndFiles.JAVA_POLICY.getFullPath(config);
        final JButton simpleEditorButton = new JButton(R("CPButSimpleEditor"));
        simpleEditorButton.addActionListener(new LaunchSimplePolicyEditorAction(fileUrlString));

        final JButton advancedEditorButton = new JButton(R("CPButAdvancedEditor"));
        advancedEditorButton.addActionListener(new LaunchPolicyToolAction(fileUrlString));

        final String pathPart = localFilePathFromUrlString(fileUrlString);
        simpleEditorButton.setToolTipText(R("CPPolicyTooltip", FileUtils.displayablePath(pathPart, 60)));

        final JTextField locationField = new JTextField(pathPart);
        locationField.setEditable(false);

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        add(aboutLabel, c);

        c.weighty = 0;
        c.weightx = 0;
        c.gridy++;
        add(locationField, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        add(simpleEditorButton, c);
        c.gridx++;
        add(advancedEditorButton, c);
        c.gridx--;

        /* Keep all the elements at the top of the panel (Extra padding)
         * Keep View/Edit button next to location field, with padding between
         * the right edge of the frame and the button
         */
        c.fill = GridBagConstraints.BOTH;
        final Component filler1 = Box.createRigidArea(new Dimension(240, 1));
        final Component filler2 = Box.createRigidArea(new Dimension(1, 1));
        c.gridx++;
        add(filler1, c);
        c.gridx--;
        c.weighty = 1;
        c.gridy++;
        add(filler2, c);
    }

    /**
     * Launch the policytool for a specified file path
     * @param parentComponent a {@link Component} to act as parent to warning dialogs which may appear
     * @param filePath a {@link String} representing the path to the file to be opened
     */
    private static void launchPolicyTool(final Component parentComponent, final String filePath) {
        try {
            final File policyFile = new File(filePath).getCanonicalFile();
            final OpenFileResult result = FileUtils.testFilePermissions(policyFile);
            if (result == OpenFileResult.SUCCESS) {
                policyToolLaunchHelper(parentComponent, filePath);
            } else if (result == OpenFileResult.CANT_WRITE) {
                LOG.warn("Opening user JNLP policy read-only");
                FileDialogFactory.showReadOnlyDialog(parentComponent);
                policyToolLaunchHelper(parentComponent, filePath);
            } else {
                LOG.error("Could not open user JNLP policy");
                FileDialogFactory.showCouldNotOpenFileDialog(parentComponent, policyFile.getPath(), result);
            }
        } catch (IOException e) {
            LOG.error("Could not open user JNLP policy", e);
            FileDialogFactory.showCouldNotOpenFilepathDialog(parentComponent, filePath);
        }
    }

    /**
     * Launch the simplified PolicyEditor for a specified file path.
     *
     * @param filePath a {@link String} representing the path to the file to be opened
     */
    private void launchSimplePolicyEditor(final String filePath) {
        if (policyEditor == null || policyEditor.getPolicyEditor().isClosed()) {
            policyEditor = PolicyEditor.getPolicyEditorFrame(filePath);
            policyEditor.getPolicyEditor().openAndParsePolicyFile();
            policyEditor.asWindow().setVisible(true);
        } else {
            policyEditor.asWindow().toFront();
            policyEditor.asWindow().repaint();
        }
    }

    /**
     * This executes a new process for policytool using ProcessBuilder, with the new process'
     * working directory set to the user's home directory. policytool then attempts to
     * open the provided policy file path, if policytool can be run. ProcessBuilder does
     * some verification to ensure that the built command can be executed - if not, it
     * throws an IOException. In this event, we try our reflective fallback launch.
     * We do this in a new {@link Thread} to ensure that the fallback launch does not
     * block the AWT thread, and neither does ProcessBuilder#start() in case it happens
     * to be synchronous on the current system.
     * @param parentComponent a {@link Component} to act as parent to warning dialogs which may appear
     * @param filePath a {@link String} representing the path to the file to be opened
     */
    private static void policyToolLaunchHelper(final Component parentComponent, final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ProcessBuilder pb = new ProcessBuilder("policytool", "-file", filePath)
                        .directory(new File(System.getProperty(USER_HOME)));
                try {
                    pb.start();
                } catch (IOException ioe) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
                    try {
                        reflectivePolicyToolLaunch(filePath);
                    } catch (Exception e) {
                        LOG.error("Could not open user JNLP policy", e);
                        FileDialogFactory.showCouldNotOpenDialog(parentComponent, R("CPPolicyEditorNotFound"));
                    }
                }
            }
        }).start();
    }

    /**
     * This is used as a fallback in case launching the policytool by executing a new process
     * fails. This probably happens because we are running on a system where the policytool
     * executable is not on the PATH, or because we are running on a non-POSIX compliant system.
     * We do this reflectively to avoid needing to add PolicyTool as build dependency simply for
     * this small edge case.
     * @param filePath a {@link String} representing the path of the file to attempt to open
     * @throws Exception if any sort of exception occurs during reflective launch of policytool
     */
    private static void reflectivePolicyToolLaunch(final String filePath) throws Exception {
        Class<?> policyTool;
        try {
            // Java 7 location
            policyTool = Class.forName("sun.security.tools.policytool.PolicyTool");
        } catch (ClassNotFoundException cnfe) {
            // Java 6 location
            policyTool = Class.forName("sun.security.tools.PolicyTool");
        }
        final Class<?>[] signature = new Class<?>[] { String[].class };
        final Method main = policyTool.getMethod("main", signature);
        final String[] args = new String[] { "-file", filePath };
        main.invoke(null, (Object) args);
    }

    /**
     * Loosely attempt to get the path part of a file URL string. If this fails,
     * simply return back the input. This is only intended to be used for displaying
     * GUI elements such as the CPPolicyTooltip.
     * @param url the {@link String} representing the URL whose path is desired
     * @return a {@link String} representing the local filepath of the given file:/ URL
     */
    private static String localFilePathFromUrlString(final String url) {
        try {
            final URL u = new URL(url);
            return u.getPath();
        } catch (MalformedURLException e) {
            return url;
        }
    }


    private Component getParentComponentOfSource(final ActionEvent event) {
        return Optional.ofNullable(event)
                .map(e -> e.getSource())
                .filter(s -> s instanceof Component)
                .map(s -> (Component) s)
                .orElse(null);
    }

    /**
     * Implements the action to be performed when the "Advanced" button is clicked
     */
    private class LaunchPolicyToolAction implements ActionListener {

        private final String fileUrlString;

        public LaunchPolicyToolAction(final String fileUrlString) {
            this.fileUrlString = fileUrlString;
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            final Component parentComponent = getParentComponentOfSource(event);
            try {
                final URL fileUrl = new URL(fileUrlString);
                SwingUtils.invokeLater(() -> launchPolicyTool(parentComponent, fileUrl.getPath()));
            } catch (MalformedURLException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                FileDialogFactory.showCouldNotOpenFilepathDialog(parentComponent, fileUrlString);
            }
        }
    }

    private class LaunchSimplePolicyEditorAction implements ActionListener {

        private final String fileUrlString;

        public LaunchSimplePolicyEditorAction(final String fileUrlString) {
            this.fileUrlString = fileUrlString;
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            try {
                final URL fileUrl = new URL(fileUrlString);
                SwingUtils.invokeLater(() -> launchSimplePolicyEditor(fileUrl.getPath()));
            } catch (MalformedURLException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                final Component parentComponent = getParentComponentOfSource(event);
                FileDialogFactory.showCouldNotOpenFilepathDialog(parentComponent, fileUrlString);
            }
        }
    }
}
