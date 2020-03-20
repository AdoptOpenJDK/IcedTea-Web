package javax.jnlp;

import java.io.File;
import java.io.IOException;

/**
 * ExtendedService provides additional support to the current JNLP API,
 * to allow applications to open a specific file or files in the client's file system.
 *
 * @since 1.5
 */
public interface ExtendedService {

    /**
     * Allows the application to open the specified file, even if the application is running in the
     * untrusted execution environment. If the application would not otherwise have permission to
     * access the file, the JNLP CLient should warn user of the potential security risk.
     * The contents of the file is returned as a FileContents object.
     *
     * @param file the file object
     * @return A FileContents object with information about the opened file
     * @throws IOException if there is any I/O error
     */
    FileContents openFile(File file) throws IOException;

    /**
     * Allows the application to open the specified files, even if the application is running in the
     * untrusted execution environment. If the application would not otherwise have permission to
     * access the files, the JNLP CLient should warn user of the potential security risk.
     * The contents of each file is returned as a FileContents object in the FileContents array.
     *
     * @param files the array of files
     * @return A FileContents[] object with information about each opened file
     * @throws IOException if there is any I/O error
     */
    FileContents[] openFiles(File[] files) throws IOException;
}
