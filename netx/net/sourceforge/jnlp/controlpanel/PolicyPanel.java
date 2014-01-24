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

package net.sourceforge.jnlp.controlpanel;

import static net.sourceforge.jnlp.runtime.Translator.R;

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.DirectoryValidator;
import net.sourceforge.jnlp.config.DirectoryValidator.DirectoryCheckResults;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

public class PolicyPanel extends NamedBorderPanel {

    /**
     * Indicates whether a file was successfully opened. If not, provides specific reasons
     * along with a general failure case
     */
    private enum OpenFileResult {
        /** The file was successfully opened */
        SUCCESS,
        /** The file could not be opened, for non-specified reasons */
        FAILURE,
        /** The file could not be opened because it did not exist and could not be created */
        CANT_CREATE,
        /** The file can be opened but in read-only */
        CANT_WRITE,
        /** The specified path pointed to a non-file filesystem object, ie a directory */
        NOT_FILE
    }

    public PolicyPanel(final JFrame frame, final DeploymentConfiguration config) {
        super(R("CPHeadPolicy"), new GridBagLayout());
        addComponents(frame, config);
    }

    private void addComponents(final JFrame frame, final DeploymentConfiguration config) {
        JLabel aboutLabel = new JLabel("<html>" + R("CPPolicyDetail") + "</html>");

        final String fileUrlString = config.getProperty(DeploymentConfiguration.KEY_USER_SECURITY_POLICY);
        JButton showUserPolicyButton = new JButton(R("CPButPolicy"));
        showUserPolicyButton.addActionListener(new ViewPolicyButtonAction(frame, fileUrlString));

        String pathPart = localFilePathFromUrlString(fileUrlString);
        showUserPolicyButton.setToolTipText(R("CPPolicyTooltip", FileUtils.displayablePath(pathPart, 60)));

        JTextField locationField = new JTextField(pathPart);
        locationField.setEditable(false);

        GridBagConstraints c = new GridBagConstraints();
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
        add(showUserPolicyButton, c);

        /* Keep all the elements at the top of the panel (Extra padding)
         * Keep View/Edit button next to location field, with padding between
         * the right edge of the frame and the button
         */
        c.fill = GridBagConstraints.BOTH;
        Component filler1 = Box.createRigidArea(new Dimension(240, 1));
        Component filler2 = Box.createRigidArea(new Dimension(1, 1));
        c.gridx++;
        add(filler1, c);
        c.gridx--;
        c.weighty = 1;
        c.gridy++;
        add(filler2, c);
    }

