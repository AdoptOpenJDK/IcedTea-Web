package javax.jnlp;

public interface DownloadService {

    public boolean isResourceCached(java.net.URL ref, java.lang.String version);

    public boolean isPartCached(java.lang.String part);

    public boolean isPartCached(java.lang.String[] parts);

    /**
     * Returns true if the given part of the given extension is cached, and the extension and part
     * are mentioned in the JNLP file for the application.
     *
     * @param ref The URL for the resource.
     * @param version The version string, or null for no version.
     * @param part The name of the part.
     * @return true if the above conditions are met, and false otherwise.
     */
    public boolean isExtensionPartCached(java.net.URL ref, java.lang.String version, java.lang.String part);

    public boolean isExtensionPartCached(java.net.URL ref, java.lang.String version, java.lang.String[] parts);

    public void loadResource(java.net.URL ref, java.lang.String version, DownloadServiceListener progress) throws java.io.IOException;

    public void loadPart(java.lang.String part, DownloadServiceListener progress) throws java.io.IOException;

    public void loadPart(java.lang.String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    public void loadExtensionPart(java.net.URL ref, java.lang.String version, java.lang.String part, DownloadServiceListener progress) throws java.io.IOException;

    public void loadExtensionPart(java.net.URL ref, java.lang.String version, java.lang.String[] parts, DownloadServiceListener progress) throws java.io.IOException;

    public void removeResource(java.net.URL ref, java.lang.String version) throws java.io.IOException;

    public void removePart(java.lang.String part) throws java.io.IOException;

    public void removePart(java.lang.String[] parts) throws java.io.IOException;

    public void removeExtensionPart(java.net.URL ref, java.lang.String version, java.lang.String part) throws java.io.IOException;

    public void removeExtensionPart(java.net.URL ref, java.lang.String version, java.lang.String[] parts) throws java.io.IOException;

    /**
     * Return a default DownloadServiceListener implementation which, when passed to a load method,
     * should pop up and update a progress window as the load progresses.
     *
     * @return A DownloadServiceListener object representing a download progress listener.
     */
    DownloadServiceListener getDefaultProgressWindow();

}
