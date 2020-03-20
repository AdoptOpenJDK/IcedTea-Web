package javax.jnlp;

import java.net.URL;

public interface DownloadService {

    boolean isResourceCached(URL ref, String version);

    /**
     * Returns true if the part referred to by the given string is cached, and that part is mentioned in the JNLP file for the application.
     *
     * @param part The name of the part.
     * @return true if the above conditions are met, and false otherwise.
     */
    boolean isPartCached(String part);

    /**
     * Returns true if the parts referred to by the given array are cached, and those parts are mentioned in the JNLP file for the application.
     *
     * @param parts An array of part names.
     * @return true if the above conditions are met, and false otherwise.
     */
    boolean isPartCached(String[] parts);

    /**
     * Returns true if the given part of the given extension is cached, and the extension and part
     * are mentioned in the JNLP file for the application.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param part The name of the part.
     * @return true if the above conditions are met, and false otherwise.
     */
    boolean isExtensionPartCached(URL ref, String version, String part);

    /**
     * Returns true if the given parts of the given extension are cached, and the extension and parts are
     * mentioned in the JNLP file for the application.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param parts An array of part names.
     * @return true if the above conditions are met, and false otherwise.
     */
    boolean isExtensionPartCached(URL ref, String version, String[] parts);

    void loadResource(URL ref, String version, DownloadServiceListener progress) throws java.io.IOException;

    void loadPart(String part, DownloadServiceListener progress) throws java.io.IOException;

    void loadPart(String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    void loadExtensionPart(URL ref, String version, String part, DownloadServiceListener progress) throws java.io.IOException;

    void loadExtensionPart(URL ref, String version, String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    void removeResource(URL ref, String version) throws java.io.IOException;

    void removePart(String part) throws java.io.IOException;

    void removePart(String[] parts) throws java.io.IOException;

    void removeExtensionPart(URL ref, String version, String part) throws java.io.IOException;

    void removeExtensionPart(URL ref, String version, String[] parts) throws java.io.IOException;

    /**
     * Return a default DownloadServiceListener implementation which, when passed to a load method,
     * should pop up and update a progress window as the load progresses.
     *
     * @return A DownloadServiceListener object representing a download progress listener.
     */
    DownloadServiceListener getDefaultProgressWindow();

}
