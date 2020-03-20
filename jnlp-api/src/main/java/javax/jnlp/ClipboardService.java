package javax.jnlp;

import java.awt.datatransfer.Transferable;

/**
 * ClipboardService provides methods for accessing the shared system-wide clipboard,
 * even for applications that are running in the untrusted execution environment.
 * Implementors should warn the user of the potential security risk of letting an
 * untrusted application have access to potentially confidential information stored
 * in the clipboard, or overwriting the contents of the clipboard.
 *
 * @since 1.4.2
 */
public interface ClipboardService {

    /**
     * Returns a Transferable object representing the current contents of the clipboard.
     * If the clipboard currently has no contents, it returns null.
     *
     * @return The current Transferable object on the clipboard.
     */
    Transferable getContents();

    /**
     * Sets the current contents of the clipboard to the specified Transferable object.
     *
     * @param contents The Transferable object representing clipboard content.
     */
    void setContents(Transferable contents);

}
