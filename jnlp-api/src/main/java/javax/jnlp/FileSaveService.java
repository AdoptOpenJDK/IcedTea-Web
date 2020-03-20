package javax.jnlp;

import java.io.IOException;
import java.io.InputStream;

/**
 * FileSaveService service allows the user to save a file to the local file system,
 * even for applications that are running in the untrusted execution environment.
 * The JNLP Client is the mediator and is therefore responsible for providing the
 * specific implementation of this, if any.
 * <p>
 * This service provides similar functionality as the Save as... functionality provided by most browsers.
 *
 * @see {@link FileOpenService}, {@link FileContents}
 * @since 1.4.2
 */
public interface FileSaveService {

    /**
     * Asks the users to save a file.
     *
     * @param pathHint   A hint from the application to the default directory to be used.
     *                   This might be ignored by the JNLP Client.
     * @param extensions A list of default extensions to show in the file chooser.
     *                   For example, String[] { "txt", "java" }. These might be ignored by the JNLP Client.
     * @param stream     The content of the file to save along represented as an InputStream
     * @param name       The suggested filename, which might be ignored by the JNLP client
     * @return A FileContents object for the saved file if the save was successfully,
     * or null if the user canceled the request.
     * @throws IOException if the requested failed in any way other than the user chose not to save the file
     */
    FileContents saveFileDialog(String pathHint, String[] extensions, InputStream stream, String name) throws IOException;

    /**
     * Asks the users to save a file.
     *
     * @param pathHint   A hint from the application to the default directory to be used.
     *                   This might be ignored by the JNLP Client.
     * @param extensions A list of default extensions to show in the file chooser.
     *                   For example, String[] { "txt", "java" }. These might be ignored by the JNLP Client.
     * @param contents   The content of the file to save along with the suggested filename.
     *                   The suggested filename might be ignored by the JNLP Client.
     * @return A FileContents object for the saved file if the save was successfully,
     * or null if the user canceled the request.
     * @throws IOException if the requested failed in any way other than the user chose not to save the file
     */
    FileContents saveAsFileDialog(String pathHint, String[] extensions, FileContents contents) throws IOException;

}
