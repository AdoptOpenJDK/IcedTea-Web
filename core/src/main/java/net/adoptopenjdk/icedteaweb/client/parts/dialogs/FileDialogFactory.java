package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.util.FileUtils;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * This factory is used to create popup dialogs for file-related user interaction.
 */

public final class FileDialogFactory {
    /**
     * Show a dialog informing the user that the file is currently read-only.
     *
     * @param frame a {@link JFrame} to act as parent to this dialog
     */
    public static void showReadOnlyDialog(final Component frame) {
        SwingUtils.invokeLater(() -> JOptionPane.showMessageDialog(frame, R("RFileReadOnly"), R("Warning"), JOptionPane.WARNING_MESSAGE));
    }

    /**
     * Show a generic error dialog indicating the  file could not be opened.
     *
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param filePath a {@link String} representing the path to the file we failed to open
     */
    public static void showCouldNotOpenFilepathDialog(final Component frame, final String filePath) {
        showCouldNotOpenDialog(frame, R("RCantOpenFile", filePath));
    }

    /**
     * Show an error dialog indicating the file could not be opened, with a particular reason.
     *
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param filePath a {@link String} representing the path to the file we failed to open
     * @param reason a {@link FileUtils.OpenFileResult} specifying more precisely why we failed to open the file
     */
    public static void showCouldNotOpenFileDialog(final Component frame, final String filePath, final FileUtils.OpenFileResult reason) {
        final String message;
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
        showCouldNotOpenDialog(frame, message);
    }

    /**
     * Show a dialog informing the user that the file could not be opened.
     *
     * @param frame a {@link JFrame} to act as parent to this dialog
     * @param message a {@link String} giving the specific reason the file could not be opened
     */
    public static void showCouldNotOpenDialog(final Component frame, final String message) {
        SwingUtils.invokeLater(() -> JOptionPane.showMessageDialog(frame, message, R("Error"), JOptionPane.ERROR_MESSAGE));
    }
}