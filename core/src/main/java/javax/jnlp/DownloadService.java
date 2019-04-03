package javax.jnlp;

public interface DownloadService {

    public boolean isResourceCached(java.net.URL ref, java.lang.String version);

    public boolean isPartCached(java.lang.String part);

    public boolean isPartCached(java.lang.String[] parts);

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

    public DownloadServiceListener getDefaultProgressWindow();

}
