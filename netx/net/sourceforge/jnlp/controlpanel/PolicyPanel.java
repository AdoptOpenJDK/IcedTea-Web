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
import sun.security.tools.policytool.PolicyTool;

public class PolicyPanel extends NamedBorderPanel {

    private enum OpenFileResult {
        SUCCESS, FAILURE, CANT_CREATE, CANT_WRITE, NOT_FILE
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

        /* Keep all the elements at the top of the panel (Extra padding) */
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
     * @param filePath the policy file path to be opened with policytool
     */
    private static void launchPolicyTool(final JFrame frame, final String filePath) {
        try {
            final File policyFile = new File(filePath).getCanonicalFile();
            OpenFileResult result = canOpenPolicyFile(policyFile);
            if (result == OpenFileResult.SUCCESS) {
                PolicyTool.main(new String[] { "-file", policyFile.getPath() });
            } else if (result == OpenFileResult.CANT_WRITE) {
                showReadOnlyDialog(frame);
                PolicyTool.main(new String[] { "-file", policyFile.getPath() });
            } else {
                showCouldNotOpenFileDialog(frame, policyFile.getPath(), result);
            }
        } catch (IOException e) {
            OutputController.getLogger().log(e);
            showCouldNotOpenFileDialog(frame, filePath);
        }
    }

    /**
     * Verify that a given file object points to a real, accessible plain file.
     * As a side effect, if the file is accessible but does not yet exist, it will be created
     * as an empty plain file.
     * @param policyFile the file to verify
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
     * @param policyFile the location of the policy file
     * @return an object representing the results of the test
     */
    private static DirectoryCheckResults testPolicyFileDirectory(final File policyFile) {
        List<File> policyDirectory = new ArrayList<File>();
        policyDirectory.add(policyFile.getParentFile());
        DirectoryValidator validator = new DirectoryValidator(policyDirectory);
        DirectoryCheckResults result = validator.ensureDirs();

        return result;
    }

    private static void showCouldNotOpenFileDialog(final JFrame frame, final String filePath) {
        showCouldNotOpenFileDialog(frame, filePath, OpenFileResult.FAILURE);
    }

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
     * @param frame the parent frame for this dialog
     * @param filePath the path to the file we tried to open
     * @param message the specific reason the file could not be opened
     */
    private static void showCouldNotOpenFileDialog(final JFrame frame, final String filePath, final String message) {
        OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Could not open user JNLP policy");
        JOptionPane.showMessageDialog(frame, message, R("Error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a dialog informing the user that the policy file is currently read-only
     * @param frame the parent frame for this dialog
     */
    private static void showReadOnlyDialog(final JFrame frame) {
        OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Opening user JNLP policy read-only");
        JOptionPane.showMessageDialog(frame, R("RFileReadOnly"), R("Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Loosely attempt to get the path part of a file URL string. If this fails,
     * simply return back the input. This is only intended to be used for displaying
     * GUI elements such as the CPPolicyTooltip.
     * @param url the String representing the URL whose path is desired
     * @return a String representing the local filepath of the given file:/ URL
     */
    private static String localFilePathFromUrlString(String url) {
        try {
            URL u = new URL(url);
            return u.getPath();
        } catch (MalformedURLException e) {
            return url;
        }
    }

    /*
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
