package javax.jnlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * FileContents objects encapsulate the name and contents of a file.
 * An implementation of this class is used by the FileOpenService, FileSaveService, and PersistenceService.
 * <p>
 * The FileContents implementation returned by {@link PersistenceService#get(java.net.URL)},
 * FileOpenService, and FileSaveService should never truncate a file if the maximum file length
 * is set to be less that the current file length.
 *
 * @see {@link FileOpenService}, {@link FileSaveService}
 * @since 1.4.2
 */
public interface FileContents {

    /**
     * Gets the file name as a String.
     *
     * @return a string containing the file name.
     * @throws IOException if an I/O exception occurs.
     */
    String getName() throws IOException;

    /**
     * Gets an InputStream from the file.
     *
     * @return an InputStream to the file.
     * @throws IOException if an I/O exception occurs.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Gets an OutputStream to the file.
     * A JNLP client may implement this interface to return an OutputStream subclass
     * which restricts the amount of data that can be written to the stream.
     *
     * @param overwrite if true, then bytes will be written to the beginning of the file rather than the end
     * @return an OutputStream from the file.
     * @throws IOException if an I/O exception occurs.
     */
    OutputStream getOutputStream(boolean overwrite) throws IOException;

    /**
     * Gets the length of the file.
     *
     * @return the length of the file as a long.
     * @throws IOException if an I/O exception occurs.
     */
    long getLength() throws IOException;

    /**
     * Returns whether the file can be read.
     *
     * @return true if the file can be read, false otherwise.
     * @throws IOException if an I/O exception occurs.
     */
    boolean canRead() throws IOException;

    /**
     * Returns whether the file can be written to.
     *
     * @return true if the file can be read, false otherwise.
     * @throws IOException if an I/O exception occurs.
     */
    boolean canWrite() throws IOException;

    /**
     * Returns a JNLPRandomAccessFile representing a random access interface to the file's contents.
     * The mode argument must either be equal to "r" or "rw", indicating the file is to be opened
     * for input only or for both input and output, respectively.
     * An IllegalArgumentException will be thrown if the mode is not equal to "r" or "rw".
     *
     * @param mode the access mode.
     * @return a JNLPRandomAccessFile.
     * @throws IOException if an I/O exception occurs.
     */
    JNLPRandomAccessFile getRandomAccessFile(String mode) throws IOException;

    /**
     * Gets the maximum file length for the file, as set by the creator of this object.
     *
     * @return the maximum length of the file.
     * @throws IOException if an I/O exception occurs.
     */
    long getMaxLength() throws IOException;

    /**
     * Sets the maximum file length for the file.
     * A JNLP client may enforce restrictions on setting the maximum file length.
     * A JNLP client should not truncate a file if the maximum file length is set that is less
     * than the current file size, but it also should not allow further writes to that file.
     *
     * @param maxlength the requested new maximum file length.
     * @return the maximum file length that was granted.
     * @throws IOException if an I/O exception occurs.
     */
    long setMaxLength(long maxlength) throws IOException;

}
