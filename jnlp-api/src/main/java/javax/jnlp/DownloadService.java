package javax.jnlp;

import java.net.URL;

public interface DownloadService {

    /**
     * Returns true if the resource referred to by the given URL and version is cached, and that resource
     * is either mentioned in the calling applications JNLP file, is within the codebase of the calling
     * applications JNLP file, or the calling application has been granted all-permissions.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @return true if the above conditions are met, and false otherwise.
     */
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

    /**
     * Downloads the given resource, if the resource is either mentioned in the calling applications JNLP file,
     * is within the codebase of the calling applications JNLP file, or if the calling application has been
     * granted all-permissions. This method will block until the download is completed or an exception occurs.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param progress Download progress callback object.
     * @throws java.io.IOException if an I/O error occurs
     */
    void loadResource(URL ref, String version, DownloadServiceListener progress) throws java.io.IOException;

    /**
     * Downloads the given part, if the part is mentioned in the JNLP file for the application.
     * This method will block until the download is completed or an exception occurs.
     *
     * @param part The name of the part.
     * @param progress Download progress callback object.
     * @throws java.io.IOException if an I/O error occurs
     */
    void loadPart(String part, DownloadServiceListener progress) throws java.io.IOException;

    /**
     * Downloads the given parts, if the parts are mentioned in the JNLP file for the application.
     * This method will block until the download is completed or an exception occurs.
     *
     * @param parts An array of part names.
     * @param progress Download progress callback object.
     * @throws java.io.IOException if an I/O error occurs
     */
    void loadPart(String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    /**
     * Downloads the given part of the given extension,
     * if the part and the extension are mentioned in the JNLP file for the application.
     * This method will block until the download is completed or an exception occurs.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param part The name of the part.
     * @param progress Download progress callback object.
     * @throws java.io.IOException if an I/O error occurs
     */
    void loadExtensionPart(URL ref, String version, String part, DownloadServiceListener progress) throws java.io.IOException;

    /**
     * Downloads the given parts of the given extension,
     * if the parts and the extension are mentioned in the JNLP file for the application.
     * This method will block until the download is completed or an exception occurs.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param parts An array of part names to load.
     * @param progress Download progress callback object.
     * @throws java.io.IOException if an I/O error occurs
     */
    void loadExtensionPart(URL ref, String version, String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    /**
     * Removes the given resource from the cache,
     * if the resource is either mentioned in the calling applications JNLP file,
     * is within the codebase of the calling applications JNLP file,
     * or if the calling application has been granted all-permissions.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @throws java.io.IOException if an I/O error occurs
     */
    void removeResource(URL ref, String version) throws java.io.IOException;

    /**
     * Removes the given part from the cache, if the part is mentioned in the JNLP file for the application.
     *
     * @param part The name of the part.
     * @throws java.io.IOException if an I/O error occurs
     */
    void removePart(String part) throws java.io.IOException;

    /**
     * Removes the given parts from the cache, if the parts are mentioned in the JNLP file for the application.
     *
     * @param parts An array of part names.
     * @throws java.io.IOException if an I/O error occurs
     */
    void removePart(String[] parts) throws java.io.IOException;

    /**
     * Removes the given part of the given extension from the cache,
     * if the part and the extension are mentioned in the JNLP file for the application.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param part The name of the part.
     * @throws java.io.IOException if an I/O error occurs
     */
    void removeExtensionPart(URL ref, String version, String part) throws java.io.IOException;

    /**
     * Removes the given parts of the given extension from the cache,
     * if the parts and the extension are mentioned in the JNLP file for the application.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param parts An array of part names.
     * @throws java.io.IOException if an I/O error occurs
     */
    void removeExtensionPart(URL ref, String version, String[] parts) throws java.io.IOException;

    /**
     * Return a default DownloadServiceListener implementation which, when passed to a load method,
     * should pop up and update a progress window as the load progresses.
     *
     * @return A DownloadServiceListener object representing a download progress listener.
     */
    DownloadServiceListener getDefaultProgressWindow();

}
