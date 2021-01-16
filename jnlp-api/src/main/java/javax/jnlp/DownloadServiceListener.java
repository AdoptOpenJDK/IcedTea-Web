package javax.jnlp;

import java.net.URL;

/**
 * The DownloadServiceListener provides an interface for a callback object implementation,
 * which may be used by a DownloadService implementation.
 * The DownloadServiceListener implementation's methods should be invoked by the
 * DownloadService implementation at various stages of the download,
 * allowing an application that uses the JNLP API to display a progress bar during a DownloadService download.
 *
 * @ee {@link DownloadService}
 * @since 1.4.2
 */
public interface DownloadServiceListener {

    /**
     * A JNLP client's DownloadService implementation should call this method several times during a download.
     * A DownloadServiceListener implementation may display a progress bar and / or update information based
     * on the parameters.
     *
     * @param url            The URL representing the resource being downloaded.
     * @param version        The version of the resource being downloaded.
     * @param readSoFar      The number of bytes downloaded so far.
     * @param total          The total number of bytes to be downloaded, or -1 if the number is unknown.
     * @param overallPercent The percentage of the overall update operation that is complete,
     *                       or -1 if the percentage is unknown.
     */
    void progress(URL url, String version, long readSoFar, long total, int overallPercent);

    /**
     * A JNLP client's DownloadService implementation should call this method at least several times during
     * validation of a download. Validation often includes ensuring that downloaded resources are authentic
     * (appropriately signed). A DownloadServiceListener implementation may display a progress bar and / or
     * update information based on the parameters.
     *
     * @param url            The URL representing the resource being validated.
     * @param version        The version of the resource being validated.
     * @param entry          The number of JAR entries validated so far.
     * @param total          The total number of entries to be validated.
     * @param overallPercent The percentage of the overall update operation that is complete,
     *                       or -1 if the percentage is unknown.
     */
    void validating(URL url, String version, long entry, long total, int overallPercent);

    /**
     * A JNLP client's DownloadService implementation should call this method at least several times when
     * applying an incremental update to an in-cache resource. A DownloadServiceListener implementation may
     * display a progress bar and / or update information based on the parameters.
     *
     * @param url            The URL representing the resource being patched.
     * @param version        The version of the resource being patched.
     * @param patchPercent   The percentage of the patch operation that is complete,
     *                       or -1 if the percentage is unknown.
     * @param overallPercent The percentage of the overall update operation that is complete,
     *                       or -1 if the percentage is unknown.
     */
    void upgradingArchive(URL url, String version, int patchPercent, int overallPercent);

    /**
     * A JNLP client's DownloadService implementation should call this method if a download fails or
     * aborts unexpectedly. In response, a DownloadServiceListener implementation may display update
     * information to the user to reflect this.
     *
     * @param url     The URL representing the resource for which the download failed.
     * @param version The version of the resource for which the download failed.
     */
    void downloadFailed(URL url, String version);
}