    /**
     * Launch the policytool for a specified file path
     * @param frame a {@link JFrame} to act as parent to warning dialogs which may appear
     * @param filePath a {@link String} representing the path to the file to be opened
     */
    private static void launchPolicyTool(final JFrame frame, final String filePath) {
        try {
            final File policyFile = new File(filePath).getCanonicalFile();
            OpenFileResult result = canOpenPolicyFile(policyFile);
            if (result == OpenFileResult.SUCCESS) {
                policyToolLaunchHelper(frame, filePath);
            } else if (result == OpenFileResult.CANT_WRITE) {
                showReadOnlyDialog(frame);
                policyToolLaunchHelper(frame, filePath);
            } else {
                showCouldNotOpenFileDialog(frame, policyFile.getPath(), result);
            }
        } catch (IOException e) {
            OutputController.getLogger().log(e);
            showCouldNotOpenFileDialog(frame, filePath);
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
     * @param frame a {@link JFrame} to act as parent to warning dialogs which may appear
     * @param filePath a {@link String} representing the path to the file to be opened
     */
    private static void policyToolLaunchHelper(final JFrame frame, final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProcessBuilder pb = new ProcessBuilder("policytool", "-file", filePath)
                        .directory(new File(System.getProperty("user.home")));
                try {
                    pb.start();
                } catch (IOException ioe) {
                    OutputController.getLogger().log(ioe);
                    try {
                        reflectivePolicyToolLaunch(filePath);
                    } catch (Exception e) {
                        OutputController.getLogger().log(e);
                        showCouldNotOpenFileDialog(frame, filePath, R("CPPolicyEditorNotFound"));
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
        Class<?> policyTool = Class.forName("sun.security.tools.policytool.PolicyTool");
        Class<?>[] signature = new Class<?>[] { String[].class };
        Method main = policyTool.getDeclaredMethod("main", signature);
        Object args = new String[] { "-file", filePath };
        main.invoke(null, args);
    }

    /**
     * Verify that a given file object points to a real, accessible plain file.
     * As a side effect, if the file is accessible but does not yet exist, it will be created
     * as an empty plain file.
     * @param policyFile the {@link File} to verify
     * @return an {@link OpenFileResult} representing the accessibility level of the file
     * @throws IOException if the file is not accessible
     */
    private static OpenFileResult canOpenPolicyFile(final File policyFile) {
        DirectoryCheckResults dcr = testPolicyFileDirectory(policyFile);
        if (dcr.getFailures() == 0) {
            if (policyFile.isDirectory())
                return OpenFileResult.NOT_FILE;
            try {
                if (!policyFile.exists() && !policyFile.createNewFile()) {
                    return OpenFileResult.CANT_CREATE;
                }
            } catch (IOException e) {
                return OpenFileResult.CANT_CREATE;
            }
            boolean read = policyFile.canRead(), write = policyFile.canWrite();
            if (read && write)
                return OpenFileResult.SUCCESS;
            else if (read)
                return OpenFileResult.CANT_WRITE;
            else
                return OpenFileResult.FAILURE;
        }
        return OpenFileResult.FAILURE;
    }

    /**
     * Ensure that the parent directory of the Policy File exists and that we are
     * able to create and access files within this directory
     * @param policyFile the {@link File} representing a Java Policy file to test
     * @return a {@link DirectoryCheckResults} object representing the results of the test
     */
    private static DirectoryCheckResults testPolicyFileDirectory(final File policyFile) {
        List<File> policyDirectory = new ArrayList<File>();
        policyDirectory.add(policyFile.getParentFile());
        DirectoryValidator validator = new DirectoryValidator(policyDirectory);
        DirectoryCheckResults result = validator.ensureDirs();

        return result;
    }

    /**
     * Show a generic error dialog indicating the policy file could not be opened
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param filePath a {@link String} representing the path to the file we failed to open
     */
    private static void showCouldNotOpenFileDialog(final JFrame frame, final String filePath) {
        showCouldNotOpenFileDialog(frame, filePath, OpenFileResult.FAILURE);
    }

    /**
     * Show an error dialog indicating the policy file could not be opened, with a particular reason
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param filePath a {@link String} representing the path to the file we failed to open
     * @param reason a {@link OpenFileResult} specifying more precisely why we failed to open the file
     */
    private static void showCouldNotOpenFileDialog(final JFrame frame, final String filePath, final OpenFileResult reason) {
        String message;
        switch (reason) {
            case CANT_CREATE:
                message = R("RCantCreateFile", filePath);
                break;
            case CANT_WRITE:
                message = R("RCantWriteFile", filePath);
                break;
            case NOT_FILE:
                message = R("RExpectedFile", filePath);
                break;
            default:
                message = R("RCantOpenFile", filePath);
                break;
        }
        showCouldNotOpenFileDialog(frame, filePath, message);
    }

    /**
     * Show a dialog informing the user that the policy file could not be opened
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param filePath a {@link String} representing the path to the file we failed to open
     * @param message a {@link String} giving the specific reason the file could not be opened
     */
    private static void showCouldNotOpenFileDialog(final JFrame frame, final String filePath, final String message) {
        OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Could not open user JNLP policy");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(frame, message, R("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Show a dialog informing the user that the policy file is currently read-only
     * @param frame a {@link JFrame} to act as parent to this dialog
     */
    private static void showReadOnlyDialog(final JFrame frame) {
        OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Opening user JNLP policy read-only");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(frame, R("RFileReadOnly"), R("Warning"), JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Loosely attempt to get the path part of a file URL string. If this fails,
     * simply return back the input. This is only intended to be used for displaying
     * GUI elements such as the CPPolicyTooltip.
     * @param url the {@link String} representing the URL whose path is desired
     * @return a {@link String} representing the local filepath of the given file:/ URL
     */
    private static String localFilePathFromUrlString(String url) {
        try {
            URL u = new URL(url);
            return u.getPath();
        } catch (MalformedURLException e) {
            return url;
        }
    }

    /**
     * Implements the action to be performed when the "View Policy" button is clicked
     */
    private class ViewPolicyButtonAction implements ActionListener {
        private final JFrame frame;
        private final String fileUrlString;

        public ViewPolicyButtonAction(final JFrame frame, final String fileUrlString) {
            this.fileUrlString = fileUrlString;
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                final URL fileUrl = new URL(fileUrlString);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        launchPolicyTool(frame, fileUrl.getPath());
                    }
                });
            } catch (MalformedURLException ex) {
                OutputController.getLogger().log(ex);
                showCouldNotOpenFileDialog(frame, fileUrlString);
            }
        }
    }
}
