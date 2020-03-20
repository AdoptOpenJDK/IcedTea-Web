package javax.jnlp;

import java.io.IOException;

/**
 * FileOpenService service allows the user to choose a file from the local file system,
 * even for applications that are running in the untrusted execution environment.
 * The JNLP Client is the mediator and is therefore responsible for providing the
 * specific implementation of this, if any.
 * <p>
 * This service provides a similar function as the file input field for HTML-based forms.
 *
 * @see {@link FileSaveService}, {@link FileContents}
 * @since 1.4.2
 */
public interface FileOpenService {

    /**
     * Asks the user to choose a single file.
     * The contents of a selected file is returned as a FileContents object.
     * The returned FileContents object contains the contents along with the name of the file.
     * The full path is not returned.
     *
     * @param pathHint   A hint from the application to the initial directory for the file chooser.
     *                   This might be ignored by the JNLP Client.
     * @param extensions A list of default extensions to show in the file chooser.
     *                   For example, String[] { "txt", "java" }. This might be ignored by the JNLP Client.
     * @return A FileContent object with information about the chosen file, or null if the user did not choose a file.
     * @throws IOException if the request failed in any way other than the user did not choose to select a file.
     */
    FileContents openFileDialog(String pathHint, String[] extensions) throws IOException;

    /**
     * Asks the user to choose one or more file.
     * The contents of selected files is returned as an array of FileContents objects.
     * The returned FileContents objects contain the contents along with the name of the file.
     * The full path is not returned.
     *
     * @param pathHint   A hint from the application to the initial directory for the file chooser.
     *                   This might be ignored by the JNLP Client.
     * @param extensions A list of default extensions to show in the file chooser.
     *                   For example, String[] { "txt", "java" }.
     *                   This might be ignored by the JNLP Client.
     * @return An array of FileContent objects with information about the chosen files,
     * or null if the user did not choose a file.
     * @throws IOException if the request failed in any way other than the user did not choose to select a file.
     */
    FileContents[] openMultiFileDialog(String pathHint, String[] extensions) throws IOException;

}
